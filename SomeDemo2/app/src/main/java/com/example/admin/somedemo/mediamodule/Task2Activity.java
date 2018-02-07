package com.example.admin.somedemo.mediamodule;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.admin.somedemo.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Task2Activity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getName().toString().trim();
    private Button mAudioRecorderBtn;
    private Button mAudioTrackBtn;
    private File mPCMFile;
    private File mWavFile;
    private boolean isRun;
    private final int WAV_HEADER_SIZE = 44;
    //采样率
    int sampleRateInHZ = 16000;
    //格式
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    //16Bit
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    //声道数
    short channelCount = 1;
    //pcm文件大小
    int pcmSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task2);
        mAudioRecorderBtn = findViewById(R.id.btn_audiorecorder);
        mAudioTrackBtn = findViewById(R.id.btn_audiotrack);
        mAudioRecorderBtn.setOnClickListener(this);
        mAudioTrackBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_audiorecorder:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //录制PCM格式
//                       startAudioRecorder();
//                      组装成wav文件
                        startAudioRecorderWave();
                    }
                }).start();
                break;

            case R.id.btn_audiotrack:
                startAudioTrack();
                break;

            default:
                break;
        }
    }


    /*解析音频*/
    private void startAudioTrack() {
        isRun = false;
        Log.d(TAG, "start to play music");
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ceshi.pcm");
        if (file == null) {
            return;
        }
        int musicLength = (int) (file.length() / 2);
        short[] music = new short[musicLength];

        try {
            InputStream is = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(is);
            DataInputStream dis = new DataInputStream(bis);
            int i = 0;
            while (dis.available() > 0) {
                music[i] = dis.readShort();
                i++;
            }
            dis.close();
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    musicLength * 2, AudioTrack.MODE_STREAM);
            audioTrack.play();
            audioTrack.write(music, 0, musicLength);
            audioTrack.stop();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*录制音频PCM*/
    private void startAudioRecorder() {
        //创建PCM文件
        mPCMFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ceshi.pcm");
        if (mPCMFile.exists()) {
            mPCMFile.delete();
        }
        try {
            mPCMFile.createNewFile();
            Log.d(TAG, "create pcm file");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "error to create pcm file");
        }
        //创建输出流
        try {
            OutputStream os = new FileOutputStream(mPCMFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHZ, channelConfiguration, audioEncoding);
            Log.d(TAG, "westalgo bufferSize :" + bufferSize);
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHZ, channelConfiguration, audioEncoding, bufferSize);
            short[] buffer = new short[bufferSize];
            //开始录音，保存到audioRecorderh缓存去区当中
            audioRecord.startRecording();
            //将audioRecorder中音频数据读到输出流当中去
            isRun = true;
            while (isRun) {
                int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                for (int i = 0; i < bufferReadResult; i++) {
                    dos.writeShort(buffer[i]);
                }
            }
            audioRecord.stop();
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*先通过上面的startAudioRecorder方法来录制一个pcm文件，
    *再利用startAudiRecorderWave方法生成一个wave的头部信息，
    *后面将只之前保存的pcm文件拼接到这个头部后面，
    * 就组装成了一个wav文件了
    * */
    private void startAudioRecorderWave() {
        //pcm data
        byte[] mOrinPcm = new byte[0];
        //创建wav文件
        mWavFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ceshi.wav");
        try {
            mOrinPcm = toByteArray3(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ceshi.pcm");
            Log.e(TAG, "westalgo mOrinPcm length: " + mOrinPcm.length);
            if (mOrinPcm.length < 1) {
                Log.e(TAG, "can not read pcm file ");
                return;
            }
            pcmSize = mOrinPcm.length;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mWavFile.exists()) {
            mWavFile.delete();
        }
        try {
            mWavFile.createNewFile();
            Log.d(TAG, "create wav file");
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "error to create wav file");
        }
        try {
            OutputStream os = new FileOutputStream(mWavFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHZ, channelConfiguration, audioEncoding);
            Log.d(TAG, "westalgo bufferSize:" + bufferSize);
            byte[] wavHeader = initWavHeader(channelCount, sampleRateInHZ,
                    (sampleRateInHZ * channelCount * (audioEncoding == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1)),
                    (short) (channelCount * (audioEncoding == AudioFormat.ENCODING_PCM_16BIT ? 2 : 1)),
                    (short) (audioEncoding == AudioFormat.ENCODING_PCM_16BIT ? 16 : 8), pcmSize);
            dos.write(wavHeader);
            dos.write(mOrinPcm);
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    偏移	大小	名字	描述
    0	4	块ID	包含ASCII字符串“RIFF”（大端形式表示为0x52494646）
    4	4	块大小	4 + (8 + 子块1大小) + (8 + 子块2大小)，该块在这个数字之后的数据大小）
    8	4	格式	包含字符串“WAVE”（大端形式表示为0x57415645）
    12	4	子块1ID	包含字符串“fmt ”（大端形式表示为0x666d7420）
    16	4	子块1大小	内容为PCM时为16，表示该子块在该数字之后的数据大小
    20	2	语音格式	内容为PCM为1
    22	2	通道数	单声道为1，立体声为2，等等
    24	4	采样率	8000、44100等
    28	4	字节率	== 采样率 * 通道数 * 每个样本的位数 / 8
    32	2	块对齐	== 通道数 * 每个样本的位数 / 8
    34	2	每个样本的位数	8位时为8，16位时为16，等等
    36	4	子块2ID	包含字符串“data”（大端形式为0x64617461）
    40	4	子块2大小	== 样本数 * 通道数 * 每个样本的位数 / 8
    44	*	数据	实际的语音数据
    */
    private byte[] initWavHeader(short channelCount, int sampleRate, int params2, short byteRate, short param5, int pcmSize) {
        int chrunckSize = pcmSize * channelCount * 16 / 8;
        int myChunkSize = chrunckSize + 36;
        ByteBuffer byteBuffer = ByteBuffer.allocate(WAV_HEADER_SIZE);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(0x46464952); //RIFF
        byteBuffer.putInt(myChunkSize);
        byteBuffer.putInt(0x45564157);
        byteBuffer.putInt(0x20746d66);
        byteBuffer.putInt(16);
        byteBuffer.putShort((short) 1);
        byteBuffer.putShort(channelCount);
        byteBuffer.putInt(sampleRate);
        byteBuffer.putInt(params2);
        byteBuffer.putShort(byteRate);
        byteBuffer.putShort(param5);
        byteBuffer.putInt(0x61746164); //data
        byteBuffer.putInt(pcmSize);
        if (byteBuffer != null) {
            return byteBuffer.array();
        }
        return null;
    }

    private byte[] toByteArray3(String filename) throws IOException {
        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(filename, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
