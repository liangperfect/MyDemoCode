package com.example.admin.somedemo.cmakejnidemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.admin.somedemo.R;

public class CmakeJniTestActivity extends AppCompatActivity {

    private native int initAssimp();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cmake_jni_test);

        TextView tv = findViewById(R.id.tv_cmakeshow);
        tv.setText("get value from  native is kkakakak " + initAssimp());
    }

    static {
        System.loadLibrary("assimp-jni");
    }

}
