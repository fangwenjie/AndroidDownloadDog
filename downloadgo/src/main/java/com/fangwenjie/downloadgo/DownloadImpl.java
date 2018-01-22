package com.fangwenjie.downloadgo;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.fangwenjie.downloadgo.task.TaskParams;
import com.fangwenjie.downloadgo.task.TaskStatus;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.fangwenjie.downloadgo.Utils.TAG;

/**
 * 下载实际执行实现
 * Created by fangwenjie on 2018/1/4.
 */

public class DownloadImpl {

    private final byte[] buffer = new byte[1024 * 100];

    private DownloadCallback mCallback;
    private final TaskParams taskParams;
    private final TaskReportEntity reportEntity;
    private final Object pauseLock = new Object();

    boolean isAlive = false;

    public DownloadImpl(TaskParams params, TaskReportEntity reportEntity) {
        this.taskParams = params;
        this.reportEntity = reportEntity;
        mCallback = new DefaultCallback();
    }

    public void setCallback(DownloadCallback callback) {
        this.mCallback = callback;
    }

    public void doDownload() {
        ResponseBody body = null;
        RandomAccessFile randomAccessFile = null;
        try {
            isAlive = true;
            Request.Builder requestBuilder = new Request.Builder();
            requestBuilder.url(taskParams.taskUrl);

            boolean useRange = false;
            //下载的断点续传
            if (reportEntity.hasDownloaded > 0
                    && !reportEntity.getStatus().equals("true")) {
                requestBuilder.addHeader("Range", "bytes=" + reportEntity.hasDownloaded + "-");
                useRange = true;
            }

            OkHttpClient client = new OkHttpClient().newBuilder().build();
            Response response = client.newCall(requestBuilder.build()).execute();

            String fileName = "downloadGo";
            String fileType = "unknown";
            if (response.isSuccessful()) {
                //确定需要下载文件的文件名称
                String reallyAddress = response.request().url().toString();
                fileName = Utils.findFilename(taskParams.taskName, response, reallyAddress);
                if (TextUtils.isEmpty(fileName)) {
                    reportEntity.taskName = Utils.hashKeyForTaskUrl(reallyAddress);
                } else {
                    reportEntity.taskName = fileName;
                }

                mCallback.updateTaskReport(reportEntity);

                fileType = MimeTypeMap.getFileExtensionFromUrl(fileName);
            }


            if (TextUtils.isEmpty(fileType)) {
                Log.d(TAG, "下载文件类型未知:.unknown");
            } else {
                Log.d(TAG, "文件类型:." + fileType);
            }

            body = response.body();
            if (body != null) {

                //本次请求数据的大小，剩余文件的大小
                long contentSize = body.contentLength();
                reportEntity.taskSize = contentSize + reportEntity.hasDownloaded;

                InputStream inputStream = body.byteStream();
                int count;
                long hasDownload = reportEntity.hasDownloaded;
                if (!TextUtils.isEmpty(reportEntity.taskName)) {
                    fileName = reportEntity.taskName;
                }

                String fileNamePath = FileStrategy.getDownloadGo().getAbsolutePath() + "/" + fileName;
                File file = new File(fileNamePath);
                if (!file.exists()) {
                    if (file.createNewFile()) {
                        file.setWritable(true);
                        file.setReadable(true);
                    }
                }

                randomAccessFile = new RandomAccessFile(file, "rwd");
                //
                if (useRange) {
                    randomAccessFile.seek(reportEntity.hasDownloaded);
                }
                reportEntity.filePath = file.getAbsolutePath();

                long speedMonitorBegin = SystemClock.elapsedRealtime();//下载速度检测
                long downloadSpeed = 0;

                while ((count = inputStream.read(buffer)) != -1) {

                    randomAccessFile.write(buffer, 0, count);
                    //已经下载的内容长度
                    hasDownload = hasDownload + count;
                    //测试间隔内下载累计
                    downloadSpeed += count;

                    //测试下载速度
                    long speedMonitorEnd = SystemClock.elapsedRealtime();
                    long speedInterval = speedMonitorEnd - speedMonitorBegin;

                    if (speedInterval > 1000) {
                        //确定下载进度
                        int downloadProgress = (int) ((hasDownload * 100) / reportEntity.taskSize);

                        //通知下载进度
                        DownloadMsgEvent event = new DownloadMsgEvent("update ExplicitTask#", downloadProgress);
                        event.taskId = taskParams.taskId;
                        event.taskName = taskParams.taskName;
                        event.taskSize = reportEntity.taskSize;
                        event.downloaded = hasDownload;
                        event.downloadSpeed = downloadSpeed;
                        EventBus.getDefault().post(event);

                        //记录一次下载进度
                        reportEntity.status = "false";
                        reportEntity.hasDownloaded = hasDownload;
                        mCallback.updateTaskReport(reportEntity);

                        downloadSpeed = 0;
                        speedMonitorBegin = speedMonitorEnd;
                    }

                    if (mCallback.readPauseFlag()) {
                        mCallback.updateStatus(TaskStatus.PAUSED);
                        //暂停下载,并对任务的状态进行保存，方便以后对任务进行还原唤醒
                        reportEntity.status = "false";
                        reportEntity.hasDownloaded = hasDownload;
                        mCallback.updateTaskReport(reportEntity);
                        synchronized (pauseLock) {
                            pauseLock.wait();
                        }
                    }
                    mCallback.updateStatus(TaskStatus.RESUMED);
//                    //清除任务痕迹
//                    if (taskCancelFlag) {
//                        //删除已经下载的内容
//                        Utils.deleteFile(reportEntity.filePath);
//                        // 取消下载,清掉文件，不能还原
//                        mCallback.updateTaskReport(null);
//                    }
                }
                randomAccessFile.close();
                reportEntity.status = "true";
                reportEntity.hasDownloaded = hasDownload;
                mCallback.updateTaskReport(reportEntity);
            } else {
                mCallback.updateTaskReport(reportEntity);
            }

        } catch (IOException e) {
            reportEntity.status = "false";
            mCallback.updateTaskReport(reportEntity);
            e.printStackTrace();
        } catch (Exception e) {
            reportEntity.status = "false";
            mCallback.updateTaskReport(reportEntity);
            e.printStackTrace();
        } finally {
            if (body != null) {
                body.close();
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isAlive = false;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            pauseLock.notify();
        }
    }


    /**
     * 下载执行过程中的
     */
    interface DownloadCallback {
        /**
         * 更新 任务记录
         */
        void updateTaskReport(TaskReportEntity entity);

        /**
         * 任务暂停的标记
         */
        boolean readPauseFlag();

        /**
         * 更新任务状态
         */
        void updateStatus(int taskStatus);
    }

    class DefaultCallback implements DownloadCallback {

        @Override
        public void updateTaskReport(TaskReportEntity entity) {

        }

        @Override
        public boolean readPauseFlag() {
            return false;
        }

        @Override
        public void updateStatus(int taskStatus) {

        }
    }
}
