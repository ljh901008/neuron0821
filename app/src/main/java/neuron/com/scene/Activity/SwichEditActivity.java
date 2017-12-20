package neuron.com.scene.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;

import neuron.com.bean.DeviceSetFragmentBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.room.Activity.DeviceListActivity;
import neuron.com.room.Activity.RoomListActivity;
import neuron.com.room.Activity.SceneListActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/4   开关编辑页面
 */
public class SwichEditActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "SwichEditActivity";
    private ImageButton back;
    private Button confirm;
    private EditText deviceName_ed;
    private RelativeLayout room_rll,swichOne_rll,swichTwo_rll, swichThree_rll;
    private TextView roomName_tv,lightOneName_tv,lightTwoName_tv, lightThreeName_tv;
    private String roomId, roomName,neuronId,deviceType;
    private String deviceName, sceneName;
    private String account,token, engineId;
    private List<DeviceSetFragmentBean> list;
    private String methodDeleteScene = "DelNeuronSetting";
    private Intent intent;
    private String swichDataMethod = "QueryNeuronDetail";
    private SharedPreferencesManager sharedPreferencesManager;
    private String updateMethod = "UpdateDevices";
    private String deviceSite = "5";
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swichedit);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
        setListener();
    }

    private void init() {
        intent = getIntent();
        neuronId = intent.getStringExtra("neuronId");
        deviceType = intent.getStringExtra("deviceType");
        back = (ImageButton) findViewById(R.id.swichedit_back_iv);
        confirm = (Button) findViewById(R.id.swichedit_fonfirm_btn);
        deviceName_ed = (EditText) findViewById(R.id.swichedit_devicename_ed);
        room_rll = (RelativeLayout) findViewById(R.id.swichedit_roomname_rll);
        swichOne_rll = (RelativeLayout) findViewById(R.id.swichedit_swichone_rll);
        swichTwo_rll = (RelativeLayout) findViewById(R.id.swichedit_swichtwo_rll);
        swichThree_rll = (RelativeLayout) findViewById(R.id.swichedit_swichthree_rll);
        roomName_tv = (TextView) findViewById(R.id.swichedit_roomname_ed);
        lightOneName_tv = (TextView) findViewById(R.id.swichedit_swichone_lightname_tv);
        lightTwoName_tv = (TextView) findViewById(R.id.swichedit_swichtwo_lightname_tv);
        lightThreeName_tv = (TextView) findViewById(R.id.swichedit_swichthree_lightname_tv);
        if ("258".equals(deviceType)) {//单键有继电器开关
            swichTwo_rll.setVisibility(View.GONE);
            swichThree_rll.setVisibility(View.GONE);
        } else if ("261".equals(deviceType)) {//双键双继电器开关
            swichThree_rll.setVisibility(View.GONE);
        }else if ("265".equals(deviceType)) {//三键三继电器开关

        }
        deviceName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        swichData();
    }
    private void setListener(){
        room_rll.setOnClickListener(this);
        swichOne_rll.setOnClickListener(this);
        swichTwo_rll.setOnClickListener(this);
        swichThree_rll.setOnClickListener(this);
        back.setOnClickListener(this);
        confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.swichedit_back_iv://返回键
                finish();
                break;
            case R.id.swichedit_fonfirm_btn://确定
                deviceName = deviceName_ed.getText().toString().toString();
                roomName = roomName_tv.getText().toString().trim();
                if (!TextUtils.isEmpty(deviceName) && !TextUtils.isEmpty(roomName)) {
                    UpdateData(deviceName, roomId, neuronId);
                } else {
                    Utils.showDialog(this,"请设置名称和房间");
                }
                break;
            case R.id.swichedit_roomname_rll: //选择房间
                Intent intent1 = new Intent(SwichEditActivity.this, RoomListActivity.class);
                startActivityForResult(intent1,10);
                break;
            case R.id.swichedit_swichone_rll: //三键开关A键
                deviceSite = "0";
                selectDialog(Integer.valueOf(deviceSite));
                break;
            case R.id.swichedit_swichtwo_rll: //三键开关B键
                deviceSite = "1";
                selectDialog(Integer.valueOf(deviceSite));
                break;
            case R.id.swichedit_swichthree_rll: //三键开关C键
                deviceSite = "2";
                selectDialog(Integer.valueOf(deviceSite));
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
                case 100://场景名称
                    if (data != null) {
                        String sceneName = data.getStringExtra("sceneName");
                        if ("0".equals(deviceSite)) {
                            lightOneName_tv.setText(sceneName);
                        } else if ("1".equals(deviceSite)) {
                            lightTwoName_tv.setText(sceneName);
                        }else if ("2".equals(deviceSite)) {
                            lightThreeName_tv.setText(sceneName);
                        }
                    }
                    break;
                case 200://设备名称
                    if (data != null) {
                        String deviceName = data.getStringExtra("deviceName");
                        if ("0".equals(deviceSite)) {
                            lightOneName_tv.setText(deviceName);
                        } else if ("1".equals(deviceSite)) {
                            lightTwoName_tv.setText(deviceName);
                        }else if ("2".equals(deviceSite)) {
                            lightThreeName_tv.setText(deviceName);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *  获取开关详情
     */
    private void swichData(){
        Utils.showWaitDialog("加载中",this,mWaitDialog);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
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
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + swichDataMethod + neuronId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", swichDataMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String swichResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "开关详情", swichResult);
                    try {
                        JSONObject json = new JSONObject(swichResult);
                        if (json.getInt("status") == 9999) {
                            JSONObject swichJson = json.getJSONObject("msg");
                            neuronId = swichJson.getString("neuron_id");
                            deviceName = swichJson.getString("neuron_name");
                            deviceName_ed.setText(deviceName);
                            roomId = swichJson.getString("room_id");
                            roomName = swichJson.getString("room_name");
                            roomName_tv.setText(roomName);
                            deviceType = swichJson.getString("device_type_id");
                            JSONArray conJsa = swichJson.getJSONArray("controlled_device_list");
                            int length = conJsa.length();
                            if (length > 0) {
                                list = new ArrayList<DeviceSetFragmentBean>();
                                DeviceSetFragmentBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject jsonObject = conJsa.getJSONObject(i);
                                    bean = new DeviceSetFragmentBean();
                                    deviceSite = jsonObject.getString("controlled_device_site");
                                    bean.setSite(deviceSite);
                                    bean.setDeviceName(jsonObject.getString("controlled_device_name"));
                                    bean.setNeuronId(jsonObject.getString("controlled_device_id"));
                                    list.add(bean);
                                    if ("0".equals(deviceSite)) {
                                        lightOneName_tv.setText(jsonObject.getString("controlled_device_name"));
                                    } else if ("1".equals(deviceSite)) {
                                        lightTwoName_tv.setText(jsonObject.getString("controlled_device_name"));
                                    }else if ("2".equals(deviceSite)) {
                                        lightThreeName_tv.setText(jsonObject.getString("controlled_device_name"));
                                    }
                                }
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(SwichEditActivity.this, "网络不通", Toast.LENGTH_LONG).show();
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
     *
     * @param tag  开关的键位  0A键  1B键  2C键
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
        Display display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        //设置对话框的宽高
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width * 90 / 100,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setContentView(view,layoutParams);
        deviceBtn.setOnClickListener(new View.OnClickListener() {//设备
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(SwichEditActivity.this, DeviceListActivity.class);
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
                Intent intent1 = new Intent(SwichEditActivity.this, SceneListActivity.class);
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
            Utils.showWaitDialog("加载中...", SwichEditActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deleteType +engineId + methodDeleteScene + neuronId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("condition", deleteType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodDeleteScene);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    try {
                        Log.e(TAG + "删除开关下绑定的设备/场景", deleteResult);
                        JSONObject json = new JSONObject(deleteResult);
                        if (json.getInt("status") == 9999) {
                            Utils.showDialog(SwichEditActivity.this, "删除成功");
                        } else {
                            Utils.showDialog(SwichEditActivity.this, json.getString("error"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(SwichEditActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
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
     * 修改红外人体感应名称和房间
     */
    private void UpdateData(String deviceName,String roomId,String deviceId){
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
            jsonObject.put("device_id", deviceId);
            jsonObject.put("room_id", roomId);
            jsonObject.put("device_name", deviceName);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + jsonObject.toString() + engineId + updateMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", updateMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        Log.e(TAG + "result", result);
                        JSONObject json = new JSONObject(result);
                        if (json.getInt("status") == 9999) {
                            Utils.showDialog(SwichEditActivity.this,"设置成功");
                        } else {
                            Utils.showDialog(SwichEditActivity.this, json.getString("error"));
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
