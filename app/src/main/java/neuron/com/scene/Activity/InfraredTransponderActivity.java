package neuron.com.scene.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.InfraredTransponderAdapter;
import neuron.com.bean.DeviceSetFragmentBean;
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
 * Created by ljh on 2017/5/4. 红外转发器
 */
public class InfraredTransponderActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private String TAG = "InfraredTransponderActivity";
    private EditText deviceName_ed;
    private RelativeLayout room_rll;
    private TextView room_tv;
    private ImageButton back_ibtn, addDevice_ibtn;
    private Button confirm_btn;
    private SwipeMenuListView listView;
    private Intent intent;
    private String neuronId, deviceType, deviceName, roomName, roomId;
    private String account, token, engineId;
    private SharedPreferencesManager sharedPreferencesManager;
    private String swichDataMethod = "QueryNeuronDetail";
    private String methodDeleteScene = "DelNeuronSetting";
    private String updateMethod = "UpdateDevices";
    private List<DeviceSetFragmentBean> list;
    private InfraredTransponderAdapter adapter;
    private boolean isPush = true;
    private WaitDialog mWaitDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.infraredtransponder);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
        setListener();
    }

    private void init() {
        intent = getIntent();
        neuronId = intent.getStringExtra("neuronId");
        deviceType = intent.getStringExtra("deviceType");
        deviceName_ed = (EditText) findViewById(R.id.infraredtransponder_devicename_ed);
        room_rll = (RelativeLayout) findViewById(R.id.infraredtransponder_roomname_rll);
        back_ibtn = (ImageButton) findViewById(R.id.infraredtransponder_back_iv);
        confirm_btn = (Button) findViewById(R.id.infraredtransponder_fonfirm_btn);
        listView = (SwipeMenuListView) findViewById(R.id.infraredtransponder_listview);
        addDevice_ibtn = (ImageButton) findViewById(R.id.infraredtransponder_add_itbn);
        room_tv = (TextView) findViewById(R.id.infraredtransponder_roomname_ed);
        deviceName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(InfraredTransponderActivity.this);
                // 设置菜单的背景
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                // 宽度：菜单的宽度是一定要有的，否则不会显示
                deleteItem.setWidth(dp2px(80));
                // 菜单标题
                deleteItem.setTitle("删除");
                // 标题文字大小
                deleteItem.setTitleSize(16);
                // 标题的颜色
                deleteItem.setTitleColor(Color.WHITE);
                // 添加到menu
                menu.addMenuItem(deleteItem);
            }
        };
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        delAirTV(list.get(position).getSite());
                        list.remove(position);
                        adapter.setList(list);
                        break;
                    default:
                        break;
                }
            }
        });
        infraredData();
    }

    private void setListener() {
        room_rll.setOnClickListener(this);
        back_ibtn.setOnClickListener(this);
        confirm_btn.setOnClickListener(this);
        addDevice_ibtn.setOnClickListener(this);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.infraredtransponder_roomname_rll://房间
                Intent intent1 = new Intent(InfraredTransponderActivity.this, RoomListActivity.class);
                startActivityForResult(intent1, 10);
                break;
            case R.id.infraredtransponder_back_iv://返回
                finish();
                break;
            case R.id.infraredtransponder_fonfirm_btn://确定
                deviceName = deviceName_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(deviceName)) {
                    updateDevice(deviceName, neuronId, roomId);
                }
                break;
            case R.id.infraredtransponder_add_itbn://添加
                Intent intent2 = new Intent(InfraredTransponderActivity.this, AddAirTVActivity.class);
                intent2.putExtra("neuronId", neuronId);
                intent2.putExtra("deviceType", deviceType);
                startActivityForResult(intent2,11);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 10://选择房间
                    if (data != null) {
                        roomId = data.getStringExtra("roomId");
                        roomName = data.getStringExtra("roomName");
                        Log.e(TAG + "房间名称", roomName);
                        room_tv.setText(roomName);
                    }
                    break;
                case 11:
                    if (data != null) {
                        if (data.getIntExtra("type", 10) == 1) {
                            isPush = false;
                            infraredData();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 获取红外转发器详情
     */
    private void infraredData() {
        Utils.showWaitDialog("加载中...", this,mWaitDialog);
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
                public void onSuccess(String infraredResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "红外转发器详情", infraredResult);
                    try {
                        JSONObject jsonObject = new JSONObject(infraredResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                            neuronId = jsonMsg.getString("neuron_id");
                            deviceName = jsonMsg.getString("neuron_name");
                            deviceName_ed.setText(deviceName);
                            roomId = jsonMsg.getString("room_id");
                            roomName = jsonMsg.getString("room_name");
                            room_tv.setText(roomName);
                            deviceType = jsonMsg.getString("device_type_id");
                            JSONArray conJsa = jsonMsg.getJSONArray("controlled_device_list");
                            int length = conJsa.length();
                            if (length > 0) {
                                list = new ArrayList<DeviceSetFragmentBean>();
                                DeviceSetFragmentBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject json = conJsa.getJSONObject(i);
                                    bean = new DeviceSetFragmentBean();
                                    bean.setDeviceName(json.getString("controlled_device_name"));
                                    bean.setNeuronId(json.getString("controlled_device_id"));
                                    bean.setSite(json.getString("controlled_device_site"));
                                    list.add(bean);
                                }
                                Log.e(TAG + "遥控器数据长度", String.valueOf(list.size()));
                                if (adapter == null) {
                                    adapter = new InfraredTransponderAdapter(InfraredTransponderActivity.this, list);
                                    listView.setAdapter(adapter);
                                } else {
                                    adapter.setList(list);
                                    adapter.notifyDataSetChanged();
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
                    Toast.makeText(InfraredTransponderActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
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
     * 删除 电视空调
     * @param devicesite  位置
     */
    private void delAirTV(String devicesite){
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
            Utils.showWaitDialog("加载中...",InfraredTransponderActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            Log.e(TAG + "删除电视/空调", aesAccount + "," + devicesite + "," + engineId + "," + methodDeleteScene + "," + neuronId + "," + token);
            String sign = MD5Utils.MD5Encode(aesAccount + devicesite +engineId + methodDeleteScene + neuronId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("condition", devicesite);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodDeleteScene);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    try {
                        Log.e(TAG + "删除电视空调", deleteResult);
                        Utils.dismissWaitDialog(mWaitDialog);
                        JSONObject json = new JSONObject(deleteResult);
                        if (json.getInt("status") == 9999) {
                            final AlertDialog builder = new AlertDialog.Builder(InfraredTransponderActivity.this).create();
                            View view = View.inflate(InfraredTransponderActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("删除成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    adapter.notifyDataSetChanged();
                                    builder.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Utils.showDialog(InfraredTransponderActivity.this, json.getString("error"));
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
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
    /**
     *   更新节点设备名称 房间
     * @param deviceName  设备名称
     * @param neuronId     设备id
     * @param roomId    房间id
     */
    private void updateDevice(String deviceName,String neuronId,String roomId){
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(InfraredTransponderActivity.this);
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
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", updateMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    try {
                        Log.e(TAG + "修改红外转发名称和房间", deleteResult);
                        JSONObject json = new JSONObject(deleteResult);
                        if (json.getInt("status") == 9999) {
                            Utils.showDialog(InfraredTransponderActivity.this,"修改成功");
                        } else {
                            Utils.showDialog(InfraredTransponderActivity.this, json.getString("error"));
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
