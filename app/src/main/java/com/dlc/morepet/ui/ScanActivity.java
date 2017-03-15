package com.dlc.morepet.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import com.dlc.morepet.R;

import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class ScanActivity extends Activity implements QRCodeView.Delegate, View.OnClickListener {

    ZXingView zxingView;
    TitleView title;
    TextView tv_switchFlash, tv_input_num;

    private boolean isOpen;//闪光灯是否打开

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        zxingView = (ZXingView) findViewById(R.id.zxingView);
        title = (TitleView) findViewById(R.id.title);
        tv_switchFlash = (TextView) findViewById(R.id.tv_switchFlash);
        tv_input_num = (TextView) findViewById(R.id.tv_input_num);

        title.iv_left.setOnClickListener(this);
        tv_switchFlash.setOnClickListener(this);
        tv_input_num.setOnClickListener(this);
        init();

    }

    private void init() {
        zxingView.setDelegate(this);

    }

    /**
     * 扫描成功
     *
     * @param result
     */
    @Override
    public void onScanQRCodeSuccess(String result) {
        scanSuccess(result);

        MediaPlayer player = MediaPlayer.create(this, R.raw.beep);//播放音效
        player.setVolume(0.5f, 0.5f);//左声道,右声道
        player.start();
    }

    private void scanSuccess(String result) {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);//震动
        vibrator.vibrate(200);

        Intent intent = new Intent();
        intent.putExtra("result", result);
        setResult(2, intent);
        finish();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {

    }

    @Override
    protected void onStart() {
        super.onStart();
        zxingView.startCamera();//打开摄像头
        zxingView.showScanRect();//显示扫描框
        zxingView.startSpot();//1.5s后开始识别
    }

    @Override
    protected void onStop() {
        zxingView.stopCamera();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        zxingView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_left://返回
                finish();
                break;
            case R.id.tv_switchFlash:// 打开/关闭闪光灯
                isOpen = !isOpen;
                switchLight(isOpen);
                break;
            case R.id.tv_input_num:// 输入序列号
                Intent intent = new Intent();
                intent.putExtra("sign", 1);
                setResult(2, intent);
                finish();
                break;
        }
    }

    /**
     * 切换灯
     *
     * @param b
     */
    private void switchLight(boolean b) {
        if (b) {
            zxingView.openFlashlight();
            tv_switchFlash.setText("关闭闪光灯");
        } else {
            zxingView.closeFlashlight();
            tv_switchFlash.setText("打开闪光灯");
        }
    }
}
