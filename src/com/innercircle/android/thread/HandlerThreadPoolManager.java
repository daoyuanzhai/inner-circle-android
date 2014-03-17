package com.innercircle.android.thread;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class HandlerThreadPoolManager {
    private static final String TAG = HandlerThreadPoolManager.class.getSimpleName();

    private static final int CORE_THREAD_NUMBER = 3;
    private static final int EXTRA_THREAD_NUMBER = 2;

    private static volatile HandlerThreadPoolManager instance;

    private volatile CoreHandlerThread[] coreThreads;
    private volatile ExtraHandlerThread[] extraThreads;

    private volatile List<Runnable> mRunnables;

    private HandlerThreadPoolManager() {}

    public static HandlerThreadPoolManager getInstance() {
        if (null == instance) {
            synchronized (HandlerThreadPoolManager.class) {
                if (null == instance) {
                    instance = new HandlerThreadPoolManager();
                    instance.init();
                }
            }
        }
        return instance;
    }

    private void init() {
        this.mRunnables = new LinkedList<Runnable>();

        this.coreThreads = new CoreHandlerThread[CORE_THREAD_NUMBER];
        this.extraThreads = new ExtraHandlerThread[EXTRA_THREAD_NUMBER];

        for (int i = 0; i < CORE_THREAD_NUMBER; i++) {
            this.coreThreads[i] = new CoreHandlerThread(i);
        }
    }

    private Runnable getRunnableFromHead() {
        Runnable runnable = null;
        if (this.mRunnables.size() > 0) {
            synchronized (this) {
                if (this.mRunnables.size() > 0) {
                    runnable = this.mRunnables.remove(0);
                }
            }
        }
        return runnable;
    }
    protected void postToThread(final AbstractHandlerThread thread) {
        final Runnable runnable = getRunnableFromHead();
        if (null != runnable) {
            thread.mIdleFlag = false;
            thread.mHandler.post(runnable);
        } else {
            thread.mIdleFlag = true;
        }
    }

    protected void closeIdleExtraThread(final int index) {
        final Runnable closingRunnable = new Runnable(){
            @Override
            public void run() {
                if (extraThreads[index] != null && extraThreads[index].mIdleFlag) {
                    Log.v(TAG, extraThreads[index].getThreadName() + " is now closing.");
                    extraThreads[index] = null;
                }
            }
        };
        boolean posted = false;
        for (int i = 0; i < CORE_THREAD_NUMBER; i++) {
            if (coreThreads[i].mIdleFlag) {
                Log.v(TAG, coreThreads[i].getThreadName() + " will be used to close " + extraThreads[index].getThreadName() + ".");
                coreThreads[i].mHandler.post(closingRunnable);
                posted = true;
                break;
            }
        }
        if (!posted) {
            final Runnable runnable = getRunnableFromHead();
            if (null != runnable) {
                extraThreads[index].mHandler.post(runnable);
            } else {
                coreThreads[0].mHandler.post(closingRunnable);
            }
        }
    }

    public void submitToFront(final Runnable runnable) {
        this.mRunnables.add(0, runnable);
        dispatchRunnables();
    }

    public void submitToBack(final Runnable runnable) {
        this.mRunnables.add(runnable);
        dispatchRunnables();
    }

    private void dispatchRunnables() {
        for (int i = 0; i < CORE_THREAD_NUMBER; i++) {
            if (this.coreThreads[i].mIdleFlag) {
                this.postToThread(this.coreThreads[i]);
                return;
            }
        }
        for (int i = 0; i < EXTRA_THREAD_NUMBER; i++) {
            if (null == this.extraThreads[i]) {
                synchronized (this) {
                    if (null == extraThreads[i]) {
                        final Runnable runnable = getRunnableFromHead();
                        if (null != runnable) {
                            extraThreads[i] = new ExtraHandlerThread(i);
                            Log.v(TAG, extraThreads[i].getName() + " has been created.");

                            extraThreads[i].mIdleFlag = false;
                            extraThreads[i].mHandler.post(runnable);
                        }
                        return;
                    }
                }
            }
        }
    }
}