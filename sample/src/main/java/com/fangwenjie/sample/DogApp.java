package com.fangwenjie.sample;

import android.app.Application;

/**
 * Created by fangwenjie on 2017/5/24.
 */

public class DogApp extends Application {

    private volatile static DogApp instance;

    public static DogApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        DownloadGo.getInstance().init(getApplicationContext());
    }
}
