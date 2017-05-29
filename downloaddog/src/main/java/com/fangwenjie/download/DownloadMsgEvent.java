package com.fangwenjie.download;

/**
 * Created by fangwenjie on 2017/4/27.
 */

public class DownloadMsgEvent {
    public String msg;
    public int downloadProgress;

    public DownloadMsgEvent(String msg, int downloadProgress) {
        this.msg = msg;
        this.downloadProgress = downloadProgress;
    }

    @Override
    public String toString() {
        return "DownloadMsgEvent{" +
                "msg='" + msg + '\'' +
                ", downloadProgress=" + downloadProgress +
                '}';
    }
}
