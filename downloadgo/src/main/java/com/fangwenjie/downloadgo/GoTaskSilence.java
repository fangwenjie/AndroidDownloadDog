package com.fangwenjie.downloadgo;

import android.util.Log;

import com.fangwenjie.downloadgo.task.GoTask;
import com.fangwenjie.downloadgo.task.TaskEvent;
import com.fangwenjie.downloadgo.task.TaskParams;
import com.fangwenjie.downloadgo.task.TaskStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 静默的下载任务
 * 此类任务，不会在消息抽屉中展示下载进度
 * <p>
 * <p>
 * Created by fangwenjie on 2018/1/4.
 */

public class GoTaskSilence extends GoTask.Stub {

    private AtomicBoolean mPauseFlag = new AtomicBoolean(false);//任务暂停的标记

    private TaskReportProvider reportProvider;

    private TaskParams taskParams;
    private TaskReportEntity reportEntity;

    private DownloadImpl downloadImpl;

    /**
     * 最大的重试次数
     */
    final int MAX_RETRY = 3;

    /**
     * 累计重试次数
     */
    int retryCount = 0;

    private boolean readRetryStrategy() {
        return MAX_RETRY - retryCount > 0;
    }

    GoTaskSilence(TaskParams taskParams, TaskReportProvider reportProvider) {
        this.taskParams = taskParams;
        this.reportProvider = reportProvider;
    }

    /**
     * 装载下载任务的配置信息
     */
    private void loadTaskConfig() {
        if (reportEntity == null) {
            reportEntity = reportProvider.getTaskReport(taskParams.taskId);
            if (reportEntity == null) {
                reportEntity = new TaskReportEntity();
            }
        }
    }

    @Override
    public void onStart() {
        status = TaskStatus.STARTED;
        Log.d("GoTaskSilence", "status>>started");
        EventBus.getDefault().post(new GoMsgEvent(taskParams.taskId, TaskEvent.START));
        //装载任务的基本配置TaskParams,TaskReportEntity
        loadTaskConfig();
        // config is ok
        if (downloadImpl == null) {
            downloadImpl = new DownloadImpl(taskParams, reportEntity);
            downloadImpl.setCallback(new DownloadImpl.DownloadCallback() {
                @Override
                public void updateTaskReport(TaskReportEntity entity) {
                    if (entity == null) {
                        //删除记录
                        reportProvider.deleteTaskReport(taskParams.taskId);
                    } else {
                        reportProvider.addTaskReport(taskParams.taskId, entity);
                    }
                }

                @Override
                public boolean readPauseFlag() {
                    return mPauseFlag.get();
                }

                @Override
                public void updateStatus(int taskStatus) {
                    if (status != taskStatus) {
                        if (taskStatus == TaskStatus.RESUMED) {
                            EventBus.getDefault().post(new GoMsgEvent(taskParams.taskId, TaskEvent.RESUME));
                        } else if (taskStatus == TaskStatus.PAUSED) {
                            EventBus.getDefault().post(new GoMsgEvent(taskParams.taskId, TaskEvent.PAUSE));
                        }
                    }
                    status = taskStatus;
                }
            });
        }
        onResume();
    }

    @Override
    public void onResume() {
        Log.d("GoTaskSilence", "status>>Resumed");
        mPauseFlag.set(false);
        //开始实际的下载动作
        downloadImpl.resume();
        //任务是非活动状态
        downloadImpl.doDownload();
        //任务下载结束
        onFinish();
    }

    public void resumeTask() {
        mPauseFlag.set(false);
        if (downloadImpl != null && downloadImpl.isAlive) {
            //开始实际的下载动作
            downloadImpl.resume();
        }
    }

    @Override
    public void onPause() {
        //暂停当前正在现在的任务
        mPauseFlag.set(true);
    }

    @Override
    public void onFinish() {
        status = TaskStatus.FINISHED;
        Log.d("GoTaskSilence", "status>>Finished");
        //readTaskConfig 确定任务的结束状态
        if ("true".equals(reportEntity.status)) {
            //成功结束
            onSucc();
        } else {
            //失败结束
            onFail();
        }
    }

    @Override
    public void onFail() {
        status = TaskStatus.FAILED;
        Log.d("GoTaskSilence", "status>>Failed");
        if (readRetryStrategy()) {
            //  满足重试策略 onRetry()
            onRetry();
        } else {
            //  超出重试策略 通知任务失败
            EventBus.getDefault().post(new GoMsgEvent(taskParams.taskId, TaskEvent.FAIL));
            Log.d("GoTaskSilence", "status>>final Failed");
        }
    }

    @Override
    public void onSucc() {
        status = TaskStatus.SUCCED;
        Log.d("GoTaskSilence", "status>>succed");
        //通知任务成功
        EventBus.getDefault().post(new GoMsgEvent(taskParams.taskId, TaskEvent.SUCC, reportEntity.filePath));
    }

    @Override
    public void onRetry() {
        Log.d("GoTaskSilence", "retry task");
        //更新任务的重试的状态
        retryCount++;
        //重新来过
        onStart();
    }

    @Override
    public String getTaskId() {
        return taskParams.taskId;
    }

    public int getTaskType() {
        return taskParams.taskType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoTaskSilence)) return false;

        GoTaskSilence that = (GoTaskSilence) o;

        return taskParams.equals(that.taskParams);
    }

    @Override
    public int hashCode() {
        return taskParams.hashCode();
    }
}
