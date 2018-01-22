package com.fangwenjie.downloadgo;

import android.text.TextUtils;
import android.util.Log;

import com.fangwenjie.downloadgo.task.TaskEvent;
import com.fangwenjie.downloadgo.task.TaskStatus;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static com.fangwenjie.downloadgo.Utils.GoDebug;
import static com.fangwenjie.downloadgo.Utils.TAG;

/**
 * 下载组件的执行核心
 * Created by fangwenjie on 2017/4/20.
 */

class DownloadExecutorCore {
    LinkedBlockingDeque<GoTaskSilence> executeTaskQueue = new LinkedBlockingDeque<>();//执行任务队列
    LinkedBlockingDeque<GoTaskSilence> silenceTaskQueue = new LinkedBlockingDeque<>();//执行任务队列

    volatile GoTaskExplicit pendingTask;//当前暂停的任务

    volatile GoTaskSilence activeTask;//当前正在执行的任务

    private GoTaskSilence activeSilenceTask;

    TaskReportProvider reportProvider;

    DownloadExecutorCore(TaskReportProvider reportProvider) {
        this.reportProvider = reportProvider;
        int MAX_THREAD_POOL = 3;
        ExecutorService taskExecutor = Executors.newFixedThreadPool(MAX_THREAD_POOL);
        taskExecutor.execute(new TaskJobRunnable());
        taskExecutor.execute(new SilenceTaskRunnable());
    }

    /**
     * 检测任务的添加
     *
     * @param task
     * @return 返回任务添加的结果
     */
    private String checkGoTask(GoTaskSilence task) {
        String taskId = task.getTaskId();
        if (activeTask != null && taskId.equals(activeTask.getTaskId())) {
            return "任务已经下载队列中，请耐心等待";
        } else if (activeSilenceTask != null && taskId.equals(activeSilenceTask.getTaskId())) {
            return "任务已经下载队列中，请耐心等待";
        } else if (executeTaskQueue.contains(task)) {
            return "任务已经下载队列中，请耐心等待";
        } else if (silenceTaskQueue.contains(task)) {
            return "任务已经下载队列中，请耐心等待";
        } else if (checkTaskSnapshot(task)) {
            return "历史上已下载完成，请安装";
        } else {
            if (task.getTaskType() == TaskMsg.TYPE_EXPLICIT) {
                executeTaskQueue.add(task);
            } else if (task.getTaskType() == TaskMsg.TYPE_SILENCT) {
                silenceTaskQueue.add(task);
            }
            return "已加入下载管理器";
        }
    }

    /**
     * 检测任务的可执行性
     *
     * @param task
     * @return
     */
    boolean checkTaskSnapshot(GoTaskSilence task) {
        if (reportProvider == null) {
            throw new IllegalStateException("TaskReportProvider is not created");
        }

        TaskReportEntity loadTask = reportProvider.getTaskReport(task.getTaskId());
        if (loadTask != null) {
            //check 文件是否如记录一样的存在
            boolean isFileExists = false;
            if (!TextUtils.isEmpty(loadTask.filePath)) {
                File file = new File(loadTask.filePath);
                if (file.exists()) {
                    isFileExists = true;
                }
            }

            //文件不存在
            if (!isFileExists) {
                //删除记录
                reportProvider.deleteTaskReport(task.getTaskId());
                return false;
            }

            if ("true".equals(loadTask.getStatus())) {
                return true;
            } else {
                return false;
            }

        } else {
            if (GoDebug) {
                Log.d(TAG, "load task null 重头开始下载");
            }
            return false;
        }

    }

    /**
     * 添加显示的下载任务
     *
     * @param task
     * @return
     */
    String addExplicitTask(GoTaskExplicit task) {
        return checkGoTask(task);
    }

    /**
     * 暂停任务 只对显示任务生效
     *
     * @param taskId
     */
    void pause(String taskId) {
        if (activeSilenceTask != null && taskId.equals(activeSilenceTask.getTaskId())) {
            activeSilenceTask.onPause();
        }
    }

    /**
     * 恢复暂停的任务，只对仍然存在的显示任务生效
     *
     * @param taskId
     */
    void resume(String taskId) {
        if (activeSilenceTask != null && taskId.equals(activeSilenceTask.getTaskId())) {
            activeSilenceTask.resumeTask();
            EventBus.getDefault().post(new GoMsgEvent(taskId, TaskEvent.RESUME));
        }
    }

    /**
     * 添加静默任务的队列中
     *
     * @param taskSilence
     */
    String addSilenceTask(GoTaskSilence taskSilence) {
        return checkGoTask(taskSilence);
    }

    /**
     * 显示任务的执行下载
     */
    class TaskJobRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    activeTask = executeTaskQueue.take();
                    activeTask.onStart();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 静默任务的执行下载
     */
    class SilenceTaskRunnable implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    activeSilenceTask = silenceTaskQueue.take();
                    activeSilenceTask.onStart();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    int getTaskStatus(String taskId) {
        if (activeSilenceTask != null && activeSilenceTask.getTaskId().equals(taskId)) {
            return activeSilenceTask.getStatus();
        }
        return TaskStatus.NONE;
    }

}
