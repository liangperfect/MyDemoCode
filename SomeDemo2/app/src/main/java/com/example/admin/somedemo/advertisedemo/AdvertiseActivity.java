package com.example.admin.somedemo.advertisedemo;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.admin.somedemo.R;
import com.example.admin.somedemo.advertisedemo.customview.MyRotateLoading;
import com.example.admin.somedemo.util.Util;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.List;

public class AdvertiseActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "AdvertiseActivity";
    private RecyclerView mRecyclerView;
    private VideoView mVideoView;
    private Button btnPic0, btnPic1, btnPic2, btnPic3;
    private TextView mTvDownLoadIndctor;
    private MyHandler mHandler;
    private String currentVideoPath;
    private int current_video_index;
    private int VIDEO_INDEX_ONE = 1;
    private int VIDEO_INDEX_TWO = 2;
    private List<Uri> picLists = new ArrayList<>();
    private MyAdapter myAdapter;
    private AdvertiseNet mAdvertiseNet;
    private RotateLoading mRotateLoading;
    private final int DOWNLOAD_START = 1;
    private final int DOWNLOAD_PROGRESS = 2;
    private final int DOWNLOAD_FAIL = 3;
    private final int DOWNLOAD_SUCCESS = 4;
    private List<String> videoPathList;
    private int mCurrentVideoIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //先不横屏，有问题
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
        current_video_index = VIDEO_INDEX_ONE;
        mRecyclerView = findViewById(R.id.recycleview_pictures);
        mVideoView = findViewById(R.id.vv_ad);
        btnPic0 = findViewById(R.id.btn_pic0);
        btnPic1 = findViewById(R.id.btn_pic1);
        btnPic2 = findViewById(R.id.btn_pic2);
        btnPic3 = findViewById(R.id.btn_pic3);
        mTvDownLoadIndctor = findViewById(R.id.tv_download_indictor);
//        mRotateLoading = findViewById(R.id.rotateloading_advertise);
        mRotateLoading = findViewById(R.id.rotateloading_advertise);
    }

    private void initVideoData() {
        currentVideoPath = "/sdcard/ad1.mp4";
        mHandler = new MyHandler();
        videoPathList = new ArrayList<>();
        //请求网络数据
        mAdvertiseNet = AdvertiseNet.getInstance(AdvertiseActivity.this, new AdvertiseNet.DownLoadVideoListener() {
            @Override
            public void onStart() {
                Message msg = mHandler.obtainMessage();
                msg.what = DOWNLOAD_START;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onProgress(int degree) {
                Message msg = mHandler.obtainMessage();
                msg.what = DOWNLOAD_PROGRESS;
                msg.arg2 = degree;
                Log.d("chenliang", "onprogress is " + degree);
                mHandler.sendMessage(msg);
            }

            @Override
            public void onSuccess(String videoPath, int videoSum) {
                //开始播放广告
                synchronized (videoPathList) {
                    videoPathList.add(videoPath);
//                currentVideoPath = videoPath;
                    if (videoSum == videoPathList.size()) {
                        mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                    }
                }

            }

            @Override
            public void onFaiure(String throwable) {
                Log.d("chenliang", "下载错误:" + throwable);
                mHandler.sendEmptyMessage(DOWNLOAD_FAIL);
            }
        });

        //开机的时候下载视频,接口为准备可以暂时不用进行网络调试
        mAdvertiseNet.downLoadAdvertise();
    }

    private class MyHandler extends Handler {
        public MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DOWNLOAD_START:
                    Toast.makeText(AdvertiseActivity.this, "开始更新广告数据", Toast.LENGTH_SHORT).show();
                    mRotateLoading.start();
                    mTvDownLoadIndctor.setVisibility(View.VISIBLE);
                    mTvDownLoadIndctor.setText("1%");
                    break;
                case DOWNLOAD_PROGRESS:
                    mTvDownLoadIndctor.setText(msg.arg2 + "%");
                    break;
                case DOWNLOAD_FAIL:
                    //弹出错误信息给用户
                    Log.e("chenliang", "DOWNLOAD_FAIL");
                    mRotateLoading.stop();
                    break;
                case DOWNLOAD_SUCCESS:
                    Log.d("chenliang", "DOWNLOAD_SUCCESS videoPathSize :" + videoPathList.size());
                    mCurrentVideoIndex = mCurrentVideoIndex % (videoPathList.size());

                    Log.d("chenliang", "play mCurrentVideoIndex:" + mCurrentVideoIndex);
                    mRotateLoading.stop();
                    mTvDownLoadIndctor.setVisibility(View.GONE);
                    mVideoView.setVideoPath(videoPathList.get(mCurrentVideoIndex));
                    mVideoView.start();
                    mCurrentVideoIndex++;
                    mHandler.sendEmptyMessageDelayed(DOWNLOAD_SUCCESS, 10000);
                    break;
                default:
                    break;
            }
        }
    }

    private void initPicsData() {
        picLists.add(Util.getImageStreamFromExternal("/yixun/ad1pic.jpg"));
        picLists.add(Util.getImageStreamFromExternal("/yixun/ad2pic.jpg"));
        picLists.add(Util.getImageStreamFromExternal("/yixun/ad3pic.jpg"));
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
//                mRotateLoading.start();
                //开始播放视频
                Log.d("chenliang", " onClick pic0000000000000 ");
//                mHandler.sendEmptyMessage(DOWNLOAD_START);
                break;
            case R.id.btn_pic1:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        int i = 0;
                        do {
                            i++;
                            try {
                                Thread.sleep(150);
                                Message msg = mHandler.obtainMessage();
                                msg.what = DOWNLOAD_PROGRESS;
                                msg.arg2 = i;
                                Log.d("chenliang", "msg.what:" + msg.what + "  msg.arg2" + msg.arg2);
                                mHandler.sendMessage(msg);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (i < 100);
                        mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                    }
                }).start();
                break;
            case R.id.btn_pic2:
//                picLists.remove(0);
//                picLists.remove(2);
//                myAdapter.notifyDataSetChanged();
//                if (picLists.isEmpty())
//                    mRecyclerView.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_pic3:
//                mRotateLoading.stop();
                break;
        }
    }
}
