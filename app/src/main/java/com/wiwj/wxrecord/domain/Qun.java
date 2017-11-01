package com.wiwj.wxrecord.domain;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zghw on 2017/10/30.
 */

public class Qun implements Serializable {
    private String qunName;
    private String qunPin;
    private String qunId;
    private String qunOwner;
    private String qunListName;
    private String qunListId;
    private List<Message> messageList;

    public String getQunName() {
        return qunName;
    }

    public void setQunName(String qunName) {
        this.qunName = qunName;
    }

    public String getQunPin() {
        return qunPin;
    }

    public void setQunPin(String qunPin) {
        this.qunPin = qunPin;
    }

    public String getQunId() {
        return qunId;
    }

    public void setQunId(String qunId) {
        this.qunId = qunId;
    }

    public String getQunOwner() {
        return qunOwner;
    }

    public void setQunOwner(String qunOwner) {
        this.qunOwner = qunOwner;
    }

    public String getQunListName() {
        return qunListName;
    }

    public void setQunListName(String qunListName) {
        this.qunListName = qunListName;
    }

    public String getQunListId() {
        return qunListId;
    }

    public void setQunListId(String qunListId) {
        this.qunListId = qunListId;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public String toString() {
        return "qunName=" + qunName + " , " + "qunPin=" + qunPin + " , " + "qunId=" + qunId + " , " + "qunOwner=" + qunOwner + " , " + "qunListName=" + qunListName + " , " + "qunListId=" + qunListId + " , ";
    }
}
