package com.example.admin.somedemo.MediaTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.example.admin.somedemo.R;

public class MediaTaskOneActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getName();
    private ImageView mImageView;
    private SurfaceView mSurfaceView;
    private CustomImageView mCustomImageView;
    private SurfaceHolder mSurfaceHolder;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_task_one);
        initView();
    }

    private void initView() {
        mImageView = findViewById(R.id.image_show);
        mSurfaceView = findViewById(R.id.surfaceview_image);
        mCustomImageView = findViewById(R.id.customimageview_iamge);
        allShow();
    }

    private void allShow() {
        imageShow();
        surfaceShow();
        customImageShow();
    }

    /*image show picture*/
    private void imageShow() {
        mImageView.setImageResource(R.drawable.bg_1);
    }

    /*surfaceview show picture*/
    private void surfaceShow() {
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "surfaceCreated");
                Canvas canvas = holder.lockCanvas();
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.FILL);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_2);
                canvas.drawBitmap(bitmap, 0, 0, paint);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "surfaceChanged");

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "surfaceDestroyed");
                bitmap.recycle();
                bitmap = null;
            }
        });
    }

    /*customview show picture*/
    public void customImageShow() {
        mCustomImageView.setResId(R.drawable.bg_3);
        mCustomImageView.show();
    }
}
