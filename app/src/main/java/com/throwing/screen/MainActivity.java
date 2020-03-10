package com.throwing.screen;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.net.SocketAddress;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ThrowingReceiveConnector receiveConnector;

    private ThrowingSendConnector sendConnector;

    private Unbinder unbinder;

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
//        Button btnSearch = findViewById(R.id.btn_search);
//        btnSearch.setOnClickListener((View v) ->
//                new Thread(() -> {
//                        if(sendConnector == null) {
//                            sendConnector = new ThrowingSendConnector();
//                        }
//                        sendConnector.search();
//                    }).start()
//        );
    }

    @OnClick(R.id.btn_search)
    void click(View v) {
        new Thread(() -> {
            if (sendConnector == null) {
                sendConnector = new ThrowingSendConnector();
            }
            sendConnector.search();
        }).start();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) {
            unbinder.unbind();
        }
        if (receiveConnector == null) {
            receiveConnector.dispose();
        }
        if (sendConnector == null) {
            sendConnector.dispose();
        }

        super.onDestroy();
    }
}
