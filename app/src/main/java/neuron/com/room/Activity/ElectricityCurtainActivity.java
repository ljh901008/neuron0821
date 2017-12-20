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
 * Created by ljh on 2017/4/17.电动窗帘详情页
 */
public class ElectricityCurtainActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "ElectricityCurtainActivity";
    private ImageButton back_ibtn;
    private Button edit_btn;
    // 电源  暂停  关闭
    private ImageButton powersource_ibtn,curtainstatus_ibtn, close_ibtn;
    //窗帘名称，房间名 状态
    private TextView curtainName_tv,roomName_tv, curtainStatus_tv;
    //电源，暂停，关闭
    private TextView open_tv,suspend_tv, close_tv;
    private ImageView cImageview;
    private Intent intent;
    private String deviceId, deviceType;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private String account,engineId, token;
    private String method = "GetControlledDeviceDetail";
    private String orderMethod = "DoOrders";
    private String cName,cRoom,roomId,cStatus;
    private String cBrand = "";
    private String cSerial = "";
    // 设备标记
    private boolean isOpen = false;
    private boolean isClose = false;
    private boolean isSuspend = false;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.curtain);
        init();
        setListener();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getStatus(deviceId,deviceType);
    }

    private void init() {
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        deviceType = intent.getStringExtra("deviceType");
        back_ibtn = (ImageButton) findViewById(R.id.curtain_back_ibtn);
        powersource_ibtn = (ImageButton) findViewById(R.id.curtain_powersource_ibtn);
        curtainstatus_ibtn = (ImageButton) findViewById(R.id.curtain_curtainstatus_ibtn);
        close_ibtn = (ImageButton) findViewById(R.id.curtain_close_ibtn);
        edit_btn = (Button) findViewById(R.id.curtain_edit_btn);
        curtainName_tv = (TextView) findViewById(R.id.curtain_devicename_tv);
        roomName_tv = (TextView) findViewById(R.id.curtain_roomname_tv);
        curtainStatus_tv = (TextView) findViewById(R.id.curtain_status_tv);
        open_tv = (TextView) findViewById(R.id.curtain_powersource_tv);
        suspend_tv = (TextView) findViewById(R.id.curtain_curtainstatus_tv);
        close_tv = (TextView) findViewById(R.id.curtain_close_tv);
        cImageview = (ImageView) findViewById(R.id.curtain_curtain_iv);
    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        powersource_ibtn.setOnClickListener(this);
        curtainstatus_ibtn.setOnClickListener(this);
        close_ibtn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.curtain_back_ibtn://返回键
                finish();
                break;
            case R.id.curtain_edit_btn://编辑
                Intent intent = new Intent(ElectricityCurtainActivity.this, EditActivity.class);
                intent.putExtra("deviceName", cName);
                intent.putExtra("deviceRoom", cRoom);
                intent.putExtra("brand", cBrand);
                intent.putExtra("serial", cSerial);
                intent.putExtra("roomId", roomId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("deviceType", deviceType);
                intent.putExtra("type", 5);
                startActivityForResult(intent,100);
                break;
            case R.id.curtain_powersource_ibtn://开启
                if ("00".equals(cStatus) || "02".equals(cStatus)) {//处于关闭或者暂停状态
                    operation("1", "03", "01",2);
                }
                break;
            case R.id.curtain_curtainstatus_ibtn://暂停
                if ("00".equals(cStatus) || "01".equals(cStatus)) {
                    operation("1", "03", "02",3);

                }
                break;
            case R.id.curtain_close_ibtn://关闭
                if ("01".equals(cStatus) || "02".equals(cStatus)) {
                    operation("1", "03", "00",4);
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
                    cName = data.getStringExtra("roomName");
                    roomName_tv.setText(cName);
                    curtainName_tv.setText(data.getStringExtra("deviceName"));
                }
            }
        }
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
            Utils.showWaitDialog("加载中...", ElectricityCurtainActivity.this, mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceType + engineId +
                    orderMethod + methodType + orderId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device_id", deviceId);
            xutilsHelper.add("device_type", deviceType);
            xutilsHelper.add("method_type", methodType);
            xutilsHelper.add("order_id", orderId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", orderMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    Log.e(TAG + "窗帘操作", s);
                    Utils.dismissWaitDialog(mWaitDialog);
                    switch(arg1){
                        case 2://开启
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") != 9999) {
                                    Toast.makeText(ElectricityCurtainActivity.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ElectricityCurtainActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                                    cStatus = "01";
                                    keyInit();
                                    powersource_ibtn.setImageResource(R.mipmap.window_open_button);
                                    cImageview.setImageResource(R.mipmap.window_open);
                                    open_tv.setTextColor(getResources().getColor(R.color.yellow));
                                    curtainStatus_tv.setText("状态:开启");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 3://暂停
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") != 9999) {
                                    Toast.makeText(ElectricityCurtainActivity.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ElectricityCurtainActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                                    cStatus = "02";
                                    keyInit();
                                    curtainstatus_ibtn.setImageResource(R.mipmap.window_suspend_button);
                                    cImageview.setImageResource(R.mipmap.window_suspend);
                                    suspend_tv.setTextColor(getResources().getColor(R.color.yellow));
                                    curtainStatus_tv.setText("状态:暂停");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 4://关闭
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                if (jsonObject.getInt("status") != 9999) {
                                    Toast.makeText(ElectricityCurtainActivity.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ElectricityCurtainActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                                    cStatus = "00";
                                    keyInit();
                                    close_ibtn.setImageResource(R.mipmap.window_close_button);
                                    cImageview.setImageResource(R.mipmap.window_close);
                                    close_tv.setTextColor(getResources().getColor(R.color.yellow));
                                    curtainStatus_tv.setText("状态:关闭");
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
                    Toast.makeText(ElectricityCurtainActivity.this, "网络超时", Toast.LENGTH_SHORT).show();
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
     * 获取窗帘的详情
     */
    private void getStatus(String deviceid,String devicetype){
        Utils.showWaitDialog(getString(R.string.loadtext_load), ElectricityCurtainActivity.this, mWaitDialog);
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
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String tvResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "窗帘详情", tvResult);
                    try {
                        JSONObject jsonObject = new JSONObject(tvResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                            JSONObject jsonBasic = jsonMsg.getJSONObject("basic_msg");
                            cName = jsonBasic.getString("controlled_device_name");
                            cRoom = jsonBasic.getString("room_name");
                            curtainName_tv.setText(cName);
                            roomName_tv.setText(cRoom);
                            roomId = jsonBasic.getString("room_id");
                            deviceId = jsonBasic.getString("controlled_device_id");
                            deviceType = jsonBasic.getString("electric_type_id");
                            cStatus = jsonBasic.getString("status");
                            if ("00".equals(cStatus)) {//关闭
                                isClose = true;
                                keyInit();
                                close_ibtn.setImageResource(R.mipmap.window_close_button);
                                cImageview.setImageResource(R.mipmap.window_close);
                                close_tv.setTextColor(getResources().getColor(R.color.yellow));
                                curtainStatus_tv.setText("状态:关闭");
                            } else if ("02".equals(cStatus)) {//暂停
                                isSuspend = true;
                                keyInit();
                                curtainstatus_ibtn.setImageResource(R.mipmap.window_suspend_button);
                                cImageview.setImageResource(R.mipmap.window_suspend);
                                suspend_tv.setTextColor(getResources().getColor(R.color.yellow));
                                curtainStatus_tv.setText("状态:暂停");
                            } else if ("01".equals(cStatus)) {//开启
                                isOpen = true;
                                keyInit();
                                powersource_ibtn.setImageResource(R.mipmap.window_open_button);
                                cImageview.setImageResource(R.mipmap.window_open);
                                open_tv.setTextColor(getResources().getColor(R.color.yellow));
                                curtainStatus_tv.setText("状态:开启");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ElectricityCurtainActivity.this, "数据异常", Toast.LENGTH_SHORT).show();
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
    private void keyInit(){
        powersource_ibtn.setImageResource(R.mipmap.window_open_button_close);
        curtainstatus_ibtn.setImageResource(R.mipmap.window_suspend_button_close);
        close_ibtn.setImageResource(R.mipmap.window_close_button_close);
        open_tv.setTextColor(getResources().getColor(R.color.white));
        suspend_tv.setTextColor(getResources().getColor(R.color.white));
        close_tv.setTextColor(getResources().getColor(R.color.white));
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
