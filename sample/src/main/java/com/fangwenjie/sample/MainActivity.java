package com.fangwenjie.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fangwenjie.download.AndroidDownloadDog;
import com.fangwenjie.download.DogMsgEvent;
import com.fangwenjie.download.DownloadMsgEvent;
import com.fangwenjie.download.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Observable;
import java.util.Observer;

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

        AndroidDownloadDog.getInstance().addObserver(observer);

    }

    private Observer observer = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Log.d("clarkfang", "arg #" + arg);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        AndroidDownloadDog.getInstance().deleteObserver(observer);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleDogMsgEvent(DogMsgEvent event) {
        String contentBuilder = mTextview.getText() + "\n" +
                event.msg;
        mTextview.setText(contentBuilder);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHandleDownloadMsgEvent(DownloadMsgEvent event) {
        progressBar.setProgress(event.downloadProgress);
    }

    private int taskIndex = 0;
    private Task currentTask;

    @OnClick(R.id.button)
    public void onClickAddDownloadTaskBtn(View view) {
        taskIndex++;
        Toast.makeText(getApplicationContext(), "添加下载任务" + taskIndex, Toast.LENGTH_SHORT).show();

        currentTask = new Task.Builder()
                .setName("news" + taskIndex)
//                .setUrl("http://download.wehelpu.cn/android/tag/1.5.4_154/hisense_serving_1.5.4_154.apk")
                .setUrl("http://p.gdown.baidu.com/26790c55169aa5c32a49f3f21375b3ee2ed8443325be695798e816c7b15b308fb911d42383015e0479aa9c6ee4a01c33b068de64cd97ebb9ed340d0b6833f068a0fdd6b926aa92be83ee3ad5b96763b50fc93f3e299923a98e93f609d514da8a398667d51c06d03fe9ce24fabc24fe16abdae430f8815ac58e1a467b316257d9cacd5c411fc0256e36079c9733fc44df75629a866869459a637b30bd4efbebadf5bb35b70142bbccc1250aa4f4faf9716748a7106ec4ab83644c188c3a886561b0a7e94476430ee33e70527b4c37b35be2ad0c75aed255fc97875d867d8f4b80226ce7455d89f6e983186e2fc8ff3da37b1cc78c7fe2a6ea")
                .build(getApplicationContext());

        AndroidDownloadDog.getInstance().addTask(currentTask);
    }

    @OnClick(R.id.button4)
    public void onCancelTaskButtonClick(View view) {
        if (currentTask != null) {
            AndroidDownloadDog.getInstance().cancelTask(currentTask);
        }
    }

    @OnClick(R.id.button3)
    public void onPauseTaskButtonClick(View view) {
        if (currentTask != null) {
            currentTask.onPause();
        }
    }

    @OnClick(R.id.button2)
    public void onResumeTaskButtonClick(View view) {
        if (currentTask != null) {
            currentTask.onResume();
        }
    }
}
