package com.fangwenjie.download;

import java.util.Observer;

/**
 * Created by fangwenjie on 2017/4/20.
 */

public class AndroidDownloadDog {

    private static volatile AndroidDownloadDog instance;

    private BigWatchDog mBigWatchDog;

    private AndroidDownloadDog() {
        executorCore = new DownloadExecutorCore();
        mBigWatchDog = new BigWatchDog();
    }

    public static AndroidDownloadDog getInstance() {
        if (instance == null) {
            synchronized (AndroidDownloadDog.class) {
                if (instance == null) {
                    instance = new AndroidDownloadDog();
                }
            }
        }
        return instance;
    }

    private DownloadExecutorCore executorCore;

    public void addTask(Task task) {
        executorCore.addTask(task);
    }

    public void addObserver(Observer observer) {
        executorCore.addObserver(observer);
    }

    public void deleteObserver(Observer observer) {
        executorCore.deleteObserver(observer);
    }

    public void cancelTask(Task task) {
        executorCore.removeTask(task);
    }

    public BigWatchDog getBigWatchDog() {
        return mBigWatchDog;
    }

}
