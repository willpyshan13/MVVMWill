package com.will.mvvm.app;

import androidx.multidex.MultiDex;

import com.will.mvvm.BuildConfig;
import com.will.mvvm.base.BaseApplication;
import com.will.mvvm.utils.KLog;

public class AppApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        //是否开启打印日志
        KLog.init(BuildConfig.DEBUG);
        MultiDex.install(this);
    }
}
