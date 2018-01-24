// IDownloadGoService.aidl
package com.fangwenjie.ddog;

// Declare any non-default types here with import statements
import com.fangwenjie.ddog.TaskMsg;
import com.fangwenjie.ddog.IGoMsgCallback;

interface IDownloadGoService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String addTask(in TaskMsg msg);

    /**
    *   查找任务并且返回任务的状态
    */
    int findTask(String taskId);

    void onSendTaskEvent(String taskId,int TaskEvent);

    void addCallback(in IGoMsgCallback callback);

}
