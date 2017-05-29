package com.fangwenjie.download;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fangwenjie on 2017/4/20.
 */

public class Task implements Cloneable {

    public String getName() {
        return taskName;
    }

    public enum Status {
        NONE,//未知状态
        PAUSE,//暂停
        ING,//下载中
        COMPLETE,//完成
    }

    public static final int RESULT_SUCC = 0;// 下载成功
    public static final int RESULT_FAIL = 1;//下载失败
    public static final int RESULT_CANCEL = 2;//取消下载

    String taskName;
    String taskUrl;
    String taskId;

    private TaskCache.TaskReportEntity entity = new TaskCache.TaskReportEntity();

    private Task(Builder builder) {
        this.taskName = builder.taskName;
        this.taskUrl = builder.taskUrl;
        entity.taskName = this.taskName;
    }

    private boolean taskPauseFlag;

    private final Object pauseLock = new Object();

    private volatile Status status = Status.NONE;

    public Status getStatus() {
        return status;
    }

    /**
     * 暂停下载
     */
    public void onPause() {
        taskPauseFlag = true;
        status = Status.PAUSE;
    }

    /**
     * 继续下载
     */
    public void onResume() {
        taskPauseFlag = false;
        synchronized (pauseLock) {
            pauseLock.notify();
        }
    }

    private boolean taskCancelFlag;

    /**
     * 取消下载（不管成功没成功）
     */
    void onCancel() {
        taskCancelFlag = true;
        taskPauseFlag = false;
        synchronized (pauseLock) {
            pauseLock.notify();
        }
    }

    private byte[] buffer = new byte[1024 * 1024];

    int onExecuteJob() {
        EventBus.getDefault().post(new DogMsgEvent(taskName + " begin"));
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(taskUrl).openConnection();
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(30000);
            connection.setUseCaches(true);
            connection.setRequestProperty("Connection", "Keep-Alive");

            boolean useRange = false;
            if (entity.hasDownloaded > 0 &&
                    !entity.getStatus().equals(Status.COMPLETE)) {
                connection.setRequestProperty("Range", "bytes=" + entity.hasDownloaded + "-");
                useRange = true;
                Log.d("clarkfang", "断点续传 skip result #");
            }
            connection.connect();

            //本次请求数据的大小，note 不是全量大小
            long contentSize = connection.getContentLength();
            entity.taskSize = contentSize + entity.hasDownloaded;

            InputStream inputStream = connection.getInputStream();

            int count;
            long hasDownload = entity.hasDownloaded;

            if (!TextUtils.isEmpty(entity.taskName)) {
                taskName = entity.taskName;
            }
            String fileName = TaskCache.getInstance().getDownloadDir().getAbsolutePath() + "/" + taskName + ".apk";

            File file = new File(fileName);
            if (!file.exists()) {
                file.createNewFile();
                file.setWritable(true);
                file.setReadable(true);
            }

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
            if (useRange) {
                randomAccessFile.seek(entity.hasDownloaded);
            }
            entity.filePath = file.getAbsolutePath();

            while ((count = inputStream.read(buffer)) != -1) {
                status = Status.ING;
                randomAccessFile.write(buffer, 0, count);


                hasDownload = hasDownload + count;
                int downloadProgress = (int) ((hasDownload * 100) / entity.taskSize);
                EventBus.getDefault().post(new DownloadMsgEvent("downloadedSize#", downloadProgress));
                if (taskPauseFlag) {
                    synchronized (pauseLock) {
                        //暂停下载,并对任务的状态进行保存，方便以后对任务进行还原唤醒
                        status = Status.PAUSE;
                        entity.status = status.name();
                        entity.hasDownloaded = hasDownload;
                        TaskCache.getInstance().addTaskToDisk(taskUrl, entity);
                        EventBus.getDefault().post(new DogMsgEvent(taskName + " pause"));
                        pauseLock.wait();
                        EventBus.getDefault().post(new DogMsgEvent(taskName + " resume"));
                    }
                }

                if (taskCancelFlag) {
                    // 取消下载,清掉文件，不能还原
                    TaskCache.getInstance().deleteTaskForm(taskUrl);
                    EventBus.getDefault().post(new DogMsgEvent(taskName + " cancel"));
                    EventBus.getDefault().post(new DownloadMsgEvent("downloadedSize#", 0));
                    return RESULT_CANCEL;
                }
            }

            randomAccessFile.close();
            status = Status.COMPLETE;
            entity.status = status.name();
            TaskCache.getInstance().addTaskToDisk(taskUrl, entity);
            EventBus.getDefault().post(new DogMsgEvent(taskName + " succ"));
            return RESULT_SUCC;
        } catch (IOException e) {
            TaskCache.getInstance().addTaskToDisk(taskUrl, entity);
            EventBus.getDefault().post(new DogMsgEvent(taskName + " fail"));
            e.printStackTrace();
            return RESULT_FAIL;
        } catch (InterruptedException e) {
            TaskCache.getInstance().addTaskToDisk(taskUrl, entity);
            EventBus.getDefault().post(new DogMsgEvent(taskName + " fail"));
            e.printStackTrace();
            return RESULT_FAIL;
        } catch (Exception e) {
            TaskCache.getInstance().addTaskToDisk(taskUrl, entity);
            EventBus.getDefault().post(new DogMsgEvent(taskName + " fail"));
            e.printStackTrace();
            return RESULT_FAIL;
        }

    }

    boolean onPreExecuteJob() {
        TaskCache.TaskReportEntity loadTask = TaskCache.getInstance().getTaskFromDisk(taskUrl);
        if (loadTask != null) {
            Log.d("clarkfang", "load task 存在还原task继续下载 #" + loadTask.toString());

            if (status.COMPLETE.equals(loadTask.getStatus())) {
                Log.d("clarkfang", "已经下载完成了 #" + loadTask.toString());
                EventBus.getDefault().post(new DogMsgEvent(taskName + " succ"));
                return false;
            } else if (!Status.COMPLETE.equals(loadTask.getStatus())) {
                entity = loadTask;
                return true;
            } else {

                return true;
            }
        } else {
            Log.d("clarkfang", "load task null 重头开始下载");
            return true;
        }
    }

    public static class Builder {
        private String taskName;
        private String taskUrl;

        public Builder setName(String name) {
            taskName = name;
            return this;
        }

        public Builder setUrl(String url) {
            taskUrl = url;
            return this;
        }

        public Task build(Context context) {
            return new Task(this);
        }

    }

}
