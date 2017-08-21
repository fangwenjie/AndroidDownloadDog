package com.fangwenjie.sample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.fangwenjie.downloadgo.TaskEvent;

/**
 * Created by fangwenjie on 2017/6/19.
 */

public class DownloadNotificationReceiver extends BroadcastReceiver {

    public static final String PAUSE = "pause";//暂停任务
    public static final String RESUME = "resume";//恢复任务
    public static final String CANCEL = "cancel";//取消任务


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String taskId = intent.getStringExtra("taskId");
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case "pause":
                    DownloadGo.getInstance().onSendTaskEvent(taskId, TaskEvent.PAUSE);
                    break;
                case "cancel":
                    DownloadGo.getInstance().onSendTaskEvent(taskId, TaskEvent.CANCEL);
                    break;
            }
        }
    }

}
