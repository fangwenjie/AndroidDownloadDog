package com.fangwenjie.ddog;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import static com.fangwenjie.ddog.Utils.GoDebug;
import static com.fangwenjie.ddog.Utils.TAG;

/**
 * Created by fangwenjie on 2017/6/9.
 */

class FileStrategy {
    static File downloadGoFile;
    final static String goDir = "downloadGo";

    static File createDownloadGoDir(Context context) {
        File file;
        if (externalStorageAvailable()) {
            file = context.getExternalFilesDir(goDir);
            if (file != null) {
                if (GoDebug) {
                    Log.d(TAG, "成功获取sd的内容，使用SD卡空间");
                }
                if (!createGoDir(file)) {
                    if (GoDebug) {
                        Log.d(TAG, "无法成功获取sd的内容，被迫使用机身空间");
                    }
                    file = context.getFileStreamPath(goDir);
                    createGoDir(file);
                }
            } else {
                if (GoDebug) {
                    Log.d(TAG, "无法成功获取sd的内容，使用机身空间");
                }
                file = context.getFileStreamPath(goDir);
                createGoDir(file);
            }
        } else {
            if (GoDebug) {
                Log.d(TAG, "sd 卡不可用时，那就不要进行下载了");
            }
            file = context.getFileStreamPath(goDir);
            createGoDir(file);
        }

        downloadGoFile = file;
        return file;
    }

    static File getDownloadGo() {
        return downloadGoFile;
    }

    static boolean externalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    static boolean createGoDir(File file) {
        if (file.exists()) {
            return true;
        } else {
            boolean result = file.mkdirs();
            if (result) {
                if (GoDebug) {
                    Log.d(TAG, "create go dir succ#" + file.getAbsolutePath());
                }
                return true;
            } else {
                if (GoDebug) {
                    Log.d(TAG, "create go dir fail#" + file.getAbsolutePath());
                }
                return false;
            }
        }
    }

}
