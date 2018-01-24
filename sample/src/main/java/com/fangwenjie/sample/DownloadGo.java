package com.fangwenjie.sample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.fangwenjie.ddog.DGoService;
import com.fangwenjie.ddog.GoEvent;
import com.fangwenjie.ddog.GoMsg;
import com.fangwenjie.ddog.IDownloadGoService;
import com.fangwenjie.ddog.IGoMsgCallback;
import com.fangwenjie.ddog.TaskMsg;
import com.fangwenjie.ddog.task.TaskEvent;
import com.fangwenjie.ddog.task.TaskStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 下载组件对接；因为需要独立的进程进行文件的下载
 * Created by fangwenjie on 2017/4/20.
 */

public class DownloadGo {

    private static volatile DownloadGo instance;

    private DownloadGo() {
    }

    public static DownloadGo getInstance() {
        if (instance == null) {
            synchronized (DownloadGo.class) {
                if (instance == null) {
                    instance = new DownloadGo();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        context.startService(new Intent(context, DGoService.class));
        context.bindService(new Intent(context, DGoService.class), connection, 0);
    }

    private boolean isBound;
    private IDownloadGoService downloadGoService;

    private List<TaskMsg> taskPendingList = new ArrayList<>();

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadGoService = IDownloadGoService.Stub.asInterface(service);
            isBound = true;
            try {
                downloadGoService.addCallback(msgCallback);
                if (!taskPendingList.isEmpty()) {
                    Iterator<TaskMsg> iterator = taskPendingList.iterator();
                    while (iterator.hasNext()) {
                        TaskMsg task = iterator.next();
                        if (addTask(task)) {
                            iterator.remove();
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            downloadGoService = null;
        }
    };

    private IGoMsgCallback msgCallback = new IGoMsgCallback.Stub() {
        @Override
        public void onBarkMsg(final GoMsg msg) throws RemoteException {
            Log.d("DownloadGo", "barkMsg:" + msg.progress);
        }

        @Override
        public void onBarkEvent(final GoEvent event) throws RemoteException {
            switch (event.event) {
                case TaskEvent.START:
                    Log.d("DownloadGo", "GoEvent: Start");
                    EventBus.getDefault().post(new DownloadEvent("Started"));

                    break;

                case TaskEvent.PAUSE:
                    Log.d("DownloadGo", "GoEvent: Pause");
                    EventBus.getDefault().post(new DownloadEvent("Paused"));

                    break;

                case TaskEvent.RESUME:
                    Log.d("DownloadGo", "GoEvent: Resume");
                    EventBus.getDefault().post(new DownloadEvent("resumed"));

                    break;
                case TaskEvent.SUCC:
                    EventBus.getDefault().post(new DownloadEvent("succed"));

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GoApp.go.getApplicationContext(), "下载完成", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;

                case TaskEvent.FAIL:
                    EventBus.getDefault().post(new DownloadEvent("failed"));

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GoApp.go.getApplicationContext(), "下载失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
            }
        }

    };

    /**
     * 添加任务
     *
     * @param taskMsg return
     */
    public boolean addTask(TaskMsg taskMsg) {
        if (isBound) {
            try {
                String addMsg = downloadGoService.addTask(taskMsg);
                Toast.makeText(GoApp.go.getApplicationContext(),addMsg,Toast.LENGTH_SHORT).show();
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            taskPendingList.add(taskMsg);
            try {
                init(GoApp.go);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 通过任务Id & 任务事件 操作任务
     *
     * @param taskId    任务Id
     * @param taskEvent 任务事件
     */
    public void onSendTaskEvent(String taskId, int taskEvent) {
        try {
            downloadGoService.onSendTaskEvent(taskId, taskEvent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询指定任务的当前状态
     *
     * @param taskId
     * @return
     */
    public int getTaskStatus(String taskId) {
        try {
            return downloadGoService.findTask(taskId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return TaskStatus.NONE;
    }
}
