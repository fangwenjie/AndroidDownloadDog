package com.fangwenjie.download;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;

/**
 * Note that this includes disk access so this should not be
 * executed on the main/UI thread.
 * <p>
 * Created by fangwenjie on 2017/5/24.
 */

public class TaskCache {
    private static final String TAG = "TaskCache";

    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10;
    private static final int DISK_CACHE_INDEX = 0;
    private static final boolean DEFAULT_INIT_DISK_CACHE_ON_CREATE = true;


    private TaskCacheParam mCacheParam;
    private DiskLruCache mDiskLruCache;

    private final Object mCacheLock = new Object();
    private boolean mDiskCacheStaring = true;

    private volatile static TaskCache instance;

    private TaskCache() {
    }

    public static TaskCache getInstance() {
        if (instance == null) {
            instance = new TaskCache();
        }
        return instance;
    }

    public void initCache(TaskCacheParam param) {
        mCacheParam = param;

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                synchronized (mCacheLock) {
                    if (mDiskLruCache == null || mDiskLruCache.isClosed()) {
                        File diskCacheDir = mCacheParam.cacheDir;
                        if (diskCacheDir != null && diskCacheDir.exists()) {
                            try {
                                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, mCacheParam.diskCacheSize);
                                Log.d("clarkfang", "创建缓存");
                            } catch (IOException e) {
                                mCacheParam.cacheDir = null;
                                e.printStackTrace();
                            }
                        }
                    }
                    mDiskCacheStaring = false;
                    mCacheLock.notifyAll();
                }
            }
        });
    }

    public void addTaskToDisk(String data, TaskReportEntity value) {
        if (data == null || value == null) {
            return;
        }

        synchronized (mCacheLock) {
            if (mDiskLruCache != null) {
                final String key = hashKeyForDisk(data);
                OutputStream out = null;
                try {
                    final DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                    if (editor != null) {
                        out = editor.newOutputStream(DISK_CACHE_INDEX);

                        Gson gson = new GsonBuilder().create();
                        String entityJson = gson.toJson(value);
                        out.write(entityJson.getBytes());

                        editor.commit();
                        out.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public TaskReportEntity getTaskFromDisk(String data) {
        final String key = hashKeyForDisk(data);
        TaskReportEntity task = null;

        synchronized (mCacheLock) {
            while (mDiskCacheStaring) {
                try {
                    mCacheLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (mDiskLruCache != null) {
                InputStream inputStream = null;

                try {
                    final DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
                    if (snapshot != null) {
                        inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
                        if (inputStream != null) {
                            StringBuilder content = new StringBuilder();
                            int i;
                            while ((i = inputStream.read()) != -1) {
                                content.append((char) i);
                            }
                            Gson gson = new GsonBuilder().create();
                            task = gson.fromJson(content.toString(), TaskReportEntity.class);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
        return task;
    }

    public void deleteTaskForm(String data) {
        synchronized (mCacheLock) {
            if (mDiskLruCache != null) {
                String key = hashKeyForDisk(data);
                try {
                    mDiskLruCache.remove(key);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void clearCache() {
        synchronized (mCacheLock) {
            mDiskCacheStaring = true;
            if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
                try {
                    mDiskLruCache.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mDiskLruCache = null;
            }
        }
    }

    public void flush() {
        synchronized (mCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    mDiskLruCache.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void close() {
        synchronized (mCacheLock) {
            if (mDiskLruCache != null) {
                try {
                    if (!mDiskLruCache.isClosed()) {
                        mDiskLruCache.close();
                        mDiskLruCache = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String hashKeyForDisk(String key) {
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

    private String bytesToHexString(byte[] bytes) {
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


    public File getCacheDir() {
        return mCacheParam.cacheDir;
    }

    public File getDownloadDir() {
        return mCacheParam.downloadDir;
    }

    public static class TaskCacheParam {
        public boolean initDiskCacheOnCreate = DEFAULT_INIT_DISK_CACHE_ON_CREATE;
        public long diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
        public File cacheDir;
        public File downloadDir;

        public TaskCacheParam(Context context) {
            cacheDir = getDownloadDir(context, "download/cache");
            downloadDir = getDownloadDir(context, "download");
        }

        private File getDownloadDir(Context context, String dirName) {
            if (!TextUtils.isEmpty(Environment.getExternalStorageState())) {
                String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + context.getPackageName() + "/" + dirName;
                File file = new File(dir);
                if (!file.isDirectory() || !file.exists()) {
                    boolean result = file.mkdirs();
                    if (result) {
                        Log.d("clarkfang", "file dir #" + file.getAbsolutePath());
                        return file;
                    }
                } else {
                    return file;
                }
            }
            return null;
        }

    }

    @Keep
    public static class TaskReportEntity {
        public String status;//下载完成状态
        public String taskName;//下载任务名称
        public long hasDownloaded;//已经下载
        public long taskSize;//下载的总大小
        public String fileType = "apk";//下载文件类型
        public String filePath;//下载文件的存储路径

        public Task.Status getStatus() {
            return Task.Status.valueOf(status);
        }

        public TaskReportEntity() {
        }

        public TaskReportEntity(String status, String taskName, long hasDownloaded, long taskSize, String fileType) {
            this.status = status;
            this.taskName = taskName;
            this.hasDownloaded = hasDownloaded;
            this.taskSize = taskSize;
            this.fileType = fileType;
        }

        @Override
        public String toString() {
            return "TaskReportEntity{" +
                    "status='" + status + '\'' +
                    ", taskName='" + taskName + '\'' +
                    ", hasDownloaded=" + hasDownloaded +
                    ", taskSize=" + taskSize +
                    ", fileType='" + fileType + '\'' +
                    ", filePath='" + filePath + '\'' +
                    '}';
        }
    }

}
