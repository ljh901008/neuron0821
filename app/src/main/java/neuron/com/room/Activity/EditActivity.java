package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/4/18. 空调，电视，窗帘,门磁 修改房间和设备名称页面
 */
public class EditActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "EditActivity";
    private ImageButton back_ibtn;
    private Button ensure_btn;
    private EditText deviceName_ed;
    private TextView roomName_tv;
    private RelativeLayout roomName_rll;

    private SharedPreferencesManager sharedPreferencesManager = null;
    private String methodUpdateDevice = "UpdateControlledDevice";
    private String account,engineId, token;
    private Intent intent;
    //设备Id,设备类别
    private String deviceId,deviceTypeId,deviceName,deviceBrand,deviceSerial,roomId,roomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        init();
        setListener();
    }

    private void init() {
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        deviceTypeId = intent.getStringExtra("deviceType");
        deviceName = intent.getStringExtra("deviceName");
        deviceBrand = intent.getStringExtra("brand");
        deviceSerial = intent.getStringExtra("serial");
        roomId = intent.getStringExtra("roomId");
        roomName = intent.getStringExtra("deviceRoom");
        Log.e(TAG + "编辑页收到数据列表", deviceId + deviceTypeId + deviceName + deviceBrand + deviceSerial + roomId + roomName);
        back_ibtn = (ImageButton) findViewById(R.id.edit_back_iv);
        ensure_btn = (Button) findViewById(R.id.edit_fonfirm_btn);
        deviceName_ed = (EditText) findViewById(R.id.edit_devicename_ed);
        roomName_tv = (TextView) findViewById(R.id.edit_selectroom_tv);
        roomName_rll = (RelativeLayout) findViewById(R.id.edit_selectroom_rll);
        deviceName_ed.setText(deviceName);
        roomName_tv.setText(roomName);
        deviceName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        ensure_btn.setOnClickListener(this);
        roomName_rll.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.edit_back_iv://返回键
                finish();
                break;
            case R.id.edit_fonfirm_btn://确定
                deviceName = deviceName_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(deviceName) && deviceName.length() >= 2 && deviceName.length() < 11) {
                    UpdateData(deviceName, roomId, deviceBrand, deviceSerial);
                    intent.putExtra("roomName", roomName);
                    intent.putExtra("deviceName", deviceName);
                    setResult(RESULT_OK, intent);
                } else {
                    Utils.showDialog(EditActivity.this, "请输入2-10位设备名称");
                }
                break;
            case R.id.edit_selectroom_rll://房间
                Intent intent1 = new Intent(EditActivity.this, RoomListActivity.class);
                startActivityForResult(intent1,10);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                if (data != null) {
                    roomId = data.getStringExtra("roomId");
                    roomName = data.getStringExtra("roomName");
                    roomName_tv.setText(roomName);
                }
            }
        }
    }

    /**
     * 修改设备名称和房间
     */
    private void UpdateData(String devicename,String roomId,String brand,String serial){
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(EditActivity.this);
        }
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
            jsonObject.put("controlled_device_name", devicename);
            jsonObject.put("room_id", roomId);
            jsonObject.put("controlled_device_brand", brand);
            jsonObject.put("controlled_device_serial", serial);
            Log.e(TAG + "", jsonObject.toString());
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceTypeId + engineId + methodUpdateDevice + jsonObject.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("electric_type_id", deviceTypeId);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodUpdateDevice);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(1, this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String updateResult) {
                    Log.e(TAG + "修改设备名称和房间", updateResult);
                    try {
                        JSONObject jsonObject = new JSONObject(updateResult);
                        if (jsonObject.getInt("status") != 9999) {
                            Utils.showDialog(EditActivity.this, jsonObject.getString("error"));
                        } else {
                            final AlertDialog builder = new AlertDialog.Builder(EditActivity.this).create();
                            View view = View.inflate(EditActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("修改成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    deviceName = deviceName_ed.getText().toString().trim();
                                    intent.putExtra("roomName", roomName);
                                    if (!TextUtils.isEmpty(deviceName)) {
                                        intent.putExtra("deviceName", deviceName);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } else {
                                        Utils.showDialog(EditActivity.this, "请输入设备名称");
                                    }
                                    builder.dismiss();
                                }
                            });
                            builder.show();
                        }
                    } catch (JSONException e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
