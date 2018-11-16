package com.example.admin.somedemo.testdemo;

import android.content.Context;

/**
 * Author liang
 * Date 2018/11/16
 * Dsc:
 */
public class ThreadCallBack {

    private Context mContext;
    private ThreadCallBackListener mThreadCallBackListener;

    interface ThreadCallBackListener {
        public void mycallback(String str);
    }

    public ThreadCallBack(Context context, ThreadCallBackListener threadCallBackListener) {
        this.mContext = context;
        mThreadCallBackListener = threadCallBackListener;
    }

    public void runThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mThreadCallBackListener.mycallback("执行成功了的");
            }
        }).start();
    }
}
