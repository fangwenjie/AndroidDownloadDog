package com.fangwenjie.downloadgo.task;

/**
 * 下载任务必要参数，下载操作完全依赖这里提供的内容进行后续的下载操作
 * <p>
 * Created by fangwenjie on 2018/1/4.
 */

public class TaskParams {
    public final String taskId;
    public final String taskName;
    public final int taskType;
    public final String taskUrl;

    /**
     * 外部指定文件名称
     */
    public final String fileName;

    public TaskParams(int taskType, String taskId, String taskName, String taskUrl, String fileName) {
        this.taskType = taskType;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskUrl = taskUrl;
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskParams)) return false;

        TaskParams params = (TaskParams) o;

        if (taskType != params.taskType) return false;
        if (!taskId.equals(params.taskId)) return false;
        if (!taskName.equals(params.taskName)) return false;
        if (!taskUrl.equals(params.taskUrl)) return false;
        return fileName != null ? fileName.equals(params.fileName) : params.fileName == null;
    }

    @Override
    public int hashCode() {
        int result = taskId.hashCode();
        result = 31 * result + taskName.hashCode();
        result = 31 * result + taskType;
        result = 31 * result + taskUrl.hashCode();
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }
}
