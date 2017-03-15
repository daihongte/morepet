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
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.dlc.morepet.ConfigFactory;
import com.dlc.morepet.R;
import com.dlc.morepet.bean.RespBean;
import com.dlc.morepet.bean.User;
import com.dlc.morepet.utils.CommonUtils;
import com.dlc.morepet.utils.DialogUtils;
import com.dlc.morepet.utils.HttpManager;
import com.dlc.morepet.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {
    private Toast toast;
    private WebView web;
    private ValueCallback<Uri> mUploadMessage;
    private String MIME = "image/*";
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;
    private InputNumDialog dialog;
    private HttpManager manager;
    private IWXAPI api;
    private KProgressHUD mKProgressHUD;
    private String CookieStr;

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
        EventBus.getDefault().register(this);

        manager = HttpManager.getInstance();
        mKProgressHUD = KProgressHUD.create(this);

        api = WXAPIFactory.createWXAPI(this, ConfigFactory.APPID, true);
        api.registerApp(ConfigFactory.APPID);
        web = (WebView) findViewById(R.id.wb_mwv);
        web.setVerticalScrollBarEnabled(false);

        web.loadUrl(ConfigFactory.URL_BASE);
        initWebSetting();
        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {
                if (url.startsWith("tel:")) {//电话
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);
                } else if (url.contains("/Home/Wxpay/pay")) {//微信支付
                    initWxPay(url);
                } else if (url.startsWith("camp:")) {//扫描
                    startActivityForResult(new Intent(getApplicationContext(), ScanActivity.class), 2);
                } else if (url.startsWith("qqweixin:")) {
                    login();
                } else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager cookieManager = CookieManager.getInstance();
                CookieStr = cookieManager.getCookie(url);
                super.onPageFinished(view, url);
            }
        });


        web.setWebChromeClient(new
              WebChromeClient() {
                                           // For Android 3.0+
                                           public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                                               mUploadMessage = uploadMsg;
                                               Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                               i.addCategory(Intent.CATEGORY_OPENABLE);
                                               i.setType(MIME);
                                               MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
                                           }


                                           // For Android 3.0+

                                           public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                                               mUploadMessage = uploadMsg;
                                               Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                               i.addCategory(Intent.CATEGORY_OPENABLE);
                                               i.setType(MIME);
                                               MainActivity.this.startActivityForResult(
                                                       Intent.createChooser(i, "File Browser"),
                                                       FILECHOOSER_RESULTCODE);
                                           }

                                           //For Android 4.1
                                           public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                                               mUploadMessage = uploadMsg;
                                               Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                               i.addCategory(Intent.CATEGORY_OPENABLE);
                                               i.setType(MIME);
                                               MainActivity.this.startActivityForResult(Intent.createChooser(i, "File Browser"), MainActivity.FILECHOOSER_RESULTCODE);
                                           }

                                           // For Android 5.0+
                                           public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                                               mUploadCallbackAboveL = filePathCallback;
                                               Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                                               i.addCategory(Intent.CATEGORY_OPENABLE);
                                               i.setType(MIME);
                                               MainActivity.this.startActivityForResult(
                                                       Intent.createChooser(i, "File Browser"),
                                                       FILECHOOSER_RESULTCODE);
                                               return true;
                                           }
                                       });
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

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRespEvent(RespBean bean) {
        LogUtils.info("收到数据...." + bean.toString());
        switch (bean.sign) {
            case 1: //微信登录
                getAccessToken(ConfigFactory.APPID, ConfigFactory.APPSECRET, (String) bean.code);
                break;
            case 2: //支付
                int code = (int) bean.code;
                if (code == 0 && !TextUtils.isEmpty(AppRegister.redirect)) {//支付成功
                    web.loadUrl(AppRegister.redirect);
                }
                break;
        }

    }

    /**
     * 使用第三方登录
     */
    public void login() {
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_aiyanjia";
        api.sendReq(req);
    }

    /**
     * 获取用户信息
     *
     * @param accessToken
     * @param openId
     */
    public void getUserInfo(String accessToken, String openId) {

        //如果获取到了AccessToken和openid，请求url获取用户信息
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" +
                accessToken + "&openid=" + openId;
        HttpManager.getInstance().getByParams(url, new HttpManager.OnBackListener() {
            @Override
            public void onSuccess(String msg) {
                LogUtils.info("-onSuccess->" + msg);
                if (!TextUtils.isEmpty(msg)) {
                    try {
                        User user = new Gson().fromJson(msg, User.class);
                        if (user != null && user.openid != null && user.unionid != null) {
                            LogUtils.info(user.toString());
                            postUserInfo(user);
                            return;
                        }
                    } catch (Exception e) {
                    }
                    ;
                }

                if (mKProgressHUD != null && mKProgressHUD.isShowing()) {
                    mKProgressHUD.dismiss();
                }
                showTips("获取授权失败");

            }

            @Override
            public void onFailed(String msg) {
                LogUtils.info("-onFailed->" + msg);
                if (mKProgressHUD != null && mKProgressHUD.isShowing()) {
                    mKProgressHUD.dismiss();
                }
                showTips("获取授权失败");
            }
        });
    }

    /**
     * 获取token
     *
     * @param appid
     * @param secret
     * @param code
     */
    public void getAccessToken(String appid, String secret, String code) {
        mKProgressHUD.setLabel("正在加载中...").show();
        //微信登录时，需要通过code请求url获取accesstoken。
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + appid +
                "&secret=" + secret +
                "&code=" + code +
                "&grant_type=authorization_code";

        //以下是网络请求拿到accesstoken以及openid.
        HttpManager.getInstance().getByParams(url, new HttpManager.OnBackListener() {
            @Override
            public void onSuccess(String msg) {
                if (!TextUtils.isEmpty(msg)) {
                    JsonReader reader = new JsonReader(new StringReader(msg));
                    reader.setLenient(true);
                    JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                    if (jsonObject != null) {
                        JsonElement access_token1 = jsonObject.get("access_token");
                        JsonElement openid1 = jsonObject.get("openid");
                        if (access_token1 != null && openid1 != null) {
                            String access_token = jsonObject.get("access_token").getAsString();
                            String openid = jsonObject.get("openid").getAsString();
                            if (!TextUtils.isEmpty(access_token) && !TextUtils.isEmpty(openid)) {
                                LogUtils.info("access_token " + access_token + " " + openid);
                                getUserInfo(access_token, openid);
                                return;
                            }
                        }
                    }

                }

                showTips("获取授权失败");
            }

            @Override
            public void onFailed(String msg) {
                LogUtils.info("onFailed --> " + msg);
                if (mKProgressHUD != null && mKProgressHUD.isShowing()) {
                    mKProgressHUD.dismiss();
                }
                showTips("获取授权失败");
            }
        });
    }

    /**
     * 向后台传递请求
     *
     * @param user
     */
    private void postUserInfo(User user) {
        Map<String, String> params = new HashMap<>();
        params.put("openid", user.openid);
        params.put("unionid", user.unionid);
        params.put("nickname", user.nickname);
        params.put("language", user.language);
        params.put("province", user.province);
        params.put("country", user.country);
        params.put("headimgurl", user.headimgurl);
        if (TextUtils.isEmpty(CookieStr)) {
            return;
        }

        HttpManager.getInstance().postFormParams(ConfigFactory.URL_POST_USER_INFO, CookieStr, params, new HttpManager.OnBackListener() {
            @Override
            public void onSuccess(String msg) {
                LogUtils.info("postUserInfo --> " + msg);
                if (mKProgressHUD != null && mKProgressHUD.isShowing()) {
                    mKProgressHUD.dismiss();
                }

                JsonReader reader = new JsonReader(new StringReader(msg));
                reader.setLenient(true);
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                if (jsonObject != null) {
                    String tip = jsonObject.get("msg").getAsString();
                    if (!TextUtils.isEmpty(tip) && tip.equals("success")) {
                        final String redirect = jsonObject.get("redirect").getAsString();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtils.info("跳转....");
                                web.loadUrl(redirect);
                                web.reload();
                            }
                        });
                    } else {
                        showTips("获取授权失败");
                    }
                } else {
                    showTips("获取授权失败");
                }

            }

            @Override
            public void onFailed(String msg) {
                LogUtils.info("postUserInfo onFailed --> " + msg);
                if (mKProgressHUD != null && mKProgressHUD.isShowing()) {
                    mKProgressHUD.dismiss();
                }

                showTips("登录失败");
            }
        });
    }


    private void showTips(final String msg) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(MainActivity.this, "", Toast.LENGTH_LONG);
                }
                toast.setText(msg);
                toast.show();
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


    private void initGetPara() {
        int sign = getIntent().getIntExtra("sign", -1);
        switch (sign) {
            case 1: //微信登录
                String mCode = getIntent().getStringExtra("code");
                getAccessToken(ConfigFactory.APPID, ConfigFactory.APPSECRET, mCode);
                break;
            case 2: //支付
                int code = getIntent().getIntExtra("code", 8);
                if (code == 0 && !TextUtils.isEmpty(AppRegister.redirect)) {//支付成功
                    web.loadUrl(AppRegister.redirect);
                }
                break;
        }


    }
}
