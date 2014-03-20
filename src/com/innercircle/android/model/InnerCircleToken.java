package com.innercircle.android.model;

public class InnerCircleToken implements InnerCircleData {
    private String mUid;
    private String mAccessToken;
    private String mRefreshToken;
    private char mGender;
    private long mTimestamp;

    public void setUid(final String uid) {
        this.mUid = uid;
    }

    public String getUid() {
        return this.mUid;
    }

    public void setAccessToken(final String accessToken) {
        this.mAccessToken = accessToken;
    }

    public String getAccessToken() {
        return this.mAccessToken;
    }

    public void setRefreshToken(final String refreshToken) {
        this.mRefreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return this.mRefreshToken;
    }

    public void setGender(final char gender) {
        this.mGender = gender;
    }

    public char getGender() {
        return this.mGender;
    }

    public void setTimestamp(final long timestamp) {
        this.mTimestamp = timestamp;
    }

    public long getTimestamp() {
        return this.mTimestamp;
    }

    private InnerCircleToken(final Builder builder) {
        this.mUid = builder.bUid;
        this.mAccessToken = builder.bAccessToken;
        this.mRefreshToken = builder.bRefreshToken;
        this.mGender = builder.bGender;
        this.mTimestamp = builder.bTimestamp;
    }

    public static class Builder{
        private String bUid;
        private String bAccessToken;
        private String bRefreshToken;
        private char bGender;
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

        public Builder setGender(final char gender) {
            this.bGender = gender;
            return this;
        }

        public Builder setTimestamp(final long timestamp) {
            this.bTimestamp = timestamp;
            return this;
        }
    }
}

