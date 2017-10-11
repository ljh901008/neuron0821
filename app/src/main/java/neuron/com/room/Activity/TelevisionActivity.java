package neuron.com.room.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
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
 * Created by ljh on 2017/4/18.  电视详情页
 */
public class TelevisionActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "TelevisionActivity";
    private ImageButton back_ibtn;
    // 电源键，上，左，右，下，返回,静音键
    private ImageButton powersource_ibtn,up_ibtn,left_ibtn,right_ibtn, bottom_ibtn,backMenu_itbn,voice_ibtn;
    //编辑，菜单键
    private Button edit_btn, menu_btn,study_btn;
    private TextView tvName_tv, tvRoom_tv;
    private Intent intent;
    private String deviceId, deviceType;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private String account,engineId, token;

    private String tvName, tvRoom,tvBrand,tvSerial,roomId;
    private String method = "GetControlledDeviceDetail";
    private String operationMethod = "RemoteControlTV";//电视机操作
    private String enterStudyMethod = "EnterLearningMode";//进入学习模式
    private String outStudyMethod = "QuitLearningMode";//退出学习模式
    private String studyInfraredMethod = "GotoLearning";//红外学习
    private boolean isStudy = false;//是否在学习模式的标记
    private Button help_btn;
    private WaitDialog mWaitDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1:
                    if (msg.what == 102) {
                        Utils.dismissWaitDialog(mWaitDialog);
                        String tvResult = (String) msg.obj;
                        Log.e(TAG + "电视详情", tvResult);
                        try {
                            JSONObject jsonObject = new JSONObject(tvResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                                if (jsonMsg.getInt("is_learning") == 0) {//可以学习
                                    study_btn.setVisibility(View.VISIBLE);
                                    help_btn.setVisibility(View.VISIBLE);
                                } else {
                                    study_btn.setVisibility(View.GONE);
                                    help_btn.setVisibility(View.GONE);
                                }
                                JSONObject jsonBasic = jsonMsg.getJSONObject("basic_msg");
                                tvName = jsonBasic.getString("controlled_device_name");
                                tvRoom = jsonBasic.getString("room_name");
                                tvName_tv.setText(tvName);
                                tvRoom_tv.setText(tvRoom);
                                tvBrand = jsonBasic.getString("controlled_device_brand");
                                tvSerial = jsonBasic.getString("controlled_device_serial");
                                roomId = jsonBasic.getString("room_id");
                                deviceId = jsonBasic.getString("controlled_device_id");
                                deviceType = jsonBasic.getString("electric_type_id");

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                    }
                    break;
                case 2://学习状态下操作
                    if (msg.what == 102) {
                        String studyResult = (String) msg.obj;
                        Log.e(TAG + "电视学习", studyResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(studyResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Toast.makeText(TelevisionActivity.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(TelevisionActivity.this, "学习成功", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                    }
                    break;
                case 3://正常操作
                    if (msg.what == 102) {
                        String updateResult = (String) msg.obj;
                        Log.e(TAG + "电视操作", updateResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(updateResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Toast.makeText(TelevisionActivity.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                            } else {
                                //Utils.showDialog(TelevisionActivity.this, "操作成功");
                                Toast.makeText(TelevisionActivity.this, "操作成功", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                    }
                    break;
                case 4://是否进入学习状态
                    if (msg.what == 102) {
                        String studyResult = (String) msg.obj;
                        Log.e(TAG + "电视操作", studyResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(studyResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Toast.makeText(TelevisionActivity.this, jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                            } else {
                                if (isStudy) {
                                    Toast.makeText(TelevisionActivity.this, "已进入学习状态", Toast.LENGTH_SHORT).show();
                                    study_btn.setText("完成学习");
                                } else {
                                    Toast.makeText(TelevisionActivity.this, "已退出学习状态", Toast.LENGTH_LONG).show();
                                    study_btn.setText("进入学习");
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
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
        setContentView(R.layout.television);
        init();
        setListener();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getStatus(deviceId,deviceType);
    }
    private void init() {
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        deviceType = intent.getStringExtra("deviceType");
        tvName_tv = (TextView) findViewById(R.id.television_devicename_tv);
        tvRoom_tv = (TextView) findViewById(R.id.television_roomname_tv);
        back_ibtn = (ImageButton) findViewById(R.id.television_back_ibtn);
        powersource_ibtn = (ImageButton) findViewById(R.id.television_powersource_ibtn);
        up_ibtn = (ImageButton) findViewById(R.id.television_up_ibtn);
        left_ibtn = (ImageButton) findViewById(R.id.television_left_ibtn);
        right_ibtn = (ImageButton) findViewById(R.id.television_right_ibtn);
        bottom_ibtn = (ImageButton) findViewById(R.id.television_bottom_ibtn);
        backMenu_itbn = (ImageButton) findViewById(R.id.television_backmenu_ibtn);
        voice_ibtn = (ImageButton) findViewById(R.id.television_voice_ibtn);
        edit_btn = (Button) findViewById(R.id.television_edit_btn);
        menu_btn = (Button) findViewById(R.id.television_menu_btn);
        study_btn = (Button) findViewById(R.id.television_study_btn);
        help_btn = (Button) findViewById(R.id.television_help_btn);
    }
    private void setListener(){
        back_ibtn.setOnClickListener(this);
        powersource_ibtn.setOnClickListener(this);
        up_ibtn.setOnClickListener(this);
        left_ibtn.setOnClickListener(this);
        right_ibtn.setOnClickListener(this);
        bottom_ibtn.setOnClickListener(this);
        backMenu_itbn.setOnClickListener(this);
        voice_ibtn.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
        menu_btn.setOnClickListener(this);
        study_btn.setOnClickListener(this);
        help_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.television_back_ibtn://返回键
                finish();
                break;
            case R.id.television_help_btn://帮助
                helpDialog();
                break;
            case R.id.television_up_ibtn://上
                if (isStudy) {
                    operationTV("4", studyInfraredMethod, 2);
                } else {
                    operationTV("4", operationMethod, 3);
                }
                break;
            case R.id.television_bottom_ibtn://下
                if (isStudy) {
                    operationTV("5", studyInfraredMethod, 2);
                } else {
                    operationTV("5", operationMethod, 3);
                }
                break;
            case R.id.television_powersource_ibtn://电源
                if (isStudy) {
                    operationTV("1", studyInfraredMethod, 2);
                } else {

                    operationTV("1", operationMethod, 3);
                }
                break;
            case R.id.television_left_ibtn://左
                if (isStudy) {
                    operationTV("6", studyInfraredMethod, 2);
                } else {
                    operationTV("6", operationMethod, 3);
                }
                break;
            case R.id.television_right_ibtn://右
                if (isStudy) {
                    operationTV("7", studyInfraredMethod, 2);
                } else {
                    operationTV("7", operationMethod, 3);
                }
                break;
            case R.id.television_backmenu_ibtn://返回菜单键
                if (isStudy) {
                    operationTV("8", studyInfraredMethod, 2);
                } else {
                    operationTV("8", operationMethod, 3);
                }
                break;
            case R.id.television_voice_ibtn://静音
                if (isStudy) {
                    operationTV("2", studyInfraredMethod, 2);
                } else {
                    operationTV("2", operationMethod, 3);
                }
                break;
            case R.id.television_edit_btn://编辑
                Intent intent = new Intent(TelevisionActivity.this, EditActivity.class);
                intent.putExtra("deviceName", tvName);
                intent.putExtra("deviceRoom", tvRoom);
                intent.putExtra("brand", tvBrand);
                intent.putExtra("serial", tvSerial);
                intent.putExtra("roomId", roomId);
                intent.putExtra("deviceId", deviceId);
                intent.putExtra("deviceType", deviceType);
                intent.putExtra("type", 2);
                startActivityForResult(intent,100);
                break;
            case R.id.television_menu_btn://菜单
                if (isStudy) {
                    operationTV("3", studyInfraredMethod, 2);
                } else {
                    operationTV("3", operationMethod, 3);
                }
                break;
            case R.id.television_study_btn://学习
                if (isStudy) {
                    TvStudy(outStudyMethod);
                    isStudy = false;
                } else {
                    TvStudy(enterStudyMethod);
                    isStudy = true;

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
                    tvName = data.getStringExtra("deviceName");
                    tvRoom_tv.setText(tvName);
                    tvRoom = data.getStringExtra("roomName");
                    tvRoom_tv.setText(tvRoom);
                }
            }
        }
    }

    /**
     *   是否进入学习模式
     * @param methodId
     */
    private void TvStudy(String methodId){
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
        try {
            Utils.showWaitDialog("加载中...", TelevisionActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            Log.e(TAG + "电视学习数据流", aesAccount +","+ deviceId +"," + deviceType +"," + engineId +"," + methodId +"," + token);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + deviceType + engineId + methodId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("electric_type_id", deviceType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodId);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(4, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *    红外学习和 电视操作接口
     * @param tvKeyId
     * @param methodName
     * @param arg1
     */
    private void operationTV(String tvKeyId,String methodName,int arg1){
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
        try {
            Log.e(TAG + "红外学习数据流", String.valueOf(isStudy));
            Utils.showWaitDialog(getString(R.string.loadtext_load),TelevisionActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceId + engineId + tvKeyId + methodName + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("controlled_device_id", deviceId);
            xutilsHelper.add("key_id", tvKeyId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodName);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(arg1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取电视的详情
     */
    private void getStatus(String deviceId,String deviceType){
        Utils.showWaitDialog(getString(R.string.loadtext_load),TelevisionActivity.this,mWaitDialog);
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
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            Log.e(TAG + "电视详情数据流", aesAccount +","+ deviceId +"," + deviceType +"," + engineId +"," + method +"," + token);
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
    private void helpDialog(){
        final Dialog dialog = new Dialog(this);
        //去掉dialog标题栏
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = LayoutInflater.from(this).inflate(R.layout.televisionhelp, null);
        Button close = (Button) view.findViewById(R.id.help_close_btn);
        dialog.show();
        dialog.setContentView(view);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
}
