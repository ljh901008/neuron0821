package neuron.com.room.Activity;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.exception.BaseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.SY_PopuWAdapter;
import neuron.com.app.OgeApplication;
import neuron.com.bean.SY_PopuWBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/10/18. 添加设备页面
 */
public class AddDeviceActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AddDeviceActivity";
    @ViewInject(value = R.id.adddevice_back_iv)
    private ImageView back_iv;
    @ViewInject(value = R.id.adddevice_devicephoto_iv)
    private ImageView devicePhoto_iv;
    @ViewInject(value = R.id.adddevice_selectroom_rll)
    private RelativeLayout selectRoom_rll;
    @ViewInject(value = R.id.adddevice_devicename_rll)
    private RelativeLayout deviceName_rll;
    @ViewInject(value = R.id.adddevice_rommname_tv)
    private TextView roomName_tv;
    @ViewInject(value = R.id.adddevice_devicename_tv)
    private TextView deviceName_tv;
    @ViewInject(value = R.id.adddevice_add_btn)
    private Button adddevice_btn;
    @ViewInject(value = R.id.adddevice_error_tv)
    private TextView addError_tv;
    private Intent intent;

    private String serial, houseId,deviceName,deviceTypeId;

    private String account,token;
    private String CHECKDEVICE_METHOD = "CheckDevice";//检验设备可用性的方法名
    private String ADDDEVICE_METHOD = "AddDevices";

    private List<SY_PopuWBean> listSY;
    private SY_PopuWAdapter sy_popuWAdapter;
    private String QUERYHOMELIST = "QueryRoomList";//获取房间列表的方法名
    private String addCameraMethod = "BindControlledDevice";
    private SharedPreferencesManager sharedPreferencesManager;
    private String roomId = "";
    private String roomName = null;
    private String engineId;
    //添加摄像头的标记　　1添加摄像头
    private int tag;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.arg1){
                case 1://校验设备可用性
                    if (msg.what == 102) {
                        try {
                            String checkResult = (String) msg.obj;
                            Log.e(TAG + "设备可用性", checkResult);
                            JSONObject jsonObject = new JSONObject(checkResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                                serial = jsonMsg.getString("serial_number");
                                deviceName = jsonMsg.getString("device_type_name");
                                deviceTypeId = jsonMsg.getString("device_type_id");
                                deviceName_tv.setText(deviceName);
                                switch(deviceTypeId){
                                    case "257":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "258":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "259":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "260":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "261":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "262":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "263":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "264":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "265":
                                        devicePhoto_iv.setImageResource(R.mipmap.intelligent_switch);
                                        break;
                                    case "769":
                                        devicePhoto_iv.setImageResource(R.mipmap.infrared_sensor);
                                        break;
                                    case "770":
                                        devicePhoto_iv.setImageResource(R.mipmap.smart_menci);
                                        break;
                                    case "1025":
                                        devicePhoto_iv.setImageResource(R.mipmap.infrared_transponder);
                                        break;
                                    case "1026":
                                        devicePhoto_iv.setImageResource(R.mipmap.air_curtain);
                                        break;
                                    case "1281":
                                        devicePhoto_iv.setImageResource(R.mipmap.air_quality_detector);
                                        break;
                                    case "1537":
                                        devicePhoto_iv.setImageResource(R.mipmap.smart_socket);
                                        break;
                                    case "2305":
                                        devicePhoto_iv.setImageResource(R.mipmap.control_the_host);
                                        break;
                                    case "4097":
                                        devicePhoto_iv.setImageResource(R.mipmap.water_purifier);
                                        break;
                                    default:
                                    break;
                                }
                            } else {
                                //Toast.makeText(AddDeviceActivity.this, jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                                Log.e(TAG + "设备可用性", (String) msg.obj);
                               // Utils.showDialog(AddDeviceActivity.this, jsonObject.getString("error"));
                                devicePhoto_iv.setImageResource(R.mipmap.equipment_wrong);
                                selectRoom_rll.setVisibility(View.GONE);
                                deviceName_rll.setVisibility(View.GONE);
                                adddevice_btn.setVisibility(View.GONE);
                                addError_tv.setVisibility(View.VISIBLE);
                                addError_tv.setText(jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(AddDeviceActivity.this, (String) msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
                case 2://房间列表
                    if (msg.what == 102) {
                        String homeListResult = (String) msg.obj;
                        Log.e(TAG, homeListResult);
                        try {
                            JSONObject homeListJson = new JSONObject(homeListResult);
                            if (homeListJson.getInt("status") == 9999) {
                                JSONArray json = homeListJson.getJSONArray("room_list");
                                int length = json.length();
                                if (length > 0) {
                                    listSY = new ArrayList<SY_PopuWBean>();
                                    SY_PopuWBean sy_popuWBean;
                                    for (int i = 0; i < length; i++) {
                                        sy_popuWBean = new SY_PopuWBean();
                                        JSONObject jsonObject = json.getJSONObject(i);
                                        sy_popuWBean.setHomeId(jsonObject.getString("room_id"));
                                        sy_popuWBean.setHomeName(jsonObject.getString("room_name"));
                                        listSY.add(sy_popuWBean);
                                    }
                                    sy_popuWAdapter = new SY_PopuWAdapter(AddDeviceActivity.this, listSY);
                                    //popuW_lv.setAdapter(sy_popuWAdapter);
                                    showRoomListDialog();
                                    Log.e(TAG + "listsize", String.valueOf(listSY.size()));
                                }
                            } else {
                                Utils.showDialog(AddDeviceActivity.this, homeListJson.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 3:
                    if (msg.what == 102) {
                        try {
                            String homeListResult = (String) msg.obj;
                            Log.e(TAG+"3333", homeListResult);
                            JSONObject jsonObject = new JSONObject(homeListResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONArray jsonArray = jsonObject.getJSONArray("roomlist");
                                int length = jsonArray.length();
                                if (length > 0) {
                                    listSY = new ArrayList<SY_PopuWBean>();
                                    SY_PopuWBean sy_popuWBean;
                                    for (int i = 0; i < length; i++) {
                                        sy_popuWBean = new SY_PopuWBean();
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        sy_popuWBean.setHomeName(json.getString("roomname"));
                                        sy_popuWBean.setHomeId(json.getString("roomid"));
                                        listSY.add(sy_popuWBean);
                                    }

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case 4://添加设备
                    if (msg.what == 102) {
                        try {
                            String addDeviceResult = (String) msg.obj;
                            Log.e(TAG + "添加设备", addDeviceResult);
                            JSONObject addJson = new JSONObject(addDeviceResult);
                            int status = addJson.getInt("status");
                            if (status == 9999) {
                                if ("2305".equals(deviceTypeId)) {//添加成功保存控制主机
                                    sharedPreferencesManager.save("engine_id", serial);
                                }

                                final AlertDialog builder = new AlertDialog.Builder(AddDeviceActivity.this).create();
                                View view = View.inflate(AddDeviceActivity.this, R.layout.dialog_textview, null);
                                TextView title = (TextView) view.findViewById(R.id.textView1);
                                Button button = (Button) view.findViewById(R.id.button1);
                                title.setText("添加成功");
                                builder.setView(view);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                        builder.dismiss();
                                    }
                                });
                                builder.show();
                            } else if (status == 3005) {//没有控制主机
                                Utils.showDialog(AddDeviceActivity.this, addJson.getString("error"));
                            }else if (status == 3001) {//设备已经被添加
                                Utils.showDialog(AddDeviceActivity.this, addJson.getString("error"));
                            } else if (status==3002) {//删除之后2分钟内不能添加
                                Utils.showDialog(AddDeviceActivity.this, addJson.getString("error"));
                            } else if (status==3003) {//无权限
                                Utils.showDialog(AddDeviceActivity.this, addJson.getString("error"));
                            }else if (status == 2000) {//设备已经被添加
                                Utils.showDialog(AddDeviceActivity.this, addJson.getString("error"));
                            } else {
                                if (tag == 1) {
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            try {
                                                Message msg = handler.obtainMessage();
                                                msg.obj = OgeApplication.getOpenSDK().deleteDevice(serial);
                                                msg.arg1 = 6;
                                                msg.what = 102;
                                                handler.sendMessage(msg);
                                            } catch (BaseException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.start();

                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 5://添加摄像头
                    if (msg.what == 102) {
                        try {
                            String addCameraResult = (String) msg.obj;
                            Log.e(TAG + "添加摄像头", addCameraResult);
                            JSONObject addJson = new JSONObject(addCameraResult);
                            int status = addJson.getInt("status");
                            if (status == 9999) {
                                Utils.showDialog(AddDeviceActivity.this, "添加成功");
                                Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else if (status == 3005) {//没有控制主机
                                Utils.showDialog(AddDeviceActivity.this, addJson.getString("error"));
                            }else if (status == 3002) {//设备已经被添加
                                Utils.showDialog(AddDeviceActivity.this, addJson.getString("error"));
                            } else {
                                Utils.showDialog(AddDeviceActivity.this, "添加失败");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 6://删除摄像头
                    if (msg.what == 102) {
                        boolean b = (boolean) msg.obj;
                        if (b) {
                            Utils.showDialog(AddDeviceActivity.this, "添加失败,请点击背面的reset键重新添加");
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
        setContentView(R.layout.adddevice);
        x.view().inject(this);
        init();
        setListener();

    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        selectRoom_rll.setOnClickListener(this);
        deviceName_rll.setOnClickListener(this);
        adddevice_btn.setOnClickListener(this);
    }
    private void init(){
        intent=getIntent();
        serial = intent.getStringExtra("serial");
        tag = intent.getIntExtra("type", 5);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(AddDeviceActivity.this);
        roomName_tv.setText("请选择房间");

        if (tag == 1) {//添加摄像头
            deviceName_tv.setText("摄像头");
        } else if (tag == 3) {//添加音响
            devicePhoto_iv.setImageResource(R.mipmap.intelligent_sound);
            deviceName_tv.setText("音响");
        } else {//添加设备
            devicePhoto_iv.setImageResource(R.mipmap.camera);
            checkDevice();
        }
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
        case R.id.adddevice_back_iv://返回键
            if (tag == 1) {
                Intent intent = new Intent(AddDeviceActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                finish();
            }
            break;
        case R.id.adddevice_selectroom_rll://房间名
            //showPopuWindow();
            //getHomeList();
            Intent intent1 = new Intent(AddDeviceActivity.this, RoomListActivity.class);
            startActivityForResult(intent1, 10);
            break;
        case R.id.adddevice_devicename_rll://设备名称
            showDialog();
            break;
        case R.id.adddevice_add_btn://添加按钮
            addDevice(roomId);
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
                    Log.e(TAG + "房间", roomId+roomName);
                }
            }
        }
    }

    private void showDialog(){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.dialog_input, null));
        dialog.show();
        dialog.getWindow().setContentView(R.layout.dialog_input);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_add);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_cancel);
        final EditText etContent = (EditText) dialog.findViewById(R.id.et_content);
        etContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        TextView titleName = (TextView) dialog.findViewById(R.id.name_tv);
        titleName.setText("请输入设备名称");
        btnPositive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String str = etContent.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    etContent.setError("输入内如不能为空");
                } else {
                    deviceName_tv.setText(str);
                    dialog.dismiss();
                }
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }

    private ListView dialogLv;
    private void showRoomListDialog(){
        final Dialog builder = new Dialog(this);
        //去掉dialog标题栏
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.show();
        View view = LayoutInflater.from(this).inflate(R.layout.adddevice_dialog_listview, null);
        FrameLayout fl = (FrameLayout) view.findViewById(R.id.adddevice_dialog_listview_add_fl);
        Display display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        //设置对话框的宽高
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width * 90 / 100,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setContentView(view, layoutParams);
        dialogLv = (ListView) view.findViewById(R.id.adddevice_dialog_listview_lv);
       // sy_popuWAdapter = new SY_PopuWAdapter(AddDeviceActivity.this, listSY);
     //   Log.e(TAG + "length", String.valueOf( .size()));
        dialogLv.setAdapter(sy_popuWAdapter);
        dialogLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                roomName = listSY.get(position).getHomeName();
                roomName_tv.setText(roomName);
                roomId = listSY.get(position).getHomeId();
                builder.dismiss();
            }
        });
        fl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddDeviceActivity.this, AddRoomActivity.class);
                intent.putExtra("type", 2);
                startActivity(intent);
            }
        });

    }
    /**
     * 获取房间列表
     */
    private void getHomeList(){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
            Log.e(TAG + "token", token);
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engineId = sharedPreferencesManager.get("engine_id");
            Log.e(TAG + "engine_id", engineId);
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + QUERYHOMELIST + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET, handler);
            xutilsHelper.add("account",aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("token",token);
            xutilsHelper.add("method",QUERYHOMELIST);
            xutilsHelper.add("sign",sign);
            xutilsHelper.sendPost(2,this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void checkDevice(){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + CHECKDEVICE_METHOD + serial + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("serial_number", serial);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", CHECKDEVICE_METHOD);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1,AddDeviceActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void addDevice(String roomId){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
            Log.e(TAG + "token", token);
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engineId = sharedPreferencesManager.get("engine_id");
            Log.e(TAG + "engine_id", engineId);
        }
        try {
            deviceName = deviceName_tv.getText().toString().trim();
            Log.e(TAG + "设备名称", deviceName);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonDeviceList = new JSONObject();
            jsonDeviceList.put("serial_number", serial);
            jsonDeviceList.put("device_name", deviceName);
            if (tag == 1) {
                jsonDeviceList.put("device_type_id", "2049");
            } else if (tag == 3) {//音响
                jsonDeviceList.put("device_type_id", "8193");
            } else {
                jsonDeviceList.put("device_type_id", deviceTypeId);

            }
            jsonDeviceList.put("room_id", roomId);
            String sign;
            if ("2305".equals(deviceTypeId)) {//2305代表控制主机类型
                sign = MD5Utils.MD5Encode(aesAccount + jsonDeviceList.toString() + ADDDEVICE_METHOD + token + URLUtils.MD5_SIGN, "");
            } else {
                sign = MD5Utils.MD5Encode(aesAccount + jsonDeviceList.toString() + engineId + ADDDEVICE_METHOD + token + URLUtils.MD5_SIGN, "");
            }
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", "2305".equals(deviceTypeId) ? "" : engineId);
            xutilsHelper.add("device", jsonDeviceList.toString());
            Log.e(TAG + "devicelist", jsonDeviceList.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", ADDDEVICE_METHOD);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(4, AddDeviceActivity.this);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加摄像头
     */
    private void addCamera(){
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
            jsonObject.put("controlled_device_site", "0");
            deviceName = deviceName_tv.getText().toString().trim();
            if (!TextUtils.isEmpty(deviceName)) {
                jsonObject.put("controlled_device_name", deviceName);
            } else {
                Utils.showDialog(AddDeviceActivity.this,"请输入设备名称");
            }
            jsonObject.put("room_id", TextUtils.isEmpty(roomId) ? "" : roomId);
            jsonObject.put("controlled_device_brand", "");
            jsonObject.put("controlled_device_serial", "");
            jsonObject.put("controlled_device_id", "");
            jsonObject.put("electric_type_id", "33010");
            jsonObject.put("status", "");
            jsonObject.put("serial_number", serial);
            String sign = MD5Utils.MD5Encode(aesAccount + jsonObject.toString() +
                    engineId + addCameraMethod + "550000000000" + token + "0" + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", "550000000000");
            xutilsHelper.add("type", "0");
            xutilsHelper.add("electric_device", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", addCameraMethod);
            xutilsHelper.add("sign", sign);
            Log.e(TAG + "数据流", aesAccount + engineId + jsonObject.toString() + token + addCameraMethod + sign);
            xutilsHelper.sendPost(5, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
