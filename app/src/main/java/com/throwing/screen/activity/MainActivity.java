package com.throwing.screen.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.throwing.screen.R;
import com.throwing.screen.bean.Receiver;
import com.throwing.screen.bean.ThrowingMsg;
import com.throwing.screen.connector.ThrowingReceiveConnector;
import com.throwing.screen.connector.ThrowingSendConnector;
import com.throwing.screen.constant.Constant;
import com.throwing.screen.helper.ScreenShotHelper;
import com.throwing.screen.util.CompressUtil;

import java.net.SocketAddress;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final static int REQUEST_MEDIA_PROJECTION = 1000;

    private ThrowingReceiveConnector receiveConnector;

    private ThrowingSendConnector sendConnector;

    private Unbinder unbinder;

    @BindView(R.id.iv_capture)
    ImageView ivCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        init();
    }

    private void init() {
        receiveConnector = new ThrowingReceiveConnector();
        receiveConnector.setOnReceiveMsgListener((SocketAddress address, ThrowingMsg msg) -> {
            switch (msg.getType()) {
                case ThrowingMsg.MsgType.THROW_REQUEST:
                case ThrowingMsg.MsgType.TEXT:
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg.getContent(), Toast.LENGTH_SHORT).show());
                    break;
                case ThrowingMsg.MsgType.IMAGE:
                    Log.e(TAG, msg.getSeq() + "->" + msg.getContent().length());
                    Bitmap bitmap = CompressUtil.base64ToBitmap(msg.getContent());
                    runOnUiThread(() -> ivCapture.setImageBitmap(bitmap));
                    break;
            }
        });
    }

    @OnClick({R.id.btn_search, R.id.btn_capture})
    void click(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                new Thread(() -> {
                    if (sendConnector == null) {
                        sendConnector = new ThrowingSendConnector();
                    }
                    sendConnector.search();
                }).start();
                break;
            case R.id.btn_capture:
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Toast.makeText(getApplicationContext(), "系统版本过低，暂不支持截屏操作！", Toast.LENGTH_SHORT).show();
                    return;
                }
                try2StartScreenShot();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void try2StartScreenShot() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    new Thread(() -> {
                        //截屏
                        ScreenShotHelper screenShotHelper = new ScreenShotHelper(MainActivity.this, resultCode, data,
                                (Bitmap bitmap) -> {
                                    runOnUiThread(() -> ivCapture.setImageBitmap(bitmap));
                                    String base64Str = CompressUtil.bitmapToBase64(bitmap);

                                    if (sendConnector == null) {
                                        sendConnector = new ThrowingSendConnector();
                                    }

                                    @ThrowingMsg.MsgType int type = ThrowingMsg.MsgType.IMAGE;
//        Receiver receiver = new Receiver("127.0.0.1", Constant.RECEIVER_PORT);
//        Receiver receiver = new Receiver("255.255.255.255", Constant.RECEIVER_PORT);
                                    Receiver receiver = new Receiver("192.168.1.24", Constant.RECEIVER_PORT);
                                    ThrowingMsg msg = new ThrowingMsg(type, base64Str, receiver, UUID.randomUUID().toString());
                                    Log.e(TAG, msg.getSeq() + "->" + msg.getContent().length());
                                    sendConnector.send(msg);
                                });
                        screenShotHelper.startScreenShot();
                    }).start();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (receiveConnector != null) {
            receiveConnector.dispose();
        }
        if (sendConnector != null) {
            sendConnector.dispose();
        }

        super.onDestroy();
    }
}
