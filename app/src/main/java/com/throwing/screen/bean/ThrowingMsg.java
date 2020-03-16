package com.throwing.screen.bean;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ThrowingMsg {

    private @MsgType int type = MsgType.NONE;
    private String content;
    private Receiver receiver;

    private String seq;//消息内容唯一标示，针对于分段消息（如图片需要分消息处理）每一条都是统一值

    public ThrowingMsg(int type, String content, Receiver receiver) {
        this.type = type;
        this.content = content;
        this.receiver = receiver;
    }

    public ThrowingMsg(int type, String content, Receiver receiver, String seq) {
        this.type = type;
        this.content = content;
        this.receiver = receiver;
        this.seq = seq;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    @IntDef({MsgType.NONE, MsgType.THROW_REQUEST, MsgType.THROW_AGREE, MsgType.TEXT, MsgType.IMAGE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MsgType {
        int NONE = -1;//默认值，无意义
        int THROW_REQUEST = 0;//投屏请求
        int THROW_AGREE = 1;//同意投屏
        int TEXT = 2;//文本
        int IMAGE = 3;//图片
    }

}
