package com.fangwenjie.ddog;

import android.database.Cursor;
import android.support.annotation.Keep;
import android.util.Log;

import com.google.gson.GsonBuilder;

import static com.fangwenjie.ddog.Utils.GoDebug;
import static com.fangwenjie.ddog.Utils.TAG;

/**
 * Created by fangwenjie on 2017/6/9.
 */
@Keep
class TaskReportEntity {
    public String status;//下载完成状态
    public String taskName;//下载任务名称
    public long hasDownloaded;//已经下载
    public long taskSize;//下载的总大小
    public String fileType = "apk";//下载文件类型
    public String filePath;//下载文件的存储路径

    public String getStatus() {
        return status;
    }

    public TaskReportEntity() {
    }

    public static TaskReportEntity generateTaskReportEntity(Cursor cursor) {
        String taskKey = cursor.getString(cursor.getColumnIndex("task_key"));
        if (GoDebug) {
            Log.d(TAG, "taskKey #" + taskKey);
        }
        String taskEntity = cursor.getString(cursor.getColumnIndex("task_entity"));
        if (GoDebug) {
            Log.d(TAG, "taskEntity #" + taskEntity);
        }
        return new GsonBuilder().create().fromJson(taskEntity, TaskReportEntity.class);
    }

    public String toJson() {
        return new GsonBuilder().create().toJson(this);
    }

    /**
     * 重置一下任务下载的进度
     */
    void resetEntity() {
        this.hasDownloaded = 0;
    }

    @Override
    public String toString() {
        return "TaskReportEntity{" +
                "status='" + status + '\'' +
                ", msg='" + taskName + '\'' +
                ", hasDownloaded=" + hasDownloaded +
                ", taskSize=" + taskSize +
                ", fileType='" + fileType + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
