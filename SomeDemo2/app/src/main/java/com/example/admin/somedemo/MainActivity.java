package com.example.admin.somedemo;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.admin.somedemo.util.CameraSettings;
import com.example.admin.somedemo.util.PermissionsActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getName();
    private final int SEEK_BAR_TIME = 1000;
    private boolean mHasCriticalPermissions;
    private Button btnStart;
    private Button btnPause;
    private Button btnGoOn;
    private EditText mEditText;
    private Button btnSeek;

    private MediaPlayer mMediaPlayer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkPermissions() || !mHasCriticalPermissions) {
            Log.v(TAG, "onCreate: Missing critical permissions.");
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView() {
        btnStart = findViewById(R.id.btn_start);
        btnPause = findViewById(R.id.btn_pause);
        btnGoOn = findViewById(R.id.btn_restart);
        btnSeek = findViewById(R.id.btn_seek);
        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnGoOn.setOnClickListener(this);
        btnSeek.setOnClickListener(this);
        mEditText = findViewById(R.id.ed_file_path);
    }

    private void initData() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnStart.setEnabled(true);
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(MainActivity.this, "MediaPlayer error", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissions() {
        boolean requestPermission = false;
        if (checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
            mHasCriticalPermissions = true;
        } else {
            mHasCriticalPermissions = false;
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isRequestShown = prefs.getBoolean(CameraSettings.KEY_REQUEST_PERMISSION, false);
        if (!isRequestShown || !mHasCriticalPermissions) {
            Log.v(TAG, "Request permission");
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(CameraSettings.KEY_REQUEST_PERMISSION, true);
            editor.apply();
            requestPermission = true;
        }
        return requestPermission;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                try {
                    mMediaPlayer.setDataSource(mEditText.getText().toString().trim());
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    //加载多媒体文件
                    mMediaPlayer.prepareAsync();
                    Log.e(TAG, "duration->" + mMediaPlayer.getDuration());
                    mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mMediaPlayer.start();
                            btnStart.setEnabled(false);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_restart:
                mMediaPlayer.start();
                break;

            case R.id.btn_pause:
                mMediaPlayer.pause();
                break;

            case R.id.btn_seek:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.seekTo(SEEK_BAR_TIME);
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }
}