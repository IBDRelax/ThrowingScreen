package com.throwing.screen;

public class SendMsg {

    private int type = -1;
    private byte[] content;
    private Receiver receiver;

    public SendMsg(int type, byte[] content, Receiver receiver) {
        this.type = type;
        this.content = content;
        this.receiver = receiver;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }
}
