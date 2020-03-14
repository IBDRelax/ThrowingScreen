package com.throwing.screen.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.throwing.screen.R;
import com.throwing.screen.activity.MainActivity;
import com.throwing.screen.bean.Receiver;
import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.connector.ThrowingSendConnector;
import com.throwing.screen.constant.Constant;
import com.throwing.screen.helper.ScreenShotHelper;
import com.throwing.screen.util.CompressUtil;

import java.util.UUID;

public class ThrowingScreenService extends Service {

    private final static String TAG = ThrowingScreenService.class.getSimpleName();

    private ThrowingSendConnector sendConnector;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        int resultCode = intent.getIntExtra("code", -1);
        Intent resultData = intent.getParcelableExtra("data");

        //截屏
        ScreenShotHelper screenShotHelper = new ScreenShotHelper(this, resultCode, resultData,
                (Bitmap bitmap) -> {
                    //图像给到activity
//                    runOnUiThread(() -> ivCapture.setImageBitmap(bitmap));

                    String base64Str = CompressUtil.bitmapToBase64(bitmap);
                    if (sendConnector == null) {
                        sendConnector = new ThrowingSendConnector();
                    }

                    @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.IMAGE;
//        Receiver receiver = new Receiver("127.0.0.1", PushConstant.PUSH_MSG_PORT);
//        Receiver receiver = new Receiver("255.255.255.255", PushConstant.PUSH_MSG_PORT);
                    Receiver receiver = new Receiver("192.168.1.24", Constant.PUSH_MSG_PORT);
                    ThrowingMsg msg = new ThrowingMsg(type, base64Str, receiver, UUID.randomUUID().toString());
                    Log.e(TAG, msg.getSeq() + "->" + msg.getContent().length());
                    sendConnector.send(msg);
                });
        screenShotHelper.startScreenShot();

        return super.onStartCommand(intent, flags, startId);
    }

    private void createNotificationChannel() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class); //点击后跳转的界面，可以设置跳转数据

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                //.setContentTitle("SMI InstantView") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
                .setContentText("is running......") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        /*以下是对Android 8.0的适配*/
        //普通notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        //前台服务notification适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
