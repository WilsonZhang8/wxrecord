package com.wiwj.wxrecord.domain;

import java.io.Serializable;

/**
 * Created by jh on 2017/11/3.
 */

public class UserInfo implements Serializable {
    private String userId;
    private String nickName;
    private String phone;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
