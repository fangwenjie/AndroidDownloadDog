package com.fangwenjie.downloadgo;

/**
 * Created by fangwenjie on 2017/4/20.
 */

class GoMsgEvent {

    public String msg;
    public int event;
    public String filePath;//下载成功的时候才会有
    public String taskId;

    public GoMsgEvent(String taskId, String goMsg, int event) {
        this.taskId = taskId;
        this.msg = goMsg;
        this.event = event;
    }

    public GoMsgEvent(String taskId, String msg, int event, String filePath) {
        this.taskId = taskId;
        this.msg = msg;
        this.event = event;
        this.filePath = filePath;
    }


    @Override
    public String toString() {
        return "GoMsgEvent{" +
                "msg='" + msg + '\'' +
                ", event=" + event +
                ", filePath='" + filePath + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
