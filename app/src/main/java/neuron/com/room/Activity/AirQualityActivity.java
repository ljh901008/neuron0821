package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/1/16. 空气质量检测仪
 */
public class AirQualityActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AirQualityActivity";

    private ImageView back_iv;
    private TextView yuSheTitle_tv;
    // 设备名称 房间名
    private TextView deviceName_tv,roomName_tv;
    //编辑按钮
    private Button redact_btn;
    //中间pm2.5的值  状态：优良字体,pm名称，pm单位
    private TextView pm_tv, pm_youliang_tv,pmName_tv,pmUnit_tv;
    //温度值，湿度值，甲醛值，时间
    private TextView tempValue_tv,humidityValue_tv,formaldehydeValue_tv, timeValue_tv;
    //预设温度最大值，温度最小值，湿度最大值，湿度最小值，甲醛值，PM2.5的值
    private TextView yTempMax_tv,yTempMin_tv,yHumidityMax_tv,yHumidityMin_tv,yFormaldehyde_tv, yPM_tv;
    //温度最大时触发的场景名称，温度最小是触发的场景，湿度最大时触发的场景，湿度最小时触发的场景，甲醛值超标时触发的场景，pm场景
    private TextView tempMaxScene_tv,tempMinScene_tv,humidityMaxScene_tv,humidityMinScene_tv,formaldehydeScene_tv, pmScene_tv;

    private SharedPreferencesManager sharedPreferencesManager = null;
    private String account,token,engineId,airId, airType;
    private String airName, airRoom,airStatus;
    //温度，甲醛，湿度，pm2.5值,时间
    private String temperature,formaldehyde,humidity, pm,time;
    private Intent intent;
    private String method = "GetControlledDeviceDetail";
    private String result = null;
    private WaitDialog mWaitDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://空气质量检测仪详情
                    if (msg.what == 102) {
                        result = (String) msg.obj;
                        Log.e("sirResult", result);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                                JSONObject json = jsonMsg.getJSONObject("basic_msg");
                                airName = json.getString("controlled_device_name");
                                airRoom = json.getString("room_name");
                                airStatus = json.getString("status");
                                deviceName_tv.setText(airName);
                                roomName_tv.setText(airRoom);
                                if (!TextUtils.isEmpty(airStatus)) {
                                    String[] s = airStatus.split(",");

                                    int slength = s.length;
                                    if (slength == 1) {
                                        tempValue_tv.setText(s[0] + "℃");//温度
                                    } else if (slength == 2) {
                                        tempValue_tv.setText(s[0] + "℃");//温度
                                        humidityValue_tv.setText(s[1] + "%");//湿度
                                    } else if (slength == 3) {
                                        tempValue_tv.setText(s[0] + "℃");//温度
                                        humidityValue_tv.setText(s[1] + "%");//湿度
                                        pm_tv.setText(s[2]);//pm2.5值
                                    } else if (slength == 4) {
                                        tempValue_tv.setText(s[0] + "℃");//温度
                                        humidityValue_tv.setText(s[1] + "%");//湿度
                                        pm_tv.setText(s[2]);//pm2.5值
                                        formaldehydeValue_tv.setText(s[3] + "mg/m³");
                                    } else if (slength == 5) {

                                        tempValue_tv.setText(s[0] + "℃");//温度
                                        humidityValue_tv.setText(s[1] + "%");//湿度
                                        pm_tv.setText(s[2]);//pm2.5值
                                        formaldehydeValue_tv.setText(s[3] + "mg/m³");
                                        timeValue_tv.setText(s[4]);//时间
                                    }
                                    int pm2 = Integer.parseInt(s[2]);
                                    Log.e(TAG + "pm2.5", String.valueOf(pm2));
                                    if (pm2 > 0 && pm2 <= 25) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_1));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_1));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_1));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_1));
                                    } else if (50 >= pm2 & pm2 > 25) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_35));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_35));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_35));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_35));
                                    } else if (75 >= pm2 && pm2 > 50) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_75));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_75));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_75));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_75));
                                    } else if (89 >= pm2 && pm2 > 75) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_80));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_80));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_80));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_80));
                                    } else if (103 >= pm2 && pm2 > 89) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_90));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_90));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_90));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_90));
                                    } else if (115 >= pm2 && pm2 > 103) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_115));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_115));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_115));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_115));
                                    } else if (127 >= pm2 && pm2 > 115) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_120));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_120));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_120));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_120));
                                    } else if (139 >= pm2 && pm2 > 127) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_135));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_135));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_135));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_135));
                                    } else if (150 >= pm2 && pm2 > 139) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_150));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_150));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_150));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_150));
                                    } else if (183 >= pm2 && pm2 > 150) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_160));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_160));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_160));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_160));
                                    } else if (216 >= pm2 && pm2 > 183) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_200));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_200));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_200));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_200));
                                    } else if (250 >= pm2 && pm2 > 216) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_250));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_250));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_250));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_250));
                                    } else if (275 >= pm2 && pm2 > 250) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_275));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_275));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_275));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_275));
                                    } else if (300 >= pm2 && pm2 > 275) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_300));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_300));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_300));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_300));
                                    } else if (pm2 > 300) {
                                        pm_tv.setTextColor(getResources().getColor(R.color.pm_325));
                                        pm_youliang_tv.setTextColor(getResources().getColor(R.color.pm_325));
                                        pmName_tv.setTextColor(getResources().getColor(R.color.pm_325));
                                        pmUnit_tv.setTextColor(getResources().getColor(R.color.pm_325));
                                    }
                                    JSONArray jsonA = jsonMsg.getJSONArray("other_msg");
                                    if (jsonA.length() > 0) {
                                        for (int i = 0; i < jsonA.length(); i++) {
                                            JSONObject json1 = jsonA.getJSONObject(i);
                                            String type = json1.getString("type");
                                            if ("01".equals(type) || "03".equals(type)) {//pm2.5大于525触发一个警报
                                                yPM_tv.setVisibility(View.VISIBLE);
                                                pmScene_tv.setVisibility(View.VISIBLE);
                                                yPM_tv.setText("PM2.5:>" + json1.getString("value") + "ug/m³");
                                                pmScene_tv.setText(json1.getString("desc"));
                                            } else if ("02".equals(type) || "04".equals(type)) {//甲醛大于60就触发一个警报
                                                yFormaldehyde_tv.setVisibility(View.VISIBLE);
                                                formaldehydeScene_tv.setVisibility(View.VISIBLE);
                                                yFormaldehyde_tv.setText("TVOC:>" + json1.getString("value") + "mg/m³");
                                                formaldehydeScene_tv.setText(json1.getString("desc"));
                                            } else if ("05".equals(type)) {//温度大于设定温度会触发一个场景
                                                yTempMax_tv.setVisibility(View.VISIBLE);
                                                tempMaxScene_tv.setVisibility(View.VISIBLE);
                                                yTempMax_tv.setText("温度:>" + json1.getString("value") + "℃");
                                                tempMaxScene_tv.setText(json1.getString("desc"));
                                            } else if ("06".equals(type)) {//温度小于设定温度会触发一个场景
                                                yTempMin_tv.setVisibility(View.VISIBLE);
                                                tempMinScene_tv.setVisibility(View.VISIBLE);
                                                yTempMin_tv.setText("温度:<" + json1.getString("value") + "℃");
                                                tempMinScene_tv.setText(json1.getString("desc"));
                                            } else if ("07".equals(type)) {//湿度大于设定温度会触发一个场景
                                                yHumidityMax_tv.setVisibility(View.VISIBLE);
                                                humidityMaxScene_tv.setVisibility(View.VISIBLE);
                                                yHumidityMax_tv.setText("湿度:>" + json1.getString("value") + "%");
                                                humidityMaxScene_tv.setText(json1.getString("desc"));
                                            } else if ("08".equals(type)) {//湿度小于设定温度会触发一个场景
                                                yHumidityMin_tv.setVisibility(View.VISIBLE);
                                                humidityMinScene_tv.setVisibility(View.VISIBLE);
                                                yHumidityMin_tv.setText("湿度:<" + json1.getString("value") + "%");
                                                humidityMinScene_tv.setText(json1.getString("desc"));
                                            }
                                        }
                                    } else {
                                        yuSheTitle_tv.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(AirQualityActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
                        Utils.dismissWaitDialog(mWaitDialog);
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
        setContentView(R.layout.airquality);
        init();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getAirQualityStatus();
        setListener();
    }

    private void init() {
        intent = getIntent();
        airId = intent.getStringExtra("deviceId");
        airType = intent.getStringExtra("deviceType");
        back_iv = (ImageView) findViewById(R.id.airquality_back_iv);
        redact_btn = (Button) findViewById(R.id.airquality_edit_btn);
        deviceName_tv = (TextView) findViewById(R.id.airquality_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.airquality_roomnama_tv);
        pm_tv = (TextView) findViewById(R.id.airquality_pmvalue_tv);
        pm_youliang_tv = (TextView) findViewById(R.id.airquality_pmrank_tv);
        pmName_tv = (TextView) findViewById(R.id.airquality_pmname_tv);
        pmUnit_tv = (TextView) findViewById(R.id.airquality_pmunit_tv);
        tempValue_tv = (TextView) findViewById(R.id.airquality_tempvalue_tv);
        humidityValue_tv = (TextView) findViewById(R.id.airquality_humidityvalue_tv);
        formaldehydeValue_tv = (TextView) findViewById(R.id.airquality_formaldehydevalue_tv);
        timeValue_tv = (TextView) findViewById(R.id.airquality_timevalue_tv);

        yTempMax_tv = (TextView) findViewById(R.id.airquality_yushetempa_tv);
        yTempMin_tv = (TextView) findViewById(R.id.airquality_yushetempb_tv);
        yHumidityMax_tv = (TextView) findViewById(R.id.airquality_yushehumiditya_tv);
        yHumidityMin_tv = (TextView) findViewById(R.id.airquality_yushehumidityb_tv);
        yFormaldehyde_tv = (TextView) findViewById(R.id.airquality_yusheformaldehyde_tv);
        yPM_tv = (TextView) findViewById(R.id.airquality_yushepm_tv);

        tempMaxScene_tv = (TextView) findViewById(R.id.airquality_yushetempascene_tv);
        tempMinScene_tv = (TextView) findViewById(R.id.airquality_yushetempbscene_tv);
        humidityMaxScene_tv = (TextView) findViewById(R.id.airquality_yushehumidityascene_tv);
        humidityMinScene_tv = (TextView) findViewById(R.id.airquality_yushehumiditybscene_tv);
        formaldehydeScene_tv = (TextView) findViewById(R.id.airquality_yusheformaldehydescene_tv);
        pmScene_tv = (TextView) findViewById(R.id.airquality_yushepmscene_tv);

        yuSheTitle_tv = (TextView) findViewById(R.id.airquality_yushe_tv);

    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        redact_btn.setOnClickListener(this);
    }

    /**
     * 获取空气质量检测仪的详情
     */
    private void getAirQualityStatus(){
        Utils.showWaitDialog(getString(R.string.loadtext_load),AirQualityActivity.this,mWaitDialog);
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        }
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engineId = sharedPreferencesManager.get("engine_id");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + airId + airType + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("controlled_device_id", airId);
            xutilsHelper.add("electric_type_id", airType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.airquality_back_iv://返回
                finish();
                break;
            case R.id.airquality_edit_btn://编辑
                Intent intent = new Intent();
                intent.setClass(AirQualityActivity.this, AirQualityEditActivity.class);
                intent.putExtra("airData", result);
                startActivity(intent);
                break;
            default:
            break;
        }
    }
}
