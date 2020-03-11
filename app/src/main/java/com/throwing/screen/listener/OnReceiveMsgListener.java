package com.throwing.screen.listener;

import com.throwing.screen.bean.ThrowingMsg;

import java.net.SocketAddress;

public interface OnReceiveMsgListener {

    void onReceiveMsg(SocketAddress address, ThrowingMsg msg);

}
