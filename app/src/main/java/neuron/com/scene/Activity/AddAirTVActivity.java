package neuron.com.scene.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
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
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/5.
 */
public class AddAirTVActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AddAirTVActivity";
    private ImageButton back_ibtn;
    private Button confirm_btn;
    private RelativeLayout room_rll,deviceType_rll,deviceBrand_rll, deviceSerial_rll;
    private EditText deviceName_ed;
    private TextView roomName_tv,deviceType_tv,devcieBrand_tv, deviceSerial_tv;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private Intent intent;
    private String roomName,roomId, neuronName,neuronId,deviceType;
    private String electricId = "";
    //设备品牌，品牌Id ，系列，系列Id
    private String brandName,brandId,serialName, serialId;
    private String method = "AddInfradedEquipment";
    private WaitDialog mWaitDialog;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1://添加电视空调
                    if (msg.what == 102) {
                        String setTimeResult = (String) msg.obj;
                        Log.e(TAG + "添加电视和空调", setTimeResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject jsonObject = new JSONObject(setTimeResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(AddAirTVActivity.this, jsonObject.getString("error"));
                            } else {
                                final AlertDialog builder = new AlertDialog.Builder(AddAirTVActivity.this).create();
                                View view = View.inflate(AddAirTVActivity.this, R.layout.dialog_textview, null);
                                TextView title = (TextView) view.findViewById(R.id.textView1);
                                Button button = (Button) view.findViewById(R.id.button1);
                                title.setText("添加成功");
                                builder.setView(view);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        /*Intent intent = new Intent(AddAirTVActivity.this, InfraredTransponderActivity.class);
                                        intent.putExtra("neuronId", neuronId);
                                        intent.putExtra("deviceType", deviceType);
                                        startActivity(intent);*/
                                        intent.putExtra("type", 1);
                                        setResult(RESULT_OK,intent);
                                        finish();
                                        builder.dismiss();
                                    }
                                });
                                builder.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.dismissWaitDialog(mWaitDialog);
                        Toast.makeText(AddAirTVActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.addairtv);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
        setListener();
    }
    private void init() {
        intent = getIntent();
        neuronId = intent.getStringExtra("neuronId");
        deviceType = intent.getStringExtra("deviceType");
        back_ibtn = (ImageButton) findViewById(R.id.addairtv_back_iv);
        confirm_btn = (Button) findViewById(R.id.addairtv_confirm_btn);

        room_rll = (RelativeLayout) findViewById(R.id.addairtv_roomname_rll);
        deviceType_rll = (RelativeLayout) findViewById(R.id.addairtv_devicetype_rll);
        deviceBrand_rll = (RelativeLayout) findViewById(R.id.addairtv_devicebrand_rll);
        deviceSerial_rll = (RelativeLayout) findViewById(R.id.addairtv_deviceserial_rll);
        deviceName_ed = (EditText) findViewById(R.id.swichedit_devicename_ed);
        deviceName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        roomName_tv = (TextView) findViewById(R.id.addairtv_roomname_tv);
        deviceType_tv = (TextView) findViewById(R.id.addairtv_devicetype_tv);
        devcieBrand_tv = (TextView) findViewById(R.id.addairtv_devicebrand_tv);
        deviceSerial_tv = (TextView) findViewById(R.id.addairtv_deviceserial_tv);
    }
    private void setListener(){
        room_rll.setOnClickListener(this);
        deviceType_rll.setOnClickListener(this);
        deviceBrand_rll.setOnClickListener(this);
        deviceSerial_rll.setOnClickListener(this);
        back_ibtn.setOnClickListener(this);
        confirm_btn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.addairtv_back_iv://返回
                finish();
                break;
            case R.id.addairtv_confirm_btn://确定
                neuronName = deviceName_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(neuronName)) {
                    if (!TextUtils.isEmpty(roomId)) {
                        if (!TextUtils.isEmpty(electricId)) {
                            if (!TextUtils.isEmpty(brandName)) {
                                if (!TextUtils.isEmpty(brandName)) {
                                    addAirTV(brandId, serialId, neuronName, electricId);
                                }else {
                                    Utils.showDialog(this,"请选择设备系列");
                                }
                            }else {
                                Utils.showDialog(this,"请选择设备品牌");
                            }
                        }else {
                            Utils.showDialog(this,"请选择设备类型");
                        }
                    }else {
                        Utils.showDialog(this,"请选择房间");
                    }
                } else {
                    Utils.showDialog(this,"请输入设备名称");
                }
                break;
            case R.id.addairtv_roomname_rll://房间
                Intent intent1 = new Intent(AddAirTVActivity.this, RoomListActivity.class);
                startActivityForResult(intent1,10);
                break;
            case R.id.addairtv_devicetype_rll://类型
                selectDialog();
                break;
            case R.id.addairtv_devicebrand_rll://品牌
                if (!TextUtils.isEmpty(electricId)) {
                    Intent intent = new Intent(AddAirTVActivity.this, AirTVBrandListActivity.class);
                    intent.putExtra("electricId", electricId);
                    startActivityForResult(intent, 100);
                } else {
                    Utils.showDialog(this,"请选择设备类型");
                }
                break;
            case R.id.addairtv_deviceserial_rll://系列
                if (!TextUtils.isEmpty(brandId)) {
                    Intent intent = new Intent(AddAirTVActivity.this, AirTVSerialListActivity.class);
                    if ("1&".equals(brandId)) {
                        intent.putExtra("brandId", brandId);
                        intent.putExtra("neuronId", neuronId);
                        intent.putExtra("electricId", electricId);
                    } else {
                        intent.putExtra("brandId", brandId.substring(2,brandId.length()));
                        intent.putExtra("neuronId", neuronId);
                        intent.putExtra("electricId", electricId);
                    }
                    startActivityForResult(intent, 101);
                } else {
                    Utils.showDialog(this,"请选择设备品牌");
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
            switch(requestCode){
                case 100://设备品牌
                    if (data != null) {
                        brandName = data.getStringExtra("brandName");
                        brandId = data.getStringExtra("brandId");
                        Log.e(TAG + "电视自定义品牌", brandId + brandName);
                        devcieBrand_tv.setText(brandName);
                    }
                    break;
                case 101://设备系列
                    if (data != null) {
                        serialName = data.getStringExtra("serialName");
                        serialId = data.getStringExtra("serialId");

                        deviceSerial_tv.setText(serialName);
                    }
                    break;
                case 10://房间
                    if (data != null) {
                        roomName = data.getStringExtra("roomName");
                        roomId = data.getStringExtra("roomId");
                        roomName_tv.setText(roomName);
                    }
                    break;
                default:
                break;
            }
        }
    }

    /**
     *
     * @param brandId       品牌Id
     * @param serialId      系列id
     */
    private void addAirTV(String brandId,String serialId,String neuronName,String electricId){
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
        Log.e(TAG + "添加电视空调数据流", brandId + "," + serialId + "," + neuronName + "," + electricId);
        Utils.showWaitDialog("加载中...", AddAirTVActivity.this,mWaitDialog);
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + brandId + neuronName + serialId + electricId + engineId + method + neuronId + roomId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("electric_type", electricId);
            xutilsHelper.add("electric_name", neuronName);
            xutilsHelper.add("room_id", roomId);
            xutilsHelper.add("electric_brand", brandId);
            xutilsHelper.add("electric_serial", serialId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 选择添加的设备类型
     */
    private void selectDialog(){
        final Dialog builder = new Dialog(this);
        //去掉dialog标题栏
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.show();
        View view = LayoutInflater.from(this).inflate(R.layout.infraredselect,null);
        Button deviceBtn = (Button) view.findViewById(R.id.infraredselect_device_btn);
        Button sceneBtn = (Button) view.findViewById(R.id.infraredselect_scene_btn);
        Button deleteBtn = (Button) view.findViewById(R.id.infraredselect_delete_btn);
        Button clearBtn = (Button) view.findViewById(R.id.infraredselect_clear_btn);
        deleteBtn.setVisibility(View.GONE);
        deviceBtn.setText("空调");
        sceneBtn.setText("电视");
        Display display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        //设置对话框的宽高
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width * 80 / 100,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setContentView(view,layoutParams);
        deviceBtn.setOnClickListener(new View.OnClickListener() {//添加空调
            @Override
            public void onClick(View view) {
                deviceType_tv.setText("空调");
                electricId = "1";
                builder.dismiss();

            }
        });
        sceneBtn.setOnClickListener(new View.OnClickListener() {//添加电视
            @Override
            public void onClick(View view) {
                deviceType_tv.setText("电视");
                electricId = "0";
                builder.dismiss();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {//
            @Override
            public void onClick(View view) {
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

}
