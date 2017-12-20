package neuron.com.comneuron;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.app.OgeApplication;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;

/**
 *
 * @author ljh
 * @data 2016.7.13 上午9:38:24
 * @类描述 BaseActivity :
 */
public class BaseActivity extends AppCompatActivity{
    private Dialog progressDialog;
    private SharedPreferencesManager sfManager;
    private String account;
    public static BaseActivity activity;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1:
                    if (msg.what == 102) {
                        String result = (String) msg.obj;
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            int status = jsonObject.getInt("status");
                            if (status == 1000 || status == 1001) {
                                Toast.makeText(BaseActivity.this, "帐号在别处登录",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        // TODO Auto-generated method stub
        //注册广播
        registerReceiver(mHomeKeyEventReceiver,
                new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        super.onCreate(savedInstanceState);
        OgeApplication.addActivity(this);
        sfManager = SharedPreferencesManager.getInstance(this);
        if (sfManager.has("token")) {
           // detectionToken(sfManager.get("token"),"CheckToken");
        }
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        activity = this;
    }
    //广播接收者 监听home键的广播
    private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {
        String SYSTEM_REASON = "reason";
        String SYSTEM_HOME_KEY = "homekey";
        String SYSTEM_HOME_KEY_LONG = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                    sfManager = SharedPreferencesManager.getInstance(context);
                    //表示按了home键,程序到了后台
                    //Toast.makeText(getApplicationContext(), "home5555", Toast.LENGTH_SHORT).show();
                    //OgeApplication.quiteApplication();
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){
                    //表示长按home键,显示最近使用的程序列表
                }
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        activity = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity = null;
        OgeApplication.removeActivity(this);
    }
}
