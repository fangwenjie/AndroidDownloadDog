package com.fangwenjie.downloadgo;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.fangwenjie.downloadgo.Utils.GoDebug;
import static com.fangwenjie.downloadgo.Utils.TAG;
import static com.fangwenjie.downloadgo.Utils.hashKeyForTaskUrl;


/**
 * Created by fangwenjie on 2017/4/20.
 */

class Task {

    public String getName() {
        return taskName;
    }

    enum Status {
        NONE("NONE"),
        PAUSE("PAUSE"),
        ING("ING"),//下载中
        COMPLETE("COMPLETE"),//完成
        ;
        private String mName;

        Status(String name) {
            this.mName = name;
        }

        public String getName() {
            return mName;
        }

        public static Status findStatusByName(String statusName) {
            for (Status status : values()) {
                if (status.getName().equals(statusName)) {
                    return status;
                }
            }
            return NONE;
        }

    }

    public static final int RESULT_SUCC = 0;// 下载成功
    public static final int RESULT_FAIL = 1;//下载失败
    public static final int RESULT_CANCEL = 2;//取消下载

    String taskName;
    String taskUrl;
    String taskId;

    private TaskReportEntity entity = new TaskReportEntity();

    private TaskReportProvider taskReportProvider;

    void setTaskReportProvider(TaskReportProvider taskReportProvider) {
        this.taskReportProvider = taskReportProvider;
    }

    private Task(Builder builder) {
        this.taskUrl = builder.taskUrl;
        this.taskName = builder.taskName;

        //other setting;
        entity.taskName = this.taskName;
        taskId = hashKeyForTaskUrl(taskUrl);
    }

    private boolean taskPauseFlag;

    private final Object pauseLock = new Object();

    private volatile Status status = Status.NONE;

