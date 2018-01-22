package com.fangwenjie.downloadgo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

import static com.fangwenjie.downloadgo.Utils.GoDebug;
import static com.fangwenjie.downloadgo.Utils.TAG;

/**
 * 使用数据库记录任务状态
 * Created by fangwenjie on 2017/6/9.
 */

class TaskProvider extends SQLiteOpenHelper implements TaskReportProvider {

    static final String DOWNLOAD_TABLE = "task_download";
    static final String DB_NAME = "download_task.db";
    static final int DB_VERSION = 1;

    public TaskProvider(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        FileStrategy.createDownloadGoDir(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        StringBuilder sqlBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
        sqlBuilder.append(DOWNLOAD_TABLE);
        sqlBuilder.append(" (");
        sqlBuilder.append(TaskDB.TASK_KEY);
        sqlBuilder.append(" TEXT PRIMARY KEY, ");
        sqlBuilder.append(TaskDB.TASK_ENTITY);
        sqlBuilder.append(" TEXT");
        sqlBuilder.append(" )");
        db.execSQL(sqlBuilder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //数据库更新是增加处理
    }

    @Override
    public void addTaskReport(String key, TaskReportEntity value) {
        SQLiteDatabase database = getWritableDatabase();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TaskDB.TASK_KEY, key);
            contentValues.put(TaskDB.TASK_ENTITY, value.toJson());
            long affectLine = database.insertWithOnConflict(DOWNLOAD_TABLE, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
            if (GoDebug) {
                Log.d(TAG, "affectLine #" + affectLine);
            }
            if (affectLine == -1) {
                contentValues.remove(TaskDB.TASK_KEY);
                int affectUpdateLine = database.update(
                        DOWNLOAD_TABLE,
                        contentValues,
                        TaskDB.TASK_KEY + " = \"" + key + "\"",
                        null);
                if (GoDebug) {
                    Log.d(TAG, "affectUpdateLine #" + affectUpdateLine);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    @Override
    public TaskReportEntity getTaskReport(String key) {
        TaskReportEntity taskReport = null;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DOWNLOAD_TABLE,
                    null,
                    TaskDB.TASK_KEY + " = \"" + key + "\"",
                    null,
                    null,
                    null,
                    null);

            if (cursor != null && cursor.moveToFirst()) {
                taskReport = TaskReportEntity.generateTaskReportEntity(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
        return taskReport;
    }

    @Override
    public void deleteTaskReport(String key) {
        SQLiteDatabase database = null;
        try {
            database = getWritableDatabase();
            database.delete(
                    DOWNLOAD_TABLE,
                    TaskDB.TASK_KEY + " = \"" + key + "\"",
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    @Override
    public void clearTaskReport() {
        SQLiteDatabase database = null;
        try {
            database = getWritableDatabase();
            database.delete(
                    DOWNLOAD_TABLE,
                    null,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null) {
                database.close();
            }
        }
    }

    @Override
    public File getDownloadDir() {
        return FileStrategy.getDownloadGo();
    }

    @Override
    public void fetchTaskReportList(OnFetchTaskReportListener listener) {
        //async read sqlite and return zhe result list
    }
}
