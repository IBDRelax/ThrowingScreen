package com.throwing.screen.bean;

/**
 * 接收方信息
 */
public class Receiver {

    private String ip;//接收方ip
    private int port;//接收方port

    public Receiver(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
