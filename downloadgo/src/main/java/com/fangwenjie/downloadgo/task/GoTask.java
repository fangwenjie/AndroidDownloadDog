package com.fangwenjie.downloadgo.task;

/**
 * 下载任务的
 * Created by fangwenjie on 2018/1/3.
 */

public interface GoTask {
    /**
     * 开始任务
     */
    void onStart();

    /**
     * 执行任务
     */
    void onResume();

    /**
     * 暂停任务
     */
    void onPause();

    /**
     * 结束任务
     */
    void onFinish();

    /**
     * 失败任务
     */
    void onFail();

    /**
     * 成功任务
     */
    void onSucc();

    /**
     * 重试任务
     */
    void onRetry();


    abstract class Stub implements GoTask {

        protected int status = TaskStatus.NONE;

        public int getStatus() {
            return status;
        }

        public abstract String getTaskId();
    }
}
