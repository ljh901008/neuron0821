package neuron.com.set.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
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

import neuron.com.adapter.HostManageAdapter;
import neuron.com.bean.HostManagerItemBean;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/3/7. 控制主机列表页面
 */
public class HostManagerActivity extends Activity implements AdapterView.OnItemClickListener,View.OnClickListener{
    private String TAG = "HostManagerActivity";
    private ImageView back_iv;
    private Button titleRight_btn;
    private TextView titleName_tv;
    private SwipeMenuListView hostList_lv;
    private String method = "GetEngineList";
    private String changeHostMethod = "ChangeAccountEngine";
    private String deleteHostMethod = "DelDevices";
    private HostManageAdapter adapter;
    private List<HostManagerItemBean> list;
    //是否处于切换状态的标记 1.切换状态  0.不是切换状态
    private int editTag = 0;
    private int indexId;
    private int index = 100;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private TextView tishi;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostmanager);
        init();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getHostManagerList();

    }
    private void init() {
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        titleName_tv = (TextView) findViewById(R.id.hostmanager_room_name_tv);
        back_iv = (ImageView) findViewById(R.id.hostmanager_back_iv);
        titleRight_btn = (Button) findViewById(R.id.hostmanager_finish_btn);
        hostList_lv = (SwipeMenuListView) findViewById(R.id.hostmanager_listview_lv);
        tishi = (TextView) findViewById(R.id.hostmanager_tishi_tv);
        hostList_lv.setVisibility(View.VISIBLE);
        back_iv.setOnClickListener(this);
        titleRight_btn.setOnClickListener(this);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(HostManagerActivity.this);
                // 设置菜单的背景
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,0x3F, 0x25)));
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
        hostList_lv.setMenuCreator(creator);
        hostList_lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        Log.e(TAG + "左滑删除", "");
                        deleteHostManager(position);
                        list.remove(position);
                        break;
                    default:
                        break;
                }
            }
        });
        hostList_lv.setOnItemClickListener(this);
    }

    /**
     * 获取控制主机列表
     */
    private void getHostManagerList() {
            String account = null, token = null;
            if (sharedPreferencesManager.has("account")) {
                account = sharedPreferencesManager.get("account");
            }
            if (sharedPreferencesManager.has("token")) {
                token = sharedPreferencesManager.get("token");
            }
            try {
                Utils.showWaitDialog(getString(R.string.loadtext_load),HostManagerActivity.this,mWaitDialog);
                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                String sign = MD5Utils.MD5Encode(aesAccount + method + token + URLUtils.MD5_SIGN, "");
                XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
                xutilsHelper.add("account", aesAccount);
                xutilsHelper.add("token", token);
                xutilsHelper.add("method", method);
                xutilsHelper.add("sign", sign);
                xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String hostManagerListResult) {
                        Utils.dismissWaitDialog(mWaitDialog);
                        Log.e(TAG + "控制主机列表",hostManagerListResult);
                        try {
                            JSONObject jsonObject = new JSONObject(hostManagerListResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONArray jsonArray = jsonObject.getJSONArray("engine_list");
                                int length = jsonArray.length();
                                if (length > 0) {
                                    list = new ArrayList<HostManagerItemBean>();
                                    HostManagerItemBean bean;
                                    for (int i = 0; i < length; i++) {
                                        bean = new HostManagerItemBean();
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        bean.setHostManagerName(json.getString("engine_name"));
                                        String engineId = json.getString("engine_id");
                                        bean.setEngineId(engineId);
                                        bean.setHostSerialNumber(json.getString("serial_number"));
                                        int d = json.getInt("_default");
                                        if (d == 0) {
                                            sharedPreferencesManager.save("engine_id", engineId);
                                        }
                                        bean.setHostState(d);
                                        list.add(bean);
                                    }
                                    Log.e(TAG + "列表长度", String.valueOf(list.size()));
                                    adapter = new HostManageAdapter(list, HostManagerActivity.this);
                                    hostList_lv.setAdapter(adapter);
                                } else {
                                    tishi.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Utils.dismissWaitDialog(mWaitDialog);
                                Log.e(TAG + "error", jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable, boolean b) {
                        Utils.dismissWaitDialog(mWaitDialog);
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
     * 切换控制主机
     */
    private void changeHostManager(int index){
            String account = null, token = null;
            if (sharedPreferencesManager.has("account")) {
                account = sharedPreferencesManager.get("account");
            }
            if (sharedPreferencesManager.has("token")) {
                token = sharedPreferencesManager.get("token");
            }
            try {
                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                String engineId = list.get(index).getEngineId();
                Log.e(TAG + "切换主机Id", engineId);
                String sign = MD5Utils.MD5Encode(aesAccount + engineId + changeHostMethod + token + URLUtils.MD5_SIGN, "");
                XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
                xutilsHelper.add("account", aesAccount);
                xutilsHelper.add("engine_id", engineId);
                xutilsHelper.add("token", token);
                xutilsHelper.add("method", changeHostMethod);
                xutilsHelper.add("sign", sign);
                xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String changeResult) {
                        Log.e(TAG + "切换",changeResult);
                        try {
                            JSONObject jsonDelete = new JSONObject(changeResult);
                            if (jsonDelete.getInt("status") == 9999) {
                                Toast.makeText(HostManagerActivity.this, "切换成功",Toast.LENGTH_LONG).show();
                                //切换成功把本地保存的控制主机id换掉
                                sharedPreferencesManager.save("engine_id",list.get(indexId).getEngineId());
                            } else {
                                Toast.makeText(HostManagerActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
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
     * 删除控制主机
     * @param index
     */
    private void deleteHostManager(int index){
            String account = null, token = null;
            if (sharedPreferencesManager.has("account")) {
                account = sharedPreferencesManager.get("account");
            }
            if (sharedPreferencesManager.has("token")) {
                token = sharedPreferencesManager.get("token");
            }
            try {
                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                JSONObject jsonObject = new JSONObject();
                String engineId = list.get(index).getEngineId();
                jsonObject.put("device_id",engineId);
                jsonObject.put("serial_number", list.get(index).getHostSerialNumber());
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
                Log.e(TAG + "主机", jsonArray.toString());
                String sign = MD5Utils.MD5Encode(aesAccount + jsonArray.toString() + deleteHostMethod + token + URLUtils.MD5_SIGN, "");
                XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
                xutilsHelper.add("account", aesAccount);
                xutilsHelper.add("engine_id","");
                xutilsHelper.add("device_list", jsonArray.toString());
                xutilsHelper.add("token", token);
                xutilsHelper.add("method", deleteHostMethod);
                xutilsHelper.add("sign", sign);
                xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String deleteResult) {
                        Log.e(TAG + "删除",deleteResult);
                        try {
                            JSONObject jsonDelete = new JSONObject(deleteResult);
                            if (jsonDelete.getInt("status") == 9999) {
                                Toast.makeText(HostManagerActivity.this, "删除成功", Toast.LENGTH_LONG).show();
                                adapter.setList(list);
                                adapter.notifyDataSetChanged();
                            } else if (jsonDelete.getInt("status") == 3002){
                                Utils.showDialog(HostManagerActivity.this, "请先删除摄像头,再删除控制主机");
                            } else {
                                Toast.makeText(HostManagerActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
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
        if (editTag == 0) {
            Intent intent = new Intent(HostManagerActivity.this, HostManagerDataActivity.class);
            intent.putExtra("hostMagName", list.get(i).getHostManagerName());
            intent.putExtra("hostMagId", list.get(i).getEngineId());
            intent.putExtra("hostMagSerial", list.get(i).getHostSerialNumber());
            index = i;
            startActivityForResult(intent,10);

        } else if (editTag == 1) {//切换状态
            for (int j = 0; j < list.size(); j++) {
                if (i == j) {
                    list.get(j).setHostState(0);
                } else {
                    list.get(j).setHostState(1);
                }
            }
            adapter.setList(list);
            adapter.notifyDataSetChanged();
            indexId = i;
            changeHostManager(i);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                if (data != null) {
                    list.get(index).setHostManagerName(data.getStringExtra("engineName"));
                    adapter.setList(list);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.hostmanager_back_iv:
                /*Intent intent = new Intent(HostManagerActivity.this, MainActivity.class);
                startActivity(intent);*/
                finish();
                break;
            case R.id.hostmanager_finish_btn:
                if (editTag == 0) {
                    titleRight_btn.setText("完成");
                    editTag = 1;
                } else if (editTag == 1) {
                    titleRight_btn.setText("切换");
                    editTag = 0;
                }
                break;
            default:
            break;
        }
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferencesManager = null;
    }
}
