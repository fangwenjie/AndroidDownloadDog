package com.fangwenjie.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fangwenjie.ddog.TaskMsg;
import com.fangwenjie.ddog.task.TaskEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.download_content)
    TextView downloadContent;

    @BindView(R.id.task_status)
    TextView status;

    @BindView(R.id.task_download_progress)
    ProgressBar downloadProgress;

    String downloadUrl = DummyData.FILE_URL_1;
    private String currentTaskId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String scanResult = data.getStringExtra("SCAN_RESULT");
        downloadContent.setText(scanResult);
        downloadUrl = scanResult;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @OnClick(R.id.scan_code)
    public void onClickScan() {
        if (!TextUtils.isEmpty(downloadUrl)) {
            downloadContent.setText("Explicit 下载>>:" + downloadUrl);
            TaskMsg taskMsg = new TaskMsg(TaskMsg.TYPE_EXPLICIT, "silenceTaskDownload", downloadUrl);
            currentTaskId = taskMsg.taskId;
            DownloadGo.getInstance().addTask(taskMsg);
        }
    }

    @OnClick(R.id.button)
    public void onClickBtn() {
        if (!TextUtils.isEmpty(downloadUrl)) {
            downloadContent.setText("Silence 下载>>:" + downloadUrl);
            TaskMsg taskMsg = new TaskMsg(TaskMsg.TYPE_SILENCT, "silenceTaskDownload", downloadUrl);
            currentTaskId = taskMsg.taskId;
            DownloadGo.getInstance().addTask(taskMsg);
        }
    }

    @OnClick(R.id.button_pause)
    public void onClickPause() {
        if (!TextUtils.isEmpty(currentTaskId)) {
            DownloadGo.getInstance().onSendTaskEvent(currentTaskId, TaskEvent.PAUSE);
        }
    }

    @OnClick(R.id.button_resume)
    public void onClickResume() {
        if (!TextUtils.isEmpty(currentTaskId)) {
            DownloadGo.getInstance().onSendTaskEvent(currentTaskId, TaskEvent.RESUME);
        }
    }

    private String getTaskStatus(String taskStatus) {
        return String.format("TaskStatus:%s", taskStatus);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent event) {
        status.setText(getTaskStatus(event.status));

        if (event.status.equals("Succed")){
            downloadProgress.setProgress(100);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPuppyEvent(PuppyEvent event){
        downloadProgress.setProgress(event.progress);
    }
}
