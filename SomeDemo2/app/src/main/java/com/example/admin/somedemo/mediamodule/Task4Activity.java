package com.example.admin.somedemo.mediamodule;

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.admin.somedemo.R;

import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.nio.ByteBuffer;
import java.nio.file.FileAlreadyExistsException;

public class Task4Activity extends AppCompatActivity {
    private final String TAG = this.getClass().getName().toString().trim();
    private final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath();
    private final String INPUT_FILE_PATH = SDCARD_PATH + "/task4.mp4";
    private final String OUT_VIDEO_PATH = SDCARD_PATH + "/video.mp4";
    private final String OUT_AUDIO_PATH = SDCARD_PATH + "/audio.mp4";
    private final String OUT_MUXER_PATH = SDCARD_PATH + "/muxer.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task4);
        Button button = findViewById(R.id.btn_extractor);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void run() {
                        //从音视频中将视频分离出来
                        trackExtractor(INPUT_FILE_PATH, OUT_VIDEO_PATH, true);
                    }
                }).start();
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void run() {
                        //从音视频中将音频分离出来
                        trackExtractor(INPUT_FILE_PATH, OUT_AUDIO_PATH, false);
                    }
                }).start();
            }
        });
        Button muxerBtn = findViewById(R.id.btn_muxer);
        muxerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void run() {
                        trackMuxer(OUT_VIDEO_PATH, OUT_AUDIO_PATH, OUT_MUXER_PATH);
                    }
                }).start();
            }
        });
    }

    /**
     * 提取视频当中的音频或视频
     *
     * @param videoPath video source file path
     * @param outPath   dist file path
     * @param isVideo
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void trackExtractor(String videoPath, String outPath, boolean isVideo) {
        Log.d(TAG, "videoPath:" + videoPath);
        File mp4File = new File(videoPath);
        if (mp4File.exists()) {
            Log.d(TAG, "mp4File exit");
        } else {
            Log.d(TAG, "mp4File not exit");
        }

        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //复用器初始化
        MediaMuxer muxer = null;
        //轨道的索引
        int trackIndex = 0;
        //音视频中的轨道数
        int trackCount = mediaExtractor.getTrackCount();
        Integer frameRate = 0;
        Log.d(TAG, "trackCount:" + trackCount);
        for (int i = 0; i < trackCount; i++) {
            //获取当前轨道的格式
            MediaFormat mediaFormat = mediaExtractor.getTrackFormat(i);
            //当前轨道类型
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "current i:" + i + "  mime:" + mime);
            if (mime.startsWith("video") && isVideo) {
                //trackIndex = i;
                //视频轨道
                mediaExtractor.selectTrack(i);
                try {
                    muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                trackIndex = muxer.addTrack(mediaFormat);
                //查看一下当前视频文件的一些参数信息
                String bitRate = mediaFormat.getString(MediaFormat.KEY_BIT_RATE);
                //Integer captureRate = mediaFormat.getInteger(MediaFormat.KEY_CAPTURE_RATE);
                String colorFormat = mediaFormat.getString(MediaFormat.KEY_COLOR_FORMAT);
                //String duaration = mediaFormat.getString(MediaFormat.KEY_DURATION);
                //String durat = mediaFormat.getString(MediaFormat.KEY_DURATION)
                frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                String IIncerval = mediaFormat.getString(MediaFormat.KEY_I_FRAME_INTERVAL);
                Log.d(TAG, "params  bitRate:" + bitRate + /*"  captureRate:" + captureRate + */"  colorFormat:" + colorFormat + "  frameRate:" + frameRate + "  IIncerval:" + IIncerval);
                muxer.start();
                break;
            } else if (mime.startsWith("audio") && !isVideo) {
                //音频轨道
                trackIndex = i;
                mediaExtractor.selectTrack(i);
                try {
                    muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                trackIndex = muxer.addTrack(mediaFormat);
                //frameRate = mediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
                muxer.start();
                break;
            }
        }
        if (muxer == null) {
            Log.e(TAG, "muxer is null");
        }
        //存放视频帧/音频帧的数据的
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 1024);
        //设置帧信息
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        //获取两帧数据显示时间差的
        long sampleTime = getSampleTime(mediaExtractor, byteBuffer, frameRate, isVideo);
