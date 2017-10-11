package neuron.com.set.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/9/23. 修改密码页面
 */
public class ChangePassword extends BaseActivity implements View.OnClickListener{
    private String TAG = "ChangePassword";
    private ImageView titleLeft_iv;
    private TextView title_tv;

    private EditText oldPwd_ed,newPwd_ed,newPwd2_ed;

    private Button finish_btn;

    private String oldPassword,newPassword,newPassword2,account;
    private SharedPreferencesManager spm;

    private String CHECKACCOUNT = "CheckAccount";

    private String CHANGEPASSWORD = "ChangePassword";

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int agr1 = msg.arg1;
            switch (agr1) {
                case 1://检测帐号可用性
                    if (msg.what == 102) {
                        String checkAccountResult = (String) msg.obj;
                        Log.e(TAG + "检测帐号可用性", checkAccountResult);
                        try {
                            JSONObject json = new JSONObject(checkAccountResult);
                            int status = json.getInt("status");
                            if (status == 9999) {//验证成功
                                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                                Log.e(TAG + "新旧密码", newPassword + oldPassword);
                                String aesOldPassword = AESOperator.encrypt(MD5Utils.MD5Encode(oldPassword + URLUtils.MD5_SIGN, ""), URLUtils.AES_SIGN);
                                String aesNewPassword = AESOperator.encrypt(MD5Utils.MD5Encode(newPassword + URLUtils.MD5_SIGN, ""), URLUtils.AES_SIGN);
                                String sign = MD5Utils.MD5Encode(aesAccount + CHANGEPASSWORD + aesNewPassword + aesOldPassword + URLUtils.MD5_SIGN, "");
                                XutilsHelper xutil = new XutilsHelper(URLUtils.USERNAME_URL, handler);
                                xutil.add("account", aesAccount);
                                xutil.add("old_password", aesOldPassword);
                                xutil.add("new_password", aesNewPassword);
                                xutil.add("method", CHANGEPASSWORD);
                                xutil.add("sign", sign);
                                xutil.sendPost(2, ChangePassword.this);

                            } else {
                                Toast.makeText(ChangePassword.this, "验证帐号失败", Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ChangePassword.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
                case 2://修改密码
                    if (msg.what == 102) {
                        String changePwdResult = (String) msg.obj;
                        Log.e(TAG + "修改密码", changePwdResult);
                        try {
                            JSONObject json = new JSONObject(changePwdResult);
                            int status = json.getInt("status");
                            if (status == 9999) {
                                Toast.makeText(ChangePassword.this, "修改成功", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(ChangePassword.this, LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(ChangePassword.this, json.getString("error"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ChangePassword.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);
        init();
        setListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        spm.save("password", newPassword);
    }

    private void init() {
        titleLeft_iv = (ImageView) findViewById(R.id.title_left_iv);
        title_tv = (TextView) findViewById(R.id.title_tv);
        title_tv.setText("修改密码");

        oldPwd_ed = (EditText) findViewById(R.id.changepassword_oldpwd_ed);
        newPwd_ed = (EditText) findViewById(R.id.changepassword_newpwd_ed);
        newPwd2_ed = (EditText) findViewById(R.id.changepassword_newpwdagain_ed);

        oldPwd_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        newPwd_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        newPwd2_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        finish_btn = (Button) findViewById(R.id.changepassword_finish_btn);
        spm = SharedPreferencesManager.getInstance(this);
    }
    private void setListener(){
        finish_btn.setOnClickListener(this);
        titleLeft_iv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.changepassword_finish_btn:
                account = spm.get("account");
                oldPassword = oldPwd_ed.getText().toString().trim();
                newPassword = newPwd_ed.getText().toString().trim();
                newPassword2 = newPwd2_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(oldPassword)&& Utils.rexCheckPassword(oldPassword)) {
                    if (!TextUtils.isEmpty(newPassword)&& Utils.rexCheckPassword(newPassword)) {
                        if (newPassword2 != null && !newPassword2.equals("")) {
                            if (newPassword2.equals(newPassword)) {
                                try {
                                    String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                                    String sign = MD5Utils.MD5Encode(aesAccount + CHECKACCOUNT + URLUtils.MD5_SIGN, "");
                                    XutilsHelper xutils = new XutilsHelper(URLUtils.USERNAME_URL, handler);
                                    xutils.add("account", aesAccount);
                                    xutils.add("method", CHECKACCOUNT);
                                    xutils.add("sign", sign);
                                    xutils.sendPost(1, this);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(ChangePassword.this,"您输入的新密码不相同，请重新输入",Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(ChangePassword.this,"请再次输入新密码",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(ChangePassword.this,"请输入新密码",Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(ChangePassword.this,"请输入旧密码",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.title_left_iv:
                finish();
                break;
            default:
                break;
        }
    }
}
