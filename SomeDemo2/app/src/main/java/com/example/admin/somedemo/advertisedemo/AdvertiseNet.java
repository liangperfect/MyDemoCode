package com.example.admin.somedemo.advertisedemo;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.example.admin.somedemo.advertisedemo.model.AdvertiseData;
import com.example.admin.somedemo.advertisedemo.model.Data;
import com.example.admin.somedemo.advertisedemo.model.DataList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Author liang
 * Date
 * Dsc:
 */
public class AdvertiseNet {
    private static String TAG = "AdvertiseNet";
    private final String ADVERTISE_INFO_BASE_URL = "http://10.0.1.245:10001/";
    private final String ADVERTISE_VIDEO_BASE_URL = "https://easy-auction-cs.oss-cn-beijing.aliyuncs.com/";
    private String screen_id = "Test0001";
    private Context mContext;
    private DownLoadVideoListener mDownLoadVideoListener;
    private static AdvertiseNet sAdvertiseNet;
    private int mVideoDataNumbers = 0;

    public static AdvertiseNet getInstance(Context context, DownLoadVideoListener downLoadVideoListener) {
        synchronized (AdvertiseNet.class) {
            if (sAdvertiseNet == null) {
                synchronized (AdvertiseNet.class) {
                    if (sAdvertiseNet == null) {
                        sAdvertiseNet = new AdvertiseNet(context, downLoadVideoListener);
                    }
                }
            }
        }
        return sAdvertiseNet;
    }

    private AdvertiseNet(Context context, DownLoadVideoListener downLoadVideoListener) {
        this.mContext = context;
        this.mDownLoadVideoListener = downLoadVideoListener;
    }

    /***
     *下载广告
     */
    public void downLoadAdvertise() {
        mDownLoadVideoListener.onStart();
        //获取下载请求参数
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ADVERTISE_INFO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<AdvertiseData> advertiseDataCall = apiService.getAdvertiseData(screen_id);
        advertiseDataCall.enqueue(new Callback<AdvertiseData>() {
            @Override
            public void onResponse(Call<AdvertiseData> call, Response<AdvertiseData> response) {

                AdvertiseData temp = response.body();
                Data data = temp.getData();
                List<DataList> dataList = data.getData_list();
                // TODO: 2018/11/15 视频列表对比后续添加,判断是否需要下载视频

                mVideoDataNumbers = dataList.size();
                for (int i = 0; i < dataList.size(); i++) {
                    downLoadAdvertisMp4(dataList.get(i).getMd5());
                }
            }

            @Override
            public void onFailure(Call<AdvertiseData> call, Throwable t) {
                mDownLoadVideoListener.onFaiure("Obtain Video Info " + t.getMessage());
            }
        });
    }

    private void downLoadAdvertisMp4(final String videoMd5) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ADVERTISE_VIDEO_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseBody> videoCall = apiService.getAdvertiseSources(videoMd5);

        videoCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                Log.d("chenliang",
                        "contentLengeth():" + response.body().contentLength() +
                                "  contentType():" + response.body().contentType() + " reponse:" + response.toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        writeResponseBodyToDisk(mContext, response.body(), videoMd5);
                    }
                }).start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Log.e(TAG, "retrofit:" + t.getMessage());
                mDownLoadVideoListener.onFaiure("DownLoad Video  " + t.getMessage());
            }
        });
    }

    public boolean writeResponseBodyToDisk(Context context, ResponseBody body, String fileName) {
        try {
            File videoFile = new File(context.getExternalFilesDir(null) + File.separator + fileName);
            if (videoFile.exists()) {
                mDownLoadVideoListener.onSuccess(videoFile.getPath(), mVideoDataNumbers);
                return true;
            }
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileTotalSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(videoFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    //将下载的进度值给返回去
                    mDownLoadVideoListener.onProgress((int) (100 * fileSizeDownloaded / fileTotalSize));
                }
                //当前文件下载完毕
                mDownLoadVideoListener.onSuccess(videoFile.getPath(), mVideoDataNumbers);
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    public interface DownLoadVideoListener {

        public void onStart();

        public void onProgress(int degree);


        /**
         * @param videoPath video保存的地址
         * @param videoSum  需要保存的视频总数，也是用来判断视频是否下完的标志
         */
        public void onSuccess(String videoPath, int videoSum);

        public void onFaiure(String errorMsg);
    }
}
