package com.example.admin.somedemo.advertisedemo;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.VideoView;

import com.example.admin.somedemo.R;
import com.example.admin.somedemo.util.Util;

import java.io.File;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class AdvertiseActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private VideoView mVideoView;
    private Button btnPic0, btnPic1, btnPic2, btnPic3;
    private MyHandler mHandler;
    private String currentVideoPath;
    private int current_video_index;
    private int VIDEO_INDEX_ONE = 1;
    private int VIDEO_INDEX_TWO = 2;
    private List<Uri> picLists = new ArrayList<>();
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advertise);
        initView();
        initVideoData();
        initPicsData();
        setListener();
    }

    @SuppressLint("WrongViewCast")
    private void initView() {
        mRecyclerView = findViewById(R.id.recycleview_pictures);
        mVideoView = findViewById(R.id.vv_ad);
        btnPic0 = findViewById(R.id.btn_pic0);
        btnPic1 = findViewById(R.id.btn_pic1);
        btnPic2 = findViewById(R.id.btn_pic2);
        btnPic3 = findViewById(R.id.btn_pic3);
    }

    private void initVideoData() {
        currentVideoPath = "/sdcard/ad1.mp4";
        current_video_index = 1;
        mVideoView.setVideoPath(currentVideoPath);
        mVideoView.start();
        mHandler = new MyHandler();
        mHandler.sendEmptyMessageDelayed(1, 5000);
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mVideoView.isPlaying()) {
                mVideoView.pause();

                if (current_video_index == VIDEO_INDEX_ONE) {
                    currentVideoPath = "/sdcard/ad2.mp4";
                    current_video_index = VIDEO_INDEX_TWO;
                } else {
                    currentVideoPath = "/sdcard/ad1.mp4";
                    current_video_index = VIDEO_INDEX_ONE;
                }
                mVideoView.setVideoPath(currentVideoPath);
                mVideoView.start();
            }
            mHandler.sendEmptyMessageDelayed(1, 10000);
        }
    }

    private void initPicsData() {
        picLists.add(Util.getImageStreamFromExternal("/yixun/ad1pic.jpg"));
        picLists.add(Util.getImageStreamFromExternal("/yixun/ad2pic.jpg"));
        picLists.add(Util.getImageStreamFromExternal("/yixun/ad3pic.jpg"));
//        picLists.add(getImageStreamFromExternal("/sdcard/ad4pic.png"));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        myAdapter = new MyAdapter(picLists);
        mRecyclerView.setAdapter(myAdapter);
    }

    private class MyAdapter extends RecyclerView.Adapter {

        private List<Uri> data;

        public MyAdapter(List<Uri> data1) {
            this.data = data1;
        }

        class MyHolder extends RecyclerView.ViewHolder {

            ImageView mImageView;

            public MyHolder(View itemView) {
                super(itemView);
//                View view = LayoutInflater.from(AdvertiseActivity.this).inflate(R.layout.pic_item_image, null);
                mImageView = (ImageView) itemView;
//                mImageView = view.findViewById(R.id.img_pic);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyHolder(new ImageView(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MyHolder myHolder = (MyHolder) holder;
            ImageView img = myHolder.mImageView;
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 960 / data.size());
            img.setLayoutParams(layoutParams);
            img.setImageURI(data.get(position));
        }

        @Override
        public int getItemCount() {
            Log.d("chenliang", "chenliang getItemCount data size is " + data.size());
            return data.size();
        }
    }

    private void setListener() {
        btnPic0.setOnClickListener(this);
        btnPic1.setOnClickListener(this);
        btnPic2.setOnClickListener(this);
        btnPic3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pic0:


                break;
            case R.id.btn_pic1:
                picLists.remove(0);
//                picLists.remove(2);
                myAdapter.notifyDataSetChanged();
                break;
            case R.id.btn_pic2:
                picLists.remove(0);
//                picLists.remove(2);
                myAdapter.notifyDataSetChanged();
                if (picLists.isEmpty())
                    mRecyclerView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_pic3:

                break;
        }
    }
}
