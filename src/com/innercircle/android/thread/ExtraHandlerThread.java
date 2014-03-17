package com.innercircle.android.thread;

import android.os.Looper;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;

class ExtraHandlerThread extends AbstractHandlerThread {
    private static final String TAG = ExtraHandlerThread.class.getSimpleName();

    private static final String NAME_PREFIX = "extra_thread_";
    private static final long TIMEOUT = 2000;

    protected ExtraHandlerThread(final int index) {
        super(NAME_PREFIX + String.valueOf(index));
        mIndex = index;
    }

    @Override
    public String getThreadName(){
        return NAME_PREFIX + String.valueOf(mIndex);
    }

    @Override
    final protected void setMessageQueue() {
        final Runnable runnable = new Runnable(){
            @Override
            public void run() {
                mMessageQueue = Looper.myQueue();
                mMessageQueue.addIdleHandler(new ExtraThreadIdleHandler());
            }
        };
        mHandler.post(runnable);
    }

    final class ExtraThreadIdleHandler implements IdleHandler {
        private ExtraThreadIdleHandler() {}

        @Override
        public boolean queueIdle() {
            Log.v(TAG, "A message has been finished by " + getThreadName());

            final long startTime = System.currentTimeMillis();
            long endTime = System.currentTimeMillis();
            while (endTime - startTime < TIMEOUT) {
                mManager.postToThread(ExtraHandlerThread.this);
                if(!mIdleFlag) {
                    break;
                }
                endTime = System.currentTimeMillis();
            }

            if (mIdleFlag) {
                Log.v(TAG, getThreadName() + " has timeout, ready to be closed.");
                mManager.closeIdleExtraThread(mIndex);
            }
            return true;
        }
    }
}