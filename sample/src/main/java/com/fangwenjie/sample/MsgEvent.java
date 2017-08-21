package com.fangwenjie.sample;

import com.fangwenjie.downloadgo.GoMsg;

/**
 * Created by fangwenjie on 2017/7/4.
 */

public class MsgEvent {
    public String msg;
    public String taskId;
    public String taskName;
    public long downloaded;
    public long taskSize;
    public long downloadSpeed;
    public int progress;

    public MsgEvent(GoMsg msg) {
        this.msg = msg.msg;
        this.taskId = msg.taskId;
        this.taskName = msg.taskName;
        this.downloaded = msg.downloaded;
        this.taskSize = msg.taskSize;
        this.downloadSpeed = msg.downloadSpeed;
        this.progress = msg.progress;
    }
}
