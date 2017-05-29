package com.fangwenjie.download;

import java.util.Observer;

/**
 * Created by fangwenjie on 2017/4/20.
 */

public interface Puppy extends Observer {
    void onWangWang(String dogMsg, int downloadProgress);
}
