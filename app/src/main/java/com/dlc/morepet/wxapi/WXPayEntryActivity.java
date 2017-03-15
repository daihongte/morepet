package com.dlc.morepet.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dlc.morepet.bean.RespBean;
import com.dlc.morepet.utils.LogUtils;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.greenrobot.eventbus.EventBus;

import static com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_COMM;
import static com.tencent.mm.opensdk.modelbase.BaseResp.ErrCode.ERR_USER_CANCEL;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.pay_result);

        api = WXAPIFactory.createWXAPI(this, "wxef5d33d399f6d9d5");
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {

    }

    @Override
    public void onResp(BaseResp resp) {
        int code = resp.errCode;
        Log.i(TAG, "onPayFinish, errCode = " + code);
        LogUtils.info("onResp --> " + code);
        switch (code) {
            case 0://支付成功
                Toast.makeText(this, "支付成功", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(this, MainActivity.class);
//                intent.putExtra("code", code);
//                intent.putExtra("sign", 2);
//                Toast.makeText(this, "成功--> " + code, Toast.LENGTH_LONG).show();
//                startActivity(intent);

                EventBus.getDefault().post(new RespBean(code, 2));
                break;
            case ERR_COMM://签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、您的微信账号异常等
                Toast.makeText(this, "支付异常", Toast.LENGTH_LONG).show();
                break;
            case ERR_USER_CANCEL://用户取消支付
                Toast.makeText(this, "取消支付", Toast.LENGTH_LONG).show();
                break;
        }

        finish();
    }
}