package com.fangwenjie.ddog;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Response;

/**
 * Created by fangwenjie on 2017/6/9.
 */

class Utils {
    public static final String TAG = "DownloadGo";

    static final Boolean GoDebug = false;
    static final Pattern CONTENT_DISPOSITION_PATTERN_FOR_FILENAME = Pattern.compile("filename\\s*=\\s*\"([^\"]*)\"");
    //用来辅助获取下载的文件类型
    static Map<String, String> contentTypeMap = new HashMap<>();

    static {
        contentTypeMap.put("text/html", ".html");
        contentTypeMap.put("java/*", ".java");
        contentTypeMap.put("image/jpeg", ".jpeg");
        contentTypeMap.put("application/vnd.android.package-archive", ".apk");
        contentTypeMap.put("android.package", ".apk");
        contentTypeMap.put("image/png", ".png");
        contentTypeMap.put("application/x-png", ".png");
        contentTypeMap.put("application/x-zip-compressed", ".zip");
    }

    /**
     * 使用文件下载地址的MD5换取一下任务的Id
     *
     * @param key
     * @return
     */
    public static String hashKeyForTaskUrl(String key) {
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

    static String findFilename(String fileName, Response response, String url) {
        if (TextUtils.isEmpty(fileName)) {
            fileName = parseDownloadUrl(url);
        }

        if (!isFilenameSuffix(fileName)) {
            //通过文件头确定文件类型
            Headers headers = response.headers();
            fileName = parseContentDisposition(headers.get("Content-Disposition"));
            if (TextUtils.isEmpty(fileName)) {
                fileName = generateFileName(url);
                String fileType = parseContentType(headers.get("Content-Type"));
                if (!TextUtils.isEmpty(fileType)) {
                    fileName = fileName + fileType;
                } else {
                    //未知的文件类型
                    fileName = fileName + ".unknown";
                }
            }
        }
        return fileName;
    }

    static String parseDownloadUrl(String url) {
        if (url == null) {
            return null;
        } else {
            if (Utils.GoDebug) {
                Log.d(TAG, "url:" + url);
            }
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return null;
        }


        int end = TextUtils.indexOf(url, "?");
        if (end < 0) {
            end = url.length();
        }
        url = TextUtils.substring(url, 0, end);

        String path = URI.create(url).getPath();
        String filename = "";
        if (!TextUtils.isEmpty(path)) {
            String[] paths = TextUtils.split(path, "/");
            if (paths.length > 0) {
                filename = paths[paths.length - 1];
                filename = filename.toLowerCase();
            }
        }
        if (GoDebug) {
            Log.d(TAG, "getFileNameFromUrl fileName#" + filename);
        }
        return filename;
    }

    /**
     * 检测文件名是否包含后缀名
     *
     * @return
     */
    public static boolean isFilenameSuffix(String fileName) {
        int lastDot = fileName.lastIndexOf(".");
        return lastDot > 0
                && lastDot < fileName.length();
    }

    /**
     * The same to com.android.providers.downloads.Helpers#parseContentDisposition.
     * </p>
     * Parse the Content-Disposition HTTP Header. The format of the header
     * is defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html
     * This header provides a filename for content that is going to be
     * downloaded to the file system. We only support the attachment type.
     */
    static String parseContentDisposition(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        } else {
            if (Utils.GoDebug) {
                Log.d(TAG, "contentDisposition:" + contentDisposition);
            }
        }
        try {
            Matcher matcher = CONTENT_DISPOSITION_PATTERN_FOR_FILENAME.matcher(contentDisposition);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (IllegalStateException ex) {
            // This function is defined as returning null when it can't parse the header
        }
        return null;
    }

    static String generateFileName(final String url) {
        return md5(url);
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    /**
     * 解析contentType ,尝试获取文件的类型
     *
     * @param contentType
     * @return
     */
    static String parseContentType(String contentType) {
        if (contentType == null) {
            return null;
        } else {
            if (Utils.GoDebug) {
                Log.d(TAG, "parseContentType:" + contentType);
            }
        }
        try {
            for (String s : contentTypeMap.keySet()) {
                if (contentType.contains(s)) {
                    return contentTypeMap.get(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
