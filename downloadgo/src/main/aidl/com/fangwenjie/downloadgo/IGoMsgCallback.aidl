// IDogMsgCallback.aidl
package com.fangwenjie.downloadgo;

// Declare any non-default types here with import statements
import com.fangwenjie.downloadgo.GoMsg;
import com.fangwenjie.downloadgo.GoEvent;

interface IGoMsgCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onBarkMsg(in GoMsg msg);

    void onBarkEvent(in GoEvent event);

}
