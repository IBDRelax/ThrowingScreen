package com.throwing.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.net.SocketAddress;

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
        receiveConnector.setOnReceiveMsgListener((SocketAddress address, byte[] msg) -> Log.e(TAG, new String(msg)));
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
                try2StartScreenShot();
                break;
        }
    }

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
                        ScreenShotHelper screenShotHelper = new ScreenShotHelper(MainActivity.this, resultCode, data, new ScreenShotHelper.OnScreenShotListener() {
                            @Override
                            public void onFinish(Bitmap bitmap) {
                                ivCapture.setImageBitmap(bitmap);
                            }
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
