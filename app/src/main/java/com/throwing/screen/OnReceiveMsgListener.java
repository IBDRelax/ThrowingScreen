package com.throwing.screen;

import java.net.SocketAddress;

public interface OnReceiveMsgListener {

    void onReceiveMsg(SocketAddress address, byte[] msg);

}
