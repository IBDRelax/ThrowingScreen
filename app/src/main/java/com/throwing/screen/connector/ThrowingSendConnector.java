package com.throwing.screen.connector;

import android.util.Log;

import com.push.component.PushHandler;
import com.push.component.client.PushClient;
import com.push.component.client.PushConfig;
import com.throwing.screen.bean.Receiver;
import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.constant.Constant;
import com.throwing.screen.util.NumberUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * 投屏发起端对象
 */
public class ThrowingSendConnector {

    private final static String TAG = ThrowingSendConnector.class.getSimpleName();

    private DatagramChannel channel;

    private ByteBuffer buffer = ByteBuffer.allocate(Constant.BUFFER_LENGTH);

    private PushClient pushClient;

    public ThrowingSendConnector() {
        init();
    }

    private void init() {
        try {
//            channel = DatagramChannel.open();
//            channel.socket().bind(new InetSocketAddress(Constant.SENDER_PORT));

            PushConfig pushConfig = new PushConfig("192.168.1.24", Constant.RECEIVER_PORT);
            pushClient = new PushClient(pushConfig);
            pushClient.setPushHandler(new PushHandler() {
                @Override
                public void onConnect(SocketChannel sc) {

                }

                @Override
                public void onMessage(SocketChannel sc, byte[] bytes) {

                }

                @Override
                public void onDisconnect(SocketChannel sc, boolean isRemote, boolean isAnomalous) {

                }
            });
            pushClient.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void search() {
        @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.THROW_REQUEST;
//        Receiver receiver = new Receiver("127.0.0.1", Constant.RECEIVER_PORT);
//        Receiver receiver = new Receiver("255.255.255.255", Constant.RECEIVER_PORT);
//        Receiver receiver = new Receiver("192.168.1.24", Constant.RECEIVER_PORT);
        Receiver receiver = new Receiver("10.180.2.86", Constant.RECEIVER_PORT);
        ThrowingMsg msg = new ThrowingMsg(type, "search test", receiver, UUID.randomUUID().toString());
        send(msg);
    }

    public synchronized void send(ThrowingMsg msg) {
        try {
            pushClient.sendMsg("呵呵呵");

//            Log.e(TAG, Thread.currentThread().getId() + "");
//            byte[] typeBytes = NumberUtil.int2Bytes(msg.getType());
//            byte[] seqBytes = msg.getSeq().getBytes();
//            byte[] contentBytes = msg.getContent().getBytes();
//            Receiver receiver = msg.getReceiver();
//
//            //先发一个start
//            buffer.clear();
//            buffer.put(typeBytes);
//            buffer.put(seqBytes);
//            buffer.put(("start" + contentBytes.length).getBytes());
//            buffer.flip();
//            channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));
//
//            //发送消息内容
//            int contentOffset = 0;//内容写流偏移值
//            int contentTotalLength = contentBytes.length;//内容总长度
//            Log.e(TAG, msg.getSeq() + "->" + contentTotalLength);
//            int sendCount = 0;
//            while (contentOffset < contentTotalLength) {
//                sendCount++;
//                buffer.clear();
//                buffer.put(typeBytes);
//                buffer.put(seqBytes);
//                int sendContentLength = buffer.capacity() - seqBytes.length - typeBytes.length;
//                int contentLeaveLength = contentTotalLength - contentOffset;
//                if(sendContentLength > contentLeaveLength){
//                    sendContentLength = contentLeaveLength;
//                }
//                buffer.put(contentBytes, contentOffset, sendContentLength);
//                Log.e(TAG, msg.getSeq() + "->" + sendContentLength);
//                contentOffset += sendContentLength;
//                buffer.flip();
//                channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));
//            }
//            Log.e(TAG, msg.getSeq() + ",sendCount->" + sendCount);
//
//            //最后发一个end
//            buffer.clear();
//            buffer.put(typeBytes);
//            buffer.put(seqBytes);
//            buffer.put("end".getBytes());
//            buffer.flip();
//            channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));
        } catch (Exception e) {
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

        if(pushClient != null){
            pushClient.close();
        }
    }
}
