package com.fangwenjie.sample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;

import com.fangwenjie.downloadgo.DGoService;
import com.fangwenjie.downloadgo.DownloadGoConst;
import com.fangwenjie.downloadgo.GoEvent;
import com.fangwenjie.downloadgo.GoMsg;
import com.fangwenjie.downloadgo.IDownloadGoService;
import com.fangwenjie.downloadgo.IGoMsgCallback;
import com.fangwenjie.downloadgo.TaskMsg;

import org.greenrobot.eventbus.EventBus;


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

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadGoService = IDownloadGoService.Stub.asInterface(service);
            isBound = true;
            try {
                downloadGoService.addCallback(msgCallback);
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
            long progress = msg.progress;
            EventBus.getDefault().post(new MsgEvent(msg));
        }

        @Override
        public void onBarkEvent(final GoEvent event) throws RemoteException {
            EventBus.getDefault().post(new StatusEvent(event));

            switch (event.event) {
                case DownloadGoConst.START:
//                    createDownloadNotification(DogApp.getInstance().getApplicationContext(), event.taskId, 0, "暂停");
                    break;

                case DownloadGoConst.PAUSE:

                    break;

                case DownloadGoConst.RESUME:

                    break;

                case DownloadGoConst.DELETE:

                    break;

                case DownloadGoConst.COMPLETE:

                    break;

                case DownloadGoConst.FAIL:
                    break;
            }
        }

    };

    /**
     * 添加任务
     *
     * @param taskMsg
     */
    public String addTask(TaskMsg taskMsg) {
        if (isBound) {
            try {
                String taskId = downloadGoService.addTask(taskMsg);
                return taskId;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return null;
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


    /*******下载相关通知，暂时丑陋的放在这里*******/
    private NotificationCompat.Builder mBuilder;
    private RemoteViews downloadNotifyView;

    public void createDownloadNotification(Context context, String taskId, int progress, String pauseString) {

        downloadNotifyView = new RemoteViews(context.getPackageName(), R.layout.download_notification_view);
        downloadNotifyView.setProgressBar(R.id.download_notification_downloadprogress, 100, progress, false);
        downloadNotifyView.setTextViewText(R.id.download_notification_pausebtn, pauseString);

        //暂停任务
        Intent pauseIntent = new Intent(context, DownloadNotificationReceiver.class);
        pauseIntent.setAction("pause");
        pauseIntent.putExtra("taskId", taskId);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        downloadNotifyView.setOnClickPendingIntent(R.id.download_notification_pausebtn, pausePendingIntent);

        Intent contentIntent = new Intent(context, DownloadNotificationReceiver.class);
        contentIntent.setAction("content");
        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(context, 2, contentIntent, 0);

        mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(android.R.drawable.stat_sys_download)
                .setContent(downloadNotifyView)
                .setContentIntent(contentPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, mBuilder.build());
    }


}
