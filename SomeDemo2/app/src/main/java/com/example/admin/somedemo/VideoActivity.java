package com.example.admin.somedemo;

import android.graphics.ImageFormat;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener {
    final String TAG = this.getClass().getName();
    /*some view */
    EditText edVideoPath;
    Button btnVideoPaly;
    Button btnVideoPause;
    Button btnVideoStop;
    Button btnVideoReset;
    SeekBar mSeekBar;
    TextView tvProgressingTime;
    TextView tvTotalTime;
    SurfaceView mSurfaceView;
    /*some data*/
    MediaPlayer mMediaPlayer;
    SurfaceHolder mSurfaceHolder;
    int mPlayerCurrentState;
    int mTotalTime;
    boolean isInterruptUpdateProgress;
    /*video state*/
    private final int STATE_IDLE = 0;
    private final int STATE_PLAYING = 1;
    private final int STATE_PAUSE = 2;
    private final int STATE_STOP = 3;
    private final int STATE_RESET = 4;
    /*progress switch*/
    private final boolean INTERRUPT_YES = true;
    private final boolean INTERRUPT_NO = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        initView();
    }

    private void initView() {
        edVideoPath = findViewById(R.id.ed_video_path);
        btnVideoPaly = findViewById(R.id.btn_video_play);
        btnVideoPause = findViewById(R.id.btn_video_pause);
        btnVideoStop = findViewById(R.id.btn_video_stop);
        btnVideoReset = findViewById(R.id.btn_video_reset);
        btnVideoPaly.setOnClickListener(this);
        btnVideoPause.setOnClickListener(this);
        btnVideoStop.setOnClickListener(this);
        btnVideoReset.setOnClickListener(this);
        mSeekBar = findViewById(R.id.seek_video);
        tvProgressingTime = findViewById(R.id.tv_progressing_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        mSurfaceView = findViewById(R.id.surfaceview);
        //init data
        mSurfaceHolder = mSurfaceView.getHolder();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_video_play:
                play();
                btnVideoPaly.setEnabled(false);
                break;

            case R.id.btn_video_pause:
                if (mMediaPlayer != null) {
                    if (STATE_PLAYING == mPlayerCurrentState) {
                        mMediaPlayer.pause();
                        mPlayerCurrentState = STATE_PAUSE;
                        btnVideoPaly.setEnabled(true);
                        isInterruptUpdateProgress = INTERRUPT_YES;
                    }
                }
                break;

            case R.id.btn_video_stop:
                if (mMediaPlayer != null) {
                    if (STATE_PLAYING == mPlayerCurrentState) {
                        mMediaPlayer.stop();
                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                        mPlayerCurrentState = STATE_STOP;
                        isInterruptUpdateProgress = INTERRUPT_YES;
                        btnVideoPaly.setEnabled(true);
                    }
                }
                break;

            case R.id.btn_video_reset:
                if (mMediaPlayer != null) {
                    if (STATE_PLAYING == mPlayerCurrentState) {
                        mMediaPlayer.reset();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                        isInterruptUpdateProgress = INTERRUPT_YES;
                        mPlayerCurrentState = STATE_RESET;
                        play();
                    }
                }
                break;

            default:
                break;
        }
    }

    private void play() {
        if (mMediaPlayer == null) {
            if (STATE_IDLE == mPlayerCurrentState || STATE_RESET == mPlayerCurrentState || STATE_STOP == mPlayerCurrentState) {
                String videPath = edVideoPath.getText().toString().trim();
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDisplay(mSurfaceHolder);
                    mMediaPlayer.setDataSource(videPath);
                    mMediaPlayer.prepareAsync();
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                            mPlayerCurrentState = STATE_PLAYING;
                            mTotalTime = mMediaPlayer.getDuration();
                            Log.e(TAG, "mTotalTime->" + mTotalTime);
                            mSeekBar.setMax(mTotalTime);
                            int m = mTotalTime / 1000 / 60;
                            int s = mTotalTime / 1000 % 60;
                            tvTotalTime.setText(m + ":" + s);
                            isInterruptUpdateProgress = INTERRUPT_NO;
                            new Thread(new ProcessingRunnbale()).start();
                        }
                    });
                    mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            Toast.makeText(VideoActivity.this, "Play Completion", Toast.LENGTH_SHORT).show();
                        }
                    });
                    mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(MediaPlayer mp) {
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (STATE_PAUSE == mPlayerCurrentState) {
                mMediaPlayer.start();
                mPlayerCurrentState = STATE_PLAYING;
            }
        }
    }

    public class ProcessingRunnbale implements Runnable {
        @Override
        public void run() {
            while (!isInterruptUpdateProgress) {
                // mTimerHandler.sendEmptyMessageDelayed(isInterruptUpdateProgress, 1000);
                if (mMediaPlayer != null) {
                    int currentTime = mMediaPlayer.getCurrentPosition();
                    final int m = currentTime / 1000 / 60;
                    final int s = currentTime / 1000 % 60;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvProgressingTime.setText(m + ":" + s);
                        }
                    });
                }
                SystemClock.sleep(1000);
            }
        }
    }

    Handler mTimerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            tvProgressingTime.
//            switch (msg.what) {
//                case INTERRUPT_YES:
//                    if (mMediaPlayer != null) {
//                        int currentTime = mMediaPlayer.getCurrentPosition();
//                        int m = currentTime / 1000 / 60;
//                        int s = currentTime / 1000 % 60;
//                        tvProgressingTime.setText(m + ":" + s);
//                    }
//                    break;
//
//                case INTERRUPT_NO:
//                    if (mMediaPlayer != null) {
//
//                    }
//                    break;
//            }
        }
    };
}
