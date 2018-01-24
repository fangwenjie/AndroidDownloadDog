package com.fangwenjie.ddog;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by fangwenjie on 2017/6/15.
 */

public class GoEvent implements Parcelable {
    public String msg;
    public int event;
    public String filePath;
    public String taskId;

    public GoEvent() {
    }

    public GoEvent(String msg, int event, String filePath, String taskId) {
        this.msg = msg;
        this.event = event;
        this.filePath = filePath;
        this.taskId = taskId;
    }

    protected GoEvent(Parcel in) {
        msg = in.readString();
        event = in.readInt();
        filePath = in.readString();
        taskId = in.readString();
    }

    public static final Creator<GoEvent> CREATOR = new Creator<GoEvent>() {
        @Override
        public GoEvent createFromParcel(Parcel in) {
            return new GoEvent(in);
        }

        @Override
        public GoEvent[] newArray(int size) {
            return new GoEvent[size];
        }
    };

    public void readFromParcel(Parcel in) {
        this.msg = in.readString();
        this.event = in.readInt();
        this.filePath = in.readString();
        this.taskId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
        dest.writeInt(event);
        dest.writeString(filePath);
        dest.writeString(taskId);
    }

    @Override
    public String toString() {
        return "GoEvent{" +
                "msg='" + msg + '\'' +
                ", event=" + event +
                ", filePath='" + filePath + '\'' +
                '}';
    }

}