//        long audioSampleTime = getSampleTime(mediaExtractor, byteBuffer, 0, false);
        //循环去读取每一帧
        while (true) {
            int readSampleDataSize = mediaExtractor.readSampleData(byteBuffer, 0);
            Log.d(TAG, "readSampleDataSize:" + readSampleDataSize);
            if (readSampleDataSize < 0) {
                Log.e(TAG, "frame data is null");
                break;
            }
            //每一帧的数据大小
            bufferInfo.size = readSampleDataSize;
            //是否为同步帧/关键帧
            bufferInfo.offset = 0;
            //起始偏移量
            bufferInfo.flags = mediaExtractor.getSampleFlags();
            //计算每一帧的时间间隔单位为us，有B帧则PTS需要相加
            //bufferInfo.presentationTimeUs += 1000 * 1000 / frameRate;
            //若视频没有B帧，PTS则如下
            bufferInfo.presentationTimeUs += sampleTime;
            //将读取到的视频帧/音频帧通过MediaMuxer写入到文件当中去
            muxer.writeSampleData(trackIndex, byteBuffer, bufferInfo);
            //读取下一帧
            mediaExtractor.advance();
        }
        mediaExtractor.release();
        muxer.stop();
        muxer.release();
    }


    /***
     * 合并音频和视频
     * @param videoPath 需要被合并的视频
     * @param audioPath 需要被合并的音频
     * @param outPath 输出合并的音视频
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void trackMuxer(String videoPath, String audioPath, String outPath) {

        File mp4File = new File(videoPath);
        if (mp4File.exists()) {
            Log.d(TAG, "mp4File exit");
        } else {
            Log.d(TAG, "mp4File not exit");
        }


        //视频部分
        MediaExtractor videoExtractor = new MediaExtractor();
        try {
            videoExtractor.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat videoMediaFormat = null;
        int videoTrack = -1;
        int videoTrackCount = -1;
        videoTrackCount = videoExtractor.getTrackCount();
        //找寻视频中的视频轨道
        for (int i = 0; i < videoTrackCount; i++) {
            videoMediaFormat = videoExtractor.getTrackFormat(i);
            String mime = videoMediaFormat.getString(MediaFormat.KEY_MIME);
            Log.d(TAG, "mime:" + mime);
            if (mime.startsWith("video/")) {
                videoTrack = i;
                break;
            }
        }

        //音频部分
        MediaExtractor audioExtractor = new MediaExtractor();
        try {
            audioExtractor.setDataSource(audioPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaFormat audioMediaFormat = null;
        int audioTrack = -1;
        int audioTrackCount = audioExtractor.getTrackCount();
        for (int j = 0; j < audioTrackCount; j++) {
            audioMediaFormat = audioExtractor.getTrackFormat(j);
            String mime = audioMediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                audioTrack = j;
                break;
            }
        }
        Log.d(TAG, "videoTrack:" + videoTrack + "  audioTrack:" + audioTrack);
        videoExtractor.selectTrack(videoTrack);
        audioExtractor.selectTrack(audioTrack);

        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

        //准备工作完成后可以开始合并音视频
        MediaMuxer muxer = null;
        try {
            muxer = new MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //添加muxer需要包含的数据轨道格式
        int videoTrackIndex =  muxer.addTrack(videoMediaFormat);
        int audioTrackIndex =  muxer.addTrack(audioMediaFormat);
        muxer.start();

        //视频帧循环添加
        ByteBuffer videoByteBuffer = ByteBuffer.allocate(1024 * 1000);
        Integer frameRate = videoMediaFormat.getInteger(MediaFormat.KEY_FRAME_RATE);
        //获取帧间隔
        long videoSampleTime = getSampleTime(videoExtractor, videoByteBuffer, frameRate, true);
        while (true) {
            int readSampleSize = videoExtractor.readSampleData(videoByteBuffer, 0);
            if (readSampleSize < 0) {
                break;
            }
            //记录每一帧大小
            videoBufferInfo.size = readSampleSize;
            videoBufferInfo.flags = videoExtractor.getSampleFlags();
            videoBufferInfo.offset = 0;
            videoBufferInfo.presentationTimeUs += videoSampleTime;
            muxer.writeSampleData(videoTrackIndex, videoByteBuffer, videoBufferInfo);
            videoExtractor.advance();
        }

        ByteBuffer audioByteByffer = ByteBuffer.allocate(1024 * 1000);
        long audioSampleTime = getSampleTime(audioExtractor, audioByteByffer, 0, false);
        //音频帧循环添加
        while (true) {
            int audioSampleSize = audioExtractor.readSampleData(audioByteByffer, 0);
            if (audioSampleSize < 0) {
                break;
            }
            audioBufferInfo.size = audioSampleSize;
            audioBufferInfo.flags = audioExtractor.getSampleFlags();
            audioBufferInfo.offset = 0;
            videoBufferInfo.presentationTimeUs += audioSampleTime;
            muxer.writeSampleData(audioTrackIndex, audioByteByffer, audioBufferInfo);
            audioExtractor.advance();
        }
        muxer.stop();
        muxer.release();
        videoExtractor.release();
        audioExtractor.release();
    }

    /***
     * 获取两帧数据时间间隔
     * @param mediaExtractor
     * @param buffer
     * @return 时间长度
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private long getSampleTime(MediaExtractor mediaExtractor, ByteBuffer buffer, int frameRate, boolean isVideo) {
        long sampleTime;
//      mediaExtractor.readSampleData(buffer, 0);
//            skip first I frame
//      if (mediaExtractor.getSampleFlags() == MediaExtractor.SAMPLE_FLAG_SYNC)
//            mediaExtractor.advance();
        if (isVideo) {
            sampleTime = 1000 * 1000 / frameRate;
            Log.d(TAG, "videoSampleTime is " + sampleTime);
        } else {
            mediaExtractor.readSampleData(buffer, 0);
            long firstVideoPTS = mediaExtractor.getSampleTime();
            mediaExtractor.advance();
            mediaExtractor.readSampleData(buffer, 0);
            long SecondVideoPTS = mediaExtractor.getSampleTime();
            sampleTime = Math.abs(SecondVideoPTS - firstVideoPTS);
            Log.d(TAG, "videoSampleTime is " + sampleTime);
        }
        return sampleTime;
    }
}
