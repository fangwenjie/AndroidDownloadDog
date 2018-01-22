package com.fangwenjie.downloadgo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 外部调用，对要启动的任务进行配置的
 * 用来启动任务
 * Created by fangwenjie on 2017/6/14.
 */

public class TaskMsg implements Parcelable {

    /**
     * 显式任务
     */
    public final static int TYPE_EXPLICIT = 0;

    /**
     * 静默任务
     */
    public final static int TYPE_SILENCT = 1;

    /**
     * 任务的类型
     *
     * @see #TYPE_EXPLICIT
     * @see #TYPE_SILENCT
     */
    int taskType;

    /**
     * 任务名称
     */
    String taskName;
    /**
     * 文件的下载地址
     */
    String taskUrl;
    /**
     * 需要下载文件的存储是的名称；
     */
    String fileName;

    /**
     * 任务Id
     */
    public String taskId;

    public TaskMsg() {

    }

    public TaskMsg(int taskType, String taskName, String taskUrl) {
        this.taskType = taskType;
        this.taskName = taskName;
        this.taskUrl = taskUrl;

        this.taskId = Utils.hashKeyForTaskUrl(taskUrl);
    }

    public TaskMsg(String taskName, String taskUrl) {
        this.taskType = TYPE_EXPLICIT;
        this.taskName = taskName;
        this.taskUrl = taskUrl;

        this.taskId = Utils.hashKeyForTaskUrl(taskUrl);
    }

    protected TaskMsg(Parcel in) {
        taskId = in.readString();
        taskName = in.readString();
        taskUrl = in.readString();
        fileName = in.readString();
        taskType = in.readInt();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "TaskMsg{" +
                "taskType=" + taskType +
                ", taskName='" + taskName + '\'' +
                ", taskUrl='" + taskUrl + '\'' +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeString(taskUrl);
        dest.writeString(fileName);
        dest.writeInt(taskType);
    }


    public static final Creator<TaskMsg> CREATOR = new Creator<TaskMsg>() {
        @Override
        public TaskMsg createFromParcel(Parcel in) {
            return new TaskMsg(in);
        }

        @Override
        public TaskMsg[] newArray(int size) {
            return new TaskMsg[size];
        }
    };
}
