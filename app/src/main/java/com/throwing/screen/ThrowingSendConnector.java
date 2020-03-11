package com.throwing.screen;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.UUID;

/**
 * 投屏发起端对象
 */
public class ThrowingSendConnector {

    private final static String TAG = ThrowingSendConnector.class.getSimpleName();

    private DatagramChannel channel;

    private ByteBuffer buffer = ByteBuffer.allocate(Constant.BUFFER_LENGTH);

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

    public void search() {
        @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.THROW_REQUEST;
        byte[] content = "search test".getBytes();
        Receiver receiver = new Receiver("127.0.0.1", Constant.RECEIVER_PORT);
//        Receiver receiver = new Receiver("255.255.255.255", Constant.RECEIVER_PORT);
        ThrowingMsg msg = new ThrowingMsg(type, content, receiver);
        send(msg);
    }

    public void send(ThrowingMsg msg) {
        try {
            buffer.clear();
            byte[] bytes = NumberUtil.int2Bytes(msg.getType());
            buffer.put(bytes);
            byte[] seqBytes = UUID.randomUUID().toString().getBytes();
            buffer.put(seqBytes);
            buffer.put(msg.getContent());
            buffer.flip();
            Receiver receiver = msg.getReceiver();
            channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void dispose() {
        if (channel != null) {
            try {
                channel.close();
                channel = null;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }
    }
}
