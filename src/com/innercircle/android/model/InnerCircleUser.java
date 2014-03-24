package com.innercircle.android.model;

public class InnerCircleUser implements InnerCircleData {
    private String mId;
    private String mEmail;
    private String mPassword;
    private String mVIPCode;
    private char mGender;
    private String mUsername;

    public void setId(String id) {
        this.mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setEmail(String email) {
        this.mEmail = email;
    }

    public String getEmail() {
        return this.mEmail;
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public void setVIPCode(String VIPCode) {
        this.mVIPCode = VIPCode;
    }

    public String getVIPCode() {
        return this.mVIPCode;
    }

    public void setGender(final char gender) {
        this.mGender = gender;
    }

    public char getGender() {
        return this.mGender;
    }

    public void setUsername(final String username) {
        this.mUsername = username;
    }

    public String getUsername() {
        return this.mUsername;
    }

    private InnerCircleUser(final Builder builder) {
        this.mId = builder.bId;
        this.mEmail = builder.bEmail;
        this.mPassword = builder.bPassword;
        this.mUsername = builder.bUsername;
        this.mGender = builder.bGender;
        this.mVIPCode = builder.bVIPCode;
    }

    public static class Builder{
        private String bId;
        private String bEmail;
        private String bPassword;
        private String bVIPCode;
        private char bGender;
        private String bUsername;

        public InnerCircleUser build(){
            return new InnerCircleUser(this);
        }

        public Builder setId(String id) {
            this.bId = id;
            return this;
        }

        public Builder setEmail(String email) {
            this.bEmail = email;
            return this;
        }

        public Builder setPassword(String password) {
            this.bPassword = password;
            return this;
        }

        public Builder setVIPCode(String VIPCode) {
            this.bVIPCode = VIPCode;
            return this;
        }

        public Builder setGender(final char gender) {
            this.bGender = gender;
            return this;
        }

        public Builder setUsername(final String username) {
            this.bUsername = username;
            return this;
        }
    }
}
