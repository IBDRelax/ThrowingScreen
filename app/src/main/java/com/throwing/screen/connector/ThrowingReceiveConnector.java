package com.throwing.screen.connector;

import android.util.Log;

import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.constant.Constant;
import com.throwing.screen.listener.OnReceiveMsgListener;
import com.throwing.screen.util.NumberUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 投屏接收端对象
 */
public class ThrowingReceiveConnector {

    private final static String TAG = ThrowingReceiveConnector.class.getSimpleName();

    private DatagramChannel channel;

    private ByteBuffer buffer = ByteBuffer.allocate(Constant.BUFFER_LENGTH);

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
            channel.configureBlocking(false);
            channel.socket().bind(new InetSocketAddress(Constant.RECEIVER_PORT));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void startListen() {
        running = true;
        new Thread(() -> {
            try {
                final Map<String, StringBuilder> msgContentMap = new HashMap<>();//存储消息的seq和content
                Selector selector = Selector.open();
                channel.register(selector, SelectionKey.OP_READ);
                while (running) {
                    while (selector.select() > 0) {
                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                        while (iterator.hasNext()) {
                            SelectionKey selectionKey = iterator.next();
                            if (selectionKey.isReadable()) {
                                buffer.clear();
                                SocketAddress address = channel.receive(buffer);
                                buffer.flip();

                                //前两位是类型
                                int typeLength = Constant.MSG_TYPE_LENGTH;
                                byte[] typeBytes = new byte[typeLength];
                                buffer.get(typeBytes, 0, typeLength);
                                int type = NumberUtil.byte2Int(typeBytes);
                                Log.e(TAG, type + "");

                                //中间36位是uuid
                                int seqLength = Constant.MSG_SEQ_LENGTH;
                                byte[] seqBytes = new byte[seqLength];
                                buffer.get(seqBytes, 0, seqLength);
                                Log.e(TAG, new String(seqBytes));

                                //后面是内容
                                int contentLength = buffer.limit() - typeLength - seqLength;
                                byte[] bytes = new byte[contentLength];
                                buffer.get(bytes, 0, contentLength);

                                String seq = new String(seqBytes);
                                String content = new String(bytes);
                                if (content.equals("start")) {
                                    msgContentMap.put(seq, new StringBuilder());
                                } else if (content.equals("end")) {
                                    StringBuilder sb = msgContentMap.remove(seq);
                                    if (sb != null) {
                                        ThrowingMsg msg = new ThrowingMsg(type, sb.toString(), null, seq);
                                        if (onReceiveMsgListener != null) {
                                            onReceiveMsgListener.onReceiveMsg(address, msg);
                                        }
                                    }
                                } else {
                                    StringBuilder sb = msgContentMap.get(seq);
                                    if (sb != null) {
                                        sb.append(content);
                                    }
                                }
                            }
                        }
                        iterator.remove();
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }).start();
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

        running = false;

        onReceiveMsgListener = null;
    }

}
