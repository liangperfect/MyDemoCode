package com.example.admin.somedemo.mediamodule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.admin.somedemo.R;
import com.example.admin.somedemo.util.CameraUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Task3Activity extends AppCompatActivity {
    final String TAG = this.getClass().getName();
    SurfaceView mSurfaceView;
    final int CAMERA_OPEN = 1;
    final int CAMERA_START_PREVIEW = 2;
    final int CAMERA_STOP = 3;
    final int CAMERA_RELEASE = 4;
    final int PREVIEW_WIDTH = 1440;
    final int PREVIEW_HEIGHT = 1080;
    final int PICTURE_WIDTH = 4160;
    final int PICTURE_HEIGHT = 3120;
    final int PICTURE_ORIENTATION_0 = 0;
    final int PICTURE_ORIENTATION_90 = 90;
    final int PICTURE_ORIENTATION_180 = 180;
    final int PICTURE_ORIENTATION_270 = 270;
    final int PREVIEW_ORIENTATION_0 = 0;
    final int PREVIEW_ORIENTATION_90 = 90;
    final int PREVIEW_ORIENTATION_180 = 180;
    final int PREVIEW_ORIENTATION_270 = 270;
    static final int CAMERA_HAL_API_VERSION_1_0 = 0x100;
    int CameraId = 0;
    int frameCount = 0;
    Camera mCamera;
    CameraHandler mCameraHandler;
    SurfaceHolder mSurfaceHolder;
    /*API2 start*/
    String currentCameraId;
    CameraDevice mCurrentCameraDevice;
    CaptureRequest.Builder mPreviewRequestBuilder;
    CameraCaptureSession mCameraCaptureSession;
    CaptureRequest mCaptureRequest;
    /*API2 end */

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task3);
        handlCamera();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void handlCamera() {
//        cameraAPI1();
        cameraAPI2(Task3Activity.this);
    }

    private void cameraAPI1() {
        openCamera();
        mSurfaceView = findViewById(R.id.task3_surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "westalgo surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "westalgo surfaceChanged");
                Message msg = mCameraHandler.obtainMessage();
                msg.obj = holder;
                msg.what = CAMERA_START_PREVIEW;
                mCameraHandler.sendMessage(msg);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        //preview Callback medthod 1
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

//                ByteBuffer byteBuffer = ByteBuffer.allocate(data.length);
//                byteBuffer.put(data);
//                int depthSize = 1040 * 780 * 4 + 256;
//                byte[] depthData = new byte[depthSize];
//                byte[] mainImageData = new byte[];
//                System.arraycopy(data, 0, depthData, 0, depthSize);

                //dump 第150帧的预览数据
                /*
                frameCount++;
                if (frameCount == 150) {
                    try {
                        File file = new File("/sdcard/frame.yuv");
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        OutputStream os = new FileOutputStream(file);
                        BufferedOutputStream bos = new BufferedOutputStream(os);
                        DataOutputStream dos = new DataOutputStream(bos);
                        dos.write(data);
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                */
            }
        });
        //preview Callback medthod 2
        /*
        * 1.可以多次添加buffer到queue，如果preview帧到达时该buffer可用就从queue取出来；如果不可用就丢弃该帧；
          2.每次preview并且buffer可用的时候callback会被重复调用；
          3.通过对buffer内存的重用可以提高preview效率和帧率；
          至于适用场景，应该是需要实时预览，并且对质量和帧率要求很高的时候
        int length = mMainPreviewWidth * mMainPreviewHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8;
        mMemory = new byte[2][length];
        mMainCamera.addCallbackBuffer(mMemory[0]);
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {

            }
        });
        * */
    }
    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void cameraAPI2(Context context) {
        mSurfaceView = findViewById(R.id.task3_surfaceview);
        mSurfaceHolder = mSurfaceView.getHolder();
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //获取Camera参数特性
        try {
            for (String camId : cameraManager.getCameraIdList()) {
                Log.d(TAG, "westalgo cameraId" + camId);
                //只看主摄的，不看副摄和前摄,对应摄像头所具有的特性都可以从CameraCharacteristics里获取
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(camId);
                Integer face = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                if (face != null && face == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (streamConfigurationMap == null)
                    continue;

                Boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                if (flashAvailable)
                    Log.d(TAG, "westalgo flash support :" + flashAvailable);
                currentCameraId = camId;
            }
            //打开主摄Camera
            //
            CameraDevice.StateCallback callback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    //当Camera被打开时候回调
                    mCurrentCameraDevice = camera;
                    //Camera被打开后要请求预览，需要设置request和session等
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    mCurrentCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    mCurrentCameraDevice = null;
                }
            };

            cameraManager.openCamera(currentCameraId, callback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*从SurfaceHolder中获取到surface再通过CameraCaptureSession将预览数据显示到surfaceview上面
    *其中涉及一个RequestBuilder将surface给加载了
    *
    */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreviewSession() {
        try {
            //创建需要发送是什么请求，mPreviewRequestBuilder是session请求的事件
            mPreviewRequestBuilder = mCurrentCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface surface = mSurfaceHolder.getSurface();

            ImageReader imageReader = ImageReader.newInstance(PREVIEW_WIDTH, PREVIEW_HEIGHT, ImageFormat.JPEG, 2);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image img = reader.acquireNextImage();
                    Log.d(TAG, "imageReader img");
                    ByteBuffer byteBuffer = img.getPlanes()[0].getBuffer();
                    int pixelStride = img.getPlanes()[0].getPixelStride();
                    int rowStride = img.getPlanes()[0].getRowStride();
                    Log.d(TAG, "pixelStride" + pixelStride + "rowStride" + rowStride);
                    byte[] data = new byte[byteBuffer.remaining()];
                    byteBuffer.put(data);
                    CameraUtils.dumpYUVImage(data, "yuv");
                    img.close();
                }
            }, null);
            //将Camera和显示预览的surfaceview结合起来
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.addTarget(imageReader.getSurface());
            //前面两步将输出位置给设置好了 创建一个CameraCaptureSession来进行预览
            mCurrentCameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    //若Camera关闭就为空
                    if (mCurrentCameraDevice == null)
                        return;
                    //显示预览
                    mCameraCaptureSession = session;
                    //时间参数设置 - 自动对焦
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    //相关事件设置好之后就可以生成请求了
                    mCaptureRequest = mPreviewRequestBuilder.build();
                    //发送预览请求，预览数据就会呈现在之前设置的surface里
                    try {
                        mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        mCameraHandler = new CameraHandler();
        try {
            Method openMethod = Class.forName("android.hardware.Camera").getMethod(
                    "openLegacy", int.class, int.class);
            mCamera = (Camera) openMethod.invoke(null, CameraId, CAMERA_HAL_API_VERSION_1_0);
        } catch (Exception e) {
            mCamera = Camera.open(CameraId);
        }
        if (mCamera == null) {
            Log.e(TAG, "init Camera error");
        }
        mCameraHandler.sendEmptyMessage(CAMERA_OPEN);
    }

    private class CameraHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case CAMERA_OPEN:
                    Log.d(TAG, "camera open done");
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);
                    parameters.setPictureSize(PICTURE_WIDTH, PICTURE_HEIGHT);
                    parameters.setRotation(PICTURE_ORIENTATION_0);
                    parameters.setPreviewFormat(ImageFormat.NV21);
                    mCamera.setDisplayOrientation(PREVIEW_ORIENTATION_90);
                    mCamera.setParameters(parameters);
                    break;
                case CAMERA_START_PREVIEW:
                    try {
                        Log.e(TAG, "westalgo start preview");
                        SurfaceHolder surfaceHolder = (SurfaceHolder) msg.obj;
                        mCamera.setPreviewDisplay(surfaceHolder);
                        mCamera.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CAMERA_STOP:
                    mCamera.stopPreview();
                    break;
                case CAMERA_RELEASE:
                    mCamera.release();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mCameraHandler.sendEmptyMessage(CAMERA_START_PREVIEW);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mCameraHandler.sendEmptyMessage(CAMERA_STOP);
    }

    @Override
    protected void onDestroy() {
//        mCameraHandler.sendEmptyMessage(CAMERA_RELEASE);
        super.onDestroy();
    }
}
