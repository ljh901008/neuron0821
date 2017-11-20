package neuron.com.login.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.ex.HttpException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import neuron.com.app.OgeApplication;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.database.UserDaoBean;
import neuron.com.lock.activity.GestureEditActivity;
import neuron.com.lock.activity.GestureVerifyActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.NetWorkUtil;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/8/25.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = "LoginActivity";
    /**
     * 帐号，密码
     */
    private EditText account_ed,password_ed;
    //选择帐号，显示密码
    private RelativeLayout selectAccount_rll,showPassword_rll;
    private ImageView isShowPassword_iv;
    //登陆
    private Button login_btn;
    //忘记帐号和忘记密码
    private TextView register_tv,forgetPassword_tv;
    //匹配手机正则表达式
    String s = "^(13[0-9]|15[0|3|6|7|8|9]|18[8|9])\\d{8}$";
    //用户名和密码
    private String userName,userPwd;
    //加密后的帐号
    private String aesUserName;
    private String token = null;
    /**
     * 检查帐号可用性 的方法名
     */
    private String CHECKACCOUNT_METHOD = "CheckAccount";
    /**
     * 登陆的方法名
     */
    private String LOGIN_METHOD = "Login";

    private SharedPreferencesManager spm;

    private PopupWindow popupWindow;
    private String dirName = "/NeuronAccount";
    private String fileName = "account.txt";
    private View view;
    private ListView popWindow_lv;
    private List<UserDaoBean> listBean;
    private StringBuffer stringBuffer;
    private WaitDialog mWaitDialog;
    //是否显示密码
    private boolean isShowPassword = false;
    //从手势密码页面进来的标记 0忘记手势密码  1其他方式登录
    private int type;
    private Intent intent;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
        setOnclick();
    }

    private void init(){
        spm = SharedPreferencesManager.getInstance(LoginActivity.this);
        account_ed = (EditText) findViewById(R.id.login_name_ed);
        password_ed = (EditText) findViewById(R.id.login_password_ed);
        selectAccount_rll = (RelativeLayout) findViewById(R.id.login_select_account_rll);
        showPassword_rll = (RelativeLayout) findViewById(R.id.login_select_password_rll);
        login_btn = (Button) findViewById(R.id.login_login_btn);
        register_tv = (TextView) findViewById(R.id.login_register_tv);
        forgetPassword_tv = (TextView) findViewById(R.id.login_forgetpassword_tv);
        isShowPassword_iv = (ImageView) findViewById(R.id.login_visiblepwd_iv);
        if (spm.has("account")) {
            account_ed.setText(spm.get("account"));
            password_ed.setText("");
        }
    }
    private void setOnclick(){
        selectAccount_rll.setOnClickListener(this);
        showPassword_rll.setOnClickListener(this);
        login_btn.setOnClickListener(this);
        register_tv.setOnClickListener(this);
        forgetPassword_tv.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.login_select_account_rll://选择帐号
                if (spm.has("accounts")) {
                    showPopupWindow();
                }
                break;
            case R.id.login_select_password_rll://显示密码
                if (!isShowPassword){
                    isShowPassword_iv.setImageResource(R.mipmap.login_password_visual);
                    //设置EditText文本为可见的
                    password_ed.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isShowPassword = true;
                }else {
                    isShowPassword_iv.setImageResource(R.mipmap.login_password_invisible);
                    //设置EditText文本为隐藏的
                    password_ed.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isShowPassword = false;
                }
                break;
            case R.id.login_login_btn://登陆按钮，先校验帐号
                if (!NetWorkUtil.checkEnable(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "网络不通，请重试", Toast.LENGTH_LONG).show();
                    Utils.dismissWaitDialog(mWaitDialog);
                    return;
                }
                userName = account_ed.getText().toString().trim();
                userPwd = password_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(userName) && userName.length() < 12) {
                    if (!TextUtils.isEmpty(userPwd) && Utils.rexCheckPassword(userPwd)) {
                        if (spm == null) {
                            spm = SharedPreferencesManager.getInstance(LoginActivity.this);
                        }
                        if (spm.has("accounts")) {
                            String s = spm.get("accounts");
                            Log.e(TAG + "帐号数组", s);
                            if (!s.contains(userName + ",")) {
                                String accounts = s + userName + ",";
                                spm.save("accounts", accounts);
                            }
                        } else {
                            spm.save("accounts", userName + ",");
                        }
                        Utils.showWaitDialog(getString(R.string.loadtext_login), LoginActivity.this,mWaitDialog);
                        try {
                            aesUserName = AESOperator.encrypt(userName, URLUtils.AES_SIGN);
                            String jiami = aesUserName + CHECKACCOUNT_METHOD + URLUtils.MD5_SIGN;
                            String sign = MD5Utils.MD5Encode(jiami, "");
                            XutilsHelper xutils = new XutilsHelper(URLUtils.USERNAME_URL, handler);
                            xutils.add("account", aesUserName);
                            xutils.add("method", CHECKACCOUNT_METHOD);
                            xutils.add("sign", sign);
                            xutils.sendPost2(new Callback.CommonCallback<String>() {
                                @Override
                                public void onSuccess(String s) {
                                        try {
                                            JSONObject json = new JSONObject(s);
                                            if (json.getInt("status") == 9999) {//请求成功，可以调用登陆接口
                                                String MacAddress = getMacAddress();
                                                String aesPwd = AESOperator.encrypt(MD5Utils.MD5Encode(userPwd + URLUtils.MD5_SIGN, ""), URLUtils.AES_SIGN);
                                                String signString = aesUserName + aesPwd + MacAddress + LOGIN_METHOD + URLUtils.MD5_SIGN;
                                                String sign = MD5Utils.MD5Encode(signString, "");
                                                XutilsHelper xutil = new XutilsHelper(URLUtils.USERNAME_URL, handler);
                                                xutil.add("account", aesUserName);
                                                xutil.add("password", aesPwd);
                                                xutil.add("device_identifier", MacAddress);
                                                xutil.add("method", LOGIN_METHOD);
                                                xutil.add("sign", sign);
                                                xutil.sendPost2(new CommonCallback<String>() {
                                                    @Override
                                                    public void onSuccess(String s) {
                                                        try {
                                                            JSONObject loginJson = new JSONObject(s);
                                                            if (loginJson.getInt("status") == 9999){
                                                                Utils.dismissWaitDialog(mWaitDialog);
                                                                intent = getIntent();
                                                                type = intent.getIntExtra("type", 3);
                                                                if (type == 0) {//从手势密码页面点击忘记密码进入的，要清空手势密码
                                                                    spm.remove("handlock");
                                                                }
                                                                token = loginJson.getString("token");
                                                                JSONObject jsonObject = loginJson.getJSONObject("msg");
                                /*if (spm.has("account")) {
                                    //如果重新登录帐号和已保存帐号不一样就删除萤石token
                                    if (!jsonObject.getString("account").equals(spm.get("account"))) {
                                        if (spm.has("EZToken")) {
                                            spm.remove("EZToken");
                                        }
                                    }
                                }*/
                                                                spm.save("account",userName);
                                                                spm.save("token",token);
                                                                Log.e(TAG + "token", token);
                                                                //储存昵称
                                                                spm.save("username", jsonObject.getString("username"));
                                                                //储存帐号权限
                                                                spm.save("userType", jsonObject.getString("type"));
                                                                JSONArray jsonArray = jsonObject.getJSONArray("engine_list");
                                                                int engineLength = jsonArray.length();
                                                                if (engineLength > 0) {
                                                                    for (int i = 0; i < engineLength; i++) {
                                                                        JSONObject js = jsonArray.getJSONObject(i);
                                                                        int engine_default = js.getInt("_default");
                                                                        if (engine_default == 0) {//表示有默认的控制主机 如果第一次登陆的话是没有控制主机的
                                                                            spm.save("engine_id", js.getString("engine_id"));
                                                                            spm.save("is_belong", String.valueOf(js.getInt("is_belong")));
                                                                        }
                                                                    }
                                                                } else {
                                                                    if (spm.has("engine_id")) {
                                                                        spm.remove("engine_id");
                                                                    }
                                                                }
                                                                if (spm.has("EZToken")) {//确保换张号的时候不会出现无法播放的问题
                                                                    spm.remove("EZToken");
                                                                }
                                                                JSONObject jsonPath = jsonObject.getJSONObject("photo_path");
                                                                String photoPath = jsonPath.getString("Android");
                                                                Log.e("登陆界面头像路径", photoPath);
                                                                spm.save("photo_path", photoPath);
                                                                //可以在次做是否是第一次登陆的判断处理
                                                                if (spm.has("isFirstLogin")) {//如果登录过，这个值肯定存在
                                                                    //第一次登录后在手势密码页面和我的页面里边可以设定这个值，0跳过，1验证手势密码
                                                                    if (!"0".equals(spm.get("isFirstLogin"))) {
                                                                        if (spm.has("handlock")) {
                                                                            Intent intent = new Intent(getApplicationContext(), GestureVerifyActivity.class);
                                                                            startActivity(intent);
                                                                        } else {
                                                                            Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
                                                                            startActivity(intent3);
                                                                        }
                                                                    } else {
                                                                        Intent intent3 = new Intent(getApplicationContext(), MainActivity.class);
                                                                        startActivity(intent3);
                                                                    }
                                                                } else {
                                                                    Intent intent = new Intent(LoginActivity.this, GestureEditActivity.class);
                                                                    startActivity(intent);
                                                                }
                                                                if (spm.has("account")) {
                                                                    JPushInterface.setAlias(LoginActivity.this, spm.get("account"), new TagAliasCallback() {
                                                                        @Override
                                                                        public void gotResult(int i, String s, Set<String> set) {
                                                                            if (i == 0) {//设置失败
                                                                                // Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                                                                                Log.e("设置别名", "成功！！！！！" + s);
                                                                            }
                                                                        }
                                                                    });
                                                                    //设置别名
                                                                    // JPushInterface.setAlias(LoginActivity.this,1, spm.get("account"));
                                                                }
                                                            }else {
                                                                Utils.dismissWaitDialog(mWaitDialog);
                                                                Toast.makeText(LoginActivity.this,loginJson.getString("error"),Toast.LENGTH_LONG).show();
                                                            }
                                                        } catch (JSONException e) {
                                                            Utils.dismissWaitDialog(mWaitDialog);
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
                                               // xutil.sendPost(2, LoginActivity.this);
                                            } else {
                                                Utils.dismissWaitDialog(mWaitDialog);
                                                Toast.makeText(LoginActivity.this, json.getString("error"), Toast.LENGTH_LONG).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                }

                                @Override
                                public void onError(Throwable throwable, boolean b) {
                                    if (throwable instanceof HttpException) { // 网络错误
                                        HttpException httpEx = (HttpException) throwable;
                                        int responseCode = httpEx.getCode();
                                        String responseMsg = httpEx.getMessage();
                                        String errorResult = httpEx.getResult();
                                        Toast.makeText(LoginActivity.this, responseMsg, Toast.LENGTH_LONG).show();
                                    } else { // 其他错误
                                        Toast.makeText(LoginActivity.this,"网络超时",Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(CancelledException e) {

                                }

                                @Override
                                public void onFinished() {

                                }
                            });
                            //xutils.sendPost(1, this);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "请输入密码", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "请输入正确的用户名", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.login_register_tv://注册
                Intent intent1 = new Intent(LoginActivity.this,RegisterActivity.class);
                intent1.putExtra("type", 1);
               //Intent intent = new Intent(this,EZ_OpenServiceActivity.class);
                startActivity(intent1);
                break;
            case R.id.login_forgetpassword_tv://忘记密码
                Intent intent2 = new Intent(LoginActivity.this,RegisterActivity.class);
                intent2.putExtra("type", 2);
                startActivity(intent2);
               /* Intent intent_1 = new Intent(this,EZ_CameraListActivity.class);
                startActivity(intent_1);*/
                break;
            default:
                break;
        }
    }

    /**
     *   获取手机MAC地址的方法
     * @return  ：手机的MAC地址
     */
    private String getMacAddress(){
        String result = "";
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        result = wifiInfo.getMacAddress();
        return result;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    PopWindowAdapter adapter;
    private  void showPopupWindow() {
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.popwindow_listv, null);
            popWindow_lv = (ListView) view.findViewById(R.id.popwindow_listv_lv);
            String listAS = spm.get("accounts");
            String[] s = listAS.split(",");
            listBean = new ArrayList<UserDaoBean>();
            UserDaoBean bean;
            for (int i = 0; i < s.length; i++) {
                if (i < 9) {
                    bean = new UserDaoBean();
                    bean.setAccount(s[i]);
                    listBean.add(bean);
                }
            }
            //listBean = SQLiteUtils.query();
            adapter = new PopWindowAdapter(listBean);
            popWindow_lv.setAdapter(adapter);
            popupWindow = new PopupWindow(view);
            popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        }
        //获取popuwindow 的焦点
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //在选择帐号控件下方弹出
        popupWindow.showAsDropDown(account_ed);
        popWindow_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                account_ed.setText(listBean.get(i).getAccount());
                password_ed.setText("");
                popupWindow.dismiss();
            }
        });

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;
    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 1300);
        } else {
            OgeApplication.quiteApplication();

        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OgeApplication.quiteApplication();
    }

    public class PopWindowAdapter extends BaseAdapter {
        private List<UserDaoBean> list;

        public PopWindowAdapter(List<UserDaoBean> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            if (list != null) {
                return list.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            ViewHolder holder;
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.popwindow_item, null);
                holder.account = (TextView) view.findViewById(R.id.popwindow_item_tv);
                holder.delete = (ImageButton) view.findViewById(R.id.popwindow_item_delete_ibtn);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.account.setText(list.get(position).getAccount());
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    list.remove(position);
                    adapter.notifyDataSetChanged();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0; i < list.size(); i++) {
                        stringBuffer.append(list.get(i).getAccount() + ",");
                    }
                    spm.save("accounts", String.valueOf(stringBuffer));
                }
            });
            return view;
        }
        class ViewHolder {
            TextView account;
            ImageButton delete;
        }
    }
}
