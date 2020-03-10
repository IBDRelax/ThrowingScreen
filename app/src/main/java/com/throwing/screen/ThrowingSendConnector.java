package com.throwing.screen;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * 投屏发起端对象
 */
public class ThrowingSendConnector {

    private final static String TAG = ThrowingSendConnector.class.getSimpleName();

    private DatagramChannel channel;

    private ByteBuffer buffer = ByteBuffer.allocate(4096);

    public ThrowingSendConnector() {
        init();
    }

    private void init() {
        try {
            channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(Constant.SENDER_PORT));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void search(){
        try {
            buffer.clear();
            buffer.put("search test".getBytes());
            buffer.flip();
            channel.send(buffer, new InetSocketAddress("127.0.0.1", Constant.RECEIVER_PORT));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void send(SendMsg msg) {
        try {
            buffer.clear();
            buffer.put(msg.getContent());
            buffer.flip();
            Receiver receiver = msg.getReceiver();
            channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
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
    }
}
