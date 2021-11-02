package com.thomas.screen;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 显示两种方式DisplayManager MediaRouter
 */
public class ScreenActivity extends AppCompatActivity {

    private CustomPresentation customPresentation;
    private DisplayManager.DisplayListener mDisplayListener;
    // request code for permission
    private final int DISPLAY_PERMISSION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_screen);
        initPermission();
        initDisplayListener();

    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:"+getPackageName()));
                startActivityForResult(intent,DISPLAY_PERMISSION);
            }else {
                handlePresentationDisplay();
            }
        }else {
            handlePresentationDisplay();
        }
    }

    /**
     * <p>一共有三种方式获取副屏，这里我使用第三种方式，多种方式详细见 MainActivity
     * {@link com.thomas.screen.MainActivity}.</p>
     * <p>There are three ways to get the screen, and I'm use the MediaRouter here
     * {@link com.thomas.screen.MainActivity}.</p>
     *
     */
    private void handlePresentationDisplay() {
        MediaRouter mediaRouter = (MediaRouter) getSystemService(MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        Display display = route.getPresentationDisplay();
        System.out.println("presentation display information" + display);
        if (display != null) {
            createDisplay(display.getDisplayId());
        }
    }

    /**
     * 这里创建熄屏时候的监听,当然你也可以使用广播的方式
     */
    private void initDisplayListener() {
        mDisplayListener = new DisplayManager.DisplayListener() {
            /**
             * 客屏屏幕信息发生添加改变会触发一次
             */
            @Override
            public void onDisplayAdded(int displayId) {
                createDisplay(displayId);
            }

            /**
             * 客屏屏幕信息发生添加改变会触发一次
             */
            @Override
            public void onDisplayRemoved(int displayId) {
                if (customPresentation != null){
                    customPresentation.dismiss();
                }
                customPresentation = null;
            }

            /**
             * 这个会触发多次尽量请不要在这里做任何操作,部分机型会改变displayId，部分机型是固定的id数值进行变化
             * @param displayId 屏幕ID
             */
            @Override
            public void onDisplayChanged(int displayId) {
                System.out.println("display changed" + displayId);
            }
        };
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        displayManager.registerDisplayListener(mDisplayListener,null);
    }

    /**
     * 这里创建副屏
     * @param displayId 屏幕ID
     */
    private void createDisplay(int displayId) {
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        Display display = displayManager.getDisplay(displayId);
        if (display != null){
            customPresentation = new CustomPresentation(this,display);
            if (customPresentation.getWindow() != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    customPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                }else {
                    customPresentation.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                }
                customPresentation.show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (customPresentation != null){
            customPresentation.dismiss();
        }
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        if (displayManager != null){
            displayManager.unregisterDisplayListener(mDisplayListener);
        }
    }

    public void changePresentationDataClick(View view) {
        if (customPresentation != null){
            // 如果想要交互或者使用事件，或者使用以下方式交互
            customPresentation.setChange();
        }
    }

    /**
     * activity permission result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISPLAY_PERMISSION){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    handlePresentationDisplay();
                }
            }
        }

    }

}
