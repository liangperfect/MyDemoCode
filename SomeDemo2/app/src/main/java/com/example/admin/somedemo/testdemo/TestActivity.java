package com.example.admin.somedemo.testdemo;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.admin.somedemo.R;

public class TestActivity extends AppCompatActivity implements View.OnClickListener {
    TextView t;
    VideoView mVideoView;
    Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        t = findViewById(R.id.tv_testthreadcallback);
        mVideoView = findViewById(R.id.vv_test);
        btnTest = findViewById(R.id.btn_test);
        btnTest.setOnClickListener(this);


        ThreadCallBack cb = new ThreadCallBack(TestActivity.this, new ThreadCallBack.ThreadCallBackListener() {
            @Override
            public void mycallback(String str) {
                Message msg = myHander.obtainMessage();
                msg.obj = str;
                myHander.sendMessage(msg);
            }
        });
        cb.runThread();
    }

    private Handler myHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("chenliang", "change message");
            t.setText((String) msg.obj);
        }
    };


    @Override
    public void onClick(View v) {
        mVideoView.setVideoPath("/storage/emulated/0/Android/data/com.example.admin.somedemo/files/613b90e0e63ee965035ded591ee62aca.mp4");
        mVideoView.start();
    }
}
