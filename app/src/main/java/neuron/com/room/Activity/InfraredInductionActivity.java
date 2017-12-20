package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
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
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/4/25. 红外人体感应
 */
public class InfraredInductionActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "InfraredInductionActivity";
    private ImageButton back_ibtn;
    private Button edit_btn;
    private TextView deviceName_tv, roomName_tv;
    private ImageView deviceImage_iv,defense_iv, trriger_iv;
    private TextView defenseName_tv, trrigerName_tv;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private String method = "GetControlledDeviceDetail";
    private String orderMethod = "DoOrders";
    private String deviceName,roomName,roomId, deviceStatus,deviceId,deviceType;
    private Intent intent;
    private String somebodySceneName, nobodySceneName;
    private String somebodySceneId, nobodySceneId;
    private String infraredResult = null;
    private WaitDialog mWaitDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infraredinduction);
        init();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getStatus(deviceId, deviceType);
    }
    private void init(){
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        deviceType = intent.getStringExtra("deviceType");
        back_ibtn = (ImageButton) findViewById(R.id.infraredinduction_back_iv);
        edit_btn = (Button) findViewById(R.id.infraredinduction_edit_btn);
        deviceName_tv = (TextView) findViewById(R.id.infraredinduction_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.infraredinduction_roomname_tv);
        defenseName_tv = (TextView) findViewById(R.id.infraredinduction_defense_tv);
        trrigerName_tv = (TextView) findViewById(R.id.infraredinduction_trigger_tv);
        deviceImage_iv = (ImageView) findViewById(R.id.infraredinduction_deviceimage_iv);
        defense_iv = (ImageView) findViewById(R.id.infraredinduction_defense_iv);
        trriger_iv = (ImageView) findViewById(R.id.infraredinduction_trigger_iv);

        defense_iv.setOnClickListener(this);
        trriger_iv.setOnClickListener(this);
        back_ibtn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);

    }
    /**
     * 获取红外人体感应的详情
     */
    private void getStatus(String deviceid,String devicetype){
        Utils.showWaitDialog(getString(R.string.loadtext_load),InfraredInductionActivity.this,mWaitDialog);
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceid + devicetype + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("controlled_device_id", deviceid);
            xutilsHelper.add("electric_type_id", devicetype);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(1, this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    infraredResult = s;
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "红外人体感应详情", infraredResult);
                    try {
                        JSONObject jsonObject = new JSONObject(infraredResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                            JSONObject jsonBasic = jsonMsg.getJSONObject("basic_msg");
                            deviceName = jsonBasic.getString("controlled_device_name");
                            roomName = jsonBasic.getString("room_name");
                            deviceName_tv.setText(deviceName);
                            roomName_tv.setText(roomName);
                            // cBrand = jsonBasic.getString("controlled_device_brand");
                            //cSerial = jsonBasic.getString("controlled_device_serial");
                            roomId = jsonBasic.getString("room_id");
                            deviceId = jsonBasic.getString("controlled_device_id");
                            deviceType = jsonBasic.getString("electric_type_id");
                            deviceStatus = jsonBasic.getString("status");
                            if (!TextUtils.isEmpty(deviceStatus)) {
                                if ("01".equals(deviceStatus)) {//布防状态
                                    defense_iv.setImageResource(R.mipmap.infrared_induction_security);
                                    defenseName_tv.setTextColor(getResources().getColor(R.color.yellow));
                                    trriger_iv.setImageResource(R.mipmap.infrared_induction_trigger_close);
                                    trrigerName_tv.setTextColor(getResources().getColor(R.color.white));
                                } else if ("00".equals(deviceStatus)) {//关闭状态
                                    defense_iv.setImageResource(R.mipmap.infrared_induction_security_close);
                                    defenseName_tv.setTextColor(getResources().getColor(R.color.white));
                                    trriger_iv.setImageResource(R.mipmap.infrared_induction_trigger_close);
                                    trrigerName_tv.setTextColor(getResources().getColor(R.color.white));
                                } else if ("02".equals(deviceStatus)) {//触发状态
                                    defense_iv.setImageResource(R.mipmap.infrared_induction_security_close);
                                    defenseName_tv.setTextColor(getResources().getColor(R.color.white));
                                    trriger_iv.setImageResource(R.mipmap.infrared_induction_trigger);
                                    trrigerName_tv.setTextColor(getResources().getColor(R.color.yellow));
                                }
                                JSONArray jsonOther = jsonMsg.getJSONArray("other_msg");
                                int length = jsonOther.length();
                                if (length > 0) {
                                    for (int i = 0; i < length; i++) {
                                        JSONObject jso = jsonOther.getJSONObject(i);
                                        if (jso.getInt("condition") == 0) {//有人模式的场景名称
                                            somebodySceneId = jso.getString("id");
                                            somebodySceneName = jso.getString("name");
                                        } else {//无人模式的场景名称
                                            nobodySceneId = jso.getString("id");
                                            nobodySceneName = jso.getString("name");
                                        }
                                    }
                                }
                            } else {//没有设备也没有场景的情况
                                defense_iv.setImageResource(R.mipmap.infrared_induction_security_close);
                                defenseName_tv.setTextColor(getResources().getColor(R.color.white));
                                trriger_iv.setImageResource(R.mipmap.infrared_induction_trigger_close);
                                trrigerName_tv.setTextColor(getResources().getColor(R.color.white));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.infraredinduction_edit_btn://编辑
                Intent intent = new Intent(InfraredInductionActivity.this, InfraredInductionEditActivity.class);
                intent.putExtra("infrared", infraredResult);
                startActivityForResult(intent,100);
                break;
            case R.id.infraredinduction_back_iv://返回
                    finish();
                break;
            case R.id.infraredinduction_defense_iv://布防
                if ("00".equals(deviceStatus) || "02".equals(deviceStatus)) {//关闭/触发状态
                    setDevice("03", "01", "1",2);
                } else if ("01".equals(deviceStatus)) {//安防开启状态
                    setDevice("03", "00", "1",3);
                } else {
                    Utils.showDialog(InfraredInductionActivity.this, "请绑定设备或场景");
                }
                break;
            case R.id.infraredinduction_trigger_iv://触发
                if ("01".equals(deviceStatus) || "00".equals(deviceStatus)) {//关闭/安防状态
                    setDevice("03", "02", "1",4);
                } else if ("02".equals(deviceStatus)){//触发开启状态
                    setDevice("03", "00", "1",3);
                }else {
                    Utils.showDialog(InfraredInductionActivity.this, "请绑定设备或场景");
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    /**
     *    红外人体感应操作
     * @param methodType  操作类型 02设定 03操作
     * @param orderId    指令
     * @param deviceCode   设备是节点设备还是电器设备 0节点设备  1电器设备  2情景模式
     */
    private void setDevice(String methodType,String orderId,String deviceCode,int arg1){
        setAccount();
        try {
            Utils.showWaitDialog("加载中", InfraredInductionActivity.this, mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceCode + engineId + orderMethod + methodType + orderId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device_id", deviceId);
            xutilsHelper.add("device_type", deviceCode);
            xutilsHelper.add("method_type", methodType);
            xutilsHelper.add("order_id", orderId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", orderMethod);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(arg1, this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    switch(arg1){
                        case 2:
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") == 9999) {
                                    Toast.makeText(InfraredInductionActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                                    deviceStatus = "01";
                                    defense_iv.setImageResource(R.mipmap.infrared_induction_security);
                                    defenseName_tv.setTextColor(getResources().getColor(R.color.yellow));
                                    trriger_iv.setImageResource(R.mipmap.infrared_induction_trigger_close);
                                    trrigerName_tv.setTextColor(getResources().getColor(R.color.white));
                                } else {
                                    Toast.makeText(InfraredInductionActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 3:
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") == 9999) {
                                    Toast.makeText(InfraredInductionActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                                    deviceStatus = "00";
                                    defense_iv.setImageResource(R.mipmap.infrared_induction_security_close);
                                    defenseName_tv.setTextColor(getResources().getColor(R.color.white));
                                    trriger_iv.setImageResource(R.mipmap.infrared_induction_trigger_close);
                                    trrigerName_tv.setTextColor(getResources().getColor(R.color.white));
                                } else {
                                    Toast.makeText(InfraredInductionActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 4:
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") == 9999) {
                                    Toast.makeText(InfraredInductionActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                                    deviceStatus = "02";
                                    defense_iv.setImageResource(R.mipmap.infrared_induction_security_close);
                                    defenseName_tv.setTextColor(getResources().getColor(R.color.white));
                                    trriger_iv.setImageResource(R.mipmap.infrared_induction_trigger);
                                    trrigerName_tv.setTextColor(getResources().getColor(R.color.yellow));
                                } else {
                                    Toast.makeText(InfraredInductionActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                        break;
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(InfraredInductionActivity.this, "网络不通", Toast.LENGTH_LONG).show();
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
