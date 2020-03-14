package com.throwing.screen.connector;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.push.component.PushHandler;
import com.push.component.server.PushServer;
import com.throwing.screen.ThrowingScreenApplication;
import com.throwing.screen.bean.ReceiveByteArray;
import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.constant.Constant;
import com.throwing.screen.listener.OnReceiveMsgListener;
import com.throwing.screen.util.NumberUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 投屏接收端对象
 */
public class ThrowingReceiveConnector {

    private final static String TAG = ThrowingReceiveConnector.class.getSimpleName();

    private DatagramChannel channel;

    private ByteBuffer buffer = ByteBuffer.allocate(Constant.BUFFER_LENGTH);

    private boolean running = false;

    private OnReceiveMsgListener onReceiveMsgListener;

    private PushServer pushServer;

    public ThrowingReceiveConnector() {
        init();
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

            pushServer = new PushServer();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void startListen() {
        new Thread(() -> {
            pushServer.setPushHandler(new PushHandler() {
                @Override
                public void onConnect(SocketChannel sc) {
                    Log.e(TAG, "onConnect");
                    Handler handler = new Handler(Looper.myLooper());
                    handler.post(() -> Toast.makeText(ThrowingScreenApplication.getInstance(),
                            "onConnect", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onMessage(SocketChannel sc, byte[] bytes) {
                    Handler handler = new Handler(Looper.myLooper());
                    handler.post(() -> Toast.makeText(ThrowingScreenApplication.getInstance(),
                            "onMessage", Toast.LENGTH_SHORT).show());

                    @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.IMAGE;
                    String msgContent = new String(bytes);
                    ThrowingMsg msg = new ThrowingMsg(type, msgContent, null, null);
                    if (onReceiveMsgListener != null) {
                        onReceiveMsgListener.onReceiveMsg(null, msg);
                    }
                }

                @Override
                public void onDisconnect(SocketChannel sc, boolean isRemote, boolean isAnomalous) {
                    Log.e(TAG, "onDisconnect");
                    Handler handler = new Handler(Looper.myLooper());
                    handler.post(() -> Toast.makeText(ThrowingScreenApplication.getInstance(),
                            "onDisconnect", Toast.LENGTH_SHORT).show());
                }
            });
            pushServer.start(Constant.RECEIVER_PORT);
        }).start();

//        running = true;
//        new Thread(() -> {
//            try {
//                final Map<String, ReceiveByteArray> msgContentMap = new ConcurrentHashMap<>();//存储消息的seq和content
//                Selector selector = Selector.open();
//                channel.register(selector, SelectionKey.OP_READ);
//                while (running) {
//                    while (selector.select() > 0) {
//                        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
//                        while (iterator.hasNext()) {
//                            SelectionKey selectionKey = iterator.next();
//                            if (selectionKey.isReadable()) {
//
//                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                                baos.write(new byte[2], 0, 100);
//
//                                buffer.clear();
//                                SocketAddress address = channel.receive(buffer);
//                                buffer.flip();
//
//                                //前两位是类型
//                                int typeLength = Constant.MSG_TYPE_LENGTH;
//                                byte[] typeBytes = new byte[typeLength];
//                                buffer.get(typeBytes, 0, typeLength);
//                                int type = NumberUtil.byte2Int(typeBytes);
//
//                                //中间36位是uuid
//                                int seqLength = Constant.MSG_SEQ_LENGTH;
//                                byte[] seqBytes = new byte[seqLength];
//                                buffer.get(seqBytes, 0, seqLength);
//
//                                //后面是内容
//                                int contentLength = buffer.remaining();
//                                byte[] bytes = new byte[contentLength];
//                                buffer.get(bytes, 0, contentLength);
//
//                                String seq = new String(seqBytes);
//                                String content = new String(bytes);
//                                Log.e(TAG, seq + "->" + contentLength);
//
//                                if (content.startsWith("start")) {
//                                    int totalLength = Integer.valueOf(content.substring(5));
//                                    if(totalLength > 0) {
//                                        msgContentMap.put(seq, new ReceiveByteArray(new byte[totalLength]));
//                                    }
//                                } else if (content.equals("end")) {
//                                    ReceiveByteArray byteArray = msgContentMap.remove(seq);
//                                    if(byteArray != null) {
//                                        String msgContent = new String(byteArray.getBytes());
//
//                                        Log.e(TAG, type + "");
//                                        Log.e(TAG, type + ":" + seq + "->" + msgContent.length());
//
//                                        if (msgContent != null) {
//                                            ThrowingMsg msg = new ThrowingMsg(type, msgContent, null, seq);
//                                            if (onReceiveMsgListener != null) {
//                                                onReceiveMsgListener.onReceiveMsg(address, msg);
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    ReceiveByteArray byteArray = msgContentMap.get(seq);
//                                    if (byteArray != null) {
//                                        int offset = byteArray.getOffset();
//                                        System.arraycopy(bytes, 0, byteArray.getBytes(), offset, bytes.length);
//                                        offset += bytes.length;
//                                        byteArray.setOffset(offset);
//                                    }
//                                }
//                            }
//                        }
//                        iterator.remove();
//                    }
//                }
//            } catch (IOException e) {
//                Log.e(TAG, e.getMessage(), e);
//            }
//        }).start();
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

        if (pushServer != null) {
            pushServer.close();
        }

        running = false;

        onReceiveMsgListener = null;
    }

}
