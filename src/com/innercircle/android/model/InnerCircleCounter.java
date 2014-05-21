package com.innercircle.android.model;

public class InnerCircleCounter implements InnerCircleData {
	private String mUid;
    private String mReceiverUid;
    private int mCount;

    public void setUid(final String uid) {
        this.mUid = uid;
    }

    public String getUid() {
        return this.mUid;
    }

    public void setReceiverUid(final String mReceiverUid) {
        this.mReceiverUid = mReceiverUid;
    }

    public String getmReceiverUid() {
        return this.mReceiverUid;
    }

    public void setCount(final int mCount) {
        this.mCount = mCount;
    }

    public int getCount() {
        return this.mCount;
    }

    private InnerCircleCounter(final Builder builder) {
        this.mUid = builder.bUid;
        this.mReceiverUid = builder.bReceiverUid;
        this.mCount = builder.bCount;
    }

    public static class Builder{
        private String bUid;
        private String bReceiverUid;
        private int bCount;

        public InnerCircleCounter build() {
            return new InnerCircleCounter(this);
        }

        public Builder setUid(final String uid) {
            this.bUid = uid;
            return this;
        }

        public Builder setReceiverUid(final String receiverUid) {
            this.bReceiverUid = receiverUid;
            return this;
        }

        public Builder setCount(final int count) {
            this.bCount = count;
            return this;
        }
    }
}
