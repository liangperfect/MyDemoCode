package com.example.admin.somedemo.advertisedemo.customview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Author liang
 * Date
 * Dsc:
 */
public class DownLoadIndicator extends LinearLayout {
    private MyRotateLoading mRotateLoading;
    private TextView mTvNumber;

    public DownLoadIndicator(Context context) {
        this(context, null);
    }

    public DownLoadIndicator(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownLoadIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutParams linearParams = new LayoutParams(100, 100);
        setLayoutParams(linearParams);
        setOrientation(LinearLayout.VERTICAL);
        LayoutParams rotateLayout = new LayoutParams(50, 50);

    }


    public void start() {

    }

    public void stop() {


    }


}
