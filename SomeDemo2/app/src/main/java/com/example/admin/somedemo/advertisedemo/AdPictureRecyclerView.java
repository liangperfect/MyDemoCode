package com.example.admin.somedemo.advertisedemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Author liang
 * Date 2018/11/9
 * Dsc:
 */
public class AdPictureRecyclerView extends RecyclerView {



    public AdPictureRecyclerView(Context context) {
        this(context, null);
    }

    public AdPictureRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdPictureRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
