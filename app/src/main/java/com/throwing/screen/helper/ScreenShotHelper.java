package com.throwing.screen.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.throwing.screen.listener.OnScreenShotListener;

import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;

/**
 * 截屏工具
 *
 * @author relax
 * @date 2020/3/11 1:53 PM
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ScreenShotHelper {

    private final static String TAG = ScreenShotHelper.class.getSimpleName();

    private OnScreenShotListener mOnScreenShotListener;

    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private final SoftReference<Context> mRefContext;

//    ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(1);

    public ScreenShotHelper(Context context, int resultCode, Intent data, OnScreenShotListener onScreenShotListener) {
        this.mOnScreenShotListener = onScreenShotListener;
        this.mRefContext = new SoftReference<>(context);

        mMediaProjection = getMediaProjectionManager().getMediaProjection(resultCode, data);
        mImageReader = ImageReader.newInstance(getScreenWidth(), getScreenHeight(), PixelFormat.RGBA_8888, 1);
    }

    public void startScreenShot() {
//        threadPool.scheduleWithFixedDelay(runnable, 500, 500, TimeUnit.MILLISECONDS);

        new Thread(runnable).start();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mImageReader = ImageReader.newInstance(getScreenWidth(), getScreenHeight(), PixelFormat.RGBA_8888, 1);
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.e(TAG, "");

                    Image image = mImageReader.acquireNextImage();
                    if (image != null) {
                        int width = image.getWidth();
                        int height = image.getHeight();
                        final Image.Plane[] planes = image.getPlanes();
                        final ByteBuffer buffer = planes[0].getBuffer();

                        int pixelStride = planes[0].getPixelStride();

                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * width;
                        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);
                        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                        image.close();

                        if (mOnScreenShotListener != null) {
                            mOnScreenShotListener.onFinish(bitmap);
                        }
                    }

                }
            }, getBackgroundHandler());

            createVirtualDisplay();

//            Image image = mImageReader.acquireLatestImage();
//            int width = image.getWidth();
//            int height = image.getHeight();
//            final Image.Plane[] planes = image.getPlanes();
//            final ByteBuffer buffer = planes[0].getBuffer();
//
//            int pixelStride = planes[0].getPixelStride();
//
//            int rowStride = planes[0].getRowStride();
//            int rowPadding = rowStride - pixelStride * width;
//            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
//            bitmap.copyPixelsFromBuffer(buffer);
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
//            image.close();
//
//            if (mOnScreenShotListener != null) {
//                mOnScreenShotListener.onFinish(bitmap);
//            }
        }
    };

    private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getContext().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
    }

    private void createVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "screen-mirror",
                getScreenWidth(),
                getScreenHeight(),
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(),
                null,
                null
        );
    }

    private Context getContext() {
        return mRefContext.get();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public void dispose() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mImageReader != null) {
            mImageReader.close();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
    }

    //在后台线程里保存文件
    Handler backgroundHandler;

    private Handler getBackgroundHandler() {
        if (backgroundHandler == null) {
            HandlerThread backgroundThread =
                    new HandlerThread("catwindow", android.os.Process
                            .THREAD_PRIORITY_BACKGROUND);
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }
        return backgroundHandler;
    }

}
