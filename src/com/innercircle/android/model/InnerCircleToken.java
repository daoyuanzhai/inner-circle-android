package com.innercircle.android.model;

public class InnerCircleToken implements InnerCircleData {
    private String mUid;
    private String mAccessToken;
    private String mRefreshToken;
    private long mTimestamp;

    public String getUid() {
        return this.mUid;
    }

    public String getAccessToken() {
        return this.mAccessToken;
    }

    public String getRefreshToken() {
        return this.mRefreshToken;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    private InnerCircleToken(final Builder builder) {
        this.mUid = builder.bUid;
        this.mAccessToken = builder.bAccessToken;
        this.mRefreshToken = builder.bRefreshToken;
        this.mTimestamp = builder.bTimestamp;
    }

    public static class Builder{
        private String bUid;
        private String bAccessToken;
        private String bRefreshToken;
        private long bTimestamp;

        public InnerCircleToken build() {
            return new InnerCircleToken(this);
        }

        public Builder setUid(final String uid) {
            this.bUid = uid;
            return this;
        }

        public Builder setAccessToken(final String accessToken) {
            this.bAccessToken = accessToken;
            return this;
        }

        public Builder setRefreshToken(final String refreshToken) {
            this.bRefreshToken = refreshToken;
            return this;
        }

        public Builder setTimestamp(final long timestamp) {
            this.bTimestamp = timestamp;
            return this;
        }
    }
}

