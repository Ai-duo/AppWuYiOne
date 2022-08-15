package com.kd.appwuyione;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;


import com.xixun.joey.uart.BytesData;
import com.xixun.joey.uart.IUartListener;
import com.xixun.joey.uart.IUartService;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ElementsService extends Service {
    public IUartService uart;
    StringBuffer builder = new StringBuffer();
    boolean start = false;
    boolean dmgd = false, weaf = false, timing = false, bright = false;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            uart = IUartService.Stub.asInterface(iBinder);
            Log.i("TAG_uart", "================ onServiceConnected ====================");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i("TAG_uart", "================== onServiceDisconnected ====================");
            uart = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TAG_Service", "服务开启");

        bindCardSystemUartAidl();
        startGetUart();
        startTimer();
      /*  final String in = "DMGD WX001 2021-03-17 16:45 1111111111111011111111111111110000000000000000000000000000000000001110000000000000000000000000000001100000000000000000000001000000000000000 00000000000000000080000000000000000 98 53 102 48 100 59 1605 98 54 101 95 1606 0 218 227 1601 218 1645 * 72 69 1601 188 165 10108 10108 1617 10106 1601 10126 0 00 98 71 000000000000000000000000000000000000000000000 *5B *33 T";
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getElements(in);
            }
        }, 0, 10000);*/
      /*  new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //restartApp();
                // reboot();
                // LiveDataBus.getInstance().getElementsMutableLiveData().postValue(new Elements("金华气象", "2020-11-04 16:35", "12", "23", "23", "56", "56", "54", "124"));
            }
        }).start();*/
    }

    Thread thread,thread1;
    private boolean openPm = false;

    private void startGetUart() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("TAG_uart", "正在获取uart======================");
                } while (null == uart);
                try {
                    //监听/dev/ttyMT2，获取数据/dev/s3c2410_serial3
                    uart.read(port, new IUartListener.Stub() {
                        @Override
                        public void onReceive(BytesData data) throws RemoteException {
                            Log.i("TAG_uart", "========获取到串口数据===========");
                            if (!PmFlag) {
                                for (byte a : data.getData()) {
                                    String s1 = "0x" + Integer.toHexString(a & 0xFF) + " ";
                                    char ss = (char) a;
                                    Log.i("TAG_uart", "ss:" + ss + ";s1:" + s1);
                                    if (ss == 'D') {
                                        dmgd = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (ss == 'W') {
                                        weaf = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (ss == 'O') {
                                        timing = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (ss == 'B') {
                                        bright = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (start) {
                                        start = true;
                                        builder.append(ss);
                                    }
                                    Log.i("TAG_uart123", builder.toString());
                                    if (builder.length() == 1) {
                                        //||!builder.toString().equals("FE")
                                        if (!builder.toString().equals("D") && !builder.toString().equals("W") && !builder.toString().equals("O") && !builder.toString().equals("B")) {
                                            builder.delete(0, builder.length());
                                            dmgd = false;
                                            weaf = false;
                                            timing = false;
                                            bright = false;
                                            start = false;
                                        }
                                    }
                                    if (builder.length() == 2) {
                                        //||!builder.toString().equals("FE")
                                        if (!builder.toString().equals("DM") && !builder.toString().equals("WE") && !builder.toString().equals("ON") && !builder.toString().equals("BR")) {
                                            builder.delete(0, builder.length());
                                            dmgd = false;
                                            weaf = false;
                                            timing = false;
                                            bright = false;
                                            start = false;
                                        }
                                    }
                                    if (builder.length() == 4) {
                                        //||!builder.toString().equals("FE")
                                        if (!builder.toString().equals("DMGD") && !builder.toString().equals("WEAF") && !builder.toString().equals("ONOF") && !builder.toString().equals("BRIG")) {
                                            builder.delete(0, builder.length());
                                            dmgd = false;
                                            weaf = false;
                                            timing = false;
                                            bright = false;
                                            start = false;
                                        }
                                    }
                                    if (((dmgd && 'T' == ss) || (';' == ss && (weaf || timing || bright))) && builder.length() > 4) {
                                        dmgd = false;
                                        weaf = false;
                                        timing = false;
                                        bright = false;
                                        start = false;
                                        Log.i("TAG_uart1234", builder.toString());
                                        getElements(port,builder.toString());
                                     //   OkHttpUploadTool.getInstance().uploadString(UrlList.sitenum, "info:" + builder.toString());
                                        builder.delete(0, builder.length());
                                        Log.i("TAG_uart1234", "END");
                                    }

                                }
                            } else {
                                for (byte b : data.getData()) {
                                    byteArrayList.add(b);
                                    if ((int) byteArrayList.get(0) != 21) {
                                        byteArrayList.clear();
                                    }
                                    s2 += "0x" + Integer.toHexString(b & 0xff) + " ";
                                }
                                // String ss = StrUtil.bytesToAscii(byteArrayList);
                                Log.i("TAG", "收到的PM：" + s2 + ";byteArrayList:" + byteArrayList.size());
                                //Log.i("TAG", "收到的PM：" + ss);
                                if (byteArrayList.size() >= 21) {
                                    int b1 = (int) byteArrayList.get(0);
                                    int b2 = (int) byteArrayList.get(1);
                                    int b3 = (int) byteArrayList.get(2);
                                    Log.i("TAG", "b1:" + b1 + ";" + "b2:" + b2 + ";" + "b3:" + b3 + ";");
                                    if (b1 == 21 && b2 == 3 && b3 == 16) {
                                        int pm = Integer.parseInt(Integer.toHexString(byteArrayList.get(7) & 0xff), 16) * 256 + Integer.parseInt(Integer.toHexString(byteArrayList.get(8) & 0xff), 16);
                                        Log.i("TAG", "pm1:" + byteArrayList.get(7) + ";" + "pm2:" + Integer.parseInt(Integer.toHexString(byteArrayList.get(8) & 0xff), 16) + ";");
                                        Log.i("TAG", "pm:" + pm);
                                        //  MainActivity.pm_text = "PM2.5:" + pm + "ug/m³";
                                        // Log.i("TAG", "pm_text:" + MainActivity.pm_text);
                                        byteArrayList.clear();
                                        PmFlag = false;
                                        s2 = "";
                                     //   OkHttpUploadTool.getInstance().uploadString(UrlList.sitenum, "PM2.5:" + pm + "ug/m³");
                                    } else {
                                        byteArrayList.clear();
                                        s2 = "";
                                    }
                                }
                            }
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                do {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.i("TAG_uart", "正在获取uart======================");
                } while (null == uart);
                try {
                    //监听/dev/ttyMT2，获取数据/dev/s3c2410_serial3
                    uart.read(port1, new IUartListener.Stub() {
                        @Override
                        public void onReceive(BytesData data) throws RemoteException {
                            Log.i("TAG_uart", "========获取到串口数据===========");
                            if (!PmFlag) {
                                for (byte a : data.getData()) {
                                    String s1 = "0x" + Integer.toHexString(a & 0xFF) + " ";
                                    char ss = (char) a;
                                    Log.i("TAG_uart", "ss:" + ss + ";s1:" + s1);
                                    if (ss == 'D') {
                                        dmgd = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (ss == 'W') {
                                        weaf = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (ss == 'O') {
                                        timing = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (ss == 'B') {
                                        bright = true;
                                        start = true;
                                        builder.append(ss);
                                    } else if (start) {
                                        start = true;
                                        builder.append(ss);
                                    }
                                    Log.i("TAG_uart123", builder.toString());
                                    if (builder.length() == 1) {
                                        //||!builder.toString().equals("FE")
                                        if (!builder.toString().equals("D") && !builder.toString().equals("W") && !builder.toString().equals("O") && !builder.toString().equals("B")) {
                                            builder.delete(0, builder.length());
                                            dmgd = false;
                                            weaf = false;
                                            timing = false;
                                            bright = false;
                                            start = false;
                                        }
                                    }
                                    if (builder.length() == 2) {
                                        //||!builder.toString().equals("FE")
                                        if (!builder.toString().equals("DM") && !builder.toString().equals("WE") && !builder.toString().equals("ON") && !builder.toString().equals("BR")) {
                                            builder.delete(0, builder.length());
                                            dmgd = false;
                                            weaf = false;
                                            timing = false;
                                            bright = false;
                                            start = false;
                                        }
                                    }
                                    if (builder.length() == 4) {
                                        //||!builder.toString().equals("FE")
                                        if (!builder.toString().equals("DMGD") && !builder.toString().equals("WEAF") && !builder.toString().equals("ONOF") && !builder.toString().equals("BRIG")) {
                                            builder.delete(0, builder.length());
                                            dmgd = false;
                                            weaf = false;
                                            timing = false;
                                            bright = false;
                                            start = false;
                                        }
                                    }
                                    if (((dmgd && 'T' == ss) || (';' == ss && (weaf || timing || bright))) && builder.length() > 4) {
                                        dmgd = false;
                                        weaf = false;
                                        timing = false;
                                        bright = false;
                                        start = false;
                                        Log.i("TAG_uart1234", builder.toString());
                                        getElements(port1,builder.toString());
                                       // OkHttpUploadTool.getInstance().uploadString(UrlList.sitenum, "info:" + builder.toString());
                                        builder.delete(0, builder.length());
                                        Log.i("TAG_uart1234", "END");
                                    }

                                }
                            } else {
                                for (byte b : data.getData()) {
                                    byteArrayList.add(b);
                                    if ((int) byteArrayList.get(0) != 21) {
                                        byteArrayList.clear();
                                    }
                                    s2 += "0x" + Integer.toHexString(b & 0xff) + " ";
                                }
                                // String ss = StrUtil.bytesToAscii(byteArrayList);
                                Log.i("TAG", "收到的PM：" + s2 + ";byteArrayList:" + byteArrayList.size());
                                //Log.i("TAG", "收到的PM：" + ss);
                                if (byteArrayList.size() >= 21) {
                                    int b1 = (int) byteArrayList.get(0);
                                    int b2 = (int) byteArrayList.get(1);
                                    int b3 = (int) byteArrayList.get(2);
                                    Log.i("TAG", "b1:" + b1 + ";" + "b2:" + b2 + ";" + "b3:" + b3 + ";");
                                    if (b1 == 21 && b2 == 3 && b3 == 16) {
                                        int pm = Integer.parseInt(Integer.toHexString(byteArrayList.get(7) & 0xff), 16) * 256 + Integer.parseInt(Integer.toHexString(byteArrayList.get(8) & 0xff), 16);
                                        Log.i("TAG", "pm1:" + byteArrayList.get(7) + ";" + "pm2:" + Integer.parseInt(Integer.toHexString(byteArrayList.get(8) & 0xff), 16) + ";");
                                        Log.i("TAG", "pm:" + pm);
                                        //  MainActivity.pm_text = "PM2.5:" + pm + "ug/m³";
                                        // Log.i("TAG", "pm_text:" + MainActivity.pm_text);
                                        byteArrayList.clear();
                                        PmFlag = false;
                                        s2 = "";
                                      //  OkHttpUploadTool.getInstance().uploadString(UrlList.sitenum, "PM2.5:" + pm + "ug/m³");
                                    } else {
                                        byteArrayList.clear();
                                        s2 = "";
                                    }
                                }
                            }
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
    }

    Timer weaTimer;

    private void weaTimer() {
        weaTimer = new Timer();
        weaTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).build();
                String result = null;
                Request request = new Request.Builder()
                        .url("")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        result = response.body().string();
                        JSONObject json = new JSONObject(result);
                        String Info = json.optString("DATE");
                        String dayinfo = json.optString("wea_txt1");
                        // site_name = json.optString("wea_logo");
                        if (!TextUtils.isEmpty(Info)) {
                            EventBus.getDefault().post(dayinfo);

                            try {
                                Thread.sleep(60 * 60 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        Log.i("TAG", "获取天气数据失败");
                    }
                } catch (Exception e) {
                    // Log.i("TAG", "网络异常");
                }

            }
        }, 0, 3000);
    }

    String s2 = "";
    ArrayList<Byte> byteArrayList = new ArrayList<>();
    static int ii = 0;
    boolean PmFlag = false;

    public void bindCardSystemUartAidl() {
        Intent intent = new Intent("xixun.intent.action.UART_SERVICE");
        intent.setPackage("com.xixun.joey.cardsystem");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    ArrayList<Integer> index = new ArrayList<Integer>();

    public int getCharCount(String chars) {
        if (TextUtils.isEmpty(chars)) {
            return 0;
        }
        index.clear();
        char[] chars1 = chars.toCharArray();
        int count = 0;
        for (int j = 0; j < chars1.length; j++) {
            if (chars1[j] == '1') {
                count++;
            }
            if (j == 0 || j == 1 || j == 12 || j == 14 || j == 15 || j == 17 || j == 20 || j == 21 || j == 25 || j == 55) {
                index.add(count - 1);
            }
        }
        return count;
    }

    //风向
    String fx = "--";//1
    //风速
    String fs = "--";//2
    //降水
    String js = "--";//13
    //温度
    String wd = "--";
    //日最大温度
    String max_wd = "--";
    //日最小温度
    String min_wd = "--";
    //湿度
    String sd = "--";//21
    //日最小湿度
    String min_sd = "--";
    //气压
    String qy = "--";//26
    //能见度
    String njd = "--";//26
    static String WEA;
    String port = "/dev/ttyMT3";//,/dev/ttysWK2   /dev/ttyMT3
    String port1  = "/dev/ttysWK2";
    //判断是否重启,每十分钟判断一次sendCount与currentCount的值，如果两者相等就重起；
    int sendCount = 0, currentCount = -1;
    Timer timer;
    public void startTimer(){
        if(timer == null){
            timer = new Timer();
            Log.i("TAG_uart", "create timer ======================");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if(uart!=null) {
                            uart.write("/dev/ttyMT3", new byte[]{0x44, 0x4d, 0x47, 0x44,0x0D, 0x0A});//, 0x5c, 0x72, 0x5c, 0x6e
                            Log.i("TAG_uart", "发送  DMGD ======================");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            },0,60*1000);
        }
    }
    public void getElements(String port,String info) {
        //builder.delete(0, builder.length());
        //开始接受PM2.5数据
        Log.i("TAG", "getElements:");
        if (openPm) {
            PmFlag = true;
            try {
                uart.write(port, new byte[]{0x15, 0x03, 0X00, 0X64, 0X00, 0x08, 0x06, (byte) 0xc7});
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        if (TextUtils.isEmpty(info)) {
            return;
        }
        if (info.startsWith("DMGD") && (info.endsWith("F") || info.endsWith("T"))) {

            String[] infoss = info.split(" ");
            for (int i = 0; i < infoss.length; i++) {
                Log.i("TAG_uart", i + ":" + infoss[i]);
            }
            //日期
            String date = infoss[2];
            Log.i("TAG", "日期:" + date);
            //时间
            String time = infoss[3];
            Log.i("TAG", "时间:" + time);
            int count = getCharCount(infoss[4]);
            int qc = 5;
            if (infoss[5].length() >= 4) {
                qc = 6;
            }
            try {
                if (infoss[4].charAt(0) == '1') {
                    String fx1 = infoss[qc + index.get(0)];
                    Log.i("TAG", "风向:" + fx1);
                    if (isNum(fx1)) {
                        float f = Float.valueOf(fx1);
                        if ((f >= 0 && f < 12.25) || (f > 348.76 && f <= 360)) {
                            fx = "北";
                        } else if (f > 12.26 && f < 33.75) {//22.5
                            fx = "北偏东北";
                        } else if (f > 33.76 && f < 56.25) {
                            fx = "东北";
                        } else if (f > 56.25 && f < 78.75) {
                            fx = "东偏东北";
                        } else if (f > 78.75 && f < 101.25) {
                            fx = "东";
                        } else if (f > 101.25 && f < 123.75) {
                            fx = "东偏东南";
                        } else if (f > 123.76 && f < 146.25) {
                            fx = "东南";
                        } else if (f > 146.26 && f < 168.75) {
                            fx = "南偏东南";
                        } else if (f > 168.75 && f < 191.25) {
                            fx = "南";
                        } else if (f > 191.25 && f < 213.75) {
                            fx = "南偏西南";
                        } else if (f > 213.75 && f < 236.25) {
                            fx = "西南";
                        } else if (f > 236.25 && f < 258.75) {
                            fx = "西偏西南";
                        } else if (f > 258.75 && f < 281.25) {
                            fx = "西";
                        } else if (f > 281.25 && f < 303.75) {
                            fx = "西偏西北";
                        } else if (f > 303.75 && f < 326.25) {
                            fx = "西北";
                        } else if (f > 326.25 && f < 348.75) {
                            fx = "北偏西北";
                        }
                    }
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析风向时出错");
            }
            try {
                if (infoss[4].charAt(1) == '1') {
                    String fs1 = infoss[qc + index.get(1)];
                    Log.i("TAG", "风速:" + fs1);
                    if (!isNum(fs1)) {
                        //fs = "";
                    } else {
                        fs = Float.parseFloat(infoss[qc + index.get(1)]) / 10 + "";
                    }
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析风速时出错");
            }

            try {
                if (infoss[4].charAt(12) == '1') {
                    String js1 = infoss[qc + index.get(2)];
                    Log.i("TAG", "降水:" + js1);
                    if (!isNum(js1)) {
                        // js = "";
                    } else {
                        js = Float.parseFloat(infoss[qc + index.get(2)]) / 10 + "";
                    }
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析雨量时出错");
            }

            try {
                if (infoss[4].charAt(14) == '1') {
                    String wd1 = infoss[qc + index.get(3)];
                    if (wd1.charAt(0) == '/') {
                        //wd = " ";
                    } else if (wd1.charAt(0) == '-') {
                        if (wd1.length() <= 3) {
                            wd1 = wd1.substring(1, wd1.length());
                            if (isNum(wd1) && Float.parseFloat(wd1) < 580) {
                                wd = "-" + Float.parseFloat(wd1) / 10;
                            }
                        }
                    } else {
                        if (isNum(infoss[qc + index.get(3)]) && Float.parseFloat(infoss[qc + index.get(3)]) < 580) {
                            wd = Float.parseFloat(infoss[qc + index.get(3)]) / 10 + "";
                        }
                    }
                    Log.i("TAG", "温度:" + wd);
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析温度时出错");
            }
            try {
                if (infoss[4].charAt(15) == '1') {
                    String max_wd1 = infoss[qc + index.get(4)];
                    if (max_wd1.charAt(0) == '/') {
                        //wd = " ";
                    } else if (max_wd1.charAt(0) == '-') {
                        if (max_wd1.length() <= 3) {
                            max_wd1 = max_wd1.substring(1, max_wd1.length());
                            if (isNum(max_wd1) && Float.parseFloat(max_wd1) < 580) {
                                max_wd = "-" + Float.parseFloat(max_wd1) / 10;
                            }
                        }
                    } else {
                        if (isNum(infoss[qc + index.get(4)]) && Float.parseFloat(infoss[qc + index.get(4)]) < 580) {
                            max_wd = Float.parseFloat(infoss[qc + index.get(4)]) / 10 + "";
                        }
                    }
                    Log.i("TAG", "日最高温度:" + max_wd);
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析温度时出错");
            }
            try {
                if (infoss[4].charAt(17) == '1') {
                    String min_wd1 = infoss[qc + index.get(5)];
                    if (min_wd1.charAt(0) == '/') {
                        //wd = " ";
                    } else if (min_wd1.charAt(0) == '-') {
                        if (min_wd1.length() <= 3) {
                            min_wd1 = min_wd1.substring(1, min_wd1.length());
                            if (isNum(min_wd1) && Float.parseFloat(min_wd1) < 580) {
                                min_wd = "-" + Float.parseFloat(min_wd1) / 10;
                            }
                        }
                    } else {
                        if (isNum(infoss[qc + index.get(5)]) && Float.parseFloat(infoss[qc + index.get(5)]) < 580) {
                            min_wd = Float.parseFloat(infoss[qc + index.get(5)]) / 10 + "";
                        }
                    }
                    Log.i("TAG", "日最低温度:" + min_wd);
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析温度时出错");
            }
            try {
                if (infoss[4].charAt(20) == '1') {
                    String sd1 = infoss[qc + index.get(6)];
                    Log.i("TAG", "湿度:" + sd1);
                    if (!isNum(sd1)) {
                        //sd = "";
                    } else {
                        sd = sd1;
                    }
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析湿度时出错");
            }
            try {
                if (infoss[4].charAt(21) == '1') {
                    String min_sd1 = infoss[qc + index.get(7)];
                    Log.i("TAG", "湿度:" + min_sd1);
                    if (!isNum(min_sd1)) {
                        //sd = "";
                    } else {
                        min_sd = min_sd1;
                    }
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析湿度时出错");
            }
            try {
                if (infoss[4].charAt(25) == '1') {
                    String qy1 = infoss[qc + index.get(8)];
                    Log.i("TAG", "气压:" + qy1);
                    if (!isNum(qy1)) {
                        //qy = "";
                    } else {
                        qy = Float.parseFloat(infoss[qc + index.get(8)]) / 10 + "";
                    }
                }
            } catch (Exception e) {
                Log.i("TAG_", "解析气压时出错");
            }
            try {
                if (infoss[4].charAt(55) == '1') {
                    //能见度
                    String njd1 = "";
                    njd1 = infoss[qc + index.get(9)];
                    Log.i("TAG", "能见度:" + njd1);
                    if (!isNum(njd1)) {
                        //njd = "12345";
                    } else {
                        njd = njd1;
                    }

                }
            } catch (Exception e) {
                Log.i("TAG_", "解析能见度时出错");
            }
            Elements elements = new Elements.Builder().
                    info(info).
                    date(date).
                    fx(fx).
                    fs(fs).
                    js(js).
                    sd(sd).
                    max_wd(max_wd).
                    min_wd(min_wd).
                    min_sd(min_sd).
                    time(time).
                    wd(wd).
                    qy(qy).
                    njd(njd).
                    build();
            LiveDataBus.get().with("elements").postValue(elements);
            sendCount++;
        }

    }

    private Timer timerListener;

    private void startRebootListener() {
        timerListener = new Timer();
        timerListener.schedule(new TimerTask() {
            @Override
            public void run() {
                //两张相等时说明十分钟之内没有更新过数据这时需要重启；
                if (sendCount == currentCount) {
                    //reboot();
                } else {
                    currentCount = sendCount;
                }
            }
        }, 0, 10 * 60 * 1000);
    }

    public boolean isNum(String str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]{1,}");
        Matcher matcher = pattern.matcher((CharSequence) str);
        boolean result = matcher.matches();
        return result;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        unbindService(conn);
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        super.onDestroy();
    }

    public String stringToGbk(String string) throws Exception {
        byte[] bytes = new byte[string.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            byte high = Byte.parseByte(string.substring(i * 2, i * 2 + 1), 16);
            byte low = Byte.parseByte(string.substring(i * 2 + 1, i * 2 + 2), 16);
            bytes[i] = (byte) (high << 4 | low);
        }
        String result = new String(bytes, "gbk");
        return result;
    }

    private void restartApp() {
        Log.i("TAG_Service", "重启app");
       /* Intent activity = new Intent(getApplicationContext(), MainActivity.class);
        activity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(activity);*/
        Intent i = getApplicationContext().getPackageManager()
                .getLaunchIntentForPackage(getApplicationContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(i);
        Process.killProcess(Process.myPid());
        // android.os.Process.killProcess(Process.myPid());
    }


}