    public Status getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (!taskName.equals(task.taskName)) return false;
        return taskUrl.equals(task.taskUrl);
    }

    @Override
    public int hashCode() {
        int result = taskName.hashCode();
        result = 31 * result + taskUrl.hashCode();
        return result;
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
     * 取消下载（不管成功与否）
     */
    void onCancel() {
        taskCancelFlag = true;
        taskPauseFlag = false;
        synchronized (pauseLock) {
            pauseLock.notify();
        }
    }

    private byte[] buffer = new byte[1024 * 100];

    /**
     * 任务开始前的一下任务检查
     *
     * @return 是否要继续任务
     */
    boolean onPreExecuteJob() {
        if (taskReportProvider == null) {
            throw new IllegalStateException("TaskReportProvider is not created");
        }

        //check 一下下载路径时候可用
        if (!FileStrategy.downloadGoDirAvailable()) {
            //下载目录不可用
            return false;
        }

        if (GoDebug) {
            Log.d(TAG, "taskId #" + taskId);
        }

        TaskReportEntity loadTask = taskReportProvider.getTaskReport(taskId);
        if (loadTask != null) {
            if (GoDebug) {
                Log.d(TAG, "load task 存在还原task继续下载 #" + loadTask.toString());
            }
            //check 文件是否如记录一样的存在
            boolean isFileExists = false;
            File file = new File(loadTask.filePath);
            if (file.exists()) {
                isFileExists = true;
            }

            if (!isFileExists) {
                loadTask.resetEntity();
                return true;
            }

            if (Status.COMPLETE.equals(loadTask.getStatus())) {
                if (GoDebug) {
                    Log.d(TAG, "已经下载完成了 #" + loadTask.toString());
                }
                EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.COMPLETE, loadTask.filePath));
                return false;
            } else {
                entity = loadTask;
                return true;
            }
        } else {
            if (GoDebug) {
                Log.d(TAG, "load task null 重头开始下载");
            }
            return true;
        }
    }

    OkHttpClient client = new OkHttpClient();

    /**
     * 执行下载任务
     *
     * @return
     */
    int onExecuteJob() {
        EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.START));
        ResponseBody body = null;
        try {
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(taskUrl);

            boolean useRange = false;
            if (entity.hasDownloaded > 0 && !entity.getStatus().equals(Status.COMPLETE)) {
                requestBuilder.addHeader("Range", "bytes=" + entity.hasDownloaded + "-");
                useRange = true;
                if (GoDebug) {
                    Log.d(TAG, "断点续传 skip result #");
                }
            }

            Response response = client.newCall(requestBuilder.build()).execute();

            Response priorResponse = response.priorResponse();
            if (priorResponse != null) {
                if (priorResponse.isRedirect()) {
                    Log.d(TAG, "发生重定向 #");
                }
            }

            if (response.isSuccessful()) {
                Log.d(TAG, "请求成功 #" + response.request().url().toString());
            } else {
                Log.d(TAG, "请求失败 #");
                taskReportProvider.addTaskReport(taskId, entity);
                EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.FAIL));
                return RESULT_FAIL;
            }

            //本次请求数据的大小，note 不是全量大小
            body = response.body();

            if (body != null) {
                long contentSize = body.contentLength();
                entity.taskSize = contentSize + entity.hasDownloaded;

                InputStream inputStream = body.byteStream();

                int count;
                long hasDownload = entity.hasDownloaded;

                if (!TextUtils.isEmpty(entity.taskName)) {
                    taskName = entity.taskName;
                }

                String fileName = taskReportProvider.getDownloadDir().getAbsolutePath() + "/" + taskName + ".apk";

                File file = new File(fileName);
                if (!file.exists()) {
                    if (file.createNewFile()) {
                        file.setWritable(true);
                        file.setReadable(true);
                    }
                }

                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
                if (useRange) {
                    randomAccessFile.seek(entity.hasDownloaded);
                }
                entity.filePath = file.getAbsolutePath();

                long speedMonitorBegin = SystemClock.elapsedRealtime();
                long downloadSpeed = 0;

                while ((count = inputStream.read(buffer)) != -1) {
                    status = Status.ING;
                    randomAccessFile.write(buffer, 0, count);

                    //已经下载的内容长度
                    hasDownload = hasDownload + count;
                    //测试间隔内下载累计
                    downloadSpeed += count;

                    //测试下载速度
                    long speedMonitorEnd = SystemClock.elapsedRealtime();
                    long speedInterval = speedMonitorEnd - speedMonitorBegin;

                    if (GoDebug) {
                        Log.d(TAG, "speedInterval #" + speedInterval);
                    }
                    if (speedInterval > 1000) {
                        //确定下载进度
                        int downloadProgress = (int) ((hasDownload * 100) / entity.taskSize);

                        //通知下载进度
                        DownloadMsgEvent event = new DownloadMsgEvent("update Task#", downloadProgress);
                        event.taskId = taskId;
                        event.taskName = taskName;
                        event.taskSize = entity.taskSize;
                        event.downloaded = hasDownload;
                        event.downloadSpeed = downloadSpeed;
                        EventBus.getDefault().post(event);

                        downloadSpeed = 0;
                        speedMonitorBegin = speedMonitorEnd;
                    }

                    if (taskPauseFlag) {
                        synchronized (pauseLock) {
                            //暂停下载,并对任务的状态进行保存，方便以后对任务进行还原唤醒
                            status = Status.PAUSE;
                            entity.status = status.getName();
                            entity.hasDownloaded = hasDownload;
                            taskReportProvider.addTaskReport(taskId, entity);
                            EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.PAUSE));
                            pauseLock.wait();
                            EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.RESUME));
                        }
                    }

                    if (taskCancelFlag) {
                        //删除已经下载的内容
                        Utils.deleteFile(entity.filePath);
                        if (GoDebug) {
                            Log.d(TAG, "删除已经下载的文件部分#" + entity.filePath);
                        }
                        // 取消下载,清掉文件，不能还原
                        taskReportProvider.deleteTaskReport(taskId);
                        if (GoDebug) {
                            Log.d(TAG, "删除已经下载的文件记录#" + taskId);
                        }

                        EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.DELETE));
                        return RESULT_CANCEL;
                    }
                }

                randomAccessFile.close();
                status = Status.COMPLETE;
                entity.status = status.getName();
                taskReportProvider.addTaskReport(taskId, entity);
                EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.COMPLETE, entity.filePath));
                return RESULT_SUCC;
            } else {
                taskReportProvider.addTaskReport(taskId, entity);
                EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.FAIL));
                return RESULT_FAIL;
            }

        } catch (IOException e) {
            taskReportProvider.addTaskReport(taskId, entity);
            EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.FAIL));
            e.printStackTrace();
            return RESULT_FAIL;
        } catch (InterruptedException e) {
            taskReportProvider.addTaskReport(taskId, entity);
            EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.FAIL));
            e.printStackTrace();
            return RESULT_FAIL;
        } catch (Exception e) {
            taskReportProvider.addTaskReport(taskId, entity);
            EventBus.getDefault().post(new GoMsgEvent(taskId, taskName, DownloadGoConst.FAIL));
            e.printStackTrace();
            return RESULT_FAIL;
        } finally {
            if (body != null) {
                body.close();
            }
        }

    }

    public static class Builder {
        private String taskName;
        private String taskUrl;

        public Builder setUrl(String url) {
            taskUrl = url;
            return this;
        }

        public Builder setName(String name) {
            taskName = name;
            return this;
        }

        public Task build() {
            return new Task(this);
        }

    }

}
