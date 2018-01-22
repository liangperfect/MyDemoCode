package com.example.admin.somedemo.mediamodule;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.admin.somedemo.R;

import java.io.IOException;
import java.nio.ByteBuffer;

public class Media2Activity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getName().toString();
    Button mediaBtn;
    private MediaMuxer mMediaMuxer;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media2);
        mediaBtn = findViewById(R.id.btn_media);
        mediaBtn.setOnClickListener(this);
        try {
            mMediaMuxer = new MediaMuxer("sdcard/chang.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_media:
                videoMediaExtractor();
                break;
            default:
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void videoMediaExtractor() {
        MediaExtractor videoME = new MediaExtractor();
        int videoTrackIndex = -1;
        try {
            videoME.setDataSource("sdcard/test.mp4");
            Log.d(TAG, "getTrackCount():" + videoME.getTrackCount());
            for (int i = 0; i < videoME.getTrackCount(); i++) {
                MediaFormat format = videoME.getTrackFormat(i);
                if (format.getString(MediaFormat.KEY_MIME).startsWith("video/")) {
                    videoME.selectTrack(i);
                    videoTrackIndex = mMediaMuxer.addTrack(format);
                    break;
                }
//                Log.d(TAG, "format:" + format.getString(MediaFormat.KEY_MIME));
            }

//            MediaExtractor audioME = new MediaExtractor();
//            audioME.setDataSource("sdcard/gangqin.mp3");
//            int audioTrackIndex = -1;
//            Log.d(TAG,"audio getTrackCount"+audioME.getTrackCount());
//            for (int i = 0; i < audioME.getTrackCount(); i++) {
//                MediaFormat format = audioME.getTrackFormat(i);
//                Log.d(TAG,"audio format"+format.toString());
//                if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
//                    audioME.selectTrack(i);
//                    audioTrackIndex = mMediaMuxer.addTrack(format);
//                    break;
//                }
//            }
            //开始封装
            mMediaMuxer.start();

            if (-1 != videoTrackIndex) {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                info.presentationTimeUs = 0;
                ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
                while (true) {
                    int sampleSize = videoME.readSampleData(buffer, 0);
                    if (sampleSize < 0)
                        break;
                    info.offset = 0;
                    info.size = sampleSize;
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                    info.presentationTimeUs = videoME.getSampleTime();
                    mMediaMuxer.writeSampleData(videoTrackIndex, buffer, info);
                    videoME.advance();
                }
            }

            // 封装音频track
//            if (-1 != audioTrackIndex) {
//                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
//                info.presentationTimeUs = 0;
//                ByteBuffer buffer = ByteBuffer.allocate(100 * 1024);
//                while (true) {
//                    int sampleSize = audioME.readSampleData(buffer, 0);
//                    if (sampleSize < 0) {
//                        break;
//                    }
//                    info.offset = 0;
//                    info.size = sampleSize;
//                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
//                    info.presentationTimeUs = audioME.getSampleTime();
//                    mMediaMuxer.writeSampleData(audioTrackIndex, buffer, info);
//                    audioME.advance();
//                }
//            }
            videoME.release();
//            audioME.release();
            //释放MediaMu
            mMediaMuxer.stop();
            mMediaMuxer.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
