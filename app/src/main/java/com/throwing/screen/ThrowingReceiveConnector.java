package com.throwing.screen;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

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
                Selector selector = Selector.open();
                channel.register(selector, SelectionKey.OP_READ);
                while (running && selector.select() > 0) {
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
                            Log.e(TAG, NumberUtil.byte2Int(typeBytes) + "");

                            //中间40位是uuid
                            int seqLength = Constant.MSG_SEQ_LENGTH;
                            byte[] seqBytes = new byte[seqLength];
                            buffer.get(seqBytes, 0, seqLength);
                            Log.e(TAG, new String(seqBytes));

                            int contentLength = buffer.limit() - typeLength - seqLength;
                            byte[] bytes = new byte[contentLength];
                            buffer.get(bytes, 0, contentLength);
                            if (onReceiveMsgListener != null) {
                                onReceiveMsgListener.onReceiveMsg(address, bytes);
                            }
                        }
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
