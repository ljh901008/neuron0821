package neuron.com.login.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/8/30. 注册界面
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener{

    private String TAG = "RegisterActivity";
    //titlebar
    private ImageView title_left_iv;
    private TextView title_tv;
    // 电话号码，验证码，密码
    private EditText phoneNumber_ed,yzCode,password_ed;
    //获取验证码
    private TextView getYZCode_tv;

    private ImageView showPassword_iv;
    private Button register_btn;
    //注册提示的布局，当手机号被注册的时候显示
    private RelativeLayout registerTiShi_rll;
    //是否显示密码
    private boolean isShowPassword = false;
    private TimeCount time;
    private String phoneNum,yzm;
    private String METHOD_REGISTER = "Register";
    private String METHOD_FORGETPASSWORD = "NewPassword";
    //匹配手机正则表达式
    String s = "^(13[0-9]|15[0|3|6|7|8|9]|18[8|9])\\d{8}$";
    private TextView userAgreement_tv;
    private RelativeLayout useragreement_rll;
    private Intent intent;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        init();
        setclick();
    }
    private void init(){
        intent = getIntent();
        type = intent.getIntExtra("type", 3);
        title_left_iv = (ImageView) findViewById(R.id.title_1_left_iv);
        title_tv = (TextView) findViewById(R.id.title_1_tv);
        userAgreement_tv = (TextView) findViewById(R.id.register_useragreement_tv);
        useragreement_rll = (RelativeLayout) findViewById(R.id.register_useragreement_rll);
        getYZCode_tv = (TextView) findViewById(R.id.register_get_yanzhengma_tv);
        phoneNumber_ed = (EditText) findViewById(R.id.register_inputiphong_ed);
        yzCode = (EditText) findViewById(R.id.register_inputcode_ed);
        password_ed = (EditText) findViewById(R.id.register_inputpassword_ed);
        showPassword_iv = (ImageView) findViewById(R.id.register_invisible_iv);
        register_btn = (Button) findViewById(R.id.register_btn);
        registerTiShi_rll = (RelativeLayout) findViewById(R.id.register_tishi_rll);
        phoneNumber_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        yzCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        password_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        if (type == 1) {
            title_tv.setText("注册");
            register_btn.setText("注 册");
            password_ed.setHint("请输入密码(6-12位字母数字)");
        } else if (type == 2){
            title_tv.setText("忘记密码");
            register_btn.setText("确 定");
            password_ed.setHint("请输入新密码(6-12位字母数字组合)");
            useragreement_rll.setVisibility(View.GONE);
        }
        //设置密码不可见
        password_ed.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
    private void setclick(){
        title_left_iv.setOnClickListener(this);
        getYZCode_tv.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        showPassword_iv.setOnClickListener(this);
        userAgreement_tv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_1_left_iv://返回键
                Intent intent1 = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.register_invisible_iv://显示密码
                if (!isShowPassword){
                    showPassword_iv.setImageResource(R.mipmap.login_password_visual);
                    //设置EditText文本为可见的
                    password_ed.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPassword = true;
                }else {
                    showPassword_iv.setImageResource(R.mipmap.login_password_invisible);
                    //设置EditText文本为隐藏的
                    password_ed.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isShowPassword = false;
                }
                break;
            case R.id.register_btn://注册 btn
                registerTiShi_rll.setVisibility(View.GONE);
                yzm = yzCode.getText().toString().trim();
                phoneNum = phoneNumber_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(yzm)){
                    setCheckCode(phoneNum,yzm);
                }else {
                    Toast.makeText(RegisterActivity.this,"请输入验证码！",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.register_get_yanzhengma_tv://获取验证码
                phoneNum = phoneNumber_ed.getText().toString().trim();
                Log.e("手机正则", phoneNum+String.valueOf(Utils.isMobileNO(phoneNum)));
                if (Utils.isMobileNO(phoneNum)){//匹配成功获取验证码
                    getYZCode(phoneNum);
                    time = new TimeCount(60000,1000);
                    time.start();
                }else {
                    Toast.makeText(RegisterActivity.this,"请输入正确的手机号！",Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.register_useragreement_tv:
                Intent intent3 = new Intent(RegisterActivity.this, UserAgreeMentActivity.class);
                startActivity(intent3);
                break;
            default:
                break;
        }
    }
    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {// 计时完毕
            getYZCode_tv.setText("重新获取验证码");
            getYZCode_tv.setClickable(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {// 计时过程
            getYZCode_tv.setClickable(false);//防止重复点击
            getYZCode_tv.setText(millisUntilFinished / 1000 + "s");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getYZCode = "GetVCode";
    private String checkCode = "CheckVCode";

    /**
     *   获取短信验证码
     * @param phoneNumber  电话号码
     */
    private void getYZCode(String phoneNumber){
        XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.Other);
        xutilsHelper.add("phone_num", phoneNumber);
        xutilsHelper.add("method", getYZCode);
        Log.e(TAG + "获取验证码", phoneNumber);
        String sign = MD5Utils.MD5Encode(getYZCode + phoneNumber + URLUtils.MD5_SIGN, "");
        xutilsHelper.add("sign", sign);
        //xutilsHelper.sendPost(1,this);
        xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String yzResult) {
                Log.e(TAG + "验证码",yzResult);
                try {
                    JSONObject jsonDelete = new JSONObject(yzResult);
                    if (jsonDelete.getInt("status") == 9999) {
                        Toast.makeText(RegisterActivity.this, "获取验证码成功",Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
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
    }

    /**
     *  校验验证码
     * @param phoneNumber 电话号码
     * @param yzCode 验证码
     */
    private void setCheckCode(String phoneNumber, String yzCode) {
        String sign = MD5Utils.MD5Encode(checkCode + phoneNumber + yzCode + URLUtils.MD5_SIGN, "");
        XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.Other);
        xutilsHelper.add("phone_num", phoneNumber);
        xutilsHelper.add("v_code", yzCode);
        xutilsHelper.add("method", checkCode);
        xutilsHelper.add("sign", sign);
       // xutilsHelper.sendPost(2,this);
        xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String Result) {
                Log.e(TAG + "验证码",Result);
                try {
                    JSONObject jsonDelete = new JSONObject(Result);
                    if (jsonDelete.getInt("status") == 9999) {
                        // Toast.makeText(RegisterActivity.this,"提交验证码成功",Toast.LENGTH_LONG).show();
                        String pwd = password_ed.getText().toString().trim();
                        if (!TextUtils.isEmpty(pwd) && Utils.rexCheckPassword(pwd)) {
                            Register(pwd);
                        } else {
                            Toast.makeText(RegisterActivity.this,"请输入合法的密码",Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
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
    }

    /**
     *  注册方法
     * @param pwd  密码password
     */
    private void Register(String pwd){
        Log.e(TAG, phoneNum + pwd);
        String aesAccount = null;
        try {
            aesAccount = AESOperator.encrypt(phoneNum, URLUtils.AES_SIGN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //对注册密码进行MD5 和AES加密'
        String md5Pwd = MD5Utils.GetMD5Code(pwd + URLUtils.MD5_SIGN);
        Log.e(TAG,"mdddd:"+md5Pwd);
        String sign_pwd = null;
        try {
            sign_pwd = AESOperator.encrypt(md5Pwd,URLUtils.AES_SIGN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "加密后密码：" + sign_pwd);
        XutilsHelper xutil = new XutilsHelper(URLUtils.USERNAME_URL);
        if (type == 1) {//注册
            String registSign = aesAccount + METHOD_REGISTER + sign_pwd + URLUtils.MD5_SIGN;
            String sign = MD5Utils.MD5Encode(registSign, "");
            Log.e(TAG, "sign:"+sign);
            Log.e(TAG, "sign_pwd:"+sign_pwd);
            xutil.add("account",aesAccount);
            xutil.add("password",sign_pwd);
            xutil.add("method",METHOD_REGISTER);
            xutil.add("sign",sign);
           // xutil.sendPost(4,RegisterActivity.this);
            xutil.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String registResult) {
                    try {
                        JSONObject regJson = new JSONObject(registResult);
                        int status = regJson.getInt("status");
                        if (status == 9999) {//注册成功
                            Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        } else if (status == 0003) {//帐号已存在
                            registerTiShi_rll.setVisibility(View.VISIBLE);
                        } else {
                            Log.e(TAG, regJson.getString("error"));
                            Toast.makeText(RegisterActivity.this, regJson.getString("error"), Toast.LENGTH_LONG).show();

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
        } else if (type == 2) {//忘记密码
            String sign = MD5Utils.MD5Encode(aesAccount + METHOD_FORGETPASSWORD + sign_pwd + URLUtils.MD5_SIGN, "");
            xutil.add("account",aesAccount);
            xutil.add("password",sign_pwd);
            xutil.add("method",METHOD_FORGETPASSWORD);
            xutil.add("sign",sign);
           // xutil.sendPost(3,RegisterActivity.this);
            xutil.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String forgetPwdResult) {
                    try {
                        JSONObject json = new JSONObject(forgetPwdResult);
                        if (json.getInt("status") == 9999) {
                               /* Utils.showDialogTwo(RegisterActivity.this, "密码重置成功,请重新登陆", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                    }
                                });*/
                            final AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            View view = View.inflate(RegisterActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("密码重置成功,请重新登陆");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }
                            });
                            builder.create().show();
                        } else {
                            Toast.makeText(RegisterActivity.this, json.getString("error"), Toast.LENGTH_LONG).show();
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
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
