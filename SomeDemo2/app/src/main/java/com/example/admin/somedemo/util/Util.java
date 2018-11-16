package com.example.admin.somedemo.util;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Author liang
 * Date
 * Dsc:
 */
public class Util {

    public static Uri getImageStreamFromExternal(String imageName) {
        File externalPubPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File picPath = new File(externalPubPath, imageName);
//        Log.d("chenliang", "getImageStreamFromExternal picPath" + picPath.getAbsolutePath());
        Uri uri = null;
        if (picPath.exists()) {
            uri = Uri.fromFile(picPath);
        }
        return uri;
    }

}
