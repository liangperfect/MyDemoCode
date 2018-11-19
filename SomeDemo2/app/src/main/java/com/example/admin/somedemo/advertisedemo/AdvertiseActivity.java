package com.example.admin.somedemo.advertisedemo;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.admin.somedemo.R;
import com.example.admin.somedemo.util.Util;
import com.victor.loading.rotate.RotateLoading;

import java.util.ArrayList;
import java.util.List;

public class AdvertiseActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "AdvertiseActivity";
    private VideoView mVideoView;
    private Button btnCustomerAD, btnPublicServiceAD, btnPromotationAD, btnSwitchPics;
    private TextView mTvDownLoadIndctor;
    private MyHandler mHandler;
    private int VIDEO_INDEX_ONE = 1;
    private int VIDEO_INDEX_TWO = 2;
    private List<Uri> picLists = new ArrayList<>();
    private AdvertiseNet mAdvertiseNet;
    private RotateLoading mRotateLoading;
    private final int DOWNLOAD_START = 1;
    private final int DOWNLOAD_PROGRESS = 2;
    private final int DOWNLOAD_FAIL = 3;
    private final int DOWNLOAD_SUCCESS = 4;

    private int currentMode;
    private final int CUSTOMER_VIDEO_MODE = 1; //客户广告模式
    private final int PROMOTIONAL_VIDEO_MODE = 2;//促销广告模式
    private final int PUBLIC_SERVICE_VIDEO_MODE = 3;//公益广告模式
    //客户广告列表
    private List<String> videoPathList;
    //公益广告列表
    private List<String> publicServiceVideoList;
    //促销广告列表
    private List<String> promotionalVideoList;
    //公益广告的assets路径
    private String mPublicServiceVideoPath;
    private int mCurrentVideoIndex = 0;

    private LinearLayout mPromotationPics;
    private ImageView mPromotationPic1;
    private ImageView mPromotationPic2;
    private ImageView mPromotationPic3;
    private int mChangeADPicIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //先不横屏，有问题,进来再进行旋转生命周期变换后会出现UI不显示的问题
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
        mVideoView = findViewById(R.id.vv_ad);
        btnCustomerAD = findViewById(R.id.btn_customer_advertiser);
        btnPublicServiceAD = findViewById(R.id.btn_public_service_advertiser);
        btnPromotationAD = findViewById(R.id.btn_promotation_advertise);
        btnSwitchPics = findViewById(R.id.btn_switch_pics);
        mTvDownLoadIndctor = findViewById(R.id.tv_download_indictor);
        mRotateLoading = findViewById(R.id.rotateloading_advertise);
        mPromotationPics = findViewById(R.id.ll_pic_contents);
        mPromotationPic1 = findViewById(R.id.img_pic1);
        mPromotationPic2 = findViewById(R.id.img_pic2);
        mPromotationPic3 = findViewById(R.id.img_pic3);
    }

    private void initVideoData() {
        mPublicServiceVideoPath = "android.resource://" + getPackageName() + "/" + R.raw.publicservice_ad;
        mHandler = new MyHandler();
        videoPathList = new ArrayList<>();
        publicServiceVideoList = new ArrayList<>();
        promotionalVideoList = new ArrayList<>();
        //默认开始是客户广告模式
        currentMode = CUSTOMER_VIDEO_MODE;
        publicServiceVideoList.add("/storage/emulated/0/Android/data/com.example.admin.somedemo/files/" + "gongyi1.mp4");
        publicServiceVideoList.add("/storage/emulated/0/Android/data/com.example.admin.somedemo/files/" + "gongyi2.mp4");
        promotionalVideoList.add("/storage/emulated/0/Android/data/com.example.admin.somedemo/files/" + "promotional1.mp4");
        promotionalVideoList.add("/storage/emulated/0/Android/data/com.example.admin.somedemo/files/" + "promotional2.mp4");
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
                    Log.d("chenliang", "videoPathList:" + videoPath);
                    videoPathList.add(videoPath);
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

    boolean isChangeAD = false;

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
                    String currentVideoPath = "";

                    switch (currentMode) {
                        case CUSTOMER_VIDEO_MODE:
                            mCurrentVideoIndex = mCurrentVideoIndex % (videoPathList.size());
                            currentVideoPath = videoPathList.get(mCurrentVideoIndex);
//                            break;
//                        case PROMOTIONAL_VIDEO_MODE:
//                            mCurrentVideoIndex = mCurrentVideoIndex % (promotionalVideoList.size());
//                            currentVideoPath = promotionalVideoList.get(mCurrentVideoIndex);
                            break;
                        case PUBLIC_SERVICE_VIDEO_MODE:
                            currentVideoPath = mPublicServiceVideoPath;
                            break;
                    }
                    mRotateLoading.stop();
                    mTvDownLoadIndctor.setVisibility(View.GONE);
                    mVideoView.setVideoPath(currentVideoPath);
                    mVideoView.start();
                    mCurrentVideoIndex++;
                    mHandler.sendEmptyMessageDelayed(DOWNLOAD_SUCCESS, 10000);

                    if (isChangeAD) {
                        mHandler.removeMessages(DOWNLOAD_SUCCESS);
                        mHandler.sendEmptyMessageDelayed(DOWNLOAD_SUCCESS, 10000);
                        isChangeAD = false;
                    }
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.pause();
    }

    private void setListener() {
        btnCustomerAD.setOnClickListener(this);
        btnPublicServiceAD.setOnClickListener(this);
        btnPromotationAD.setOnClickListener(this);
        btnSwitchPics.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_customer_advertiser:
                currentMode = CUSTOMER_VIDEO_MODE;
                mCurrentVideoIndex = 0;
                isChangeAD = true;
                mPromotationPics.setVisibility(View.GONE);
                mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                Toast.makeText(AdvertiseActivity.this, "客户广告", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_promotation_advertise:
//                currentMode = PROMOTIONAL_VIDEO_MODE;
                currentMode = CUSTOMER_VIDEO_MODE;
                mPromotationPics.setVisibility(View.VISIBLE);
                mPromotationPic1.setVisibility(View.VISIBLE);
                mPromotationPic2.setVisibility(View.VISIBLE);
                mPromotationPic3.setVisibility(View.VISIBLE);
                mCurrentVideoIndex = 0;
                isChangeAD = true;
                mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                Toast.makeText(AdvertiseActivity.this, "客户广告", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_public_service_advertiser:
                currentMode = PUBLIC_SERVICE_VIDEO_MODE;
                mCurrentVideoIndex = 0;
                isChangeAD = true;
                mPromotationPics.setVisibility(View.GONE);
                //不用更新视频
                mHandler.sendEmptyMessage(DOWNLOAD_SUCCESS);
                Toast.makeText(AdvertiseActivity.this, "公益广告", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_switch_pics:

                if (currentMode == CUSTOMER_VIDEO_MODE || currentMode == PROMOTIONAL_VIDEO_MODE) {

                    mChangeADPicIndex = mChangeADPicIndex % 4;
                    Log.d("chenliang", "mChangeADPicIndex:" + mChangeADPicIndex);
                    switch (mChangeADPicIndex) {
                        case 0:
                            mPromotationPic1.setVisibility(View.GONE);
                            break;
                        case 1:
                            mPromotationPic2.setVisibility(View.GONE);
                            break;
                        case 2:
                            mPromotationPic3.setVisibility(View.GONE);
                            mPromotationPics.setVisibility(View.GONE);
                            break;
                        default:
                            mPromotationPics.setVisibility(View.VISIBLE);
                            mPromotationPic1.setVisibility(View.VISIBLE);
                            mPromotationPic2.setVisibility(View.VISIBLE);
                            mPromotationPic3.setVisibility(View.VISIBLE);
                            break;
                    }
                } else {
                    Toast.makeText(AdvertiseActivity.this, "公益广告不能添加促销", Toast.LENGTH_SHORT).show();
                }
                mChangeADPicIndex++;
        }
    }
}
