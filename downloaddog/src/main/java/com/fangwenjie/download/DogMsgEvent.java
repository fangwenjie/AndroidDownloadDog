package com.fangwenjie.download;

/**
 * Created by fangwenjie on 2017/4/20.
 */

public class DogMsgEvent {

    public final int ADD = 0;//  添加任务
    public final int PAUSE = 1;// 暂停任务
    public final int RESUME = 2;//继续任务
    public final int DELETE = 3;//删除任务

    public String msg;
    public int event;

    public DogMsgEvent(String dogMsg) {
        this.msg = dogMsg;
    }
}
