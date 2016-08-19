package com.ysr.ftpdemo;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.ysr.ftpdemo.utils.ConfigEntity;
import com.ysr.ftpdemo.utils.ConfigUtils;

/**
 * Created by Administrator on 2016/8/11.
 */
public class MyApplication extends Application {
    private MyApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public void InitApp(final Context context, final Handler handler) {
        ConfigEntity configEntity = ConfigUtils.loadftpConfig(context);
    }
}
