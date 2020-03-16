package com.push.component.client;

/**
 * push参数配置项
 *
 * @author relax
 * @date 2020/3/14 12:58 PM
 */
public class PushConfig {

    private String ip;
    private int port = -1;
    private int connectTimeOut = 60 * 1000;
    private long soTimeout = 120 * 1000;

    public PushConfig(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public PushConfig(String ip, int port, int connectTimeOut, long soTimeout) {
        this.ip = ip;
        this.port = port;
        if(connectTimeOut >0) {
            this.connectTimeOut = connectTimeOut;
        }
        if(soTimeout > 0) {
            this.soTimeout = soTimeout;
        }
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

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public long getSoTimeout() {
        return soTimeout;
    }

    public void setSoTimeout(long soTimeout) {
        this.soTimeout = soTimeout;
    }

}
