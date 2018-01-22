package com.fangwenjie.downloadgo.task;

/**
 * 标记针对任务的操作事件
 * Created by fangwenjie on 2018/1/3.
 */

public class TaskEvent {
    /**
     * 开始进入任务
     */
    public static final int START = 0;

    /**
     * 执行任务
     */
    public static final int RESUME = 1;

    /**
     * 暂停任务
     */
    public static final int PAUSE = 2;

    /**
     * 结束任务
     */
    public static final int FINISH = 3;

    /**
     * 失败任务
     */
    public static final int FAIL = 4;

    /**
     * 成功任务
     */
    public static final int SUCC = 5;

    /**
     * 重试任务
     */
    public static final int RETRY = 6;
}
