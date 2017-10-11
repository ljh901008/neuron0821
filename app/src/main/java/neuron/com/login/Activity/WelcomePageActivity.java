package neuron.com.login.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.lock.activity.GestureVerifyActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/6/5.
 */
public class WelcomePageActivity extends BaseActivity {
    private String TAG = "WelcomePageActivity";
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://校验token
                    if (msg.what == 102) {
                        String result = (String) msg.obj;
                        Log.e(TAG + "校验token", result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                                JSONObject jsonPhoto = jsonMsg.getJSONObject("photo_path");
                                sharedPreferencesManager.save("photo_path", jsonPhoto.getString("Android"));
                                if (sharedPreferencesManager.has("isFirstLogin")) {
                                    if (!"0".equals(sharedPreferencesManager.get("isFirstLogin"))) {
                                        if (sharedPreferencesManager.has("handlock")) {
                                            Intent intent = new Intent(getApplicationContext(), GestureVerifyActivity.class);
                                            startActivity(intent);
                                        } else {
                                            sharedPreferencesManager.save("isFirstLogin", "0");
                                            Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(intent3);
                                        }
                                    } else {
                                        Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent3);
                                    }

                                } else {
                                    Intent intent2 = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent2);
                                }
                            } else {
                                //此处使用的是context的startactivity方法。必须加flags
                                Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent1);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Toast.makeText(WelcomePageActivity.this, "网络不通", Toast.LENGTH_LONG).show();
                        Intent intent5 = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent5);
                    }
                    break;
                default:
                    break;
            }
           /* if (msg.what == 1) {
                Intent intent = new Intent(WelcomePageActivity.this, MainActivity.class);
                startActivity(intent);
            }*/
        }
    };
    private SharedPreferencesManager sharedPreferencesManager;
    private String account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomepage);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("token")) {
            detectionToken(sharedPreferencesManager.get("token"), "CheckToken");
        } else {//如果没有token说明是第一次登录
            //此处使用的是context的startactivity方法。必须加flags
            Intent intent1 = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent1);
        }
       /* new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    sleep(2000);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }.start();*/
    }
    /**
     *    校验token可用性
     * @param token
     * @param method
     */
    private void detectionToken(String token,String method){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.USERNAME_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
