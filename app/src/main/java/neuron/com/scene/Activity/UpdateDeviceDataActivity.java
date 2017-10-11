package neuron.com.scene.Activity;

import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.room.Activity.RoomListActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/9.  修改节点设备名称和房间
 */
public class UpdateDeviceDataActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "UpdateDeviceDataActivity";
    private ImageButton back_ibtn;
    private Button ensure_btn;
    private EditText deviceName_ed;
    private TextView roomName_tv;
    private RelativeLayout roomName_rll;
    private TextView titleName;
    private String deviceName;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId,roomId,roomName;
    private String neuronId,deviceType;
    private String updateMethod = "UpdateDevices";
    private Intent intent;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1://修改房间名 和设备名称
                    if (msg.what == 102) {
                        String updateResult = (String) msg.obj;
                        Log.e(TAG + "修改设备名称和房间", updateResult);
                        try {
                            JSONObject jsonObject = new JSONObject(updateResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(UpdateDeviceDataActivity.this, jsonObject.getString("error"));
                            } else {
                              //  Utils.showDialog(UpdateDeviceDataActivity.this, "修改成功");
                                final AlertDialog builder = new AlertDialog.Builder(UpdateDeviceDataActivity.this).create();
                                View view = View.inflate(UpdateDeviceDataActivity.this, R.layout.dialog_textview, null);
                                TextView title = (TextView) view.findViewById(R.id.textView1);
                                Button button = (Button) view.findViewById(R.id.button1);
                                title.setText("修改成功");
                                builder.setView(view);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        intent.putExtra("tag", 1);
                                        setResult(RESULT_OK);
                                        builder.dismiss();
                                        finish();
                                    }
                                });
                                builder.show();
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
        setContentView(R.layout.edit);
        init();
        setListener();
    }

    private void init() {
        intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        deviceName = intent.getStringExtra("deviceName");
        roomName = intent.getStringExtra("roomName");
        neuronId = intent.getStringExtra("neuronId");
        deviceType = intent.getStringExtra("deviceType");
        back_ibtn = (ImageButton) findViewById(R.id.edit_back_iv);
        ensure_btn = (Button) findViewById(R.id.edit_fonfirm_btn);
        deviceName_ed = (EditText) findViewById(R.id.edit_devicename_ed);
        titleName = (TextView) findViewById(R.id.edit_titlename_tv);
        deviceName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        roomName_tv = (TextView) findViewById(R.id.edit_selectroom_tv);
        roomName_rll = (RelativeLayout) findViewById(R.id.edit_selectroom_rll);
        roomName_tv.setText(roomName);
        deviceName_ed.setText(deviceName);
        Log.e("设备类型", deviceType);
        if (deviceType.equals("769")) {
            titleName.setText("红外人体感应器编辑");
        } else if (deviceType.equals("770")) {
            titleName.setText("智能门磁编辑");
        }else if (deviceType.equals("1026")) {
            titleName.setText("智能窗帘编辑");
        }else if (deviceType.equals("1281")) {
            titleName.setText("空气质量检测仪编辑");
        }else if (deviceType.equals("2049")) {
            titleName.setText("摄像头编辑");
        }else if (deviceType.equals("4097")) {
            titleName.setText("净水器编辑");
        }else if (deviceType.equals("1537")) {
            titleName.setText("智能插座编辑");
        }
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
                if (!TextUtils.isEmpty(deviceName)) {
                    updateDevice(deviceName, neuronId, roomId);
                } else {
                    Utils.showDialog(UpdateDeviceDataActivity.this, "请输入名称");
                }
                break;
            case R.id.edit_selectroom_rll://房间
                Intent intent1 = new Intent(UpdateDeviceDataActivity.this, RoomListActivity.class);
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
     *   更新节点设备名称 房间
     * @param deviceName  设备名称
     * @param neuronId     设备id
     * @param roomId    房间id
     */
    private void updateDevice(String deviceName,String neuronId,String roomId){
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(UpdateDeviceDataActivity.this);
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
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_id", neuronId);
            jsonObject.put("device_name", deviceName);
            jsonObject.put("room_id", roomId);
            String sign = MD5Utils.MD5Encode(aesAccount + jsonObject.toString() + engineId + updateMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", updateMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
