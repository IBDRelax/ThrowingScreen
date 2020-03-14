package com.push.component.client;

import android.util.Log;

import com.push.component.PushHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * push客户端
 *
 * @author relax
 * @date 2020/3/14 12:58 PM
 */
public class PushClient implements Runnable {

    private final static String TAG = PushClient.class.getSimpleName();

    private PushConfig config;
    private PushHandler pushHandler = null;
    //信道选择器
    private Selector selector;
    //与服务器通信的信道
    private SocketChannel socketChannel;
    private ExecutorService threadPool = null;
    private Future future = null;

    public void setPushHandler(PushHandler pushHandler) {
        this.pushHandler = pushHandler;
    }

    public PushClient(PushConfig config) {
        this.config = config;
        threadPool = Executors.newFixedThreadPool(4);
    }

    public void start() throws IOException {
        checkConfigValid();
        //打开监听信道并设置为非阻塞模式
        socketChannel = SocketChannel.open();
        socketChannel.socket().connect(new InetSocketAddress(config.getIp(), config.getPort()), config.getConnectTimeOut());
        if (pushHandler != null) {
            doTask(() -> pushHandler.onConnect(socketChannel));
        }
        socketChannel.configureBlocking(false);
        //打开并注册选择器到信道
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_READ);//SelectionKey.OP_READ表示读就绪事件

        future = doTask(this);
    }

    private void checkConfigValid() throws RuntimeException {
        if (config == null) {
            throw new RuntimeException("PushConfig must not be null!");
        }

        if (config.getIp() == null || config.getIp().isEmpty()) {
            throw new RuntimeException("PushConfig.ip must not be null!");
        }

        if (config.getPort() <= 0) {
            throw new RuntimeException("PushConfig.port must >= 0!");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select(config.getSoTimeout()); // 阻塞selector
                // ================如果有新连接
                Set<SelectionKey> selectedKeys = selector.selectedKeys();// 获得事件集合;
                // ================遍历selectedKeys
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                boolean hasReadKey = false;
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();// 获得到当前的事件
                    if (key.isReadable()) {
                        hasReadKey = true;
                        // ===============处理事件
                        handelReadable(key);
                    }
                    // ===============
                    iterator.remove(); // 移除事件
                }

                if (!hasReadKey) {
                    readTimeOut();//读取超时
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 处理读事件
    private void handelReadable(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel(); // TODO:
        byte[] bytes = readBytes(sc);
        if (bytes != null) {
            if (pushHandler != null) {
                doTask(() -> pushHandler.onMessage(sc, bytes));
            }
        } else {
            key.cancel();
            closeConnect();
            if (pushHandler != null) {
                doTask(() -> pushHandler.onDisconnect(sc, true, true));
            }
        }
    }

    private byte[] readBytes(SocketChannel sc) throws IOException {
        // ==================我们要将数据从通道读到buffer里
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
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

    private void readTimeOut() {
        System.out.println("读取超时！");
        closeConnect();
        if (pushHandler != null) {
            doTask(() -> pushHandler.onDisconnect(socketChannel, false, true));
        }
    }

    private void closeConnect() {
        try {
            if (socketChannel.isConnected()) {
                socketChannel.close();
            }

            if (future != null && !future.isCancelled() && !future.isDone()) {
                future.cancel(true);
            }

            if (selector.isOpen()) {
                selector.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void destroy() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }

    private Future doTask(Runnable runnable) {
        if (threadPool != null && !threadPool.isShutdown()) {
            return threadPool.submit(runnable);
        }

        return null;
    }

    /**
     * 发送字符串到服务器
     */
    public void sendMsg(String message) {
        if (socketChannel != null && socketChannel.isConnected()) {
            doTask(() -> {
                try {
                    ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
                    int sendCount = socketChannel.write(writeBuffer);
                    Log.e(TAG, sendCount + "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void close(){
        closeConnect();
        destroy();
    }

}
