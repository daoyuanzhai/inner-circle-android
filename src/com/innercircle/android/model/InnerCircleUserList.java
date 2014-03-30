package com.innercircle.android.model;

import java.util.List;

public class InnerCircleUserList implements InnerCircleData {
    private String uid;
    private List<InnerCircleUser> userList;

    public void setUid(String uId) {
        this.uid = uId;
    }

    public String getUid() {
        return uid;
    }

    public void setUserList(List<InnerCircleUser> userList) {
        this.userList = userList;
    }

    public List<InnerCircleUser> getUserList() {
        return this.userList;
    }
}
