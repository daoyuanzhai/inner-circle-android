package com.innercircle.android.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.MessageQueue;

abstract class AbstractHandlerThread extends HandlerThread {
    protected int mIndex;
    protected Handler mHandler;
    protected MessageQueue mMessageQueue;

    protected volatile boolean mIdleFlag;
    protected volatile HandlerThreadPoolManager mManager;

    public AbstractHandlerThread(String name) {
        super(name);
        start();

        this.mManager = HandlerThreadPoolManager.getInstance();
        this.mIdleFlag = true;

        this.mHandler = new Handler(getLooper());
        setMessageQueue();
    }

    protected abstract void setMessageQueue();

    protected abstract String getThreadName();
}