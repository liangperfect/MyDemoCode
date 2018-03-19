package com.example.admin.somedemo.mediamodule;

import android.content.pm.ActivityInfo;
import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.admin.somedemo.MediaTask.MyTDView;
import com.example.admin.somedemo.R;

public class Task5Activity extends AppCompatActivity {
    GLSurfaceView mGLSurfaceView;
    MyTDView mTDView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置为竖屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mTDView = new MyTDView(this);
        mTDView.requestFocus();
        mTDView.setFocusableInTouchMode(true);
        setContentView(mTDView);
    }

    private void initView() {
        mGLSurfaceView = findViewById(R.id.gl_trangle);
    }
}
