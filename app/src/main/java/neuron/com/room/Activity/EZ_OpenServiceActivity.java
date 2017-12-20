package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.karics.library.zxing.android.CaptureActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.Date;

import neuron.com.app.OgeApplication;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/11/3.开通萤石云服务页面
 */
public class EZ_OpenServiceActivity extends BaseActivity implements View.OnClickListener{
    private ImageView back_iv;
    private EditText iphoneNumber_ed,getPhoneMsg_ed;
    private Button getAuth_btn,openRZService_btn,cancel_btn;

    private String EzTime;
    /**
     * 获取萤石短信的方法名
     */
    private String method_getMsg="msg/server/openYSService/smsCode";
    /**
     * 开通萤石服务的方法名
     */
    private String method_openService="user/server/openYSService";
    /**
     * 第三方获取accesstoken的方法名
     */
    private String method_getAccessToken = "token/getAccessToken";
    /**
     * 萤石所需参数   版本号
     */
    private static String version = "1.0";
    /**
     * 萤石所需参数   id
     */
    private static String id = "1.0";

    private String s = "1[3|4|5|7|8][0-9]{9}";
    private String phoneNumber;
    private String phoneMsg;
    private SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ezservice);
            init();
            setListener();
    }

    private void setListener() {
        back_iv.setOnClickListener(this);
        getAuth_btn.setOnClickListener(this);
        openRZService_btn.setOnClickListener(this);
        cancel_btn.setOnClickListener(this);
    }

    private void init() {
        back_iv = (ImageView) findViewById(R.id.ezserviece_back_iv);
        iphoneNumber_ed = (EditText) findViewById(R.id.ezserviece_iphonenum_ed);
        getPhoneMsg_ed = (EditText) findViewById(R.id.ezserviece_auth_ed);

        getAuth_btn = (Button) findViewById(R.id.ezserviece_auth_btn);
        openRZService_btn = (Button) findViewById(R.id.ezserviece_openservice_btn);
        cancel_btn = (Button) findViewById(R.id.ezserviece_cancle_btn);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ezserviece_back_iv://返回键
                finish();
                break;
            case R.id.ezserviece_auth_btn://获取萤石短信
                phoneNumber = iphoneNumber_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(phoneNumber)) {
                    if (sharedPreferencesManager != null) {
                        sharedPreferencesManager.save("EZphone", phoneNumber);
                    } else {
                        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
                        sharedPreferencesManager.save("EZphone", phoneNumber);
                    }
                    //Matcher matcher = Pattern.compile(s).matcher(phoneNumber);
                    if (Utils.isMobileNO(phoneNumber)) {
                        getTime();
                    } else {
                        Toast.makeText(this, "请输入正确的手机号码", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            case R.id.ezserviece_openservice_btn://开通萤石云服务、
                phoneMsg = getPhoneMsg_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(phoneMsg)) {
                    openEZ(phoneNumber, phoneMsg);
                }
                break;
            case R.id.ezserviece_cancle_btn://取消
                finish();
                break;
            default:
                break;
        }
    }
    /**
     * 获取萤石服务器时间
     */
    public void getTime(){
        XutilsHelper xutil = new XutilsHelper(URLUtils.EZTIME_URL);
        xutil.add("id", "12345646");
        xutil.add("appKey", OgeApplication.AppKey);
        //xutil.sendPost(1, this);
        xutil.sendPost2(new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String EZTimeResult) {
                //获取萤石服务器时间
                try {
                    JSONObject json=new JSONObject(EZTimeResult);
                    JSONObject result=json.getJSONObject("result");
                    JSONObject data=result.getJSONObject("data");
                    EzTime= data.getString("serverTime");
                    if (sharedPreferencesManager != null) {
                        sharedPreferencesManager.save("EZTime", EzTime);
                    }
                    //判断权限
                    if (sharedPreferencesManager.get("userType").equals("01")) {
                        if (phoneNumber.equals(sharedPreferencesManager.get("account"))) {
                            getMessage(phoneNumber);//拿到时间获取短信
                        } else {
                            Utils.showDialog(EZ_OpenServiceActivity.this,"请输入用户名手机号");
                        }
                    } else {
                        Utils.showDialog(EZ_OpenServiceActivity.this,"子帐号、分享帐号，请输入主账号开通萤石云服务的手机号");
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
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
     * 获取开通萤石云服务的短信
     */
    private void getMessage(String phoneNumber){
        String sign = MD5Utils.MD5Encode("phone:" + phoneNumber + ",method:msg/server/openYSService/smsCode,time:" + EzTime + ",secret:" + OgeApplication.screct, "");
        try {
            JSONObject json1 = new JSONObject();
            json1.put("sign", sign);
            json1.put("time", EzTime);
            json1.put("ver", version);
            json1.put("key", OgeApplication.AppKey);

            JSONObject json2 = new JSONObject();
            json2.put("phone", phoneNumber);

            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("system", json1);
            json.put("method", method_getMsg);
            json.put("params", json2);
            Log.e("OpenEzService:json:", json.toString());
            XutilsHelper xutil = new XutilsHelper(URLUtils.EZ_URL);
            xutil.addRequestParams(json);
            //xutil.sendPost(2, this);
            xutil.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String EZMsgResult) {
                    //获取萤石服务器短信
                    try {
                        JSONObject json=new JSONObject(EZMsgResult);
                        JSONObject j = json.getJSONObject("result");
                        int code = j.getInt("code");
                        String message = j.getString("msg");
                        if (code == 200) {
                            Toast.makeText(EZ_OpenServiceActivity.this, message, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(EZ_OpenServiceActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
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
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 开通萤石云服务
     */
    private void openEZ(String phoneNumber,String msgCode){

        String sss = "phone:" + phoneNumber + ",smsCode:" + msgCode + ",method:user/server/openYSService,time:" + EzTime + ",secret:" + OgeApplication.screct;
        String sign=MD5Utils.MD5Encode(sss,"");
        Log.e("OpenEzService:sign>>>", sign);
        try {
            JSONObject json = new JSONObject();
            json.put("key", OgeApplication.AppKey);
            json.put("sign", sign);
            json.put("time", EzTime);
            json.put("ver", version);

            JSONObject json2 = new JSONObject();
            json2.put("phone", phoneNumber);
            json2.put("smsCode", msgCode);

            JSONObject json1 = new JSONObject();
            json1.put("id", id);
            json1.put("system", json);
            json1.put("method", method_openService);
            json1.put("params", json2);
            Log.e("OpenEzService:json:", json1.toString());
            XutilsHelper xutil = new XutilsHelper(URLUtils.EZ_URL);
            xutil.addRequestParams(json1);
            //xutil.sendPost(3, this);
            xutil.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String EZServiceResult) {
                    //开通萤石云服务
                    try {
                        JSONObject jsonService = new JSONObject(EZServiceResult);
                        JSONObject jsonObject = jsonService.getJSONObject("result");
                        String message = jsonObject.getString("msg");
                        int code = jsonObject.getInt("code");
                        if (code == 200) {//开通成功
                            Date date = new Date();
                            long t = date.getTime() / 1000;//获取当前时间  。以秒为单位
                            long l = Integer.parseInt(EzTime);
                            long differ = t - l;
                            if (differ >= 300) {//判断是否超过5分钟
                                getTime();
                            }
                            getAccessToken(phoneNumber);
                        } else if (code == 10012) {//说明此手机号已经开通萤石云服务，直接获取accesstoken
                            Log.e("测试2", "萤石服务已开通，获取token");
                            getAccessToken(phoneNumber);
                        } else {
                            Toast.makeText(EZ_OpenServiceActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
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
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * 	第三方获取accesstoken值
     * @param phoneNumber 用户电话号码
     */
    public void getAccessToken(String phoneNumber){
        String sign = MD5Utils.MD5Encode("phone:" + phoneNumber + ",method:" + method_getAccessToken + ",time:" + EzTime + ",secret:" + OgeApplication.screct, "");
        try {
            JSONObject json1 = new JSONObject();
            json1.put("key", OgeApplication.AppKey);
            json1.put("sign", sign);
            json1.put("time", EzTime);
            json1.put("ver", version);

            JSONObject json2 = new JSONObject();
            json2.put("phone", phoneNumber);

            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("system", json1);
            json.put("method", method_getAccessToken);
            json.put("params", json2);
            XutilsHelper xutil = new XutilsHelper(URLUtils.EZ_URL);
            xutil.addRequestParams(json);
            //xutil.sendPost(4, this);
            xutil.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String EZResult) {
                    //第三方获取token
                    try {
                        JSONObject json = new JSONObject(EZResult);
                        JSONObject js1 = json.getJSONObject("result");
                        int code = js1.getInt("code");
                        if (code == 200) {
                            JSONObject js = js1.getJSONObject("data");
                            String accessTonken = js.getString("accessToken");
                            sharedPreferencesManager.save("EZToken", accessTonken);
                            Log.e("Openservicetoken", accessTonken);
                            Utils.detectionEZToken(EZ_OpenServiceActivity.this);
                            //开通服务成功 进入扫描界面
                            Intent intent = new Intent(EZ_OpenServiceActivity.this, CaptureActivity.class);
                            intent.putExtra("type", 2);
                            startActivity(intent);
                            if (sharedPreferencesManager != null) {
                                sharedPreferencesManager = null;
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
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
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
