package com.dlc.morepet.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.dlc.morepet.ConfigFactory;
import com.dlc.morepet.bean.RespBean;
import com.dlc.morepet.ui.MainActivity;
import com.dlc.morepet.utils.HttpManager;
import com.dlc.morepet.utils.LogUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

import java.io.StringReader;

/**
 * Auther by winds on 2017/2/23
 * Email heardown@163.com
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.pay_result);

        api = WXAPIFactory.createWXAPI(this, ConfigFactory.APPID);
        api.registerApp(ConfigFactory.APPID);
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                //如果集成了分享和登录功能，那么可以通过baseResp.getType()来判断是哪个回调信息，1为登录授权，2为分享
                //如果是登录授权，那么调用了登录api以后，这里会返回获取accessToken需要的code
                SendAuth.Resp sendResp = (SendAuth.Resp) baseResp;
                String code = sendResp.code;
                LogUtils.info("code --> " + code);
//                Intent intent = new Intent(WXEntryActivity.this, MainActivity.class);
//                intent.putExtra("code", code);
//                intent.putExtra("sign", 1);
//                startActivity(intent);
                EventBus.getDefault().post(new RespBean(code, 1));
                break;
            case BaseResp.ErrCode.ERR_SENT_FAILED:
                Toast.makeText(this, "授权失败", Toast.LENGTH_LONG).show();
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                Toast.makeText(this, "授权取消", Toast.LENGTH_LONG).show();
                break;
        }
        finish();
    }

    public void getAccessToken(String appid, String secret, String code) {
        //微信登录时，需要通过code请求url获取accesstoken。
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid=" + appid +
                "&secret=" + secret +
                "&code=" + code +
                "&grant_type=authorization_code";

        LogUtils.info("url --> " + url);
        //以下是网络请求拿到accesstoken以及openid.
        HttpManager.getInstance().getByParams(url, new HttpManager.OnBackListener() {
            @Override
            public void onSuccess(String msg) {
                LogUtils.info("onSuccess --> " + msg);
                JsonReader reader = new JsonReader(new StringReader(msg));
                reader.setLenient(true);
                JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
                String access_token = jsonObject.get("access_token").getAsString();
                String openid = jsonObject.get("openid").getAsString();

                if (!TextUtils.isEmpty(access_token) && !TextUtils.isEmpty("openid")) {
                    Intent intent = new Intent(WXEntryActivity.this, MainActivity.class);
                    intent.putExtra("access_token", access_token);
                    intent.putExtra("openid", openid);
                    intent.putExtra("sign", 1);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailed(String msg) {
                LogUtils.info("onFailed --> " + msg);
            }
        });
    }

    public void getUserInfo(String accessToken, String openId) {
        //如果获取到了AccessToken和openid，请求url获取用户信息
        String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" +
                accessToken + "&openid=" + openId;
    }

}
