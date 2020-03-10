package com.throwing.screen;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * 投屏接收端对象
 */
public class ThrowingReceiveConnector {

    private final static String TAG = ThrowingReceiveConnector.class.getSimpleName();

    private DatagramChannel channel;

    private ByteBuffer buffer = ByteBuffer.allocate(4096);

    private boolean running = false;

    private OnReceiveMsgListener onReceiveMsgListener;

    public ThrowingReceiveConnector() {
        init();
        startListen();
    }

    public OnReceiveMsgListener getOnReceiveMsgListener() {
        return onReceiveMsgListener;
    }

    public void setOnReceiveMsgListener(OnReceiveMsgListener onReceiveMsgListener) {
        this.onReceiveMsgListener = onReceiveMsgListener;
    }

    private void init() {
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(Constant.RECEIVER_PORT));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void startListen() {
        running = true;
        new Thread(() -> {
                while (running) {
                    try {
                        buffer.clear();
                        SocketAddress address = channel.receive(buffer);
                        buffer.flip();
                        byte[] bytes = new byte[buffer.limit()];
                        buffer.get(bytes);
                        if(onReceiveMsgListener != null){
                            onReceiveMsgListener.onReceiveMsg(address, bytes);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
        }).start();
    }

    public void dispose() {
        if (channel != null ) {
            try {
                channel.close();
                channel = null;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        if(buffer != null) {
            buffer.clear();
            buffer = null;
        }

        running = false;

        onReceiveMsgListener = null;
    }

}
