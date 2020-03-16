package com.throwing.screen.connector;

import android.util.Log;

import com.push.component.PushHandler;
import com.push.component.server.PushServer;
import com.throwing.screen.bean.Receiver;
import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.constant.Constant;
import com.throwing.screen.listener.OnReceiveMsgListener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

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
//            channel = DatagramChannel.open();
//            channel.configureBlocking(false);
//            channel.socket().bind(new InetSocketAddress(Constant.FIND_PORT));

            pushServer = new PushServer();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void waitSearch() {
        try {
            MulticastSocket socket = new MulticastSocket(Constant.FIND_PORT);
            socket.setLoopbackMode(true);
//            socket.joinGroup(InetAddress.getByName(Constant.FIND_BROADCAST_IP));
            new Thread(() -> {
                byte[] data = new byte[1024];
                DatagramPacket pack = new DatagramPacket(data, data.length);
                while (true) {
                    // 等待主机的搜索
                    try {
                        socket.receive(pack);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                    byte[] bytes = new byte[pack.getLength()];
                    System.arraycopy(pack.getData(), 0, bytes, 0, bytes.length);
                    String str = new String(bytes);
                    if(str.equals(Constant.SEARCH)){
                        @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.THROW_REQUEST;
                        Receiver receiver = new Receiver(pack.getAddress().getHostAddress(), -1);
                        ThrowingMsg msg = new ThrowingMsg(type, str, receiver, null);
                        if (onReceiveMsgListener != null) {
                            onReceiveMsgListener.onReceiveMsg(null, msg);
                        }
                    }
                    Log.e(TAG, str);
                }
            }).start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void startListen() {
        StringBuffer sb = new StringBuffer();
        new Thread(() -> {
            pushServer.setPushHandler(new PushHandler() {
                @Override
                public void onConnect(SocketChannel sc) {
                    Log.e(TAG, "onConnect");
                }

                @Override
                public void onMessage(SocketChannel sc, byte[] bytes) {
                    @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.IMAGE;
                    String msgContent = new String(bytes);
//                    Log.e(TAG, msgContent);

                    //没有end，继续往sb中缓存
                    if (!msgContent.contains(Constant.MSG_SUFFIX)) {
                        sb.append(msgContent);
                        //有end，截取end前的先结束消息，再把后面的加到后面一个消息（需要注意end在最后面的临界情况，会导致split出的数组长度只有1）
                    } else {
                        String[] subMsgArr = new String[2];//subMsgArr[0]表示前一个消息的内容，subMsgArr[1]是后面一个消息的内容，前面是以end为分界
                        String[] splitStrArr = msgContent.split(Constant.MSG_SUFFIX);
                        if (splitStrArr.length == 1) {//说明end在这次消息的开头或者结尾
                            if (msgContent.startsWith(Constant.MSG_SUFFIX)) {//开头
                                subMsgArr[1] = splitStrArr[0];
                            } else {//结尾
                                subMsgArr[0] = splitStrArr[0];
                            }
                        } else {
                            subMsgArr = splitStrArr;
                        }

                        if (subMsgArr[0] != null) {
                            sb.append(subMsgArr[0]);
                        }

                        //先结束这次消息
                        ThrowingMsg msg = new ThrowingMsg(type, sb.toString(), null, null);
                        if (onReceiveMsgListener != null) {
                            onReceiveMsgListener.onReceiveMsg(null, msg);
                        }

                        //再把后面的内容加到下一个消息buffer中
                        sb.delete(0, sb.length());
                        if (subMsgArr[1] != null) {
                            sb.append(subMsgArr[1]);
                        }

                    }
                }

                @Override
                public void onDisconnect(SocketChannel sc, boolean isRemote, boolean isAnomalous) {
                    Log.e(TAG, "onDisconnect");
                }
            });
            pushServer.start(Constant.PUSH_MSG_PORT);
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

        if (pushServer != null) {
            pushServer.close();
        }

        running = false;

        onReceiveMsgListener = null;
    }

}
