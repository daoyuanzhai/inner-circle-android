package com.innercircle.android.thread;

import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;

class CoreHandlerThread extends AbstractHandlerThread {
    private static final String TAG = CoreHandlerThread.class.getSimpleName();

    private static final String NAME_PREFIX = "core_thread_";

    protected CoreHandlerThread(final int index) {
        super(NAME_PREFIX + String.valueOf(index));
        mIndex = index;
    }

    @Override
    protected String getThreadName(){
        return NAME_PREFIX + String.valueOf(mIndex);
    }

    @Override
    final protected void setMessageQueue() {
        final Runnable runnable = new Runnable(){
            @Override
            public void run() {
                mMessageQueue = Looper.myQueue();
                mMessageQueue.addIdleHandler(new CoreThreadIdleHandler());
            }
        };
        mHandler.post(runnable);
    }

    final class CoreThreadIdleHandler implements IdleHandler {
        private CoreThreadIdleHandler() {}

        @Override
        public boolean queueIdle() {
            Log.v(TAG, "A message has been finished by " + getThreadName());
            mManager.postToThread(CoreHandlerThread.this);
            return true;
        }
    }
}