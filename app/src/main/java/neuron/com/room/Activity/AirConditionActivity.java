package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by ljh on 2017/4/17. 空调详情页
 */
public class AirConditionActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AirConditionActivity";
    private ImageButton back_ibtn;
    //温度加，减，电源，模式，风速
    private ImageButton addTemp_ibtn, minusTemp_ibtn,powersource_ibtn,pattern_ibtn, airspeed_ibtn;
    //空调名称，房间名，温度值，模式文字显示，风速规格显示
    private TextView airName_tv,roomName_tv,temperatureValue_tv,airPattern_tv, airSpeed_tv;
    //下方按键文字  电源 ，模式，风速
    private TextView powerSource_tv,pattern_tv, airSpeedtwo_tv;
    //编辑按键
    private Button edit_btn;
    private Intent intent;
    private String deviceId, deviceType;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private String account,engineId, token;
    private String method = "GetControlledDeviceDetail";
    private String airName,airRoom,roomId,airBrand,airSerial,airStatus;
    private String operationMethod = "RemoteControlAC";
    private WaitDialog mWaitDialog;
    //温度值，模式值，风速值,电源值
    private String tempValue,patternValue, speedValue,powerSourceValue;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1:
                    if (msg.what == 102) {
                        String tvResult = (String) msg.obj;
                        Log.e(TAG + "空调详情",tvResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(tvResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                                JSONObject jsonBasic = jsonMsg.getJSONObject("basic_msg");
                                airName = jsonBasic.getString("controlled_device_name");
                                airRoom = jsonBasic.getString("room_name");
                                airName_tv.setText(airName);
                                roomName_tv.setText(airRoom);
                                airBrand = jsonBasic.getString("controlled_device_brand");
                                airSerial = jsonBasic.getString("controlled_device_serial");
                                roomId = jsonBasic.getString("room_id");
                                deviceId = jsonBasic.getString("controlled_device_id");
                                deviceType = jsonBasic.getString("electric_type_id");
                                airStatus = jsonBasic.getString("status");
                                if (!TextUtils.isEmpty(airStatus)) {
                                    String[] s = airStatus.split(",");
                                    powerSourceValue = s[0];//电源值
                                    patternValue = s[1];//模式
                                    speedValue = s[2];//风速
                                    tempValue = s[3];//温度值
                                    if ("0".equals(s[0])) {
                                        powersource_ibtn.setImageResource(R.mipmap.air_temperature_open);
                                        powerSource_tv.setText("开启");
                                        powerSource_tv.setTextColor(getResources().getColor(R.color.yellow));
                                        if ("0".equals(s[2])) {
                                            pattern_ibtn.setImageResource(R.mipmap.air_temperature_cold);
                                            pattern_tv.setText("制冷");
                                            airPattern_tv.setText("模式:制冷");
                                            pattern_tv.setTextColor(getResources().getColor(R.color.yellow));
                                        } else {
                                            pattern_ibtn.setImageResource(R.mipmap.air_temperature_hot);
                                            pattern_tv.setText("制热");
                                            airPattern_tv.setText("模式:制热");
                                            pattern_tv.setTextColor(getResources().getColor(R.color.yellow));
                                        }
                                        if ("0".equals(s[2])) {
                                            airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed);
                                            airSpeed_tv.setText("风速:高");
                                            airSpeedtwo_tv.setText("风速:高");
                                            airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.yellow));
                                        } else if ("1".equals(s[2])) {
                                            airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed);
                                            airSpeed_tv.setText("风速:中");
                                            airSpeedtwo_tv.setText("风速:中");
                                            airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.yellow));
                                        } else {
                                            airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed);
                                            airSpeed_tv.setText("风速:低");
                                            airSpeedtwo_tv.setText("风速:低");
                                            airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.yellow));
                                        }
                                    } else {
                                        powersource_ibtn.setImageResource(R.mipmap.air_temperature_close);
                                        powerSource_tv.setText("关闭");
                                        powerSource_tv.setTextColor(getResources().getColor(R.color.white));
                                        if ("0".equals(s[1])) {
                                            pattern_ibtn.setImageResource(R.mipmap.air_temperature_cold_close);
                                            pattern_tv.setText("制冷");
                                            airPattern_tv.setText("模式:制冷");
                                            pattern_tv.setTextColor(getResources().getColor(R.color.white));
                                        } else {
                                            pattern_ibtn.setImageResource(R.mipmap.air_temperature_hot_close);
                                            pattern_tv.setText("制热");
                                            airPattern_tv.setText("模式:制热");
                                            pattern_tv.setTextColor(getResources().getColor(R.color.white));
                                        }
                                        if ("0".equals(s[2])) {
                                            airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed_close);
                                            airSpeed_tv.setText("风速:高");
                                            airSpeedtwo_tv.setText("风速:高");
                                            airSpeed_tv.setTextColor(getResources().getColor(R.color.white));
                                            airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.white));
                                        } else if ("1".equals(s[2])) {
                                            airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed_close);
                                            airSpeed_tv.setText("风速:中");
                                            airSpeedtwo_tv.setText("风速:中");
                                            airSpeed_tv.setTextColor(getResources().getColor(R.color.white));
                                            airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.white));
                                        } else {
                                            airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed_close);
                                            airSpeed_tv.setText("风速:低");
                                            airSpeedtwo_tv.setText("风速:低");
                                            airSpeed_tv.setTextColor(getResources().getColor(R.color.white));
                                            airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.white));
                                        }
                                    }
                                    temperatureValue_tv.setText(s[3]);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {
                        Utils.dismissWaitDialog(mWaitDialog);
                        Toast.makeText(AirConditionActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2://操作空调
                    if (msg.what == 102) {
                        Utils.dismissWaitDialog(mWaitDialog);
                        String updateResult = (String) msg.obj;
                        Log.e(TAG + "空调操作", updateResult);
                        try {
                            JSONObject jsonObject = new JSONObject(updateResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Toast.makeText(AirConditionActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(AirConditionActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
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
        setContentView(R.layout.air_conditionoperate);
        init();
        setListener();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getStatus(deviceId,deviceType);
    }

    private void init() {
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        deviceType = intent.getStringExtra("deviceType");
        back_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_back_iv);
        addTemp_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_add_ibtn);
        minusTemp_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_minus_ibtn);
        powersource_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_powersource_ibtn);
        pattern_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_pattern_ibtn);
        airspeed_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_airspeed_ibtn);
        airName_tv = (TextView) findViewById(R.id.air_conditionoperate_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.air_conditionoperate_roomname_tv);
        temperatureValue_tv = (TextView) findViewById(R.id.air_conditionoperate_temperaturevalue_tv);
        airPattern_tv = (TextView) findViewById(R.id.air_conditionoperate_airpattern_tv);
        airSpeed_tv = (TextView) findViewById(R.id.air_conditionoperate_airspeed_tv);
        powerSource_tv = (TextView) findViewById(R.id.air_conditionoperate_powersource_tv);
        pattern_tv = (TextView) findViewById(R.id.air_conditionoperate_pattern_tv);
        airSpeedtwo_tv = (TextView) findViewById(R.id.air_conditionoperate_airspeedtwo_tv);
        edit_btn = (Button) findViewById(R.id.air_conditionoperate_edit_btn);
    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        addTemp_ibtn.setOnClickListener(this);
        minusTemp_ibtn.setOnClickListener(this);
        powersource_ibtn.setOnClickListener(this);
        pattern_ibtn.setOnClickListener(this);
        airspeed_ibtn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.air_conditionoperate_back_iv://返回键
                finish();
                break;
            case R.id.air_conditionoperate_add_ibtn://温度加
                if ("0".equals(powerSourceValue)) {
                    tempValue = String.valueOf(Integer.valueOf(tempValue) + 1);
                    if (Integer.valueOf(tempValue) > 30) {
                        tempValue = "30";
                        temperatureValue_tv.setText(tempValue);
                        operation(patternValue, tempValue, speedValue);
                    } else {
                        temperatureValue_tv.setText(tempValue);
                        operation(patternValue, tempValue, speedValue);
                    }
                } else {
                    tempValue = String.valueOf(Integer.valueOf(tempValue) + 1);
                    if (Integer.valueOf(tempValue) > 30) {
                        tempValue = "30";
                        temperatureValue_tv.setText(tempValue);
                    } else {
                        temperatureValue_tv.setText(tempValue);
                    }
                }
                break;
            case R.id.air_conditionoperate_minus_ibtn://温度减
                if ("0".equals(powerSourceValue)) {
                    tempValue = String.valueOf(Integer.valueOf(tempValue) - 1);
                    if (Integer.valueOf(tempValue) < 18) {
                        tempValue = "18";
                        temperatureValue_tv.setText(tempValue);
                        operation(patternValue, tempValue, speedValue);
                    } else {
                        temperatureValue_tv.setText(tempValue);
                        operation(patternValue, tempValue, speedValue);
                    }
                } else {
                    tempValue = String.valueOf(Integer.valueOf(tempValue) - 1);
                    if (Integer.valueOf(tempValue) < 18) {
                        tempValue = "18";
                        temperatureValue_tv.setText(tempValue);
                    } else {
                        temperatureValue_tv.setText(tempValue);
                    }
                }
                break;
            case R.id.air_conditionoperate_powersource_ibtn://电源
                if ("0".equals(powerSourceValue)) {//开
                    powerSourceValue = "1";
                    powersource_ibtn.setImageResource(R.mipmap.air_temperature_close);
                    if ("0".equals(patternValue)) {
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_cold_close);
                    } else {
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_hot_close);
                    }
                    airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed_close);
                    powerSource_tv.setTextColor(getResources().getColor(R.color.white));
                    pattern_tv.setTextColor(getResources().getColor(R.color.white));
                    airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.white));
                    operation("0","0","0");//关闭
                } else {
                    powerSourceValue = "0";
                    powersource_ibtn.setImageResource(R.mipmap.air_temperature_open);
                    if ("0".equals(patternValue)) {
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_cold);
                    } else {
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_hot);
                    }
                    airspeed_ibtn.setImageResource(R.mipmap.air_temperature_speed);
                    powerSource_tv.setTextColor(getResources().getColor(R.color.yellow));
                    pattern_tv.setTextColor(getResources().getColor(R.color.yellow));
                    airSpeedtwo_tv.setTextColor(getResources().getColor(R.color.yellow));
                    operation(patternValue,tempValue,speedValue);
                }
                break;
            case R.id.air_conditionoperate_pattern_ibtn://模式
                if ("0".equals(powerSourceValue)) {
                    if ("0".equals(patternValue)) {//制冷
                        patternValue = "1";
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_hot);
                        operation(patternValue, tempValue, speedValue);
                        airPattern_tv.setText("模式:制热");
                        pattern_tv.setText("制热");
                    } else {//制热
                        patternValue = "0";
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_cold);
                        operation(patternValue, tempValue, speedValue);
                        airPattern_tv.setText("模式:制冷");
                        pattern_tv.setText("制冷");
                    }
                } else {
                    if ("0".equals(patternValue)) {//制冷
                        patternValue = "1";
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_hot);
                        airPattern_tv.setText("模式:制热");
                        pattern_tv.setText("制热");
                    } else {//制热
                        patternValue = "0";
                        pattern_ibtn.setImageResource(R.mipmap.air_temperature_cold);
                        airPattern_tv.setText("模式:制冷");
                        pattern_tv.setText("制冷");
                    }
                }
                break;
            case R.id.air_conditionoperate_airspeed_ibtn://风速
                if ("0".equals(powerSourceValue)) {
                    if ("0".equals(speedValue)) {//风速高
                        speedValue = "2";
                        airSpeedtwo_tv.setText("风速:低");
                        airSpeed_tv.setText("风速:低");
                        operation(patternValue, tempValue, speedValue);
                    } else if ("1".equals(speedValue)) {//中
                        speedValue = "0";
                        airSpeedtwo_tv.setText("风速:高");
                        airSpeed_tv.setText("风速:高");
                        operation(patternValue, tempValue, speedValue);
                    } else if ("2".equals(speedValue)) {//低
                        speedValue = "1";
                        airSpeedtwo_tv.setText("风速:中");
                        airSpeed_tv.setText("风速:中");
                        operation(patternValue, tempValue, speedValue);
                    }
                } else {
                    if ("0".equals(speedValue)) {//风速高
                        speedValue = "2";
                        airSpeedtwo_tv.setText("风速:低");
                        airSpeed_tv.setText("风速:低");
                    } else if ("1".equals(speedValue)) {//中
                        speedValue = "0";
                        airSpeedtwo_tv.setText("风速:高");
                        airSpeed_tv.setText("风速:高");
                    } else if ("2".equals(speedValue)) {//低
                        speedValue = "1";
                        airSpeedtwo_tv.setText("风速:中");
                        airSpeed_tv.setText("风速:中");
                    }
                }
                break;
            case R.id.air_conditionoperate_edit_btn://编辑
                Intent intent = new Intent(AirConditionActivity.this, EditActivity.class);
                intent.putExtra("deviceName", airName);
                intent.putExtra("deviceRoom", airRoom);
                intent.putExtra("brand", airBrand);
                intent.putExtra("serial", airSerial);
                intent.putExtra("roomId", roomId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("deviceType", deviceType);
                intent.putExtra("type", 1);
                startActivityForResult(intent,102);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 102) {
                if (data != null) {
                    airRoom = data.getStringExtra("roomName");
                    airName = data.getStringExtra("deviceName");
                    roomName_tv.setText(airRoom);
                    airName_tv.setText(airName);
                }
            }
        }
    }

    /**
     * 获取空调的详情
     */
    private void getStatus(String deviceId,String deviceType){

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

            Utils.showWaitDialog(getString(R.string.loadtext_load),AirConditionActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceType + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("electric_type_id", deviceType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *   空调操作
     * @param modeType  模式
     * @param temperature  温度
     * @param wind_speed  风速
     */
    private void operation(String modeType,String temperature,String wind_speed){
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
            Log.e(TAG + "空调操作数据流", modeType + "," + temperature + "," + wind_speed);
            Utils.showWaitDialog("加载中...", AirConditionActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + engineId + operationMethod
                    + modeType + temperature + token + wind_speed + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("mode_type", modeType);
            xutilsHelper.add("temperature", temperature);
            xutilsHelper.add("wind_speed", wind_speed);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", operationMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(2,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
