package com.dlc.morepet.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.dlc.morepet.R;
import com.dlc.morepet.utils.CommonUtils;
import com.dlc.morepet.utils.DialogUtils;
import com.dlc.morepet.utils.HttpManager;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity_1 extends Activity implements View.OnClickListener {
    WebView web;
    private ValueCallback<Uri> mUploadMessage;
    private String MIME = "image/*";
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private InputNumDialog dialog;
    private HttpManager manager;
    private IWXAPI api;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()) {
            web.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = HttpManager.getInstance();
        api = WXAPIFactory.createWXAPI(this, "wxef5d33d399f6d9d5");
        web = (WebView) findViewById(R.id.wb_mwv);
        web.setVerticalScrollBarEnabled(false);


//        web.loadUrl("http://www.taobao.com/");
        web.loadUrl("http://aiyanjia.xiaozhuschool.com/app");
        initWebSetting();
        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                Log.i("Case", "--> " + url);

                if (url.startsWith("tel:")) {//电话
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);
                } else if (url.contains("/Home/Wxpay/pay")) {//微信支付
                    Log.i("微信支付", "微信支付");
                    initWxPay(url);
                } else if (url.startsWith("camp:")) {//扫描
                    startActivityForResult(new Intent(getApplicationContext(), ScanActivity.class), 2);
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                } else if (url.contains("weixin://login")) {
                    Log.i("Case", "--> startsWith " + url);
//                    http://aiyanjia.xiaozhuschool.com/App/Vip/login/backurl/L0FwcC9WaXAvbG9naW4%3D
                }
                return true;
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            // For Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(MIME);
                MainActivity_1.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }

            // For Android 3.0+
            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(MIME);
                MainActivity_1.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
            }

            //For Android 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(MIME);
                MainActivity_1.this.startActivityForResult(Intent.createChooser(i, "File Browser"), MainActivity_1.FILECHOOSER_RESULTCODE);
            }

            // For Android 5.0+
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType(MIME);
                MainActivity_1.this.startActivityForResult(
                        Intent.createChooser(i, "File Browser"),
                        FILECHOOSER_RESULTCODE);
                return true;
            }
        });
    }


    /**
     * 微信支付
     *
     * @param url
     */
    private void initWxPay(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    manager.getByParams(url, new HttpManager.OnBackListener() {
                        @Override
                        public void onSuccess(final String msg) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                            });
                            Log.i("msg==", msg);
                            if (TextUtils.isEmpty(msg)) {
                                return;
                            }
                            try {
                                JSONObject json = new JSONObject(msg).getJSONObject("package");

                                if (json != null) {
                                    PayReq req = new PayReq();
                                    req.appId = json.getString("appid");
                                    req.partnerId = json.getString("partnerid");
                                    req.prepayId = json.getString("prepayid");
                                    req.nonceStr = json.getString("noncestr");
                                    req.timeStamp = json.getString("timestamp");
                                    req.packageValue = json.getString("package");
                                    req.sign = json.getString("paySign");

                                    AppRegister.redirect = new JSONObject(msg).getString("redirect");

                                    // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                                    api.registerApp(req.appId);
                                    api.sendReq(req);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailed(String msg) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void initPay() {
        Log.i("initPay", "initPay---------");
        int code = getIntent().getIntExtra("code", 8);
        if (code == 0 && !TextUtils.isEmpty(AppRegister.redirect)) {//支付成功
            web.loadUrl(AppRegister.redirect);
        }
    }

    /**
     * 属于设置
     */
    private void initWebSetting() {
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);//允许DCOM
        //隐藏缩放工具
        settings.setDisplayZoomControls(false);
        // 设置可以支持缩放
        settings.setSupportZoom(true);
        // 设置出现缩放工具
        settings.setBuiltInZoomControls(true);
        //扩大比例的缩放
        settings.setUseWideViewPort(true);
        //自适应屏幕
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }

        /**
         * 处理二维码扫描结果
         */
        if (requestCode == 2) {//处理扫描结果（在界面上显示）
            if (null != data) {
                String result = data.getExtras().getString("result");
                int sign = data.getExtras().getInt("sign");
                if (!TextUtils.isEmpty(result)) {
                    if (web != null) {
                        web.loadUrl(result);
                    }
                }
                if (sign == 1) {//输入设备序列号
                    dialog = new InputNumDialog(this);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setOnConfirmListener(this);
                    WindowManager manager = getWindowManager();
                    DialogUtils.showDialog(dialog, manager, 0.25, 0.83);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE
                || mUploadCallbackAboveL == null) {
            return;
        }

        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {

            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        mUploadCallbackAboveL.onReceiveValue(results);
        mUploadCallbackAboveL = null;
        return;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_ok:
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(dialog.findViewById(R.id.et_num).getWindowToken(), 0); //强制隐藏键盘

                if (dialog != null) {
                    String num = dialog.getNum();
                    if (TextUtils.isEmpty(num)) {
                        Toast.makeText(getApplicationContext(), "设备序列号不能为空!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    isExist(num);
                }
                break;
        }
    }

    /**
     * 查询设备序列号是否存在
     *
     * @param num
     */
    private void isExist(final String num) {
        if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
            Map<String, String> params = new HashMap<>();
            params.put("chk", "1");
            params.put("macno", num);
            manager.getInstance().getByParams("http://aiyanjia.xiaozhuschool.com/App/shop/qrOrder", params, new HttpManager.OnBackListener() {
                @Override
                public void onSuccess(String msg) {
                    Log.i("msg", msg);
                    if (!TextUtils.isEmpty(msg)) {
                        try {
                            JSONObject object = new JSONObject(msg);
                            String status = object.getString("status");
                            Log.i("status", status + "");
                            if (status.equals("0")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "设备序列号不存在!请重新输入!", Toast.LENGTH_SHORT).show();
                                        dialog.clear();
                                    }
                                });
                            } else if (status.equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        web.loadUrl("http://aiyanjia.xiaozhuschool.com/App/shop/qrOrder?macno=" + num);
                                        dialog.dismiss();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailed(String msg) {
                    Toast.makeText(getApplicationContext(), "请检查网络", Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "当前没有连接网络", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        initPay();
    }
}
