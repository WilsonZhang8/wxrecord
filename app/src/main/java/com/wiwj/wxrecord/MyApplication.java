package com.wiwj.wxrecord;

/**
 * Created by Administrator on 2017/2/18 0018.
 */

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * 编写自己的Application，管理全局状态信息，比如Context
 * @author zghw
 *
 */
public class MyApplication extends Application {
    private static Context context;

    public void onCreate() {
        //获取Context
        context = getApplicationContext();
    }
    //返回
    public static Context getContextObject(){
        return context;
    }
    public static SharedPreferences getSharedPreferences(){
        return context.getSharedPreferences("recordMsgSeq", Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
    }
}