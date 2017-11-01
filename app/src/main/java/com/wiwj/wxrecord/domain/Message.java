package com.wiwj.wxrecord.domain;

import java.io.Serializable;

/**
 * Created by jh on 2017/10/30.
 */

public class Message implements Serializable {
    private String msgId;
    private String content;
    private String createTime;
    private String msgSeq;
    private String talker;
    private Contact contact;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getMsgSeq() {
        return msgSeq;
    }

    public void setMsgSeq(String msgSeq) {
        this.msgSeq = msgSeq;
    }

    public String getTalker() {
        return talker;
    }

    public void setTalker(String talker) {
        this.talker = talker;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }
}
