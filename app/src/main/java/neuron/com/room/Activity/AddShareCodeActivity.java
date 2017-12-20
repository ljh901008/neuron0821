package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.set.Activity.HostManagerActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/11. 分享码页面
 */
public class AddShareCodeActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AddShareCodeActivity";
    private ImageButton back;
    private Button confirm;
    private EditText shareCode_ed;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private String shareMetho = "AddSharedEngines";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addsharecode);
        init();
    }

    private void init() {
        back = (ImageButton) findViewById(R.id.addsharecode_back_iv);
        confirm = (Button) findViewById(R.id.addsharecode_fonfirm_btn);
        shareCode_ed = (EditText) findViewById(R.id.addsharecode_sharecode_ed);
        shareCode_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        confirm.setOnClickListener(this);
        back.setOnClickListener(this);
    }
    private void share(String shareCode){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + shareMetho + shareCode + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.SHAREHOSMANAGER);
            xutils.add("account", aesAccount);
            xutils.add("share_code", shareCode);
            xutils.add("token", token);
            xutils.add("method", shareMetho);
            xutils.add("sign", sign);
            ///xutils.sendPost(1, this);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        Log.e(TAG + "分享码", result);
                        JSONObject json = new JSONObject(result);
                        if (json.getInt("status") == 9999) {

                            final AlertDialog builder = new AlertDialog.Builder(AddShareCodeActivity.this).create();
                            View view = View.inflate(AddShareCodeActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("  设置分享成功,请在控制主机列表页面选择控制主机");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(AddShareCodeActivity.this, HostManagerActivity.class);
                                    startActivity(intent);
                                    builder.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Utils.showDialog(AddShareCodeActivity.this, json.getString("error"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {

                }

                @Override
                public void onCancelled(CancelledException e) {

                }

                @Override
                public void onFinished() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.addsharecode_back_iv://fanhui
                finish();
                break;
            case R.id.addsharecode_fonfirm_btn://确定
                String sc = shareCode_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(sc)) {
                    share(sc);
                }
                break;
            default:
            break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(AddShareCodeActivity.this, MainActivity.class);
            startActivity(intent);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
