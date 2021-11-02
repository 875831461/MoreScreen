
package com.thomas.screen;

import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


/**
 * 不建议在onResume中处理客屏，延时操作虽然可行，但是无法灭屏再亮屏马上显示客屏
 */
public class MainActivity extends AppCompatActivity
{
    private DisplayManager displayManager;
    private MediaRouter mediaRouter;
    private CustomPresentation customPresentation;
    private boolean hasPermission;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
            mediaRouter = (MediaRouter) getSystemService(MEDIA_ROUTER_SERVICE);
            // 初始化权限
            initPermission();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initPermission() {
        if (!Settings.canDrawOverlays(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:"+getPackageName()));
            startActivityForResult(intent,10);
        }else {
            hasPermission = true;
        }

    }
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            initDisplay();
            return false;
        }
    });

    @Override
    protected void onResume() {
        super.onResume();
        // 这里延时的原因是因为可能是息屏后又开屏需要时间获取屏幕，如果直接获取可能获取不到多个屏幕
        if (hasPermission){
            mHandler.sendEmptyMessageDelayed(1,2000);
        }
    }

    /**
     * 初始化副屏
     * 为什么放在onResume中执行呢？为了防止息屏后客屏消失
     */
    private void initViceScreen(final Display display) {
        // 屏幕发生改变时副屏会自动消失，因此需要将原本显示的副屏置为空避免占用
        if (customPresentation != null && customPresentation.isChange()){
            customPresentation.dismiss();
            customPresentation = null;
        }
        // 屏幕发生改变时需要重新创建
        if (customPresentation == null ){
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


    /**
     * 获取屏幕数量
     */
    private void initDisplay() {
        Display[] displays = displayManager.getDisplays();
        // 说明有副屏
        if (displays.length >1){
            initViceScreen(displays[1]);
        }else {
            // 获取屏幕方式一
            //displays = displayManager.getDisplays();
            // 获取屏幕方式二
            displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
            if (displays.length > 1){
                initViceScreen(displays[1]);
            }else {
                // 获取客屏方式三
                MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                Display display = route.getPresentationDisplay();
                if (display != null) {
                    initViceScreen(display);
                }
            }


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (customPresentation != null){
            customPresentation.dismiss();
        }
        mHandler.removeCallbacksAndMessages(null);
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Settings.canDrawOverlays(this)) {
            hasPermission = true;
            mHandler.sendEmptyMessage(1);
        }
    }

    public void openClick(View view) {
        customPresentation.setChange();
    }
}
