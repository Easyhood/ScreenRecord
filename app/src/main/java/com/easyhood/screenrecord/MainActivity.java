package com.easyhood.screenrecord;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 功能：录屏软件
 * 详细描述：录屏时可以输出H264文件
 * 作者：guan_qi
 * 创建日期：2023-03-20
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // 录屏工具
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mediaProjection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    /**
     * 权限检查
     * @return false
     */
    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 1);

        }
        return false;
    }

    /**
     * 按键点击事件
     * @param view View
     */
    public void start(View view) {
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || requestCode != 1) {
            return;
        }
        // 下面开始进行录屏，并输出H264
        mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
        H264Encoder h264Encoder = new H264Encoder(mediaProjection);
        h264Encoder.start();
    }
}