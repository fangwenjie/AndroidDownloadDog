package com.fangwenjie.downloadgo;

/**
 * Created by fangwenjie on 2017/4/27.
 */

class DownloadMsgEvent {
    public String msg;
    public String taskId;
    public String taskName;
    public long downloaded;
    public long taskSize;
    public long downloadSpeed;
    public int downloadProgress;

    public DownloadMsgEvent(String msg, int downloadProgress) {
        this.msg = msg;
        this.downloadProgress = downloadProgress;
    }

    @Override
    public String toString() {
        return "DownloadMsgEvent{" +
                "msg='" + msg + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", downloaded=" + downloaded +
                ", taskSize=" + taskSize +
                ", downloadSpeed=" + downloadSpeed +
                ", downloadProgress=" + downloadProgress +
                '}';
    }
}
