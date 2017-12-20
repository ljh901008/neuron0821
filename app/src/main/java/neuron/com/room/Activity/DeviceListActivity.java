package neuron.com.room.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.AirQualityAdapter;
import neuron.com.bean.AirQualityBean;
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
 * Created by ljh on 2017/5/2. 红外人体感应,智能开关  可绑定设备列表
 */
public class DeviceListActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private String TAG = "DeviceListActivity";
    private ImageView back_iv;
    private Button button;
    private ListView listView;
    private Intent intent;
    private SharedPreferencesManager sharedPreferencesManager;
    private String neuronId,account,token,engineId;
    private String deviceType;
    private String method = "GetControlledList";
    private String electricActionSetMethod = "ElectricActionSet";
    private String bindMethod = "BindControlledDevice";
    private List<AirQualityBean> list;
    private AirQualityAdapter adapter;
    private AirQualityAdapter dialogAdapter;
    private List<AirQualityBean> dialogList;
    //有人状态和无人状态的标识 0有人 1无人
    private int condition;
    private int index;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicelist);
        init();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getDeviceList();
    }
    private void init(){
        intent = getIntent();
        deviceType = intent.getStringExtra("deviceType");
        condition = intent.getIntExtra("tag", 10);
        neuronId = intent.getStringExtra("neuronId");
        back_iv = (ImageView) findViewById(R.id.devicelist_back_iv);
        button = (Button) findViewById(R.id.devicelist_queding_bnt);
        listView = (ListView) findViewById(R.id.devicelist_listview);
        back_iv.setOnClickListener(this);
        button.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.devicelist_back_iv://返回
                finish();
                break;
            case R.id.devicelist_queding_bnt://确定
                intent.putExtra("deviceName", list.get(index).getSceneName());
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 获取设备列表
     */
    private void getDeviceList(){
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
            String sign = MD5Utils.MD5Encode(aesAccount + deviceType + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device_type_id", deviceType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(1,this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deviceResult) {
                    try {
                        JSONObject jsonObject = new JSONObject(deviceResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONArray jsonArray = jsonObject.getJSONArray("msg");
                            int length = jsonArray.length();
                            if (length > 0) {
                                list = new ArrayList<AirQualityBean>();
                                AirQualityBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject jsonBean = jsonArray.getJSONObject(i);
                                    bean = new AirQualityBean();
                                    bean.setSelect(false);
                                    bean.setSceneName(jsonBean.getString("controlled_device_name"));
                                    bean.setSceneId(jsonBean.getString("controlled_device_id"));
                                    bean.setDeviceType(jsonBean.getString("electric_type_id"));
                                    list.add(bean);
                                }
                                adapter = new AirQualityAdapter(list, DeviceListActivity.this);
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
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        for (int i = 0; i < list.size(); i++) {
            if (i == position) {
                list.get(position).setSelect(true);
            } else {
                list.get(i).setSelect(false);
            }
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
        deviceData(position);
        index = position;
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
           // xutilsHelper.sendPost(2,this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
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
                                dialogAdapter = new AirQualityAdapter(dialogList, DeviceListActivity.this);
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
     * 设备状态的列表
     */
    private void showDeviceStatusDialog(){
        final Dialog builder = new Dialog(this);
        //去掉dialog标题栏
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.show();
        View view = LayoutInflater.from(this).inflate(R.layout.devicestatuslist,null);

        ListView dialoglv = (ListView) view.findViewById(R.id.devicestatuslist_lv);
        Display display = this.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        //设置对话框的宽高
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width * 90 / 100,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        builder.setContentView(view,layoutParams);
        dialoglv.setAdapter(dialogAdapter);
        dialoglv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bindDevice(String.valueOf(condition), i);
                builder.dismiss();
            }
        });
    }

    /**
     *  绑定设备
     * @param condition  0 有人状态 1无人状态，0智能开关A键 1B键2C键
     * @param position
     */
    private void bindDevice(String condition,int position){
        Utils.showWaitDialog("等待中", this, mWaitDialog);
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
            JSONObject bindDevice = new JSONObject();
            bindDevice.put("controlled_device_site", condition);
            bindDevice.put("controlled_device_name", list.get(index).getSceneName());
            bindDevice.put("room_id", "");
            bindDevice.put("controlled_device_brand", "");
            bindDevice.put("controlled_device_serial", "");
            bindDevice.put("controlled_device_id", list.get(index).getSceneId());
            bindDevice.put("electric_type_id", list.get(index).getDeviceType());
            bindDevice.put("status", dialogList.get(position).getSceneId());
            bindDevice.put("serial_number", "");
            Log.e(TAG + "绑定设备的数据源", aesAccount + bindDevice.toString() + engineId
                    + bindMethod + neuronId + token + "1");
            String sign = MD5Utils.MD5Encode(aesAccount + bindDevice.toString() + engineId
                    + bindMethod + neuronId + token + "1" + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("type", "1");
            xutilsHelper.add("electric_device", bindDevice.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", bindMethod);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(3,this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String setResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "绑定设备",setResult);
                    try {
                        JSONObject jsonObject = new JSONObject(setResult);
                        if (jsonObject.getInt("status") == 9999) {
                            Toast.makeText(DeviceListActivity.this, "绑定成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Utils.showDialog(DeviceListActivity.this, jsonObject.getString("error"));
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
