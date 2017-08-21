package com.fangwenjie.downloadgo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 用来启动任务
 * Created by fangwenjie on 2017/6/14.
 */

public class TaskMsg implements Parcelable{

    public String taskName;
    public String taskUrl;

    public TaskMsg(String taskName, String taskUrl) {
        this.taskName = taskName;
        this.taskUrl = taskUrl;
    }

    protected TaskMsg(Parcel in) {
        taskName = in.readString();
        taskUrl = in.readString();
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

    @Override
    public String toString() {
        return "TaskMsg{" +
                "msg='" + taskName + '\'' +
                ", taskUrl='" + taskUrl + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskName);
        dest.writeString(taskUrl);
    }
}
