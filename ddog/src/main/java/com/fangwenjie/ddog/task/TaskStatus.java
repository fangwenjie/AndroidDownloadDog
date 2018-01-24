package com.fangwenjie.ddog.task;

/**
 * 标记 任务的执行状态
 * Created by fangwenjie on 2018/1/3.
 */

public class TaskStatus {

    /**
     * 任务未进入执行状态
     */
    public static final int NONE = 0;

    /**
     * 任务开始
     */
    public static final int STARTED = 1;

    /**
     * 任务暂停
     */
    public static final int PAUSED = 2;

    /**
     * 任务执行中
     */
    public static final int RESUMED = 3;

    /**
     * 任务 结束
     */
    public static final int FINISHED = 4;

    /**
     * 任务 失败
     */
    public static final int FAILED = 5;

    /**
     * 任务 成功
     */
    public static final int SUCCED = 6;
}
