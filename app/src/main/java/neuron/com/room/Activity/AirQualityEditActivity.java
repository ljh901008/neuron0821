package neuron.com.room.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

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
 * Created by ljh on 2017/3/31.空气质量检测仪边界页面
 */
public class AirQualityEditActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AirQualityEditActivity";
    private ImageButton back_ibtn;
    //设备名称
    private EditText deviceName_ed;
    private Button fonfirm_btn;
    // 房间名，北京时间
    private RelativeLayout roomName_rll, time_rll;
    //温度最大值，最小值，湿度最大值，最小值，甲醛，PM2.5
    private RelativeLayout tempMax, tempMin,humidityMax,humidityMin,formaldehyde,PM;
    // 房间名，北京时间
    private TextView roomName_tv, time_tv;
    //温度最大值，最小值，湿度最大值，最小值，甲醛，PM2.5
    private TextView tempMax_tv,tempMin_tv,humidityMax_tv, humidityMin_tv,formaldehyde_tv,Pm_tv;
    //温度最大触发的场景，最小值，湿度最大值，最小值，甲醛触发的模式，PM2.5触发的模式
    private TextView tempMaxScene,tempMinScene,humidityMaxScene,humidityMinScene,formaldehydePattern, PMPattern;

    private String deviceName,time,account,token,engineId,deviceId;
   // private String roomName = null, roomId = null;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private Intent intent;
    private String airName,airRoom,airRoomId,neuronId;
    //设备类别
    private String deviceTypeId;
    private String methodSetTime = "SetNeuron";
    private String methodUpdateDevice = "UpdateControlledDevice";
    private String hour = "";
    private String min = "";
    private WaitDialog mWaitDialog;
    private String openTime;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://设置空气质量监测仪时间
                    if (msg.what == 102) {
                        Utils.dismissWaitDialog(mWaitDialog);
                        String setTimeResult = (String) msg.obj;
                        Log.e(TAG + "设置时间", setTimeResult);
                        try {
                            JSONObject jsonObject = new JSONObject(setTimeResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(AirQualityEditActivity.this, jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                        Toast.makeText(AirQualityEditActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 2://修改设备名和房间
                    if (msg.what == 102) {
                        String updateResult = (String) msg.obj;
                        Log.e(TAG + "修改设备名称和房间", updateResult);
                        try {
                            JSONObject jsonObject = new JSONObject(updateResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(AirQualityEditActivity.this, jsonObject.getString("error"));
                            } else {
                                Utils.showDialog(AirQualityEditActivity.this, "修改成功");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        setContentView(R.layout.airqualityedit);
        init();
        setListener();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        airData();//空气质量监测仪数据
    }
    private void init(){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        deviceName_ed = (EditText) findViewById(R.id.airqualityedit_devicenama_ed);
        back_ibtn = (ImageButton) findViewById(R.id.airqualityedit_back_iv);
        fonfirm_btn = (Button) findViewById(R.id.airqualityedit_fonfirm_btn);
        roomName_rll = (RelativeLayout) findViewById(R.id.airqualityedit_roomname_rll);
        time_rll = (RelativeLayout) findViewById(R.id.airqualityedit_timename_rll);
        roomName_tv = (TextView) findViewById(R.id.airqualityedit_roomnama_tv);
        time_tv = (TextView) findViewById(R.id.airqualityedit_timenama_tv);
        tempMax = (RelativeLayout) findViewById(R.id.airqualityedit_temperture_rll);
        tempMin = (RelativeLayout) findViewById(R.id.airqualityedit_temperturemin_rll);
        humidityMax = (RelativeLayout) findViewById(R.id.airqualityedit_humiditymax_rll);
        humidityMin = (RelativeLayout) findViewById(R.id.airqualityedit_humiditymin_rll);
        formaldehyde = (RelativeLayout) findViewById(R.id.airqualityedit_formaldehyde_rll);
        PM = (RelativeLayout) findViewById(R.id.airqualityedit_pm2_rll);
        tempMax_tv = (TextView) findViewById(R.id.airqualityedit_tempmaxvalue_tv);
        tempMin_tv = (TextView) findViewById(R.id.airqualityedit_tempminvalue_tv);
        humidityMax_tv = (TextView) findViewById(R.id.airqualityedit_humiditymaxvalue_tv);
        humidityMin_tv = (TextView) findViewById(R.id.airqualityedit_humidityminvalue_tv);
        formaldehyde_tv = (TextView) findViewById(R.id.airqualityedit_formaldehyde_tv);
        Pm_tv = (TextView) findViewById(R.id.airqualityedit_pm2_tv);
        tempMaxScene = (TextView) findViewById(R.id.airqualityedit_tempmaxscene_tv);
        tempMinScene = (TextView) findViewById(R.id.airqualityedit_tempminscene_tv);
        humidityMaxScene = (TextView) findViewById(R.id.airqualityedit_humiditymaxscene_tv);
        humidityMinScene = (TextView) findViewById(R.id.airqualityedit_humidityminscene_tv);
        formaldehydePattern = (TextView) findViewById(R.id.airqualityedit_formaldehydePattern_tv);
        PMPattern = (TextView) findViewById(R.id.airqualityedit_PMPattern_tv);
        deviceName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        fonfirm_btn.setOnClickListener(this);
        roomName_rll.setOnClickListener(this);
        time_rll.setOnClickListener(this);
        tempMax.setOnClickListener(this);
        tempMin.setOnClickListener(this);
        humidityMax.setOnClickListener(this);
        humidityMin.setOnClickListener(this);
        formaldehyde.setOnClickListener(this);
        PM.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Intent intent1 = new Intent(AirQualityEditActivity.this, AirQualitySelectSceneActivity.class);
        intent1.putExtra("deviceId", neuronId);
        switch(view.getId()){
            case R.id.airqualityedit_back_iv://返回
                finish();
                break;
            case R.id.airqualityedit_fonfirm_btn://确定
                deviceName = deviceName_ed.getText().toString().trim();
                String roomNa = roomName_tv.getText().toString().trim();
                time = time_tv.getText().toString().trim();
                if (!TextUtils.isEmpty(deviceName) && !TextUtils.isEmpty(roomNa)) {
                    UpdateData(deviceName,airRoomId);
                }
                break;
            case R.id.airqualityedit_roomname_rll://房间
                Intent intent = new Intent(AirQualityEditActivity.this, RoomListActivity.class);
                startActivityForResult(intent,100);
                break;
            case R.id.airqualityedit_timename_rll://时间
                TimeDialog(time_tv);
                break;
            case R.id.airqualityedit_temperture_rll://温度最大值
                intent1.putExtra("tag", 1);
                startActivityForResult(intent1,1000);
                break;
            case R.id.airqualityedit_temperturemin_rll://温度最小值
                intent1.putExtra("tag", 2);
                startActivityForResult(intent1,1001);
                break;
            case R.id.airqualityedit_humiditymax_rll://最大湿度
                intent1.putExtra("tag", 3);
                startActivityForResult(intent1,1002);
                break;
            case R.id.airqualityedit_humiditymin_rll://最小湿度
                intent1.putExtra("tag", 4);
                startActivityForResult(intent1,1003);
                break;
            case R.id.airqualityedit_formaldehyde_rll://甲醛
                intent1.putExtra("tag", 5);
                startActivityForResult(intent1,1004);
                break;
            case R.id.airqualityedit_pm2_rll://PM2.5
                intent1.putExtra("tag", 6);
                startActivityForResult(intent1,1005);
                break;
            default:
            break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch(requestCode){
                case 100://房间
                    if (data != null) {
                        airRoom = data.getStringExtra("roomName");
                        airRoomId = data.getStringExtra("roomId");
                        roomName_tv.setText(airRoom);
                    }
                    break;
                case 1000://温度高
                    if (data != null) {
                        tempMax_tv.setText(data.getStringExtra("value"));
                        tempMaxScene.setText(data.getStringExtra("sceneName"));
                    }
                    break;
                case 1001://温度低
                    if (data != null) {
                        tempMin_tv.setText(data.getStringExtra("value"));
                        tempMinScene.setText(data.getStringExtra("sceneName"));
                    }
                    break;
                case 1002://湿度高
                    if (data != null) {
                        humidityMax_tv.setText(data.getStringExtra("value"));
                        humidityMaxScene.setText(data.getStringExtra("sceneName"));
                    }
                    break;
                case 1003://湿度低
                    if (data != null) {
                        humidityMin_tv.setText(data.getStringExtra("value"));
                        humidityMinScene.setText(data.getStringExtra("sceneName"));
                    }
                    break;
                case 1004://甲醛
                    if (data != null) {
                        formaldehyde_tv.setText(data.getStringExtra("value"));
                        formaldehydePattern.setText(data.getStringExtra("sceneName"));
                    }
                    break;
                case 1005://PM2.5
                    if (data != null) {
                        Pm_tv.setText(data.getStringExtra("value"));
                        PMPattern.setText(data.getStringExtra("sceneName"));
                    }
                    break;
                default:
                break;
            }

        }
    }

    /**
     * 修改空气质量检测仪名称和房间
     */
    private void UpdateData(String deviceName,String roomId){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engineId = sharedPreferencesManager.get("engine_id");
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("controlled_device_name", deviceName);
            jsonObject.put("room_id", roomId);
            jsonObject.put("controlled_device_brand", "");
            jsonObject.put("controlled_device_serial", "");
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceTypeId + engineId + methodUpdateDevice + jsonObject.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("electric_type_id", deviceTypeId);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodUpdateDevice);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(2, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 空气质量监测仪数据
     */
    private void airData(){
        try {
            intent = getIntent();
            String airData = intent.getStringExtra("airData");
            JSONObject jsonObject = new JSONObject(airData);
            if (jsonObject.getInt("status") == 9999) {
                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                JSONObject json = jsonMsg.getJSONObject("basic_msg");
                airName = json.getString("controlled_device_name");
                deviceId = json.getString("controlled_device_id");
                neuronId = json.getString("neuron_id");
                deviceTypeId = json.getString("electric_type_id");
                airRoom = json.getString("room_name");
                airRoomId =  json.getString("room_id");
                deviceName_ed.setText(airName);
                roomName_tv.setText(airRoom);
                JSONArray jsonA = jsonMsg.getJSONArray("other_msg");
                for (int i = 0; i < jsonA.length(); i++) {
                    JSONObject json1 = jsonA.getJSONObject(i);
                    String type = json1.getString("type");
                    if ("05".equals(type)) {//温度大于设定温度会触发一个场景
                        tempMax_tv.setText(json1.getString("value"));
                        tempMaxScene.setText(json1.getString("desc").substring(3));
                    } else if ("06".equals(type)) {//温度小于设定温度会触发一个场景
                        tempMin_tv.setText(json1.getString("value"));
                        tempMinScene.setText(json1.getString("desc").substring(3));
                    } else if ("07".equals(type)) {//湿度大于设定温度会触发一个场景
                        humidityMax_tv.setText(json1.getString("value"));
                        humidityMaxScene.setText(json1.getString("desc").substring(3));
                    } else if ("08".equals(type)) {//湿度小于设定温度会触发一个场景
                        humidityMin_tv.setText(json1.getString("value"));
                        humidityMinScene.setText(json1.getString("desc").substring(3));
                    } else if ("01".equals(type) || "03".equals(type)) {//pm2.5大于525触发一个警报
                        Pm_tv.setText(json1.getString("value"));
                        PMPattern.setText(json1.getString("desc").substring(3));
                    }else if ("02".equals(type) || "04".equals(type)) {//甲醛大于60就触发一个警报
                        formaldehyde_tv.setText(json1.getString("value"));
                        formaldehydePattern.setText(json1.getString("desc").substring(3));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *   设置空气质量监测仪时间时间
     * @param time
     */
    private void setTime(String time){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engineId = sharedPreferencesManager.get("engine_id");
        }
        try {
            Utils.showWaitDialog("加载中...", AirQualityEditActivity.this, mWaitDialog);
            Log.e(TAG + "时间", time);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contextual_model_id", "");
            jsonObject.put("triggering_condition", "00." + time);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + methodSetTime + jsonObject.toString() + neuronId + "3" + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("set_type", "3");
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodSetTime);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private TimePicker timePicker;
    private void TimeDialog(final TextView textView){
        LinearLayout dateTimeLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.timepickdialog, null);
        timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.timepickerdialog_time);
        Calendar calendar = Calendar.getInstance();
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        timePicker.setIs24HourView(true);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                if (hourOfDay < 10 && minute >= 10) {
                    textView.setText("0" + hourOfDay + ":" + minute);
                } else if (hourOfDay < 10 && minute < 10) {
                    textView.setText("0" + hourOfDay + ":" + "0" + minute);
                } else if (hourOfDay >= 10 && minute < 10) {
                    textView.setText(hourOfDay + ":" + "0" + minute);
                } else {
                    textView.setText(hourOfDay + ":" + minute);
                }
            }
        });
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setView(dateTimeLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int tempHour = 0;
                        int tempMin = 0;
                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            tempHour = timePicker.getHour();
                            tempMin = timePicker.getMinute();
                        } else {
                            tempHour = timePicker.getCurrentHour();
                            tempMin = timePicker.getCurrentMinute();
                        }
                        if (tempHour >= 10) {
                            hour = tempHour + "";
                        } else {
                            hour = "0" + tempHour;
                        }
                        if (tempMin >= 10) {
                            min = tempMin + "";
                        } else {
                            min = "0" + tempMin;
                        }
                        textView.setText(hour + ":" + min + ":00");
                        String time = time_tv.getText().toString().trim();
                        if (!TextUtils.isEmpty(time)) {
                            Log.e(TAG + "空气质量检测仪时间", time);
                            setTime(time);
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        dialog.show();
    }

}
