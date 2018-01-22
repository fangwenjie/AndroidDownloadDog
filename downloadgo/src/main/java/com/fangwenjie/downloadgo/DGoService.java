package com.fangwenjie.downloadgo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fangwenjie.downloadgo.task.GoTask;
import com.fangwenjie.downloadgo.task.TaskParams;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.fangwenjie.downloadgo.Utils.GoDebug;
import static com.fangwenjie.downloadgo.Utils.TAG;
import static com.fangwenjie.downloadgo.task.TaskEvent.PAUSE;
import static com.fangwenjie.downloadgo.task.TaskEvent.RESUME;
/**
 * Created by fangwenjie on 2017/6/12.
 */

public class DGoService extends Service {
    DownloadExecutorCore executorCore;
    volatile TaskReportProvider taskReportProvider;
    IGoMsgCallback msgCallback;
    public IDownloadGoService.Stub mBinder = new IDownloadGoService.Stub() {

        @Override
        public String addTask(TaskMsg msg) throws RemoteException {
            return addNewTask(msg);
        }

        @Override
        public int findTask(String taskId) throws RemoteException {
            return executorCore.getTaskStatus(taskId);
        }

        @Override
        public void onSendTaskEvent(String taskId, final int TaskEvent) throws RemoteException {
            switch (TaskEvent) {
                case PAUSE:
                    if (executorCore != null) {
                        executorCore.pause(taskId);
                    }
                    break;
                case RESUME:
                    if (executorCore != null) {
                        executorCore.resume(taskId);
                    }
                    break;
            }

        }

        @Override
        public void addCallback(IGoMsgCallback callback) throws RemoteException {
            msgCallback = callback;
        }


    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (GoDebug) {
            Log.d(TAG, "launch service #");
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        EventBus.getDefault().register(this);

        taskReportProvider = new TaskProvider(getApplicationContext());
        executorCore = new DownloadExecutorCore(taskReportProvider);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleGoMsgEvent(GoMsgEvent event) {
        if (GoDebug) {
            Log.d(TAG, "onHandleGoMsgEvent #" + event.toString());
        }

        if (msgCallback != null) {
            try {
                GoEvent goEvent = new GoEvent("DownloadGo", event.event, event.filePath, event.taskId);
                goEvent.filePath = event.filePath;
                if (GoDebug) {
                    Log.d(TAG, "goEvent #" + goEvent.toString());
                }
                msgCallback.onBarkEvent(goEvent);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleDownloadMsgEvent(DownloadMsgEvent event) {
        if (GoDebug) {
            Log.d(TAG, "DownloadMsgEvent #" + event.toString());
        }

        if (msgCallback != null) {
            try {
                GoMsg msg = new GoMsg(
                        event.msg,
                        event.taskId,
                        event.taskName,
                        event.downloaded,
                        event.taskSize,
                        event.downloadSpeed,
                        event.downloadProgress);
                msgCallback.onBarkMsg(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    String addNewTask(TaskMsg msg) {
        if (GoDebug) {
            Log.d(TAG, "add new task #" + msg.toString());
        }
        if (msg.taskType == TaskMsg.TYPE_EXPLICIT) {
            TaskParams params = new TaskParams(msg.taskType, msg.taskId, msg.taskName, msg.taskUrl, msg.fileName);
            GoTask task = new GoTaskExplicit(params, taskReportProvider);
            return executorCore.addExplicitTask((GoTaskExplicit) task);
        } else if (msg.taskType == TaskMsg.TYPE_SILENCT) {
            TaskParams params = new TaskParams(msg.taskType, msg.taskId, msg.taskName, msg.taskUrl, msg.fileName);
            GoTaskSilence taskSilence = new GoTaskSilence(params, taskReportProvider);
            return executorCore.addSilenceTask(taskSilence);
        }
        return "";
    }

}
