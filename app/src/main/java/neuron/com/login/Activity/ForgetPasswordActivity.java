package neuron.com.login.Activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2016/8/30.
 */
public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener{
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
    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.register);
        init();
        setOnclick();
    }
    private void init(){
        title_left_iv = (ImageView) findViewById(R.id.title_1_left_iv);
        title_tv = (TextView) findViewById(R.id.title_1_tv);
        title_tv.setText("忘记密码");
        getYZCode_tv = (TextView) findViewById(R.id.register_get_yanzhengma_tv);
        phoneNumber_ed = (EditText) findViewById(R.id.register_inputiphong_ed);
        yzCode = (EditText) findViewById(R.id.register_inputcode_ed);
        password_ed = (EditText) findViewById(R.id.register_inputpassword_ed);
        showPassword_iv = (ImageView) findViewById(R.id.register_invisible_iv);
        register_btn = (Button) findViewById(R.id.register_btn);
        register_btn.setText("确 定");
        registerTiShi_rll = (RelativeLayout) findViewById(R.id.register_tishi_rll);
    }
    private void setOnclick(){
        title_left_iv.setOnClickListener(this);
        getYZCode_tv.setOnClickListener(this);
        register_btn.setOnClickListener(this);
        showPassword_iv.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.title_1_left_iv://返回键
                break;
            case R.id.register_invisible_iv://显示密码
                break;
            case R.id.register_btn://确定 btn
                break;
            case R.id.register_get_yanzhengma_tv://获取验证码
                break;
            default:
                break;
        }
    }
}
