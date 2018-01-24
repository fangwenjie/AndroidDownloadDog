// IDogMsgCallback.aidl
package com.fangwenjie.ddog;

// Declare any non-default types here with import statements
import com.fangwenjie.ddog.GoMsg;
import com.fangwenjie.ddog.GoEvent;

interface IGoMsgCallback {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onBarkMsg(in GoMsg msg);

    void onBarkEvent(in GoEvent event);

}
