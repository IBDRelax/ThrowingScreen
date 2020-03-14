package com.throwing.screen.bean;

/**
 * description
 *
 * @author relax
 * @date 2020/3/12 12:07 PM
 */
public class ReceiveByteArray {

    private int offset = 0;
    private byte[] bytes;

    public ReceiveByteArray(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte[] getBytes() {
        return bytes;
    }

}
