package com.kd.appwuyione;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.kd.appwuyione.databinding.ActivityMainBinding;
import com.kd.appwuyione.databinding.ActivityMainsBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    ActivityMainsBinding mainBinding;
    FirstFragment firstFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Log.i("TAG", "onCreate");
        initFragment();
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_mains);
        Intent intent = new Intent(this, ElementsService.class);
        startService(intent);
        initTask();
        initLiveData();
        changFragment();
    }

    public void initLiveData(){
        LiveDataBus.get().with("elements",Elements.class).observe(this, new Observer<Elements>() {
            @Override
            public void onChanged(Elements elements) {
                Log.i("TAG","收到数据。。。。。。。。");
               mainBinding.setElements(elements);
            }
        });
    }

    public void initFragment() {
        firstFragment = new FirstFragment();
    }

    Timer timer, timer1, timer2;
    int index =0;
    int page = 4;
    public void changFragment() {
        timer = new Timer();

        timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                final Request live = new Request.Builder().url("http://61.153.246.242:8888/qxdata/QxService.svc/getnewzdzhourdata/58642").build();
                Request zhish = new Request.Builder().url("http://115.220.4.68:8081/qxdata/QxService.svc/gettszsybdata/K2159").build();

                Request wea = new Request.Builder().url("http://61.153.246.242:8888/qxdata/QxService.svc/getdayybdata/58642").build();
                Request seven = new Request.Builder().url("http://115.220.4.68:8081/qxdata/QxService.svc/geths7dayybdata/K2159").build();

             /*   client.newCall(live).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body().string();
                        body = body.replace("℃","").replace("%","").replace("mm","").
                                replace("m/s","").replace("hPa","");
                        if (TextUtils.isEmpty(body) || body.length() < 5) return;
                        Gson gson = new Gson();
                        Live lives = gson.fromJson(body, Live.class);
                        EventBus.getDefault().post(lives);
                    }
                });*/

                client.newCall(wea).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String body = response.body().string();
                        if (TextUtils.isEmpty(body) || body.length() < 5) return;
                        Gson gson = new Gson();
                        Wea lives = gson.fromJson(body, Wea.class);
                        EventBus.getDefault().post(lives);
                    }

                });

            }
        }, 0, 5 * 60 * 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changBackImage(Integer index) {
        mainBinding.setBackIndex(index);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateTime(UpdateTime index) {
        Log.i("TAG", "收到：" + index.toString());
        mainBinding.setUpdate(index.time);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateWea(Wea index) {
        Log.i("TAG", "收到：" + index.toString());
        mainBinding.setWea(index);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateLive(Live live) {

        firstFragment.updateInfo(live);
    }

    public TimerTask dates;
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年M月dd日 E HH:mm", Locale.CHINA);

    public void initTask() {

        dates = new TimerTask() {
            @Override
            public void run() {
                Lunar lunar = new Lunar(Calendar.getInstance());
                EventBus.getDefault().post(dateFormat.format(new Date())+"\n"+lunar.getAllDate() );
            }
        };
        timer1 = new Timer();

        timer1.schedule(dates, 0, 40 * 1000);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updataDate(String bean) {
        Log.i("TAG", "时间：" + bean);
        mainBinding.setDate(bean);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

}