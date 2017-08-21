package com.fangwenjie.downloadgo;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static com.fangwenjie.downloadgo.Task.RESULT_CANCEL;
import static com.fangwenjie.downloadgo.Task.RESULT_SUCC;

/**
 * Created by fangwenjie on 2017/4/20.
 */

class DownloadExecutorCore {
    private LinkedBlockingDeque<Task> taskQueue = new LinkedBlockingDeque<>();
    private volatile List<Task> activeQueue = new LinkedList<>();

    DownloadExecutorCore() {
        int MAX_THREAD_POOL = 3;
        ExecutorService taskExecutor = Executors.newFixedThreadPool(MAX_THREAD_POOL);
        taskExecutor.execute(new TaskJobRunnable());
    }

    private synchronized void addActiveTask(Task task) {
        activeQueue.add(task);
    }

    private synchronized void removeActiveTask(Task task) {
        activeQueue.remove(task);
    }

    //添加下载任务
    void addTask(Task task) {
        taskQueue.add(task);
    }

    Task findTaskByTaskId(String taskId) {
        for (Task task : activeQueue) {
            if (task.taskId.equals(taskId)) {
                return task;
            }
        }
        return null;
    }
    //任务执行
    private class TaskJobRunnable implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Task task = taskQueue.take();
                    //预读一下载的数据
                    boolean needContinue = task.onPreExecuteJob();
                    if (needContinue) {
                        //添加到活动队列中
                        addActiveTask(task);
                        //执行任务
                        int resultCode = task.onExecuteJob();
                        if (resultCode == RESULT_SUCC) {
                            removeActiveTask(task);
                        } else if (resultCode == RESULT_CANCEL) {
                            removeActiveTask(task);
                        } else {
                            //下载失败了，保存状态
                            removeActiveTask(task);
                        }
                    } else {
                        //不需要进行下载操作
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
