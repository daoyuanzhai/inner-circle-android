package com.innercircle.android.model;

public class InnerCircleResponse {
    public enum Status {
        SUCCESS,
        FAILED,
        EMAIL_EXISTS_ERROR,
        EMAIL_PASSWORD_MISMATCH,
        TOKEN_EXPIRE_ERROR,
        TOKEN_MISMATCH,
    }

    private Status status;
    private InnerCircleData data;

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setData(final InnerCircleData data) {
        this.data = data;
    }

    public InnerCircleData getData() {
        return this.data;
    }
}
