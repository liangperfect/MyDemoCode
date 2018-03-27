package com.example.admin.somedemo.mediamodule;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.admin.somedemo.MediaTask.MyGlPicView;
import com.example.admin.somedemo.MediaTask.MyTDView;
import com.example.admin.somedemo.MediaTask.PicTriangle;
import com.example.admin.somedemo.R;

public class Task6Activity extends AppCompatActivity {
    MyGlPicView mMyGlPicView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_task6);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mMyGlPicView = new MyGlPicView(Task6Activity.this);
        setContentView(mMyGlPicView);
    }
}
