package com.example.admin.somedemo.mediamodule;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.admin.somedemo.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.IllegalFormatCodePointException;

public class Task7Activity extends AppCompatActivity {
    final String TAG = "Task7Activity";
    /*pcm编码成aac*/
    MediaCodec mAudioCodec;
    OutputStream outputStream;
    /*aac解码成pcm*/
    MediaCodec mAudioDecode;
    MediaExtractor mAudioExtractor;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task7);

    }

    /**
     * 将pcm编码成能播放的aac
     * MediaCodec简单的使用步骤
     * <p>
     * 1.初始化MediaCodec，给其设置好MediaFormat（包含媒体信息的参数）
     * 2.从MediaCodec中获取输入和输出的缓冲队列，输入队列存放了需要进行编码或者解码的媒体文件，
     * 缓冲队列存放了是已经被编码或解码后的媒体文件
     * 3.从输入队列里面获取到输入的ByteBuffer，然后给buffer填充需要编码或解码的数据
     * 4.进行编码或者解码
     * 5.从输出缓冲队列里面获取到输出的ByteBuffer,里面包含的数据已经是编码或者解码后的数据
     * 6.关闭相关流和清空buffer
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public boolean codePcmToAAC(byte[] inputPcmData) {
        boolean r = false;
        File mAACFile = new File(Environment.getExternalStorageDirectory(), "audio_test.aac");
        if (mAACFile.exists()) {
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(mAACFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            r = true;
        } else {
            Log.e(TAG, "aac文件无法创建");
            return r;
        }

        //MediaCode实例化
        String AUDIO_MIME = "audio/map4-latm";
        try {
            //创建编码的MediaCodec
            mAudioCodec = MediaCodec.createEncoderByType(AUDIO_MIME);
            MediaFormat audioFormat = new MediaFormat();
            //文件类型
            audioFormat.setString(MediaFormat.KEY_MIME, AUDIO_MIME);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 32000);
            audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
            audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 48000);
//          audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,BUFFERIN);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            mAudioCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);//mediaCodec 当前的状态是configurtation
            mAudioCodec.start();//进入Executing状态
            //获取输入和输出缓存队列
            ByteBuffer[] mAudioInputBuffers = mAudioCodec.getInputBuffers();
            ByteBuffer[] mAudioOutBuffers = mAudioCodec.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            //初始化完成

            //根据索引获取输入ByteBuffer
            byte[] pcmData = new byte[]{0x12, 0x26};//假设已经获取到了pcm数据
            //参数 -1确保数据都被读取到了的
            int inputIndex = mAudioCodec.dequeueInputBuffer(-1);
            //从输入队列里面获取到
            ByteBuffer mInputBuffer = mAudioInputBuffers[inputIndex];
            //清空之前可能存在的buffer数据
            mInputBuffer.clear();
            //将pcm数据放入到输入buffer里面
            mInputBuffer.put(pcmData);
            //将装有pcm数据的buffer入队后，告诉MediaCodec要开始编码了
            mAudioCodec.queueInputBuffer(inputIndex, 0, pcmData.length, 0, 0);
            //开始去获取编码后的aac数据
            int outIndex = mAudioCodec.dequeueOutputBuffer(bufferInfo, 1000);
            ByteBuffer mOutBuffer;
            while (outIndex >= 0) {
                //编码成aac数据后的buffer大小
                int outBufferSize = bufferInfo.size;
                //因为编码后的aac是裸流，播放器是无法解析播放的，所以需要在aac裸前面加上7字节的adts的头
                int outPacketSize = outBufferSize + 7;
                //获取已经被编码成aac数据的buffer
                mOutBuffer = mAudioOutBuffers[outIndex];
                mOutBuffer.position(bufferInfo.offset);
                mOutBuffer.limit(bufferInfo.offset + outBufferSize);
                //包含adts头的aac的字节数组，也就是完整数据
                byte[] outData = new byte[outBufferSize];
                //先添加7字节的头
                addADTStoPacket(outData, outPacketSize);
                //将aac数据填充到输出字节数组当中去
                mOutBuffer.get(outData, 7, outBufferSize);
                mOutBuffer.position(bufferInfo.size);
                //将获取到aac数据保存到文件里面去
                outputStream.write(outData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //释放相关资源
        try {
            outputStream.close();
            if (mAudioCodec != null) {
                mAudioCodec.stop();
                mAudioCodec.release();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return r;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public boolean decodeAACToPcm(byte[] aacData) {
        boolean r = false;

        //aac文件的原始路径
        String aacFilePath = "aac文件的路径";
        //解析完aac保存的pcm文件
        File mAACFile = new File(Environment.getExternalStorageDirectory(), "audio_test.pcm");
        if (mAACFile.exists()) {
            try {
                outputStream = new BufferedOutputStream(new FileOutputStream(mAACFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            r = true;
        } else {
            Log.e(TAG, "aac文件无法创建");
            return r;
        }
        String AUDIO_MIME = "audio/map4-latm";
        //实例化MediaCodec
        try {
            mAudioDecode = MediaCodec.createDecoderByType(AUDIO_MIME);
            MediaFormat mDecodeFormat = createFormat(aacFilePath);
            // 配置好mAudioDecode
            mAudioDecode.configure(mDecodeFormat, null, null, 0);
            //前面两步将空闲状态需要配置的东西配置完成后，MediaCodec转换成Ｒunnable状态
            mAudioDecode.start();
            //获取输入输出的缓冲队列
            ByteBuffer[] mAudioInputBuffers = mAudioCodec.getInputBuffers();
            ByteBuffer[] mAudioOutBuffers = mAudioCodec.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            mAudioInputBuffers = mAudioDecode.getInputBuffers();
            mAudioOutBuffers = mAudioCodec.getOutputBuffers();
            //初始化完成

            //开始解码
            boolean whileInterupt = false;
            //循环去读取音频的帧数据
            while (!whileInterupt) {
                //获取输入缓冲buffer index
                int inputBufferIndex = mAudioDecode.dequeueInputBuffer(5000);
                if (inputBufferIndex > 0) {
                    ByteBuffer inputBuffer = mAudioInputBuffers[inputBufferIndex];
                    //从MediaExtractor中去读取音频帧数据,并返回读取到的数据大小
                    int s =  mAudioExtractor.readSampleData(inputBuffer, 0);

                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return r;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public MediaFormat createFormat(String aacFilePath) {
        MediaFormat m = null;
        mAudioExtractor = new MediaExtractor();
        try {
            mAudioExtractor.setDataSource(aacFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < mAudioExtractor.getTrackCount(); i++) {
            m = mAudioExtractor.getTrackFormat(i);
            String mime = m.getString(MediaFormat.KEY_MIME);
            if (mime.contains("audio/")) {
                mAudioExtractor.selectTrack(i);
                break;
            }
        }
        return m;
    }


    public void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        // 39=MediaCodecInfo.CodecProfileLevel.AACObjectELD;
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE
        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
