package neuron.com.set.Activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.DataCleraManager;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/24.  我的页面中的其他
 */
public class AboutUsActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AboutUsActivity";
    private ImageButton back;
    private RelativeLayout clear_rll,aboutUs_rll, outLogin_rll,update_rll;
    private TextView version;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account;
    private String outLoginMethod = "LoginOut";
    private WaitDialog mWaitDialog;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1:
                    if (msg.what == 102) {
                        String updateResult = (String) msg.obj;
                        Log.e(TAG + "退出登录", updateResult);
                        try {
                            JSONObject jsonObject = new JSONObject(updateResult);
                            if (jsonObject.getInt("status") != 9999) {

                                Utils.showDialog(AboutUsActivity.this, jsonObject.getString("error"));
                            } else {
                                final AlertDialog builder = new AlertDialog.Builder(AboutUsActivity.this).create();
                                builder.setCanceledOnTouchOutside(false);
                                builder.setCancelable(false);
                                View view = View.inflate(AboutUsActivity.this, R.layout.dialog_textview, null);
                                TextView title = (TextView) view.findViewById(R.id.textView1);
                                Button button = (Button) view.findViewById(R.id.button1);
                                title.setText("退出成功");
                                builder.setView(view);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        sharedPreferencesManager.remove("token");
                                        sharedPreferencesManager.remove("engine_id");
                                        JPushInterface.setAlias(AboutUsActivity.this, "", new TagAliasCallback() {
                                            @Override
                                            public void gotResult(int i, String s, Set<String> set) {
                                                if (i == 0) {
                                                    Log.e(TAG + "清空别名", "清空别名成功qaq");
                                                } else if (i == 6002) {//设置别名超时
                                                    Log.e(TAG + "清空别名", "清空别名超时啦啦阿qaq");
                                                }
                                            }
                                        });
                                        Intent intent = new Intent(AboutUsActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                        builder.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2:
                    Utils.dismissWaitDialog(mWaitDialog);
                    Utils.showDialog(AboutUsActivity.this, "清除成功");
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutus);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        init();
        setListener();
    }

    private void init() {
        back = (ImageButton) findViewById(R.id.aboutus_back_iv);
        clear_rll = (RelativeLayout) findViewById(R.id.aboutus_clear_rll);
        aboutUs_rll = (RelativeLayout) findViewById(R.id.aboutus__rll);
        outLogin_rll = (RelativeLayout) findViewById(R.id.aboutus_outlogin_rll);
        update_rll = (RelativeLayout) findViewById(R.id.aboutus_update_rll);
        version = (TextView) findViewById(R.id.aboutus_version_tv);
        version.setText("版本号:" + getVersion());
    }
    private void setListener(){
        back.setOnClickListener(this);
        clear_rll.setOnClickListener(this);
        outLogin_rll.setOnClickListener(this);
        aboutUs_rll.setOnClickListener(this);
        update_rll.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.aboutus_back_iv://返回
                finish();
                break;
            case R.id.aboutus_clear_rll://清缓存
                Utils.showWaitDialog("清除中...", AboutUsActivity.this,mWaitDialog);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        DataCleraManager.cleanInternalCache(AboutUsActivity.this);
                        try {
                            sleep(3000);
                            Message message = new Message();
                            message.arg1 = 2;
                            handler.sendMessage(message);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }.start();
                break;
            case R.id.aboutus_outlogin_rll://退出登陆
                outLogin();
                break;
            case R.id.aboutus__rll://关于我们
                Intent intent = new Intent(AboutUsActivity.this, AboutWeActivity.class);
                startActivity(intent);
                break;
            case R.id.aboutus_update_rll://检查更新
                String verId = sharedPreferencesManager.get("versionCode");
                if (sharedPreferencesManager.has("isUpdate") && verId.compareTo(getVersion()) > 0) {
                    final AlertDialog builder = new AlertDialog.Builder(AboutUsActivity.this).create();
                    View view1 = View.inflate(AboutUsActivity.this, R.layout.dialog_alert, null);
                    TextView content = (TextView) view1.findViewById(R.id.dialog_alert_content);
                    Button cancle = (Button) view1.findViewById(R.id.dialog_alert_cancle);
                    Button enter = (Button) view1.findViewById(R.id.dialog_alert_enter);
                    content.setText("检测到新版本，请及时更新");
                    builder.setView(view1);
                    cancle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            sharedPreferencesManager.save("isUpdate", URLUtils.noUpdate);
                            builder.dismiss();
                        }
                    });
                    enter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri uri = Uri.parse(sharedPreferencesManager.get("versionUrl"));
                            intent.setData(uri);
                            startActivity(intent);
                            builder.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(AboutUsActivity.this, "没有新版本更新", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
            break;
        }
    }
    private void outLogin(){

        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + outLoginMethod + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.USERNAME_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("method", outLoginMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 2  * 获取版本号
     * 3  * @return 当前应用的版本号
     * 4
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }
}
