package com.fangwenjie.downloadgo.es_detector;

import android.content.Context;
import android.os.Environment;

/**
 * Created by fangwenjie on 2018/1/8.
 */

public class Dectector {

    public void dectorExtrenalStorage(Context context){

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
