package neuron.com.set.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import org.xutils.common.Callback;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/20. 添加子账号页面
 */
public class AddZiAccountActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "AddZiAccountActivity";
    private ImageView titleLeft_iv;
    private TextView title_tv;

    private EditText oldPwd_ed,newPwd_ed,newPwd2_ed;

    private Button finish_btn;
    private SharedPreferencesManager sharedPreferencesManager;
    private String ziAccount,ziPwd, ziUserName;
    private String account, token;
    private String addMethod = "AddKidAccount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);
        init();
        setListener();
    }
    private void init() {
        titleLeft_iv = (ImageView) findViewById(R.id.title_left_iv);
        title_tv = (TextView) findViewById(R.id.title_tv);
        title_tv.setText("添加子账号");

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
        oldPwd_ed.setHint("请输入子帐号（6-10位字母或数字）");
        newPwd_ed.setHint("请输入子账号密码（6-12位字母或数字）");
        newPwd2_ed.setHint("请输入昵称");
        finish_btn = (Button) findViewById(R.id.changepassword_finish_btn);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
    }
    private void setListener(){
        finish_btn.setOnClickListener(this);
        titleLeft_iv.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.changepassword_finish_btn://确认
                ziAccount = oldPwd_ed.getText().toString().trim();
                ziPwd = newPwd_ed.getText().toString().trim();
                ziUserName = newPwd2_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(ziAccount) && Utils.rexCheckPassword(ziAccount) && ziAccount.length() > 5 && ziAccount.length() < 11) {
                    if (!TextUtils.isEmpty(ziPwd) && Utils.rexCheckPassword(ziPwd)) {
                        if (!TextUtils.isEmpty(ziUserName) && ziUserName.length() < 8) {
                            try {
                                String aesPwd = AESOperator.encrypt(MD5Utils.MD5Encode(ziPwd + URLUtils.MD5_SIGN, ""), URLUtils.AES_SIGN);
                                String aesZiAccount = AESOperator.encrypt(ziAccount, URLUtils.AES_SIGN);
                                addZiAccount(aesZiAccount, aesPwd, ziUserName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(AddZiAccountActivity.this, "请输入昵称", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AddZiAccountActivity.this, "请输入合法的密码", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AddZiAccountActivity.this, "请输入合法帐号", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.title_left_iv://退出
                finish();
                break;
            default:
                break;
        }
    }

    /**
     *   添加子账号
     * @param ziAccount  子帐号
     * @param ziPwd      子密码
     * @param ziUserName  子昵称
     */
    private void addZiAccount(String ziAccount,String ziPwd,String ziUserName){
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        }
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + ziAccount + addMethod + ziPwd + token + ziUserName + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.USERNAME_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("accountc", ziAccount);
            xutilsHelper.add("password", ziPwd);
            xutilsHelper.add("username", ziUserName);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", addMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    Log.e(TAG + "添加zi账号", deleteResult);
                    try {
                        JSONObject jsonDelete = new JSONObject(deleteResult);
                        if (jsonDelete.getInt("status") == 9999) {
                            final AlertDialog builder = new AlertDialog.Builder(AddZiAccountActivity.this).create();
                            View view = View.inflate(AddZiAccountActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("添加成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = getIntent();
                                    intent.putExtra("tag", 1);
                                    setResult(RESULT_OK,intent);
                                    finish();
                                    builder.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Toast.makeText(AddZiAccountActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
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

}
