package com.fangwenjie.downloadgo;

import com.fangwenjie.downloadgo.task.TaskEvent;

/**
 * 下载任务操作事件
 * Created by fangwenjie on 2017/4/20.
 */

class GoMsgEvent {

    public int event;
    public String filePath;//下载成功的时候才会有
    public String taskId;
    public String succMsg;

    public GoMsgEvent(String taskId,  int event) {
        this.taskId = taskId;
        this.event = event;
    }

    public GoMsgEvent(String taskId, int event, String filePath) {
        this.taskId = taskId;
        this.event = event;
        this.filePath = filePath;
    }

    public GoMsgEvent(String taskId,String succMsg){
        this.taskId = taskId;
        this.event = TaskEvent.FINISH;
        this.succMsg = succMsg;
    }


    @Override
    public String toString() {
        return "GoMsgEvent{" +
                ", event=" + event +
                ", filePath='" + filePath + '\'' +
                ", taskId='" + taskId + '\'' +
                '}';
    }
}
