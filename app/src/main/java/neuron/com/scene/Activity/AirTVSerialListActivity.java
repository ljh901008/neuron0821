package neuron.com.scene.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import neuron.com.bean.AirQualityBean;
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
 * Created by ljh on 2017/5/8.
 */
public class AirTVSerialListActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AirTVSerialListActivity";
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token,engineId,neuronId;
    private List<AirQualityBean> list;
    private ImageButton back_ibtn;
    //温度加，减，电源，模式，风速
    private ImageButton addTemp_ibtn, minusTemp_ibtn,powersource_ibtn,pattern_ibtn,airspeed_ibtn;
    //空调名称，房间名，温度值，模式文字显示，风速规格显示
    private TextView temperatureValue_tv,airPattern_tv, airSpeed_tv;
    //下方按键文字  电源 ，模式，风速
    private TextView powerSource_tv,pattern_tv, airSpeedtwo_tv;

    // 电源键，上，左，右，下，返回,静音键
    private ImageButton tvpowersource_ibtn,up_ibtn,left_ibtn,right_ibtn, bottom_ibtn,backMenu_itbn,voice_ibtn;
    //菜单键
    private Button menu_btn;
    private TextView deviceName_tv, room_tv;
    private Button next_btn,confirm_btn, up_btn;

    private String serialMethod = "GetSerialList";
    private String testTvMethod = "TestTV";
    private String testAirMethod = "TestAC";
    private Intent intent;
    private String brandId;
    //电视空调的标记  0 电视 ， 1 空调
    private String electricId;
    private int tvPosition = 1;
    private int airPosition = 1;
    private int airIndex, tnIndex;
    private int tvLength, airLength;

    //温度值，模式值，风速值,电源值
    private String tempValue = "18",patternValue ="0", speedValue = "2", powerSourceValue = "";
    private WaitDialog mWaitDialog;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1://电视，空调系列列表
                    if (msg.what == 102) {
                        String serialResult = (String) msg.obj;
                        Log.e(TAG + "系列列表", serialResult);
                        try {
                            JSONObject jsr = new JSONObject(serialResult);
                            if (jsr.getInt("status") == 9999) {
                                JSONArray jsBrand = jsr.getJSONArray("serial_list");
                                int length = jsBrand.length();
                                if (length > 0) {
                                    list = new ArrayList<AirQualityBean>();
                                    AirQualityBean bean;
                                    for (int i = 0; i < length; i++) {
                                        JSONObject json = jsBrand.getJSONObject(i);
                                        bean = new AirQualityBean();
                                        bean.setSceneId(json.getString("serial_id"));
                                        bean.setSceneName(json.getString("serial_name"));
                                        bean.setSelect(false);
                                        list.add(bean);
                                    }
                                    room_tv.setText(list.get(tvPosition).getSceneName() + " " + tvPosition + "/" + list.size()
                                    );
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2://电视测试
                    if (msg.what == 102) {
                        String testTvResult = (String) msg.obj;
                        Log.e(TAG + "测试电视", testTvResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(testTvResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(AirTVSerialListActivity.this, jsonObject.getString("error"));
                            } else {
                                Utils.showDialog(AirTVSerialListActivity.this,"测试成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                        Toast.makeText(AirTVSerialListActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 3://空调测试
                    if (msg.what == 102) {
                        String testairResult = (String) msg.obj;
                        Log.e(TAG + "测试空调", testairResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(testairResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(AirTVSerialListActivity.this, jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                        Toast.makeText(AirTVSerialListActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.devicelist);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();

    }
    private void init(){
        intent = getIntent();
        brandId = intent.getStringExtra("brandId");
        neuronId = intent.getStringExtra("neuronId");
        electricId = intent.getStringExtra("electricId");
        if ("0".equals(electricId)) {//电视
            initTV();
        } else if ("1".equals(electricId)) {//空调
            initAir();
        }
        Log.e(TAG + "品牌id", brandId);
        getBrandList(brandId);
    }
    private void initTV(){
        setContentView(R.layout.tvseriallist);
        deviceName_tv = (TextView) findViewById(R.id.television_devicename_tv);
        room_tv = (TextView) findViewById(R.id.television_roomname_tv);
        back_ibtn = (ImageButton) findViewById(R.id.television_back_ibtn);
        tvpowersource_ibtn = (ImageButton) findViewById(R.id.television_powersource_ibtn);
        up_ibtn = (ImageButton) findViewById(R.id.television_up_ibtn);
        left_ibtn = (ImageButton) findViewById(R.id.television_left_ibtn);
        right_ibtn = (ImageButton) findViewById(R.id.television_right_ibtn);
        bottom_ibtn = (ImageButton) findViewById(R.id.television_bottom_ibtn);
        backMenu_itbn = (ImageButton) findViewById(R.id.television_backmenu_ibtn);
        voice_ibtn = (ImageButton) findViewById(R.id.television_voice_ibtn);
        menu_btn = (Button) findViewById(R.id.television_menu_btn);
        next_btn = (Button) findViewById(R.id.tvserial_next_btn);
        up_btn = (Button) findViewById(R.id.tvserial_up_btn);
        confirm_btn = (Button) findViewById(R.id.tvserial_queding_btn);
        tvSetListener();
        helpDialog();
    }
    private void tvSetListener(){
        back_ibtn.setOnClickListener(this);
        tvpowersource_ibtn.setOnClickListener(this);
        up_ibtn.setOnClickListener(this);
        left_ibtn.setOnClickListener(this);
        right_ibtn.setOnClickListener(this);
        bottom_ibtn.setOnClickListener(this);
        backMenu_itbn.setOnClickListener(this);
        voice_ibtn.setOnClickListener(this);
        menu_btn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        up_btn.setOnClickListener(this);
        confirm_btn.setOnClickListener(this);
    }
    private void initAir(){
        setContentView(R.layout.airseriallist);
        back_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_back_iv);
        addTemp_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_add_ibtn);
        minusTemp_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_minus_ibtn);
        powersource_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_powersource_ibtn);
        pattern_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_pattern_ibtn);
        airspeed_ibtn = (ImageButton) findViewById(R.id.air_conditionoperate_airspeed_ibtn);
        deviceName_tv = (TextView) findViewById(R.id.air_conditionoperate_devicename_tv);
        room_tv = (TextView) findViewById(R.id.air_conditionoperate_roomname_tv);
        temperatureValue_tv = (TextView) findViewById(R.id.air_conditionoperate_temperaturevalue_tv);
        airPattern_tv = (TextView) findViewById(R.id.air_conditionoperate_airpattern_tv);
        airSpeed_tv = (TextView) findViewById(R.id.air_conditionoperate_airspeed_tv);
        powerSource_tv = (TextView) findViewById(R.id.air_conditionoperate_powersource_tv);
        pattern_tv = (TextView) findViewById(R.id.air_conditionoperate_pattern_tv);
        airSpeedtwo_tv = (TextView) findViewById(R.id.air_conditionoperate_airspeedtwo_tv);
        next_btn = (Button) findViewById(R.id.airserial_next_btn);
        up_btn = (Button) findViewById(R.id.airserial_up_btn);
        confirm_btn = (Button) findViewById(R.id.airserial_queding_btn);
        airSetListener();
    }

    private void airSetListener(){
        back_ibtn.setOnClickListener(this);
        addTemp_ibtn.setOnClickListener(this);
        minusTemp_ibtn.setOnClickListener(this);
        powersource_ibtn.setOnClickListener(this);
        pattern_ibtn.setOnClickListener(this);
        airspeed_ibtn.setOnClickListener(this);
        next_btn.setOnClickListener(this);
        up_btn.setOnClickListener(this);
        confirm_btn.setOnClickListener(this);
    }

    /**
     *      获取电视，空调系列 列表
     * @param brandId     品牌Id
     */
    private void getBrandList(String brandId){
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
            String sign = MD5Utils.MD5Encode(aesAccount + brandId + serialMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("brand_id", brandId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", serialMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        /*if ("0".equals(brandId)) {//电视
            tvSetOnclick(view.getId());
        } else if ("1".equals(brandId)) {//空调
            airSetOnclick(view.getId());
        }*/
        switch(view.getId()){
            case R.id.television_back_ibtn://返回键
                finish();
                break;
            case R.id.television_up_ibtn://上
                Log.e(TAG + "电视点击", "上");
                testTV("4", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.television_bottom_ibtn://下
                Log.e(TAG + "电视点击", "下");
                testTV("5", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.television_powersource_ibtn://电源
                Log.e(TAG + "电视点击", "电源");
                testTV("1", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.television_left_ibtn://左
                testTV("6", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.television_right_ibtn://右
                testTV("7", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.television_backmenu_ibtn://返回菜单键
                testTV("8", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.television_voice_ibtn://静音
                Log.e(TAG + "电视点击", "静音");
                testTV("2", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.television_menu_btn://菜单
                testTV("3", list.get(tvPosition - 1).getSceneId());
                break;
            case R.id.tvserial_up_btn://上一个
                Log.e(TAG + "电视点击", "上一个");
                if (tvPosition == 1) {
                    Utils.showDialog(AirTVSerialListActivity.this, "前边已经没有了");
                } else {
                    tvPosition--;
                    room_tv.setText(list.get(tvPosition).getSceneName() + " " + tvPosition + "/" + list.size());
                }
                break;
            case R.id.tvserial_next_btn://下一个
                Log.e(TAG + "电视点击", "下一个");
                if (tvPosition == list.size()) {
                    Utils.showDialog(AirTVSerialListActivity.this, "后边已经没有了");
                } else {
                    tvPosition++;
                    room_tv.setText(list.get(tvPosition-1).getSceneName() + " " + tvPosition + "/" + list.size());
                }
                break;
            case R.id.tvserial_queding_btn://确定
                if ("1&".equals(brandId)) {//自定义的电视
                    intent.putExtra("serialName", "自定义");
                    intent.putExtra("serialId", "1&");
                    setResult(RESULT_OK,intent);
                } else {
                    intent.putExtra("serialName", list.get(tvPosition-1).getSceneName());
                    intent.putExtra("serialId", "0&" + list.get(tvPosition-1).getSceneId());
                    setResult(RESULT_OK,intent);
                }
                finish();
                break;
            case R.id.air_conditionoperate_back_iv://返回键
                finish();
                break;
            case R.id.air_conditionoperate_add_ibtn://温度加
                tempValue = String.valueOf(Integer.valueOf(tempValue) + 1);
                if (Integer.valueOf(tempValue) > 30) {
                    tempValue = "30";
                    temperatureValue_tv.setText(tempValue);
                    testAir(patternValue, tempValue, speedValue, list.get(airPosition - 1).getSceneId());
                } else {
                    temperatureValue_tv.setText(tempValue);
                    temperatureValue_tv.setText(tempValue);
                    testAir(patternValue, tempValue, speedValue, list.get(airPosition - 1).getSceneId());
                }
                break;
            case R.id.air_conditionoperate_minus_ibtn://温度减
                tempValue = String.valueOf(Integer.valueOf(tempValue) - 1);
                if (Integer.valueOf(tempValue) <18) {
                    tempValue = "18";
                    temperatureValue_tv.setText(tempValue);
                    testAir(patternValue, tempValue, speedValue, list.get(airPosition - 1).getSceneId());
                } else {
                    temperatureValue_tv.setText(tempValue);
                    temperatureValue_tv.setText(tempValue);
                    testAir(patternValue, tempValue, speedValue, list.get(airPosition - 1).getSceneId());
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
                    airSpeed_tv.setTextColor(getResources().getColor(R.color.white));
                    testAir("0","0","0",list.get(airPosition - 1).getSceneId());//关闭
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
                    airSpeed_tv.setTextColor(getResources().getColor(R.color.yellow));
                    testAir(patternValue, tempValue, speedValue, list.get(airPosition - 1).getSceneId());//开启
                }
                break;
            case R.id.air_conditionoperate_pattern_ibtn://模式
                if ("0".equals(patternValue)) {//制冷
                    patternValue = "1";
                    pattern_ibtn.setImageResource(R.mipmap.air_temperature_hot);
                    testAir(patternValue, tempValue, speedValue, list.get(airPosition - 1).getSceneId());
                } else {//制热
                    patternValue = "0";
                    pattern_ibtn.setImageResource(R.mipmap.air_temperature_cold);
                    testAir(patternValue, tempValue, speedValue, list.get(airPosition - 1).getSceneId());
                }
                break;
            case R.id.air_conditionoperate_airspeed_ibtn://风速
                if ("0".equals(speedValue)) {//风速高
                    speedValue = "2";
                    airSpeedtwo_tv.setText("高");
                    airSpeed_tv.setText("高");
                    testAir(patternValue,tempValue,speedValue,list.get(airPosition - 1).getSceneId());
                } else if ("1".equals(speedValue)) {//中
                    speedValue = "0";
                    airSpeedtwo_tv.setText("中");
                    airSpeed_tv.setText("中");
                    testAir(patternValue,tempValue,speedValue,list.get(airPosition - 1).getSceneId());
                }else if ("2".equals(speedValue)) {//低
                    speedValue = "1";
                    airSpeedtwo_tv.setText("低");
                    airSpeed_tv.setText("低");
                    testAir(patternValue,tempValue,speedValue,list.get(airPosition - 1).getSceneId());
                }
                break;
            case R.id.airserial_up_btn://上一个
                if (airPosition == 1) {
                    Utils.showDialog(AirTVSerialListActivity.this, "前边已经没有了");
                } else {
                    airPosition--;
                    Log.e("空调上一个", String.valueOf(airPosition));
                    room_tv.setText(list.get(airPosition-1).getSceneName() + " " + airPosition + "/" + list.size());
                    //airIndex = airPosition;
                }
                break;
            case R.id.airserial_next_btn://下一个
                if (airPosition == list.size()) {
                    Utils.showDialog(AirTVSerialListActivity.this, "后边已经没有了");
                } else {
                    airPosition++;
                    Log.e("空调下一个", String.valueOf(airPosition));
                    room_tv.setText(list.get(airPosition-1).getSceneName() + " " + airPosition + "/" + list.size());
                    //airIndex = airPosition;
                }
                break;
            case R.id.airserial_queding_btn://确定
                intent.putExtra("serialName", list.get(airPosition-1).getSceneName());
                intent.putExtra("serialId", "0&" + list.get(airPosition-1).getSceneId());
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }

    }
   /* private void tvSetOnclick(int viewId){
        Log.e(TAG + "电视点击", "11111111111");

    }
    private void airSetOnclick(int viewId){
        switch(viewId){

            default:
            break;
        }
    }*/

    /**
     *    电视测试
     * @param tvKeyId  按键id
     * @param serialId  电视系列id
     */
    private void testTV(String tvKeyId,String serialId){
        setshare();
        try {
            Utils.showWaitDialog("加载中...", AirTVSerialListActivity.this,mWaitDialog);
            Log.e(TAG + "电视测试数据", tvKeyId + ",," + serialId);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + tvKeyId + testTvMethod +neuronId + serialId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("serial_id", serialId);
            xutilsHelper.add("key_id", tvKeyId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", testTvMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(2, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *   空调测试
     * @param modeType    空调模式  0制冷  1制热
     * @param temperature  温度
     * @param wind_speed   风速
     * @param serialId     空调系列id
     */
    private void testAir(String modeType,String temperature,String wind_speed,String serialId){
        setshare();
        try {
            Utils.showWaitDialog("加载中...", AirTVSerialListActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + testAirMethod + modeType + neuronId +
                    serialId + temperature + token + wind_speed + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("serial_id", serialId);
            xutilsHelper.add("mode_type", modeType);
            xutilsHelper.add("temperature", temperature);
            xutilsHelper.add("wind_speed", wind_speed);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", testAirMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(3,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setshare(){
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
    }
    private void helpDialog(){
        final Dialog dialog = new Dialog(this);
        //去掉dialog标题栏
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this).inflate(R.layout.televisionhelp, null);
        TextView title = (TextView) view.findViewById(R.id.help_title);
        TextView content = (TextView) view.findViewById(R.id.help_one);
        Button close = (Button) view.findViewById(R.id.help_close_btn);
        title.setText("操作提醒");
        content.setText("  将手机对准设备，点击手机上的按\n钮，确认设备是否有反应。能正确\n开启设备的为对应的系列，无反应\n则换下一个遥控面板继续进行调试。");
        dialog.show();
        dialog.setContentView(view);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
