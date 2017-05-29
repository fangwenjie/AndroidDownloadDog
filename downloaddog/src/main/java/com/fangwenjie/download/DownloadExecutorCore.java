package com.fangwenjie.download;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static com.fangwenjie.download.Task.RESULT_CANCEL;
import static com.fangwenjie.download.Task.RESULT_SUCC;

/**
 * Created by fangwenjie on 2017/4/20.
 */

class DownloadExecutorCore extends Observable {

    private LinkedBlockingDeque<Task> taskQueue = new LinkedBlockingDeque<>();
    private List<Task> activeQueue;

    private synchronized void addActiveTask(Task task) {
        activeQueue.add(task);
        setChanged();
        notifyObservers("执行活动任务****" + task.getName());
    }

    private synchronized void removeActiveTask(Task task, int taskResultCode) {
        activeQueue.add(task);
        setChanged();
        notifyObservers("end#" + task.getName() + "#resultCode#" + taskResultCode + "#status#" + task.getStatus());
    }

    DownloadExecutorCore() {
        int MAX_THREAD_POOL = 3;
        ExecutorService taskExecutor = Executors.newFixedThreadPool(MAX_THREAD_POOL);
        taskExecutor.execute(new TaskJobRunnable());
        activeQueue = new LinkedList<>();
    }

    //添加下载任务
    void addTask(Task task) {
        taskQueue.add(task);
        setChanged();
        notifyObservers("添加任务");
    }


    //移除下载任务
    void removeTask(Task task) {
        if (task != null) {
            task.onCancel();
        }

        setChanged();
        notifyObservers("取消任务");
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
                            removeActiveTask(task, resultCode);
                        } else if (resultCode == RESULT_CANCEL) {
                            removeActiveTask(task, resultCode);
                        } else {
                            //下载失败了，保存状态
                            removeActiveTask(task, resultCode);
                            //支持重试，才会重新添加到队列中
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
