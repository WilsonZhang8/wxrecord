package com.wiwj.wxrecord.domain;

import java.io.Serializable;

/**
 * Created by zghw on 2017/10/30.
 */

public class Contact implements Serializable {
    private String nickName;
    private String userName;
    private String alias;
    private String quanpin;
    private String type;
    private String conRemark;
    private String conRemarkPyFull;
    private String showHead;

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getQuanpin() {
        return quanpin;
    }

    public void setQuanpin(String quanpin) {
        this.quanpin = quanpin;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConRemark() {
        return conRemark;
    }

    public void setConRemark(String conRemark) {
        this.conRemark = conRemark;
    }

    public String getConRemarkPyFull() {
        return conRemarkPyFull;
    }

    public void setConRemarkPyFull(String conRemarkPyFull) {
        this.conRemarkPyFull = conRemarkPyFull;
    }

    public String getShowHead() {
        return showHead;
    }

    public void setShowHead(String showHead) {
        this.showHead = showHead;
    }
}
