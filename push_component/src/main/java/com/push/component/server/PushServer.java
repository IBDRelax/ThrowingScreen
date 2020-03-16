package com.push.component.server;

import android.util.Log;

import com.push.component.PushHandler;
import com.push.component.constant.PushConstant;
import com.push.component.util.TimeUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * push服务端
 *
 * @author relax
 * @date 2020/3/14 12:54 PM
 */
public class PushServer {

    private final static String TAG = PushServer.class.getSimpleName();

    private ServerSocketChannel server;

    private PushHandler pushHandler = null;
    private Selector selector = null;
    private ExecutorService threadPool = null;

    private boolean running = false;

    public void setPushHandler(PushHandler pushHandler) {
        this.pushHandler = pushHandler;
    }

    public void start(int port) {
        threadPool = Executors.newFixedThreadPool(1);
        initSelector();// 初始化selector
        initServerSocketChannel(port); // 初始化serverSocketChannel
        run();
    }

    // first
    private void initSelector() {
        try {
            selector = Selector.open();// 打开selector
        } catch (IOException e) {
            // 初始化selector失败
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void initServerSocketChannel(int port) {
        try {
            server = ServerSocketChannel.open();
            server.socket().bind(new InetSocketAddress(port));
            server.socket().setSoTimeout(5 * 1000);
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            // 初始化serverSocket失败
            Log.e(TAG, e.getMessage(), e);
        }
    }

    private void run() {
        running = true;
        while (running) {
            try {
                selector.select(); // 阻塞selector
                // ================如果有新连接
                Set<SelectionKey> selectedKeys = selector.selectedKeys();// 获得事件集合;
                // ================遍历selectedKeys
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();// 获得到当前的事件
                    // ===============处理事件
                    handle(key);
                    // ===============
                    iterator.remove(); // 移除事件
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    // 初始化seclector和serverSocket
    // 当一个selector上有新的事件有反应后 select();
    // 获得获得事件集合
    // 遍历集合事件
    // 处理事件
    public void handle(SelectionKey key) {
        try {
            // 连接就绪
            if (key.isAcceptable()) {
                handleAcceptable(key);
            }
            // 读就绪
            if (key.isReadable()) {
                handleReadable(key);
            }
        } catch (IOException e) {
            key.cancel();
            if (key.channel() != null) {
                try {
                    key.channel().close();
                } catch (IOException e1) {
                }
            }
        }
    }

    // 处理读事件
    public void handleReadable(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel(); // TODO:
        byte[] bytes = readBytes(sc);
        if (bytes != null) {
            if (pushHandler != null) {
                threadPool.submit(() -> pushHandler.onMessage(sc, bytes));
            }
            //发送收到消息的回执
            sendMsg(sc, "msg received!");
        } else {//数据为空，说明远程连接已经断开
            sc.close();
            if (pushHandler != null) {
                threadPool.submit(() -> pushHandler.onDisconnect(sc, true, true));
            }
        }
    }

    private byte[] readBytes(SocketChannel sc) throws IOException {
        // ==================我们要将数据从通道读到buffer里
        ByteBuffer byteBuffer = ByteBuffer.allocate(PushConstant.MSG_BUFFER_SIZE);
        int readBytes = sc.read(byteBuffer);// channel ==> buffer
        if (readBytes > 0) {// 代表读完毕了,准备写(即打印出来)
            byteBuffer.flip(); // 为write()准备
            // =====取出buffer里的数据
            byte[] bytes = new byte[byteBuffer.remaining()]; // 创建字节数组
            byteBuffer.get(bytes);// 将数据取出放到字节数组里

            return bytes;
        } else {
            return null;
        }
    }

    // 处理连接事件
    public void handleAcceptable(SelectionKey key) throws IOException {
        // 获得对应的ServerSocketChannel TODO: 这里为什么是socketChannel
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        // 得到对应的SocketChannel TODO:accpet是什么意思
        SocketChannel channel = ssc.accept();// 在非阻塞模式下，accept()可能为null
        if (pushHandler != null) {
            threadPool.submit(() -> pushHandler.onConnect(channel));
        }
        // 处理socketChannel
        channel.configureBlocking(false); // TODO: 为什么设置非阻塞
        channel.register(selector, SelectionKey.OP_READ); // TODO: 将准备状态转化为读状态

        // 将key对应Channel设置为准备接受其他请求
        key.interestOps(SelectionKey.OP_ACCEPT);// TODO:
    }

    // ============= 发送消息
    public void sendMsg(SocketChannel sc, String data) throws IOException {
        byte[] req = data.getBytes();
        ByteBuffer byteBuffer = ByteBuffer.allocate(req.length);
        byteBuffer.put(req);
        byteBuffer.flip();
        sc.write(byteBuffer);
        if (!byteBuffer.hasRemaining()) {
            System.out.println(TimeUtil.getTimeStr() + data + "   Send 2 Client successed");
        }
    }

    public void close() {
        running = false;

        try {
            if (server != null) {
                server.close();
            }
            if (selector != null) {
                selector.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }
    }

}
