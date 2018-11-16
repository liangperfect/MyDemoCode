package com.example.admin.somedemo.databingdemo;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

/**
 * Author liang
 * Date
 * Dsc:
 */
public class MyStringUtils {

    public static String capitalize(final String word) {
        if (word.length() > 1) {

            return String.valueOf(word.charAt(0)).toUpperCase() + word.substring(1);
        }
        return word;
    }

    @BindingAdapter({"url"})
    public static void loadImage(ImageView view, String url) {


    }
}
