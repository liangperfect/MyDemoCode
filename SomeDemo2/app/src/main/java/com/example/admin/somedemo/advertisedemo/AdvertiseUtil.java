package com.example.admin.somedemo.advertisedemo;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Author liang
 * Date
 * Dsc:
 */
public class AdvertiseUtil {


    /**
     * 将Response写入到手机当中去
     */
//    public static boolean writeResponseBodyToDisk(Context context, ResponseBody body, String fileName) {
//        try {
//            File videoFile = new File(context.getExternalFilesDir(null) + File.separator + fileName);
//            if (videoFile.exists()) {
//                return true;
//            }
//            InputStream inputStream = null;
//            OutputStream outputStream = null;
//            try {
//                byte[] fileReader = new byte[4096];
//                long fileSize = body.contentLength();
//                long fileSizeDownloaded = 0;
//                inputStream = body.byteStream();
//                outputStream = new FileOutputStream(videoFile);
//                while (true) {
//                    int read = inputStream.read(fileReader);
//                    if (read == -1) {
//                        break;
//                    }
//                    outputStream.write(fileReader, 0, read);
//                    fileSizeDownloaded += read;
//                    //这里将下载的进度值给返回去
//
//                }
//                outputStream.flush();
//                return true;
//            } catch (IOException e) {
//                return false;
//            } finally {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//            }
//        } catch (IOException e) {
//            return false;
//        }
//    }
}
