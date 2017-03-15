package com.dlc.morepet;

import android.app.Application;
import android.content.Context;

import com.tencent.bugly.Bugly;

/**
 * Auther by winds on 2016/12/28
 * Email heardown@163.com
 */
public class AppConfig extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        Bugly.init(this, "f6b900decf", false);
//        CrashHandler.getInstance().init(this);
    }


    public static Context getContext() {
        return sContext;
    }
}
