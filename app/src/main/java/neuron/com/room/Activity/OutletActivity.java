package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
 * Created by ljh on 2017/4/25.  插座页面
 */
public class OutletActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "OutletActivity";
    private ImageButton back_ibtn;
    private Button edit_btn;
    private TextView deviceName_tv, roomName_tv;
    private ImageView deviceImage_iv,close_iv;
    private TextView close_tv;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private String method = "GetControlledDeviceDetail";
    private String deviceName,roomName,roomId, deviceStatus,deviceId,deviceType;
    private Intent intent;
    private String orderMethod = "DoOrders";
    private boolean isOpen;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doorsensor);
        init();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getStatus(deviceId, deviceType);
    }
    private void init(){
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        deviceType = intent.getStringExtra("deviceType");
        back_ibtn = (ImageButton) findViewById(R.id.doorsensor_back_iv);
        edit_btn = (Button) findViewById(R.id.doorsensor_edit_btn);
        deviceName_tv = (TextView) findViewById(R.id.doorsensor_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.doorsensor_roomname_tv);
        deviceImage_iv = (ImageView) findViewById(R.id.doorsensor_deviceimage_iv);
        close_iv = (ImageView) findViewById(R.id.doorsensor_close_iv);
        close_tv = (TextView) findViewById(R.id.doorsensor_close_tv);
        back_ibtn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
        close_iv.setOnClickListener(this);
        deviceImage_iv.setImageResource(R.mipmap.intelligent_socket);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.doorsensor_edit_btn://编辑
                Intent intent1 = new Intent(OutletActivity.this, EditActivity.class);
                intent1.putExtra("deviceName", deviceName);
                intent1.putExtra("deviceRoom", roomName);
                intent1.putExtra("brand", "");
                intent1.putExtra("serial", "");
                intent1.putExtra("roomId", roomId);
                intent1.putExtra("deviceId", deviceId);
                intent1.putExtra("deviceType", deviceType);
                startActivityForResult(intent1,100);
                break;
            case R.id.doorsensor_back_iv://返回
                finish();
                break;
            case R.id.doorsensor_close_iv://电源键
                if ("00".equals(deviceStatus)) {//关闭状态
                    setDevice("03", "01", "1",2);
                } else if ("01".equals(deviceStatus)) {//开启状态
                    setDevice("03", "00", "1",3);

                }
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
                    deviceName = data.getStringExtra("deviceName");
                    deviceName_tv.setText(deviceName);
                    roomName = data.getStringExtra("roomName");
                    roomName_tv.setText(roomName);
                }
            }
        }
    }

    /**
     *
     * @param methodType  操作类型
     * @param orderId    指令
     * @param deviceCode   设备是节点设备还是电器设备
     */
    private void setDevice(String methodType,String orderId,String deviceCode,int num){
        setAccount();
        try {
            Utils.showWaitDialog("加载中...", OutletActivity.this,mWaitDialog);
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
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "插座操作", s);
                    switch(num){
                        case 2:

                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") != 9999) {
                                    Toast.makeText(OutletActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(OutletActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                                    close_iv.setImageResource(R.mipmap.tv_open);
                                    close_tv.setText("开启");
                                    close_tv.setTextColor(getResources().getColor(R.color.yellow));
                                    deviceStatus = "01";
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 3:
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") != 9999) {
                                    Toast.makeText(OutletActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(OutletActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                                    close_iv.setImageResource(R.mipmap.tv_close);
                                    close_tv.setText("关闭");
                                    close_tv.setTextColor(getResources().getColor(R.color.white));
                                    deviceStatus = "00";
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
    /**
     * 获取插座详情
     */
    private void getStatus(String deviceid,String devicetype){
        Utils.showWaitDialog(getString(R.string.loadtext_load),OutletActivity.this,mWaitDialog);
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
                public void onSuccess(String Result) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "插座详情", Result);
                    try {
                        JSONObject jsonObject = new JSONObject(Result);
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
                            if ("00".equals(deviceStatus)) {//关闭
                                close_iv.setImageResource(R.mipmap.tv_close);
                                close_tv.setText("关闭");
                                close_tv.setTextColor(getResources().getColor(R.color.white));
                            } else {//开启
                                close_iv.setImageResource(R.mipmap.tv_open);
                                close_tv.setText("开启");
                                close_tv.setTextColor(getResources().getColor(R.color.yellow));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(OutletActivity.this, "数据异常", Toast.LENGTH_LONG).show();
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
}
