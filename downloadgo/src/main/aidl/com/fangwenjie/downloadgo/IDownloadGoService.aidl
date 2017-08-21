// IDownloadGoService.aidl
package com.fangwenjie.downloadgo;

// Declare any non-default types here with import statements
import com.fangwenjie.downloadgo.TaskMsg;
import com.fangwenjie.downloadgo.IGoMsgCallback;

interface IDownloadGoService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String addTask(in TaskMsg msg);

    void onSendTaskEvent(String taskId,int TaskEvent);

    void addCallback(in IGoMsgCallback callback);
}
