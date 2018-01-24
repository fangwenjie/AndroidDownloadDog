package com.fangwenjie.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import com.fangwenjie.ddog.TaskMsg;
import com.fangwenjie.ddog.task.TaskEvent;
import com.fangwenjie.ddog.task.TaskStatus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.download_content)
    TextView downloadContent;

    @BindView(R.id.button_status)
    Button status;

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

    @OnClick(R.id.button_status)
    public void onClickStatus(Button btnStatus) {
        if (!TextUtils.isEmpty(currentTaskId)) {
            int taskStatus = DownloadGo.getInstance().getTaskStatus(currentTaskId);
            switch (taskStatus) {
                case TaskStatus.NONE:
                    btnStatus.setText("NONE");
                    break;
                case TaskStatus.STARTED:
                    btnStatus.setText("started");
                    break;
                case TaskStatus.PAUSED:
                    btnStatus.setText("paused");
                    break;
                case TaskStatus.RESUMED:
                    btnStatus.setText("resumed");
                    break;
                case TaskStatus.FINISHED:
                    btnStatus.setText("finished");
                    break;
                case TaskStatus.SUCCED:
                    btnStatus.setText("succed");
                    break;
                case TaskStatus.FAILED:
                    btnStatus.setText("failed");
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(DownloadEvent event) {
        status.setText(event.status);
    }
}
