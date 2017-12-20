package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
 * Created by ljh on 2017/1/12.
 */
public class LightActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "LightActivity";
    //返回键 右侧图，设备图片，开关图片
    private ImageView back_iv,devieceImg_iv, deviceStatus_iv;
    private TextView deviceName_tv, roomName_tv;
    private Button rightTitle_tv;
    private Intent intent;
    //灯id ，类型，灯名称，灯所在房间名称,灯状态
    private String lightId, lightType,lightName,lightRoom,lightStatu,token,account,engineId;
    private String roomId,lightSite;
    private String lightBrand = "";
    private String lightSerial = "";
    private SharedPreferencesManager sharedPreferencesManager;
    private String method = "GetControlledDeviceDetail";
    private String orderMethod = "DoOrders";
    private boolean lightTag;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lightoperate);
        init();
        setListener();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getLightStatus(lightId,lightType);
    }

    private void init() {
        intent = getIntent();
        lightId = intent.getStringExtra("deviceId");
        lightType = intent.getStringExtra("deviceType");
        back_iv = (ImageView) findViewById(R.id.lightoperate_back_iv);
        rightTitle_tv = (Button) findViewById(R.id.lightoperate_edit_btn);
        devieceImg_iv = (ImageView) findViewById(R.id.lightoperate_deviceimage_iv);
        deviceStatus_iv = (ImageView) findViewById(R.id.lightoperate_open_iv);
        deviceName_tv = (TextView) findViewById(R.id.lightoperate_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.lightoperate_roomnama_tv);

    }

    private void setListener(){
        back_iv.setOnClickListener(this);
        rightTitle_tv.setOnClickListener(this);
        deviceStatus_iv.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.lightoperate_back_iv://返回键
                intent.putExtra("tag", 1);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.lightoperate_edit_btn://title right
                Intent intent = new Intent(LightActivity.this, EditActivity.class);
                intent.putExtra("deviceName", lightName);
                intent.putExtra("deviceRoom", lightRoom);
                intent.putExtra("brand", lightBrand);
                intent.putExtra("serial", lightSerial);
                intent.putExtra("roomId", roomId);
                intent.putExtra("deviceId", lightId);
                intent.putExtra("deviceType", lightType);
                startActivityForResult(intent,100);
                break;
            case R.id.lightoperate_open_iv: //开关
                if ("01".equals(lightStatu)) {//开启状态点击事件
                    operation("1", "03", lightSite + "0",2);//关灯
                } else if ("00".equals(lightStatu)){//关闭状态点击事件
                    operation("1", "03", lightSite + "1",3);//开灯
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
                    lightRoom = data.getStringExtra("roomName");
                    roomName_tv.setText(lightRoom);
                    lightName = data.getStringExtra("deviceName");
                    deviceName_tv.setText(lightName);
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
            Utils.showWaitDialog("加载中...",LightActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceType + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("electric_type_id", deviceType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(1, this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String lightResult) {
                    Log.e(TAG + "灯详情", lightResult);
                    try {
                        //Utils.dismissWaitDialog();
                        Utils.dismissWaitDialog(mWaitDialog);
                        JSONObject jsonObject = new JSONObject(lightResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                            JSONObject json = jsonMsg.getJSONObject("basic_msg");
                            lightName = json.getString("controlled_device_name");
                            lightRoom = json.getString("room_name");
                            roomId = json.getString("room_id");
                            lightId = json.getString("controlled_device_id");
                            lightType = json.getString("electric_type_id");
                            lightSite = json.getString("controlled_device_site");
                            lightStatu = json.getString("status");
                            deviceName_tv.setText(lightName);
                            roomName_tv.setText(lightRoom);
                            if ("01".equals(lightStatu)) {//开启状态
                                devieceImg_iv.setImageResource(R.mipmap.home_light_open);
                                deviceStatus_iv.setImageResource(R.mipmap.home_light_start_open);
                            } else if ("00".equals(lightStatu)) {//关闭状态
                                devieceImg_iv.setImageResource(R.mipmap.home_light);
                                deviceStatus_iv.setImageResource(R.mipmap.home_light_shut_open);
                            }
                        } else {

                            Toast.makeText(LightActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(LightActivity.this, "网络不通", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     *
     * @param deviceType 设备类别0节点设备   1被控设备   2情景模式
     * @param methodType  操作类型
     * @param orderId   指令id
     */
    private void operation(String deviceType,String methodType,String orderId,int arg1){
        setAccount();
        try {
            Utils.showWaitDialog("加载中...",LightActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + lightId + deviceType + engineId +
                    orderMethod + methodType + orderId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device_id", lightId);
            xutilsHelper.add("device_type", deviceType);
            xutilsHelper.add("method_type", methodType);
            xutilsHelper.add("order_id", orderId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", orderMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "result", s);
                    switch(arg1){
                        case 2://关灯
                            try {
                                JSONObject json = new JSONObject(s);
                                if (json.getInt("status") == 9999) {
                                    Toast.makeText(LightActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                                    devieceImg_iv.setImageResource(R.mipmap.home_light);
                                    deviceStatus_iv.setImageResource(R.mipmap.home_light_shut_open);
                                    lightStatu = "00";
                                } else {
                                    Toast.makeText(LightActivity.this, json.getString("error"), Toast.LENGTH_LONG).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        break;
                        case 3://开灯
                            try {
                                JSONObject json = new JSONObject(s);
                                if (json.getInt("status") == 9999) {
                                    Toast.makeText(LightActivity.this, "操作成功", Toast.LENGTH_LONG).show();
                                    devieceImg_iv.setImageResource(R.mipmap.home_light_open);
                                    deviceStatus_iv.setImageResource(R.mipmap.home_light_start_open);
                                    lightStatu = "01";

                                } else {
                                    Toast.makeText(LightActivity.this, json.getString("error"), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(LightActivity.this, "网络不通", Toast.LENGTH_LONG).show();
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
