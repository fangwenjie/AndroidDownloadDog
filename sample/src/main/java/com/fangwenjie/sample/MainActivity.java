package com.fangwenjie.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fangwenjie.downloadgo.TaskEvent;
import com.fangwenjie.downloadgo.TaskMsg;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.textView)
    TextView mTextview;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleStatusEvent(StatusEvent event) {
        String contentBuilder = mTextview.getText() + "\n" +
                event.msg;
        mTextview.setText(contentBuilder);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleMsgEvent(MsgEvent event) {
        progressBar.setProgress(event.progress);
    }

    private int taskIndex = 0;

    private String taskId;

    @OnClick(R.id.button)
    void onClickLaunchDownloadDog(View view) {
    }

    @OnClick(R.id.button0)
    void onClickBindService(View view) {
    }

    @OnClick(R.id.button1)
    public void onClickAddDownloadTaskBtn(View view) {
        taskIndex++;
        Toast.makeText(getApplicationContext(), "添加下载任务" + taskIndex, Toast.LENGTH_SHORT).show();

        TaskMsg taskMsg = new TaskMsg(
                "news" + taskIndex,
                "https://tg.pyw.cn/Ssxy/check?g=3766&p=aqy&c=aqyfxl&adt=3766M8001&a=2390"
        );

        taskId = DownloadGo.getInstance().addTask(taskMsg);
    }

    @OnClick(R.id.button2)
    public void onResumeTaskButtonClick(View view) {
        onSendTaskEvent(taskId, TaskEvent.RESUME);
    }

    @OnClick(R.id.button3)
    public void onPauseTaskButtonClick(View view) {
        onSendTaskEvent(taskId, TaskEvent.PAUSE);
    }

    @OnClick(R.id.button4)
    public void onCancelTaskButtonClick(View view) {
        onSendTaskEvent(taskId, TaskEvent.CANCEL);
    }

    /**
     * 通过任务Id & 任务事件 操作任务
     *
     * @param taskId    任务Id
     * @param taskEvent 任务事件
     */
    public void onSendTaskEvent(String taskId, int taskEvent) {
        DownloadGo.getInstance().onSendTaskEvent(taskId, taskEvent);
    }


}
