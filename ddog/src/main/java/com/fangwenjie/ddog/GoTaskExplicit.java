package com.fangwenjie.ddog;

import com.fangwenjie.ddog.task.TaskParams;

/**
 * 显示任务
 * Created by fangwenjie on 2018/1/5.
 */

public class GoTaskExplicit extends GoTaskSilence {

    GoTaskExplicit(TaskParams taskParams, TaskReportProvider reportProvider) {
        super(taskParams, reportProvider);
    }
}
