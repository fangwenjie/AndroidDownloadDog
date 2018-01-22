package com.fangwenjie.sample;

import android.app.Application;

/**
 * Created by fangwenjie on 2017/10/14.
 */

public class GoApp extends Application {

    public static GoApp go;

    @Override
    public void onCreate() {
        super.onCreate();
        go = this;
        //下载管理器初始化,暂时未常驻进程
        DownloadGo.getInstance().init(getApplicationContext());
    }
}
