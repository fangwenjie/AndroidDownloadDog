package com.fangwenjie.downloadgo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fangwenjie on 2017/6/12.
 */

public class GoMsg implements Parcelable {
    public String msg;
    public String taskId;
    public String taskName;
    public long downloaded;
    public long taskSize;
    public long downloadSpeed;
    public int progress;

    public GoMsg() {
    }

    public GoMsg(String msg, String taskId, String taskName, long downloaded, long taskSize, long downloadSpeed, int progress) {
        this.msg = msg;
        this.taskId = taskId;
        this.taskName = taskName;
        this.downloaded = downloaded;
        this.taskSize = taskSize;
        this.downloadSpeed = downloadSpeed;
        this.progress = progress;
    }

    public GoMsg(Parcel in) {
        msg = in.readString();
        taskId = in.readString();
        taskName = in.readString();
        downloaded = in.readLong();
        taskSize = in.readLong();
        downloadSpeed = in.readLong();
        progress = in.readInt();
    }

    public static final Creator<GoMsg> CREATOR = new Creator<GoMsg>() {
        @Override
        public GoMsg createFromParcel(Parcel in) {
            return new GoMsg(in);
        }

        @Override
        public GoMsg[] newArray(int size) {
            return new GoMsg[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeLong(downloaded);
        dest.writeLong(taskSize);
        dest.writeLong(downloadSpeed);
        dest.writeInt(progress);
    }

    public void readFromParcel(Parcel in) {
        msg = in.readString();
        taskId = in.readString();
        taskName = in.readString();
        downloaded = in.readLong();
        taskSize = in.readLong();
        downloadSpeed = in.readLong();
        progress = in.readInt();
    }

    @Override
    public String toString() {
        return "GoMsg{" +
                "msg='" + msg + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", downloaded=" + downloaded +
                ", taskSize=" + taskSize +
                ", downloadSpeed=" + downloadSpeed +
                ", progress=" + progress +
                '}';
    }
}
