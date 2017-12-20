package neuron.com.scene.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.AirQualityAdapter;
import neuron.com.adapter.SceneBindDeviceListAdapter;
import neuron.com.bean.AirQualityBean;
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
 * Created by ljh on 2017/5/12. 场景可以绑定的设备列表
 */
public class SceneDeviceListActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private String TAG = "SceneDeviceListActivity";
    private ImageView back_iv;
    private Button button;
    private ListView listView;
    private Intent intent;
    private SharedPreferencesManager sharedPreferencesManager;
    private String sceneId,account,token,engineId;
    private String deMethod = "GetElectricList";
    private String electricActionSetMethod = "ElectricActionSet";
    private String bindMethod = "AddControlledDevice";
    private List<DeviceSetFragmentBean> list;
    private SceneBindDeviceListAdapter adapter;
    private AirQualityAdapter dialogAdapter;
    private List<AirQualityBean> dialogList;
    private int mIndex = 500;
    private int mPosition = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicelist);
        init();
    }

    private void init() {
        intent = getIntent();
        sceneId = intent.getStringExtra("sceneId");
        back_iv = (ImageView) findViewById(R.id.devicelist_back_iv);
        button = (Button) findViewById(R.id.devicelist_queding_bnt);
        listView = (ListView) findViewById(R.id.devicelist_listview);
        back_iv.setOnClickListener(this);
        button.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        getDeviceList(sceneId, deMethod);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.devicelist_back_iv://返回
                finish();
                break;
            case R.id.devicelist_queding_bnt://确定
                if (mPosition != 500) {
                    int length = list.size();
                    JSONArray js = new JSONArray();
                    for (int i = 0; i < length; i++) {
                        if (list.get(i).isSelect()) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("controlled_device_id", list.get(i).getNeuronId());
                                jsonObject.put("method", list.get(i).getDeviceStatus());
                                js.put(jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (js.length() > 0) {
                        bindDevice(js.toString(),sceneId,bindMethod);

                    } else {
                        Utils.showDialog(SceneDeviceListActivity.this,"请选择设备");
                    }
                } else {
                    Utils.showDialog(this,"请选择设备");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        deviceData(position);
        mPosition = position;
            if (list.get(position).isSelect()) {
                list.get(position).setSelect(false);
            } else {
                list.get(position).setSelect(true);
            }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }
    /**
     * 设备状态的列表
     */
    private void showDeviceStatusDialog(){
        final Dialog builder = new Dialog(this);
        //去掉dialog标题栏
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.show();
        View view = LayoutInflater.from(this).inflate(R.layout.devicestatuslist,null);

        final ListView dialoglv = (ListView) view.findViewById(R.id.devicestatuslist_lv);
        Display display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        //设置对话框的宽高
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width * 90 / 100,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setContentView(view,layoutParams);
        dialoglv.setAdapter(dialogAdapter);
        dialoglv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                dialogList.get(position).setSelect(true);
                dialogAdapter.setList(dialogList);
                dialogAdapter.notifyDataSetChanged();

                list.get(mPosition).setDeviceStatus(dialogList.get(position).getSceneId());
                adapter.setList(list);
                adapter.notifyDataSetChanged();
                builder.dismiss();
            }
        });
    }
    /**
     * 获取情景可绑定设备列表
     * @param sceneId  场景id
     * @param method  方法名
     */
    private void getDeviceList(String sceneId,String method){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("contextual_model_id", sceneId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deviceResult) {
                    Log.e(TAG + "设备列表", deviceResult);
                    try {
                        JSONObject jsonObject = new JSONObject(deviceResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONArray jsonArray = jsonObject.getJSONArray("msg");
                            int length = jsonArray.length();
                            if (length > 0) {
                                list = new ArrayList<DeviceSetFragmentBean>();
                                DeviceSetFragmentBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject jsonBean = jsonArray.getJSONObject(i);
                                    bean = new DeviceSetFragmentBean();
                                    bean.setSelect(false);
                                    bean.setDeviceName(jsonBean.getString("electric_name"));
                                    bean.setNeuronId(jsonBean.getString("electric_id"));
                                    bean.setDeviceType(jsonBean.getString("electric_type_id"));
                                    bean.setRoomName(jsonBean.getString("room_name"));
                                    bean.setDeviceStatus("00");
                                    list.add(bean);
                                }
                                Log.e(TAG + "list长度", String.valueOf(list.size()));
                                adapter = new SceneBindDeviceListAdapter(SceneDeviceListActivity.this, list);
                                listView.setAdapter(adapter);
                            }
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
    /**
     * 获取可控操作类型
     */
    private void deviceData(int index){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + list.get(index).getDeviceType() + electricActionSetMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("electric_type_id", list.get(index).getDeviceType());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", electricActionSetMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Log.e(TAG + "设备指令列表", result);
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONArray jsonArray = jsonObject.getJSONArray("msg");
                            int length = jsonArray.length();
                            if (length > 0) {
                                dialogList = new ArrayList<AirQualityBean>();
                                AirQualityBean bean1;
                                for (int i = 0; i < length; i++) {
                                    JSONObject jsond = jsonArray.getJSONObject(i);
                                    bean1 = new AirQualityBean();
                                    bean1.setSceneId(jsond.getString("action"));
                                    bean1.setSceneName(jsond.getString("action_desc"));
                                    bean1.setSelect(false);
                                    dialogList.add(bean1);
                                }
                                dialogAdapter = new AirQualityAdapter(dialogList, SceneDeviceListActivity.this);
                                showDeviceStatusDialog();
                            }
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

    /**
     *       情景模式绑定设备
     * @param deviceList 设备列表
     * @param sceneId
     * @param method
     */
    private void bindDevice(String deviceList,String sceneId,String method){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + deviceList + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("contextual_model_id", sceneId);
            xutilsHelper.add("controlled_device_list", deviceList);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String setResult) {
                    Log.e(TAG + "绑定设备",setResult);
                    try {
                        JSONObject jsonObject = new JSONObject(setResult);
                        if (jsonObject.getInt("status") == 9999) {
                            final AlertDialog builder = new AlertDialog.Builder(SceneDeviceListActivity.this).create();
                            View view = View.inflate(SceneDeviceListActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("绑定成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    JSONArray jsonArray = new JSONArray();
                                    for (int i = 0; i < list.size(); i++) {
                                        if (list.get(i).isSelect()) {
                                            try {
                                                JSONObject json = new JSONObject();
                                                json.put("deviceId", list.get(i).getNeuronId());
                                                json.put("deviceName", list.get(i).getDeviceName());
                                                json.put("deviceType", list.get(i).getDeviceType());
                                                json.put("deviceStatus", list.get(i).getDeviceStatus());
                                                json.put("deviceRoom", list.get(i).getRoomName());
                                                jsonArray.put(json);
                                                //DeviceSetFragmentBean bean = new DeviceSetFragmentBean();
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    intent.putExtra("devicelist", jsonArray.toString());
                                    Log.e(TAG + "数据长度", String.valueOf(jsonArray.length()));
                                    setResult(RESULT_OK, intent);
                                    builder.dismiss();
                                    SceneDeviceListActivity.this.finish();
                                }
                            });
                            builder.show();
                        } else {
                            Utils.showDialog(SceneDeviceListActivity.this, jsonObject.getString("error"));
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
    private void setAccount(){
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
    }
}
