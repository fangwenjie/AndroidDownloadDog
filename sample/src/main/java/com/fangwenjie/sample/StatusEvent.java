package com.fangwenjie.sample;

import com.fangwenjie.downloadgo.GoEvent;

/**
 * Created by fangwenjie on 2017/7/4.
 */

public class StatusEvent {

    public String msg;
    public int event;
    public String filePath;
    public String taskId;

    public StatusEvent(GoEvent event) {
        this.msg = event.msg;
        this.event = event.event;
        this.filePath = event.filePath;
        this.taskId = event.taskId;
    }

    @Override
    public String toString() {
        return "StatusEvent{" +
                "msg='" + msg + '\'' +
                ", event=" + event +
                ", filePath='" + filePath + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
