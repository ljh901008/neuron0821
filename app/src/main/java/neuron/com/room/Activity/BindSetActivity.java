package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.BindSetAdapter;
import neuron.com.bean.RoomItemBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;
import neuron.com.view.MyListview;

/**
 * Created by ljh on 2016/11/2.
 */
public class BindSetActivity extends BaseActivity implements View.OnClickListener {

    private String TAG = "BindSetActivity";
    //双键开关TAG
    private String switchTwoATag = "sn000010007db10007_01";
    private String switchTwoBTag = "sn000010007db10007_02";
    //三键开关TAG
    private String switchThreeATag = "sn000010012db10012_01";
    private String switchThreeBTag = "sn000010012db10012_02";
    private String switchThreeCTag = "sn000010012db10012_03";
    private ImageView back_iv;
    //开关 A,B,C的名字
    private TextView kaiguanAName_tv,kaiguanBName_tv,kaiguanCName_tv;
    //开关A,B,C 添加 绑定
    private ImageView kaiguanAAdd_iv,kaiguanBAdd_iv,kaiguanCAdd_iv;
   //A,B,C三个listview
    private MyListview kaiguanA_lv,kaiguanB_lv, kaiguanC_lv;

    //B,C键的布局，可根据开关的类型修改布局
    private RelativeLayout kaiguanB_rll,kaiguanC_rll;
    private Intent intent;
    private Bundle bundle;
    private List<RoomItemBean> listRoomBean;
    private BindSetAdapter bindSetAdapterA, bindSetAdapterB, bindSetAdapterC;
    private String deviceId, account, serialNumber, token;
    private String METHOD = "QueryDeviceDetail";
    private SharedPreferencesManager sharedPreferencesManager = null;
    private int type;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://解绑设备
                    if (msg.what == 102) {
                        try {
                            String deleteResult = (String) msg.obj;
                            Log.e(TAG + "delete", deleteResult);
                            JSONObject jsonObject = new JSONObject(deleteResult);
                            if (jsonObject.getInt("status") == 9999) {
                                Utils.showDialog(BindSetActivity.this,"删除成功");
                                if (sharedPreferencesManager.has("account")) {
                                    account = sharedPreferencesManager.get("account");
                                }
                                if (sharedPreferencesManager.has("token")) {
                                    token = sharedPreferencesManager.get("token");
                                }
                                XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
                                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                                Utils.getDeviceDetail(aesAccount,token,METHOD,serialNumber,deviceId,
                                        xutilsHelper,2,BindSetActivity.this);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case 2://获取设备详情
                    if (msg.what == 102) {
                        try {
                            String branchId;
                            String deviceResult = (String) msg.obj;
                            Log.e(TAG + "绑定页设备详情",deviceResult);
                            JSONObject jsonObject = new JSONObject(deviceResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                                JSONArray jsonDeviceList = jsonMsg.getJSONArray("controled_devicelist");
                                int length = jsonDeviceList.length();
                                if (length > 0) {
                                    for (int i = 0; i < length; i++) {
                                        JSONObject jsControled = jsonDeviceList.getJSONObject(i);
                                        branchId = jsControled.getString("branch_id");
                                        JSONArray jsonArray = jsControled.getJSONArray("controled_devicelist");
                                        List<RoomItemBean> listRb = null;
                                        RoomItemBean bean = null;
                                        if (type == 1) {//单键开关
                                            listRb = new ArrayList<RoomItemBean>();
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject json = jsonArray.getJSONObject(j);
                                                bean = new RoomItemBean();
                                               // bean.setRoomId(json.getString("roomid"));
                                                //bean.setRoomName(json.getString("roomname"));
                                                bean.setDeviceName(json.getString("device_name"));
                                                bean.setSerialNumber(json.getString("device_serial"));
                                                bean.setDeviceType(json.getString("device_brand"));
                                                bean.setBranchId(branchId);
                                                bean.setSign(json.getInt("sign"));
                                                listRb.add(bean);
                                            }
                                            bindSetAdapterA.setList(listRb);
                                            bindSetAdapterA.notifyDataSetChanged();
                                        } else {//双键和三键开关
                                            listRb = new ArrayList<RoomItemBean>();
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject json = jsonArray.getJSONObject(j);
                                                bean = new RoomItemBean();
                                               // bean.setRoomId(json.getString("roomid"));
                                               // bean.setRoomName(json.getString("roomname"));
                                                bean.setDeviceName(json.getString("device_name"));
                                                bean.setSerialNumber(json.getString("device_serial"));
                                                bean.setDeviceType(json.getString("device_brand"));
                                                bean.setBranchId(branchId);
                                                bean.setSign(json.getInt("sign"));
                                                listRb.add(bean);
                                            }
                                            if (branchId.equals(switchTwoATag) || branchId.equals(switchThreeATag)) {//双键三键开关的A键
                                                bindSetAdapterA.setList(listRb);
                                                bindSetAdapterA.notifyDataSetChanged();
                                            } else if (branchId.equals(switchTwoBTag) || branchId.equals(switchThreeBTag)) {//双键三键开关的B键
                                                bindSetAdapterB.setList(listRb);
                                                bindSetAdapterB.notifyDataSetChanged();
                                            } else if (branchId.equals(switchThreeCTag)) {//三键开关C键
                                                bindSetAdapterC.setList(listRb);
                                                bindSetAdapterC.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                } else {
                                    List<RoomItemBean> listbean = new ArrayList<RoomItemBean>();
                                    bindSetAdapterA.setList(listbean);
                                    bindSetAdapterA.notifyDataSetChanged();
                                    bindSetAdapterB.setList(listbean);
                                    bindSetAdapterB.notifyDataSetChanged();
                                    bindSetAdapterC.setList(listbean);
                                    bindSetAdapterC.notifyDataSetChanged();
                                }
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
        setContentView(R.layout.bindset);
        init();
        setListener();
    }

    private void setListener() {
        back_iv.setOnClickListener(this);
        kaiguanAName_tv.setOnClickListener(this);
        kaiguanAAdd_iv.setOnClickListener(this);
        kaiguanBName_tv.setOnClickListener(this);
        kaiguanBAdd_iv.setOnClickListener(this);
        kaiguanCName_tv.setOnClickListener(this);
        kaiguanCAdd_iv.setOnClickListener(this);
        kaiguanA_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        kaiguanB_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
        kaiguanC_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });

    }

    private void init() {
        intent = getIntent();
         type = intent.getIntExtra("type", 4);
        deviceId = intent.getStringExtra("deviceId");
        serialNumber = intent.getStringExtra("serialNumber");
        bundle = intent.getExtras();
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        }
        back_iv = (ImageView) findViewById(R.id.bindset_back_iv);
        kaiguanAName_tv = (TextView) findViewById(R.id.bindset_akaiguan_name_tv);
        kaiguanAAdd_iv = (ImageView) findViewById(R.id.bindset_akaiguan_iv);
        kaiguanA_lv = (MyListview) findViewById(R.id.bindset_akaiguan_lv);
        kaiguanBName_tv = (TextView) findViewById(R.id.bindset_bkaiguan_name_tv);
        kaiguanBAdd_iv = (ImageView) findViewById(R.id.bindset_bkaiguan_iv);
        kaiguanB_lv = (MyListview) findViewById(R.id.bindset_bkaiguan_lv);
        kaiguanCName_tv = (TextView) findViewById(R.id.bindset_ckaiguan_name_tv);
        kaiguanCAdd_iv = (ImageView) findViewById(R.id.bindset_ckaiguan_iv);
        kaiguanC_lv = (MyListview) findViewById(R.id.bindset_ckaiguan_lv);
        kaiguanB_rll = (RelativeLayout) findViewById(R.id.bindset_device_2_rll);
        kaiguanC_rll = (RelativeLayout) findViewById(R.id.bindset_device_3_rll);
        listRoomBean = (List<RoomItemBean>) bundle.getSerializable("deviceDetails");
        Log.e(TAG + "数据长度", String.valueOf(listRoomBean.size()));
        int size = listRoomBean.size();
        switch(type){
            case 1://SwitchOneActivity界面进入此页面的
                kaiguanB_rll.setVisibility(View.GONE);
                kaiguanC_rll.setVisibility(View.GONE);
                if ( size> 0) {
                    for (int i = 0; i < size; i++) {
                        RoomItemBean roomItemBean= listRoomBean.get(i);
                        List<RoomItemBean> list = roomItemBean.getRoomItemBeanList();
                        List<RoomItemBean> deviceList = new ArrayList<RoomItemBean>();
                        for (int j = 0; j < list.size(); j++) {
                            RoomItemBean bean = list.get(j);
                            //bean.setDeviceId(deviceId);
                            bean.setBranchId(roomItemBean.getBranchId());
                            deviceList.add(bean);
                        }
                        bindSetAdapterA = new BindSetAdapter(this, deviceList, handler, deviceId);
                        kaiguanA_lv.setAdapter(bindSetAdapterA);
                    }
                }
                break;
            case 2://SwitchTwoActivity界面进入此页面的
                kaiguanC_rll.setVisibility(View.GONE);
                if ( size> 0) {
                    for (int i = 0; i < size; i++) {
                        RoomItemBean roomItemBean= listRoomBean.get(i);
                        List<RoomItemBean> list = roomItemBean.getRoomItemBeanList();
                        List<RoomItemBean> deviceList = new ArrayList<RoomItemBean>();
                        for (int j = 0; j < list.size(); j++) {
                            RoomItemBean bean = list.get(j);
                            bean.setBranchId(roomItemBean.getBranchId());
                            deviceList.add(bean);
                        }
                        if (roomItemBean.getBranchId().equals(switchTwoATag)) {//双键开关A键下TAG
                            bindSetAdapterA = new BindSetAdapter(this, deviceList,handler, deviceId);
                            kaiguanA_lv.setAdapter(bindSetAdapterA);
                        } else if (roomItemBean.getBranchId().equals(switchTwoBTag)) {//双键开关B键下TAG
                            bindSetAdapterB = new BindSetAdapter(this, deviceList,handler, deviceId);
                            kaiguanB_lv.setAdapter(bindSetAdapterB);
                        }
                    }
                }
                break;
            case 3://SwitchThreeActivity界面进入此页面的
                if ( size> 0) {
                    for (int i = 0; i < size; i++) {
                        RoomItemBean roomItemBean= listRoomBean.get(i);
                        List<RoomItemBean> list = roomItemBean.getRoomItemBeanList();
                        List<RoomItemBean> deviceList = new ArrayList<RoomItemBean>();
                        for (int j = 0; j < list.size(); j++) {
                            RoomItemBean bean = list.get(j);
                            bean.setBranchId(roomItemBean.getBranchId());
                            deviceList.add(bean);
                        }
                        if (roomItemBean.getBranchId().equals(switchThreeATag)) {//三键开关A键下TAG
                            bindSetAdapterA = new BindSetAdapter(this, deviceList, handler, deviceId);
                            kaiguanA_lv.setAdapter(bindSetAdapterA);
                        } else if (roomItemBean.getBranchId().equals(switchThreeBTag)) {//三键开关B键下TAG
                            bindSetAdapterB = new BindSetAdapter(this, deviceList, handler, deviceId);
                            kaiguanB_lv.setAdapter(bindSetAdapterB);
                        } else if (roomItemBean.getBranchId().equals(switchThreeCTag)) {//三键开关C键下TAG
                            bindSetAdapterC = new BindSetAdapter(this, deviceList, handler, deviceId);
                            kaiguanC_lv.setAdapter(bindSetAdapterC);
                        }
                    }
                }
                break;
            default:
                break;
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bindset_akaiguan_name_tv: //A开关名称
                break;
            case R.id.bindset_akaiguan_iv://A开关设备 绑定 ImageView
                break;
            case R.id.bindset_bkaiguan_name_tv: //B开关名称
                break;
            case R.id.bindset_bkaiguan_iv://B开关设备 绑定 ImageView
                break;
            case R.id.bindset_ckaiguan_name_tv: //C开关名称
                break;
            case R.id.bindset_ckaiguan_iv://C开关设备 绑定 ImageView
                break;
            case R.id.bindset_back_iv://返回键
                finish();
                break;
            default:
                break;
        }
    }
}
