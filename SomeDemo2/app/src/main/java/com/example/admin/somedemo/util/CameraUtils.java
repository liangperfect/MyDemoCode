package com.example.admin.somedemo.util;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by admin on 2018/2/23.
 */

public class CameraUtils {

    /**
     * dump yuv data into file
     *
     * @param data yuv data
     */
    public static void dumpYUVImage(byte[] data) {
        try {
            File file = new File("/sdcard/camera2frame.yuv");
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
}
