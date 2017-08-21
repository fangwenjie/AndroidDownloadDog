package com.fangwenjie.downloadgo;

import java.io.File;
import java.util.List;

/**
 * task report interface
 * Created by fangwenjie on 2017/6/9.
 */

interface TaskReportProvider {

    void addTaskReport(String key, TaskReportEntity value);

    TaskReportEntity getTaskReport(String key);

    void deleteTaskReport(String key);

    void clearTaskReport();

    File getDownloadDir();

    void fetchTaskReportList(OnFetchTaskReportListener listener);

    public interface OnFetchTaskReportListener {
        void onFetchTaskReport(List<TaskReportEntity> taskReportEntityList);
    }
}
