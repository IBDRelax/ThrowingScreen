package com.throwing.screen.connector;

import android.util.Log;

import com.push.component.PushHandler;
import com.push.component.client.PushClient;
import com.push.component.client.PushConfig;
import com.throwing.screen.bean.Receiver;
import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.constant.Constant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
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

    private MulticastSocket hostSocket;

    public ThrowingSendConnector() {
        init();
    }

    private void init() {
        try {
//            channel = DatagramChannel.open();
//            channel.socket().bind(new InetSocketAddress(Constant.FIND_PORT));

            hostSocket = new MulticastSocket();
//            hostSocket.joinGroup(InetAddress.getByName(Constant.FIND_BROADCAST_IP));
            hostSocket.setTimeToLive(1);
            hostSocket.setLoopbackMode(false);
            hostSocket.setNetworkInterface(NetworkInterface.getByName("wlan0"));
            // 设置接收超时时间
            hostSocket.setSoTimeout(Constant.FIND_PORT);

            PushConfig pushConfig = new PushConfig("192.168.1.24", Constant.PUSH_MSG_PORT);
            pushClient = new PushClient(pushConfig);
            pushClient.setPushHandler(new PushHandler() {
                @Override
                public void onConnect(SocketChannel sc) {
                    Log.e(TAG, "onConnect");
                }

                @Override
                public void onMessage(SocketChannel sc, byte[] bytes) {
                    Log.e(TAG, "onMessage" + new String(bytes));
                }

                @Override
                public void onDisconnect(SocketChannel sc, boolean isRemote, boolean isAnomalous) {
                    Log.e(TAG, "onConnect");
                    pushClient.start();
                }
            });
            pushClient.start();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void search() {
        @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.THROW_REQUEST;
//        Receiver receiver = new Receiver("127.0.0.1", Constant.FIND_PORT);
        Receiver receiver = new Receiver(Constant.FIND_BROADCAST_IP, Constant.FIND_PORT);
//        Receiver receiver = new Receiver("192.168.1.24", Constant.FIND_PORT);
//        Receiver receiver = new Receiver("10.180.2.86", Constant.FIND_PORT);
        ThrowingMsg msg = new ThrowingMsg(type, Constant.SEARCH, receiver, UUID.randomUUID().toString());

        try {
            byte[] bytes = msg.getContent().getBytes();
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName(receiver.getIp()), receiver.getPort());
            hostSocket.setReuseAddress(true);
            hostSocket.send(packet);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

//        buffer.clear();
//        buffer.put(msg.getContent().getBytes());
//        buffer.flip();
//        try {
//            channel.send(buffer, new InetSocketAddress(receiver.getIp(), receiver.getPort()));
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage(), e);
//        }
    }

    public synchronized void send(ThrowingMsg msg) {
        try {
            pushClient.sendMsg(msg.getContent());
            pushClient.sendMsg(Constant.MSG_SUFFIX);
            Thread.sleep(100);

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

        if (pushClient != null) {
            pushClient.close();
        }
    }
}
