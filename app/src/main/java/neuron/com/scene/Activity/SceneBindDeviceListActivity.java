package neuron.com.scene.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

import neuron.com.adapter.SceneBindDeviceListAdapter;
import neuron.com.bean.DeviceSetFragmentBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/12.  场景中已经绑定的设备列表
 */
public class SceneBindDeviceListActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "SceneBindDeviceListActivity";
    private ImageView back_iv;
    private Button button;
    private SwipeMenuListView listView;
    private Intent intent;
    private String deviceLists;
    private List<DeviceSetFragmentBean> list;
    private SceneBindDeviceListAdapter adapter;
    private SharedPreferencesManager sharedPreferencesManager;
    private String sceneId,account,token,engineId;
    private String delBindDevice = "DelControlledDevice";
    private TextView warning_tv,title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sceneswichlist);
        init();
        seeListener();
    }

    private void init() {
        intent = getIntent();
        deviceLists = intent.getStringExtra("deviceList");
        sceneId = intent.getStringExtra("sceneId");
        back_iv = (ImageView) findViewById(R.id.sceneswichlist_back_iv);
        button = (Button) findViewById(R.id.sceneswichlist_queding_bnt);
        title = (TextView) findViewById(R.id.sceneswichlist_title_tv);
        title.setText("已绑定设备列表");
        warning_tv = (TextView) findViewById(R.id.sceneswichlist_tishi_tv);
        listView = (SwipeMenuListView) findViewById(R.id.sceneswichlist_listview);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(SceneBindDeviceListActivity.this);
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
                switch(index){
                    case 0:
                        delBindDevice(sceneId,position,delBindDevice);
                        list.remove(position);
                        adapter.setList(list);
                        break;
                    default:
                        break;
                }
            }
        });
        devicelist(deviceLists);
    }
    private void seeListener(){
        back_iv.setOnClickListener(this);
        button.setOnClickListener(this);
    }

    private int length;
    /**
     *  场景已有的设备列表
     * @param deviceLists  从SceneEditActivity 场景编辑页面 传过来的已有设备列表
     */
    private void devicelist(String deviceLists){
        Log.e(TAG + "解析已经绑定设备列表", deviceLists);
        try {
            JSONArray jsonArray = new JSONArray(deviceLists);
            length = jsonArray.length();
            if (length > 0) {
                list = new ArrayList<DeviceSetFragmentBean>();
                DeviceSetFragmentBean bean;
                for (int i = 0; i < length; i++) {
                    bean = new DeviceSetFragmentBean();
                    JSONObject j = jsonArray.getJSONObject(i);
                    bean.setDeviceName(j.getString("controlled_device_name"));
                    bean.setNeuronId(j.getString("controlled_device_id"));
                    bean.setDeviceType(j.getString("electric_type_id"));
                    if (j.getString("method").equals("开启") || j.getString("method").equals("布防")) {
                        bean.setDeviceStatus("01");
                    } else if (j.getString("method").equals("关闭")) {
                        bean.setDeviceStatus("00");
                    }else if (j.getString("method").equals("暂停")|| j.getString("method").equals("触发")) {
                        bean.setDeviceStatus("02");
                    }

                    bean.setRoomName(j.getString("room_name"));
                    bean.setSelect(false);
                    list.add(bean);
                }
                adapter = new SceneBindDeviceListAdapter(this, list);
                listView.setAdapter(adapter);
            } else {
                warning_tv.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
    private void setAccount(){
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
    }

    /**
     *   删除场景绑定设备
     * @param sceneId   场景id
     * @param position  list下标
     * @param method   方法名
     */
    private void delBindDevice(String sceneId,int position,String method){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("controlled_device_id", list.get(position).getNeuronId());
            jsonObject.put("method", list.get(position).getDeviceStatus());
            jsonArray.put(jsonObject);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + jsonArray.toString() + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("contextual_model_id", sceneId);
            xutils.add("controlled_device_list", jsonArray.toString());
            xutils.add("token", token);
            xutils.add("method", method);
            xutils.add("sign", sign);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    try {
                        Log.e(TAG + "删除场景所绑定的设备", deleteResult);
                        JSONObject json = new JSONObject(deleteResult);
                        if (json.getInt("status") == 9999) {
                            Utils.showDialog(SceneBindDeviceListActivity.this,"删除成功");
                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showDialog(SceneBindDeviceListActivity.this, json.getString("error"));
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
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sceneswichlist_back_iv://返回
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.sceneswichlist_queding_bnt://添加
                Intent intent1 = new Intent(SceneBindDeviceListActivity.this, SceneDeviceListActivity.class);
                intent1.putExtra("sceneId", sceneId);
                startActivityForResult(intent1, 10);
                break;
            default:
                break;
        }

    }

    private int listLength;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                if (data != null) {
                    warning_tv.setVisibility(View.GONE);
                    String deviceList = data.getStringExtra("devicelist");
                    Log.e(TAG + "返回数据长度", deviceList);
                    if (list != null) {
                        try {
                            JSONArray jsonArray = new JSONArray(deviceList);
                            DeviceSetFragmentBean bean;
                            listLength = jsonArray.length();
                            for (int i = 0; i < listLength; i++) {
                                JSONObject j = jsonArray.getJSONObject(i);
                                bean = new DeviceSetFragmentBean();
                                bean.setDeviceName(j.getString("deviceName"));
                                bean.setNeuronId(j.getString("deviceId"));
                                bean.setDeviceType(j.getString("deviceType"));
                                bean.setDeviceStatus(j.getString("deviceStatus"));
                                bean.setRoomName(j.getString("deviceRoom"));
                                bean.setSelect(false);
                                list.add(bean);
                            }
                            adapter.setList(list);
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(deviceList);
                            list = new ArrayList<DeviceSetFragmentBean>();
                            DeviceSetFragmentBean bean;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject j = jsonArray.getJSONObject(i);
                                bean = new DeviceSetFragmentBean();
                                bean.setDeviceName(j.getString("deviceName"));
                                bean.setNeuronId(j.getString("deviceId"));
                                bean.setDeviceType(j.getString("deviceType"));
                                bean.setDeviceStatus(j.getString("deviceStatus"));
                                bean.setRoomName(j.getString("deviceRoom"));
                                bean.setSelect(false);
                                list.add(bean);
                            }
                            adapter = new SceneBindDeviceListAdapter(this, list);
                            listView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }
}
