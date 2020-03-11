package com.throwing.screen.connector;

import android.util.Log;

import com.throwing.screen.bean.Receiver;
import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.constant.Constant;
import com.throwing.screen.util.NumberUtil;

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
//        Receiver receiver = new Receiver("127.0.0.1", Constant.RECEIVER_PORT);
//        Receiver receiver = new Receiver("255.255.255.255", Constant.RECEIVER_PORT);
        Receiver receiver = new Receiver("192.168.1.24", Constant.RECEIVER_PORT);
        ThrowingMsg msg = new ThrowingMsg(type, "search test", receiver, UUID.randomUUID().toString());
        send(msg);
    }

    public synchronized void send(ThrowingMsg msg) {
        try {
            Log.e(TAG, Thread.currentThread().getId() + "");
            byte[] typeBytes = NumberUtil.int2Bytes(msg.getType());
            byte[] seqBytes = msg.getSeq().getBytes();
            byte[] contentBytes = msg.getContent().getBytes();
            Receiver receiver = msg.getReceiver();

            //先发一个start
            buffer.clear();
            buffer.put(typeBytes);
            buffer.put(seqBytes);
            buffer.put("start".getBytes());
            buffer.flip();
            channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));

            //发送消息内容
            int contentOffset = 0;
            while (contentOffset < contentBytes.length) {
                buffer.clear();
                buffer.put(typeBytes);
                buffer.put(seqBytes);
                int sendContentLength = buffer.remaining() - seqBytes.length - typeBytes.length;
                if(sendContentLength > contentBytes.length - contentOffset){
                    sendContentLength = contentBytes.length - contentOffset;
                }
                buffer.put(contentBytes, contentOffset, sendContentLength);
                contentOffset += sendContentLength;
                buffer.flip();
                channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));
            }

            //最后发一个end
            buffer.clear();
            buffer.put(typeBytes);
            buffer.put(seqBytes);
            buffer.put("end".getBytes());
            buffer.flip();
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
