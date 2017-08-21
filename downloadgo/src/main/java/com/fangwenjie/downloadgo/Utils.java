package com.fangwenjie.downloadgo;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by fangwenjie on 2017/6/9.
 */

class Utils {
    public static final String TAG = "DownloadGo";

    static final Boolean GoDebug = true;

    /**
     * 使用文件下载地址的MD5换取一下任务的Id
     *
     * @param key
     * @return
     */
    static String hashKeyForTaskUrl(String key) {
        String cacheKey;
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(key.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    static String bytesToHexString(byte[] bytes) {
        StringBuilder strBuild = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                strBuild.append('0');
            }
            strBuild.append(hex);
        }
        return strBuild.toString();
    }

    static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }

}
