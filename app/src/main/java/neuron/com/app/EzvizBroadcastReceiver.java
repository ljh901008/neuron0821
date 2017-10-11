package neuron.com.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.videogo.constant.Constant;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAccessToken;

import neuron.com.database.SharedPreferencesManager;

/**
 * Created by ljh on 2017/2/14.
 */
public class EzvizBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Constant.OAUTH_SUCCESS_ACTION)) {
            EZOpenSDK openSdk = EZOpenSDK.getInstance();
            EZAccessToken token = null;
            if(openSdk != null) {
                    token = openSdk.getEZAccessToken();
            }
            //保存萤石token值在本地
            SharedPreferencesManager spf =   SharedPreferencesManager.getInstance(context);
            spf.save("EZtoken", token.getAccessToken());
            spf.save("EZtokenTime", String.valueOf(token.getExpire()));//token有效期，一般为604800秒   即7天
        }
    }
}
