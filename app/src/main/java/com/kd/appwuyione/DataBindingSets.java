package com.kd.appwuyione;

import android.graphics.Color;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;

public  class DataBindingSets {

    @BindingAdapter("setBackImage")
    public static void setBackImage(LinearLayout view,Integer index){
        if (index==null)return;
        if(index==0){
            view.setBackgroundResource(R.drawable.db1);
        }else if(index==1){
            view.setBackgroundResource(R.drawable.db2);
        }else if(index==2){
            view.setBackgroundResource(R.drawable.db3);
        }
    }
    @BindingAdapter("setTextColor")
    public static void setTextColor(TextView view, Integer index){
        if (index==null)return;
        if(index==0){
            view.setTextColor(Color.WHITE);
        }else if(index==1){
            view.setTextColor(Color.YELLOW);
        }else if(index==2){
            view.setTextColor(Color.argb(255,255,255,0));
        }
    }
    @BindingAdapter("setText")
    public static void setText(MarqueeView view, String text){
        if (TextUtils.isEmpty(text))return;
        view.setContent(text);
    }


}
