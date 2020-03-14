package com.push.component;

import java.nio.channels.SocketChannel;

/**
 * push回调
 *
 * @author relax
 * @date 2020/3/14 12:53 PM
 */
public interface PushHandler {

    void onConnect(SocketChannel sc);

    void onMessage(SocketChannel sc, byte[]bytes);

    void onDisconnect(SocketChannel sc, boolean isRemote, boolean isAnomalous);

}
