package com.fangwenjie.downloadgo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.fangwenjie.downloadgo.TaskEvent.CANCEL;
import static com.fangwenjie.downloadgo.TaskEvent.PAUSE;
import static com.fangwenjie.downloadgo.TaskEvent.RESUME;
import static com.fangwenjie.downloadgo.Utils.GoDebug;
import static com.fangwenjie.downloadgo.Utils.TAG;

/**
 * Created by fangwenjie on 2017/6/12.
 */

public class DGoService extends Service {
    private DownloadExecutorCore executorCore;
    private volatile TaskReportProvider taskReportProvider;

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
        executorCore = new DownloadExecutorCore();
        this.taskReportProvider = new TaskProvider(getApplicationContext());
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
                GoEvent goEvent = new GoEvent(event.msg, event.event, event.filePath, event.taskId);
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

    public IDownloadGoService.Stub mBinder = new IDownloadGoService.Stub() {

        @Override
        public String addTask(TaskMsg msg) throws RemoteException {
            if (GoDebug) {
                Log.d(TAG, "goMsg #" + msg.toString());
            }

            Task task = new Task.Builder()
                    .setName(msg.taskName)
                    .setUrl(msg.taskUrl)
                    .build();
            task.setTaskReportProvider(taskReportProvider);
            executorCore.addTask(task);

            return task.taskId;
        }

        @Override
        public void onSendTaskEvent(String taskId, final int TaskEvent) throws RemoteException {
            Task task = executorCore.findTaskByTaskId(taskId);
            if (task != null) {
                switch (TaskEvent) {
                    case PAUSE:
                        if (task.getStatus().equals(Task.Status.PAUSE)) {
                            task.onResume();
                        } else {
                            task.onPause();
                        }
                        break;
                    case RESUME:
                        task.onResume();
                        break;
                    case CANCEL:
                        task.onCancel();
                        break;
                }

            }
        }

        @Override
        public void addCallback(IGoMsgCallback callback) throws RemoteException {
            msgCallback = callback;
        }
    };

    private IGoMsgCallback msgCallback;

}
