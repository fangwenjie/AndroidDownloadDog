package com.fangwenjie.sample;

import android.app.Application;

import com.fangwenjie.download.TaskCache;

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

        //init cache
        TaskCache.getInstance().initCache(new TaskCache.TaskCacheParam(getApplicationContext()));
    }
}
