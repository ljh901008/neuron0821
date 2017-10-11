package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
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
 * Created by ljh on 2017/7/19.
 */
public class WaterFuntainActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "WaterFuntainActivity";
    private ImageButton back_ibtn, edit_ibtn;
    private TextView deviceName_tv, roomName_tv;
    //水量，是否在线，TDS,水温,滤芯,剩余水量，已用天数，剩余天数
    private TextView waterValue_tv, isOnline_tv,tds_tv,temp_tv,ceramic_tv,residueWater_tv,useDay_tv,ceramicDay_tv;
    private ImageButton next_ibtn;
    private Intent intent;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId,deviceId, deviceType,deviceName,deviceRoom,roomId,deviceStatus;
    private String method = "GetControlledDeviceDetail";
    private String otherMsg;
    private WaitDialog mWaitDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1:
                    if (msg.what == 102) {
                    Utils.dismissWaitDialog(mWaitDialog);
                        String result = (String) msg.obj;
                        Log.e(TAG + "水详情", result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                                JSONObject jsonBasic = jsonMsg.getJSONObject("basic_msg");
                                deviceName = jsonBasic.getString("controlled_device_name");
                                deviceName_tv.setText(deviceName);
                                deviceRoom = jsonBasic.getString("room_name");
                                roomName_tv.setText(deviceRoom);
                                roomId = jsonBasic.getString("room_id");
                                deviceStatus = jsonBasic.getString("status");
                                String s[] = deviceStatus.split(",");
                                if (s[0].equals("0")) {//是否在线
                                    isOnline_tv.setText("(不在线)");
                                } else {
                                    isOnline_tv.setText("(在线)");
                                }
                                int w = Integer.parseInt(s[2]);
                                if (w > 300) {
                                    tds_tv.setText("TDS: " + w + "mg/L (差)");
                                } else if (w > 150 && w < 300) {
                                    tds_tv.setText("TDS: " + w + "mg/L (良)");
                                } else if (w < 150) {
                                    tds_tv.setText("TDS: " + w + "mg/L (优)");
                                }
                                temp_tv.setText("水温: " + s[3] + "℃");
                                ceramic_tv.setText("滤芯寿命:" + s[4] + "%");
                                residueWater_tv.setText("可用水量:" + s[5] + "L");
                                waterValue_tv.setText(s[6]+"L");
                                useDay_tv.setText("已用"+s[7]+"天");
                                ceramicDay_tv.setText("可用"+s[8]+"天");
                                otherMsg = jsonMsg.getString("other_msg");
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                        Toast.makeText(WaterFuntainActivity.this, "网络不通", Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.waterfountain);
        init();
        setListener();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getLightStatus(deviceId,deviceType);
    }

    private void init() {
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        deviceType = intent.getStringExtra("deviceType");
        back_ibtn = (ImageButton) findViewById(R.id.waterfountain_back_ibtn);
        edit_ibtn = (ImageButton) findViewById(R.id.waterfountain_edit_btn);
        next_ibtn = (ImageButton) findViewById(R.id.waterfountain_next_iv);
        deviceName_tv = (TextView) findViewById(R.id.waterfountain_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.waterfountain_roomnama_tv);
        waterValue_tv = (TextView) findViewById(R.id.waterfountain_watervalue_tv);
        isOnline_tv = (TextView) findViewById(R.id.waterfountain_isonline_tv);
        tds_tv = (TextView) findViewById(R.id.waterfountain_tds_tv);
        temp_tv = (TextView) findViewById(R.id.waterfountain_tds1_tv);
        ceramic_tv = (TextView) findViewById(R.id.waterfountain_waterlife_tv);
        residueWater_tv = (TextView) findViewById(R.id.waterfountain_waterlife1_tv);
        useDay_tv = (TextView) findViewById(R.id.waterfountain_wateruse_tv);
        ceramicDay_tv = (TextView) findViewById(R.id.waterfountain_user1_tv);

    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        edit_ibtn.setOnClickListener(this);
        next_ibtn.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.waterfountain_back_ibtn://返回
                finish();
                break;
            case R.id.waterfountain_edit_btn://编辑
                Intent intent = new Intent(WaterFuntainActivity.this, EditActivity.class);
                intent.putExtra("deviceName", deviceName);
                intent.putExtra("deviceRoom", deviceRoom);
                intent.putExtra("brand", "");
                intent.putExtra("serial", "");
                intent.putExtra("roomId", roomId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("deviceType", deviceType);
                startActivityForResult(intent,100);
                break;
            case R.id.waterfountain_next_iv://下个页面
                Intent intent1 = new Intent(WaterFuntainActivity.this, WaterCombinedChartActivity.class);
                intent1.putExtra("otherMsg", otherMsg);
                intent1.putExtra("deviceName", deviceName);
                intent1.putExtra("deviceId", deviceId);
                intent1.putExtra("deviceRoom", deviceRoom);
                intent1.putExtra("roomId", roomId);
                intent1.putExtra("deviceType", deviceType);
                startActivity(intent1);
                this.overridePendingTransition(R.anim.activity_open, 0);
                break;
            default:
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                if (data != null) {
                    deviceRoom = data.getStringExtra("roomName");
                    roomName_tv.setText(deviceRoom);
                    deviceName = data.getStringExtra("deviceName");
                    deviceName_tv.setText(deviceName);
                }
            }
        }
    }

    /**
     * 获取灯详情
     */
    private void getLightStatus(String deviceId,String deviceType){
        setAccount();
        try {
            Utils.showWaitDialog("加载中...",WaterFuntainActivity.this,mWaitDialog);
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
    private void setAccount(){
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
}
