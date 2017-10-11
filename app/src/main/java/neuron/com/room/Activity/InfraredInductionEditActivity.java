package neuron.com.room.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/4/26. 红外人体感应编辑页面
 */
public class InfraredInductionEditActivity extends BaseActivity implements View.OnClickListener {
    private String TAG = "InfraredInductionEditActivity";
    private ImageButton back_ibtn;
    private Button edit_btn;
    private TextView roomName_tv,somebodySceneName_tv, nobodySceneName_tv;
    private RelativeLayout roomName_rll,somebodySceneName_rll, nobodySceneName_rll;
    private EditText deviceName_ed;
    private String roomId, roomName;
    private String deviceName,deviceId,deviceType;
    private String somebodySceneId,nobodySceneId,somebodySceneName, nobodySceneName,neuronId;
    private Intent intent;
    private String infraredData = null;
    private int condition = 5;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private String methodDeleteScene = "DelNeuronSetting";
    private String updateMethod = "UpdateControlledDevice";
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://删除场景
                    if (msg.what == 102) {
                        String deleteResult = (String) msg.obj;
                        Log.e(TAG + "删除场景，警报",deleteResult);
                        try {
                            JSONObject jsonObject = new JSONObject(deleteResult);
                            if (jsonObject.getInt("status") == 9999) {
                                Utils.showDialog(InfraredInductionEditActivity.this, "删除成功");
                            } else {
                                Utils.showDialog(InfraredInductionEditActivity.this, jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2://修改名称和房间
                    if (msg.what == 102) {
                        try {
                            String result = (String) msg.obj;
                            Log.e(TAG + "result", result);
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("status") == 9999) {
                               Utils.showDialog(InfraredInductionEditActivity.this,"设置成功");
                            } else {
                                Utils.showDialog(InfraredInductionEditActivity.this, json.getString("error"));
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
        setContentView(R.layout.infraredinductionedit);
        init();
        setListener();
    }

    private void init() {
        intent = getIntent();
        infraredData = intent.getStringExtra("infrared");
        back_ibtn = (ImageButton) findViewById(R.id.infraredinductionedit_back_ibtn);
        edit_btn = (Button) findViewById(R.id.infraredinductionedit_edit_btn);
        roomName_tv = (TextView) findViewById(R.id.infraredinductionedit_roomname_ed);
        somebodySceneName_tv = (TextView) findViewById(R.id.infraredinductionedit_somebody_tv);
        nobodySceneName_tv = (TextView) findViewById(R.id.infraredinductionedit_nobody_tv);

        roomName_rll = (RelativeLayout) findViewById(R.id.infraredinductionedit_roomname_rll);
        somebodySceneName_rll = (RelativeLayout) findViewById(R.id.infraredinductionedit_somebody_rll);
        nobodySceneName_rll = (RelativeLayout) findViewById(R.id.infraredinductionedit_nobody_rll);
        deviceName_ed = (EditText) findViewById(R.id.infraredinductionedit_devicename_ed);
        deviceName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        analysis(infraredData);
    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
        roomName_rll.setOnClickListener(this);
        somebodySceneName_rll.setOnClickListener(this);
        nobodySceneName_rll.setOnClickListener(this);

    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.infraredinductionedit_back_ibtn://返回键
                finish();
                break;
            case R.id.infraredinductionedit_edit_btn://确定
                deviceName = deviceName_ed.getText().toString().trim();
                roomName = roomName_tv.getText().toString().trim();
                if (!TextUtils.isEmpty(deviceName) && !TextUtils.isEmpty(roomName)) {
                    UpdateData(deviceName, roomId, deviceId,deviceType);
                } else {
                    Utils.showDialog(this,"请设置名称和房间");
                }
                break;
            case R.id.infraredinductionedit_roomname_rll: //选择房间
                Intent intent1 = new Intent(InfraredInductionEditActivity.this, RoomListActivity.class);
                startActivityForResult(intent1,10);
                break;
            case R.id.infraredinductionedit_somebody_rll: //有人场景
                condition = 0;
                selectDialog(condition);
                break;
            case R.id.infraredinductionedit_nobody_rll: //无人场景
                condition = 1;
                selectDialog(condition);
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
                case 10:
                    if (data != null) {
                        roomId = data.getStringExtra("roomId");
                        roomName = data.getStringExtra("roomName");
                        roomName_tv.setText(roomName);
                    }
                    break;
                case 100://设置场景
                    if (data != null) {
                        String sceneName = data.getStringExtra("sceneName");
                        Log.e(TAG + "场景名称", deviceName);
                        if (condition == 0) {
                            somebodySceneName_tv.setText(sceneName);
                        } else {
                            nobodySceneName_tv.setText(sceneName);
                        }
                    }
                    break;
                case 200://shebe
                    if (data != null) {
                        String deviceName = data.getStringExtra("roomName");
                        //Log.e(TAG + "场景名称", deviceName);
                        if (condition == 0) {
                            somebodySceneName_tv.setText(deviceName);
                        } else {
                            nobodySceneName_tv.setText(deviceName);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *  解析 红外人体感应 详情
     * @param infrared
     */
    private void analysis(String infrared){
        try {
            JSONObject jsonObject = new JSONObject(infrared);
            if (jsonObject.getInt("status") == 9999) {
                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                JSONObject jsonBasic = jsonMsg.getJSONObject("basic_msg");
                deviceName = jsonBasic.getString("controlled_device_name");
                roomName = jsonBasic.getString("room_name");
                deviceName_ed.setText(deviceName);
                roomName_tv.setText(roomName);
                roomId = jsonBasic.getString("room_id");
                deviceId = jsonBasic.getString("controlled_device_id");
                deviceType = jsonBasic.getString("electric_type_id");
                neuronId = jsonBasic.getString("neuron_id");
                //deviceStatus = jsonBasic.getString("status");
                JSONArray jsonOther = jsonMsg.getJSONArray("other_msg");
                int length = jsonOther.length();
                if (length > 0) {
                    for (int i = 0; i < length; i++) {
                        JSONObject jso = jsonOther.getJSONObject(i);
                        if (jso.getInt("condition") == 0) {//有人模式的场景名称
                            somebodySceneId = jso.getString("id");
                            somebodySceneName = jso.getString("name");
                            somebodySceneName_tv.setText(somebodySceneName);

                        } else {//无人模式的场景名称
                            nobodySceneId = jso.getString("id");
                            nobodySceneName = jso.getString("name");
                            nobodySceneName_tv.setText(nobodySceneName);

                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param tag 红外人体感应  有人或者无人的标记 0 有人  1无人
     */
    private void selectDialog(final int tag){
        final Dialog builder = new Dialog(this);
        //去掉dialog标题栏
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.show();
        View view = LayoutInflater.from(this).inflate(R.layout.infraredselect,null);
        Button deviceBtn = (Button) view.findViewById(R.id.infraredselect_device_btn);
        Button sceneBtn = (Button) view.findViewById(R.id.infraredselect_scene_btn);
        Button deleteBtn = (Button) view.findViewById(R.id.infraredselect_delete_btn);
        Button clearBtn = (Button) view.findViewById(R.id.infraredselect_clear_btn);
        deviceBtn.setVisibility(View.GONE);//此功能暂时不做
        Display display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        //设置对话框的宽高
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width * 90 / 100,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setContentView(view,layoutParams);
        deviceBtn.setOnClickListener(new View.OnClickListener() {//设备
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(InfraredInductionEditActivity.this, DeviceListActivity.class);
                intent2.putExtra("deviceType", deviceType);
                intent2.putExtra("tag", tag);
                intent2.putExtra("neuronId", neuronId);
                startActivityForResult(intent2, 200);
                builder.dismiss();
            }
        });
        sceneBtn.setOnClickListener(new View.OnClickListener() {//场景
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(InfraredInductionEditActivity.this, SceneListActivity.class);
                intent1.putExtra("neuronId", neuronId);
                intent1.putExtra("tag", tag);
                startActivityForResult(intent1,100);
                builder.dismiss();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {//删除
            @Override
            public void onClick(View view) {
                deleteScene(String.valueOf(tag));
                builder.dismiss();
            }
        });
        clearBtn.setOnClickListener(new View.OnClickListener() {//取消
            @Override
            public void onClick(View view) {
                builder.dismiss();
            }
        });
    }
    /**
     *  删除绑定的场景
     * @param deleteType
     */
    private void deleteScene(String deleteType){
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
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
            String sign = MD5Utils.MD5Encode(aesAccount + deleteType +engineId + methodDeleteScene + neuronId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("condition", deleteType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodDeleteScene);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 修改红外人体感应名称和房间
     */
    private void UpdateData(String deviceName,String roomId,String deviceId,String deviceType){
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
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
            jsonObject.put("controlled_device_name", deviceName);
            jsonObject.put("room_id", roomId);
            jsonObject.put("controlled_device_brand", "");
            jsonObject.put("controlled_device_serial", "");
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            Log.e(TAG + "修改红外房间和名称数据流", aesAccount + "," + deviceId + "," + deviceType + "," + engineId + "," + updateMethod + "," + jsonObject.toString() + "," + token);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceType + engineId + updateMethod + jsonObject.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("electric_type_id", deviceType);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", updateMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(2, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
