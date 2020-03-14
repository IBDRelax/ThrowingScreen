package com.throwing.screen;

import android.app.Application;

/**
 * @author relax
 * @date 2020/3/14 2:08 PM
 */
public class ThrowingScreenApplication extends Application {
    private static ThrowingScreenApplication instance;

    public static ThrowingScreenApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
