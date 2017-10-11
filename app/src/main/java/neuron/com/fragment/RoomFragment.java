package neuron.com.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.karics.library.zxing.android.CaptureActivity;
import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import neuron.com.adapter.RoomAdapter;
import neuron.com.adapter.SY_PopuWAdapter;
import neuron.com.adapter.SceneAdapter;
import neuron.com.app.OgeApplication;
import neuron.com.bean.RoomItemBean;
import neuron.com.bean.SY_PopuWBean;
import neuron.com.bean.SceneItemBean;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;
import neuron.com.room.Activity.AddRoomActivity;
import neuron.com.room.Activity.AddShareCodeActivity;
import neuron.com.room.Activity.AirConditionActivity;
import neuron.com.room.Activity.AirQualityActivity;
import neuron.com.room.Activity.AlertTwoActivity;
import neuron.com.room.Activity.DoorSecsorActivity;
import neuron.com.room.Activity.EZ_CameraRePlayerActivity;
import neuron.com.room.Activity.ElectricityCurtainActivity;
import neuron.com.room.Activity.HOPE_SongListActivity;
import neuron.com.room.Activity.InfraredInductionActivity;
import neuron.com.room.Activity.LightActivity;
import neuron.com.room.Activity.OutletActivity;
import neuron.com.room.Activity.TelevisionActivity;
import neuron.com.room.Activity.WaterFuntainActivity;
import neuron.com.scene.Activity.AddSceneActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.DataManager;
import neuron.com.util.EZUtils;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;
import neuron.com.view.MyGridView;
import neuron.com.view.MyListview;
import sound.ConstantValue;
import sound.DensityUtils;
import sound.KeyBoardUtils;
import sound.Result;
import sound.SPUtils;
import sound.ScreenUtils;
import sound.SpeechUtil;

/**
 * Created by ljh on 2016/8/28.
 */
public class RoomFragment extends Fragment implements View.OnClickListener,View.OnTouchListener, View.OnLongClickListener{
    private String TAG = "RoomFragment";

    private View view;
    //首页listview
    private MyListview deviceLv;
    private MyGridView sceneGv;

    //titilebar
    private ImageView alert_iv,alertTishi_iv;
    private RelativeLayout alert_rll;
    private ImageButton sweep_iv;
    private PullToRefreshScrollView pullToRefreshScrollView;
    private ScrollView sv;
    private List<RoomItemBean> deviceList;
    private List<SceneItemBean> sceneItemBeanList;
    private String METHOD = "QueryAllList";
    private SharedPreferencesManager sharedPreferencesManager;
    private String account, token,engine_id;
    private RoomAdapter roomAdapter;
    private SceneAdapter sceneAdapter;
    private String pullstorefreshScrollViewResult;
    private PopupWindow popupWindow;

    private RelativeLayout roomSelect_rll;
    private TextView roomName_tv;
    private List<SY_PopuWBean> listSY;
    private SY_PopuWAdapter sy_popuWAdapter;
    private String QUERYHOMELIST = "QueryRoomList";
    //根据房间查询设备列表
    private String QUERYLIST = "QueryRoomElectriclist";
    private ListView popuW_lv;
    private RelativeLayout popuW_fl;
    private boolean isSelect = false;
    private boolean isFirstRefresh = true;
    private MainActivity mainActivity;
    private String controlMethod = "DoOrders";
    private RelativeLayout noScene_rll, noDevice_rll;
    /**
     * 第三方获取accesstoken的方法名
     */
    private String method_getAccessToken = "token/getAccessToken";
    /**
     * 萤石所需参数   版本号
     */
    private String version = "1.0";
    /**
     * 萤石所需参数   id
     */
    private String id = "1.0";
    private String cameraAccount;
    private String cameraName;
    private String cameraSerial;//序列号
    private String thirdAccount;//
    private int cameraPosition = -1;
    private WaitDialog mWaitDialog;

    private String methodSceneList = "GetHSContextualModelList";

    /**
     * 是否语音操作设备
     */
    boolean isSpeechOpera = false;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1://设备列表
                    if (msg.what == 102) {
                        String roomResult = (String) msg.obj;
                        Log.e(TAG + "首页列表", roomResult);

                        Utils.dismissWaitDialog(mWaitDialog);
                        try {
                            JSONObject js = new JSONObject(roomResult);
                            int status = js.getInt("status");
                            if (status == 9999) {
                                noDevice_rll.setVisibility(View.GONE);
                                deviceLv.setVisibility(View.VISIBLE);
                                JSONObject msgJs = js.getJSONObject("msg");
                                int isMsg = msgJs.getInt("is_message");
                                Log.e(TAG + "消息标记", String.valueOf(isMsg));
                                if (isMsg == 0) {
                                    //接收到熬manifragment传来的数据显示小红点
                                    alertTishi_iv.setVisibility(View.VISIBLE);
                                } else {
                                    alertTishi_iv.setVisibility(View.GONE);
                                }
                                JSONArray deviceArray = msgJs.getJSONArray("controlled_device_list");//被控制设备列表
                                Log.e(TAG + "首页设备列表", deviceArray.toString());
                                int deviceLength = deviceArray.length();
                                if (deviceLength > 0) {
                                    deviceList = new ArrayList<RoomItemBean>();
                                    RoomItemBean deviceBean;
                                    for (int i = 0; i < deviceLength; i++) {
                                        JSONObject roomJs = deviceArray.getJSONObject(i);
                                        deviceBean = new RoomItemBean();
                                        deviceBean.setDeviceId(roomJs.getString("controlled_device_id"));
                                        deviceBean.setDeviceName(roomJs.getString("controlled_device_name"));
                                        deviceBean.setDeviceSite(roomJs.getString("controlled_device_site"));
                                        deviceBean.setSerialNumber(roomJs.getString("serial_number"));
                                        deviceBean.setThirdAccount(roomJs.getString("third_account"));
                                        String typeId = roomJs.getString("electric_type_id");
                                        deviceBean.setDeviceType(typeId);
                                        if (typeId.equals("33001") || typeId.equals("33009")) {//开关和灯在首页有开关按钮
                                            deviceBean.setDeviceStatu(roomJs.getInt("status"));
                                        }
                                        String roomId = roomJs.getString("room_id");
                                        deviceBean.setDeviceRoomId(roomId);
                                        if (!TextUtils.isEmpty(roomId)) {
                                            deviceBean.setDeviceRoom(roomJs.getString("room_name"));
                                        }

                                        deviceList.add(deviceBean);
                                    }
                                    if (isFirstRefresh) {//初始化获取数据
                                        roomAdapter = new RoomAdapter(getActivity(), deviceList, handler, sharedPreferencesManager);
                                        deviceLv.setAdapter(roomAdapter);
                                    } else {//下拉刷新获取数据
                                        if (roomName_tv.getText().toString().trim().equals("全部房间")) {
                                            roomAdapter.setList(deviceList);
                                            roomAdapter.notifyDataSetChanged();
                                        }
                                    }
                                } else {
                                    deviceLv.setVisibility(View.GONE);
                                    noDevice_rll.setVisibility(View.VISIBLE);
                                }
                                JSONArray sceneArray = msgJs.getJSONArray("contextual_model_list");//场景列表
                                Log.e(TAG + "场景列表", sceneArray.toString());
                                int sceneLength = sceneArray.length();
                                if (sceneLength > 0) {
                                    sceneGv.setVisibility(View.VISIBLE);
                                    noScene_rll.setVisibility(View.GONE);
                                    sceneItemBeanList = new ArrayList<SceneItemBean>();
                                    SceneItemBean sceneItemBean;
                                    for (int j = 0; j < sceneLength; j++) {
                                        JSONObject sceneJs = sceneArray.getJSONObject(j);
                                        sceneItemBean = new SceneItemBean();
                                        sceneItemBean.setSceneId(sceneJs.getString("contextual_model_id"));
                                        sceneItemBean.setSceneName(sceneJs.getString("contextual_model_name"));
                                        sceneItemBean.setIsDefault(sceneJs.getInt("contextual_model_type"));
                                        //sceneItemBean.setOpenTime(sceneJs.getString("open_time"));'
                                        int img = sceneJs.getInt("contextual_model_img");
                                        // Log.e(TAG + "场景图标", String.valueOf(img));
                                        sceneItemBean.setSceneImg(img);
                                        sceneItemBean.setSceneStatus(sceneJs.getInt("status"));
                                        sceneItemBeanList.add(sceneItemBean);
                                    }
                                    Log.e(TAG + "场景数据长度", String.valueOf(sceneItemBeanList.size()) + "," + isFirstRefresh);
                                    if (isFirstRefresh) {
                                        sceneAdapter = new SceneAdapter(sceneItemBeanList, getActivity());
                                        sceneGv.setAdapter(sceneAdapter);
                                        isFirstRefresh = false;
                                    } else {
                                        sceneAdapter.setList(sceneItemBeanList);
                                        sceneAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    sceneGv.setVisibility(View.GONE);
                                    noScene_rll.setVisibility(View.VISIBLE);
                                }
                            } else if (status == 1000 || status == 1001) {
                                Toast.makeText(getActivity(), "帐号在别处登录",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity(), "网络不通", Toast.LENGTH_LONG).show();
                        Utils.dismissWaitDialog(mWaitDialog);
                    }
                    break;
                case 5://操作设备
                    if (msg.what == 102) {
                        try {
                            String result = (String) msg.obj;
                            Log.e(TAG + "result", result);
                            if (mWaitDialog.isShowing()) {

                                Utils.dismissWaitDialog(mWaitDialog);
                            }
                            JSONObject json = new JSONObject(result);
                            int status1 = json.getInt("status");
                            if (status1 == 9999) {
                                reslutShow();
                                //Toast.makeText(getActivity(), "操作成功", Toast.LENGTH_LONG).show();
                            } else if (status1 == 1000 || status1 == 1001) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getActivity(), json.getString("error"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (mWaitDialog.isShowing()) {

                            Utils.dismissWaitDialog(mWaitDialog);
                        }
                        Toast.makeText(getActivity(), "网络不通", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 4://根据房间获取设备列表
                    if (msg.what == 102) {
                        String dResult = (String) msg.obj;
                        Log.e(TAG + "房间下的设备列表",dResult);
                        try {
                            JSONObject jsonObject = new JSONObject(dResult);
                            int status2 = jsonObject.getInt("status");
                            if (status2 == 9999) {
                                JSONArray jsonArray = jsonObject.getJSONArray("electric_list");
                                int length = jsonArray.length();
                                if (length > 0) {
                                    if (deviceList == null) {
                                        deviceList = new ArrayList<RoomItemBean>();
                                    } else {
                                        deviceList.clear();
                                    }
                                    RoomItemBean dBean;
                                    for (int i = 0; i < length; i++) {
                                        dBean = new RoomItemBean();
                                        JSONObject jsonB = jsonArray.getJSONObject(i);
                                        dBean.setDeviceId(jsonB.getString("controlled_device_id"));
                                        dBean.setDeviceName(jsonB.getString("controlled_device_name"));
                                        dBean.setDeviceSite(jsonB.getString("controlled_device_site"));
                                        dBean.setDeviceRoom(jsonB.getString("room_name"));
                                        dBean.setDeviceRoomId(jsonB.getString("room_id"));
                                        dBean.setSerialNumber(jsonB.getString("serial_number"));
                                        String t = jsonB.getString("electric_type_id");
                                        dBean.setDeviceType(t);
                                        if (!"33008".equals(t) && !"33010".equals(t) && !"33006".equals(t) && !"33007".equals(t)&& !"33003".equals(t)) {
                                            dBean.setDeviceStatu(jsonB.getInt("status"));
                                        }
                                        deviceList.add(dBean);
                                    }
                                    roomAdapter.setList(deviceList);
                                    roomAdapter.notifyDataSetChanged();
                                }
                            }else if (status2 == 1000 || status2 == 1001) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2://摄像头
                    EZDeviceInfo deviceInfo = (EZDeviceInfo) msg.obj;
                    if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                        LogUtil.d(TAG, "cameralist have one camera");

                        final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                        cameraInfo.setCameraName(cameraName);
                        if (sharedPreferencesManager.has(cameraSerial)) {
                            DataManager.getInstance().setDeviceSerialVerifyCode(cameraSerial, sharedPreferencesManager.get(cameraSerial));
                        }
                        if (cameraInfo == null) {
                            return;
                        }
                        Intent intent = new Intent(getActivity(), EZ_CameraRePlayerActivity.class);
                        intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                        intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                        startActivity(intent);
                        return;
                    }
                    break;
                case 3://摄像头错误码
                    int errorCode = (int) msg.obj;
                    if (errorCode == 120018) {//120018：此token不是这个帐号对应token
                        getTime();
                    }
                    break;
                case 6://房间列表
                    if (msg.what == 102) {
                        String roomResult = (String) msg.obj;
                        Log.e(TAG + "房间列表", roomResult);
                        try {
                            JSONObject jsonObject = new JSONObject(roomResult);
                            int status3 = jsonObject.getInt("status");
                            if (status3 == 9999) {
                                JSONArray roomArray = jsonObject.getJSONArray("room_list");
                                int roomLength = roomArray.length();
                                if (roomLength > 0) {
                                    listSY = new ArrayList<SY_PopuWBean>();
                                    SY_PopuWBean sy_popuWBean;
                                    for (int k = 0; k < roomLength; k++) {
                                        JSONObject roomJs = roomArray.getJSONObject(k);
                                        sy_popuWBean = new SY_PopuWBean();
                                        sy_popuWBean.setHomeName(roomJs.getString("room_name"));
                                        sy_popuWBean.setHomeId(roomJs.getString("room_id"));
                                        listSY.add(sy_popuWBean);
                                    }
                                    if (sy_popuWAdapter == null) {
                                        sy_popuWAdapter = new SY_PopuWAdapter(getActivity(), listSY);
                                        Log.e(TAG + "适配器中数据长度", String.valueOf(sy_popuWAdapter.getCount()));
                                        showPopuWindow(0);
                                    } else {
                                        sy_popuWAdapter.setList(listSY);
                                        showPopuWindow(1);
                                    }
                                }
                            }else if (status3 == 1000 || status3 == 1001) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 7://情景模式操作
                    if (msg.what == 102) {
                        String controlRestlt = (String) msg.obj;
                        Log.e("操作场景" + TAG, controlRestlt);
                        try {
                            JSONObject json = new JSONObject(controlRestlt);
                            int status3 = json.getInt("status");
                            if (status3 == 9999) {
                                getSceneList();//操作成功更新场景列表
                            }else if (status3 == 1000 || status3 == 1001) {
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);
                            }else {
                                Utils.showDialog(getActivity(), json.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(getActivity(), "网络不通", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 8://获取萤石服务器时间
                    String EZTimeResult = (String) msg.obj;
                    try {
                        JSONObject json=new JSONObject(EZTimeResult);
                        JSONObject result=json.getJSONObject("result");
                        JSONObject data=result.getJSONObject("data");
                        String EzTime= data.getString("serverTime");
                        sharedPreferencesManager.save("EZTime", EzTime);
                        getAccessToken(cameraAccount,EzTime);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case 9://获取token
                    String EZResult = (String) msg.obj;
                    try {
                        JSONObject json = new JSONObject(EZResult);
                        JSONObject js1 = json.getJSONObject("result");
                        int code = js1.getInt("code");
                        if (code == 200) {
                            JSONObject js = js1.getJSONObject("data");
                            String accessTonken = js.getString("accessToken");
                            sharedPreferencesManager.save("EZToken", accessTonken);
                            Log.e("Openservicetoken", accessTonken);
                            OgeApplication.getOpenSDK().setAccessToken(accessTonken);
                            if (cameraPosition != -1) {
                                getCameraDeviceInfo(cameraPosition);
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    break;
                case 10://有状态的场景列表。更新首页场景列表
                    if (msg.what == 102) {
                        String sceneListResult = (String) msg.obj;
                        Log.e(TAG + "有状态的场景列表",sceneListResult);
                        try {
                            JSONObject jsonObject = new JSONObject(sceneListResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONArray jsonMsg = jsonObject.getJSONArray("contextual_model_list");
                                int length = jsonMsg.length();
                                if (length > 0) {
                                    sceneItemBeanList.clear();
                                    SceneItemBean sceneItemBean;
                                    for (int j = 0; j < length; j++) {
                                        JSONObject sceneJs = jsonMsg.getJSONObject(j);
                                        sceneItemBean = new SceneItemBean();
                                        sceneItemBean.setSceneId(sceneJs.getString("contextual_model_id"));
                                        sceneItemBean.setSceneName(sceneJs.getString("contextual_model_name"));
                                        sceneItemBean.setIsDefault(sceneJs.getInt("contextual_model_type"));
                                        //sceneItemBean.setOpenTime(sceneJs.getString("open_time"));'
                                        int img = sceneJs.getInt("contextual_model_img");
                                        // Log.e(TAG + "场景图标", String.valueOf(img));
                                        sceneItemBean.setSceneImg(img);
                                        sceneItemBean.setSceneStatus(sceneJs.getInt("status"));
                                        sceneItemBeanList.add(sceneItemBean);
                                    }
                                    //sceneAdapter.setList(sceneItemBeanList);
                                    sceneAdapter.notifyDataSetChanged();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.roomfragment, container, false);
        init();
        initFAB();
        mWaitDialog = new WaitDialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        getDeviceList(1);
        setLitener();
        return view;
    }

    private void init() {
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getContext());
        alert_iv = (ImageView) view.findViewById(R.id.roomfragment_alert_iv);
        alertTishi_iv = (ImageView) view.findViewById(R.id.roomfragment_alertdian_iv);
        sweep_iv = (ImageButton) view.findViewById(R.id.roomfragment_add_ivbtn);
        deviceLv = (MyListview) view.findViewById(R.id.roomfragment_devicelist_lv);
        sceneGv = (MyGridView) view.findViewById(R.id.roomfragment_scene_gv);
        roomName_tv = (TextView) view.findViewById(R.id.roomfragment_roomname_tv);
        roomSelect_rll = (RelativeLayout) view.findViewById(R.id.roomfragment_roomname_rll);
        noScene_rll = (RelativeLayout) view.findViewById(R.id.roomfragment_noscene_rll);
        noDevice_rll = (RelativeLayout) view.findViewById(R.id.roomfragment_nodevice_rll);
        alert_rll = (RelativeLayout) view.findViewById(R.id.roomfragment_alert_rll);
        pullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.roomfragment_pullsv);
        //子帐号没有添加 和分享权限
        if (sharedPreferencesManager.has("userType")) {
            if (sharedPreferencesManager.get("userType").equals("02")) {
                sweep_iv.setVisibility(View.GONE);
            }
        }
        pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                getDeviceList(1);
                new GetDataTask().execute();
            }


        });

        sv = pullToRefreshScrollView.getRefreshableView();
        //设置scrollview在最顶端
        sv.smoothScrollTo(0, 0);


    }

    private void setLitener() {
        alert_rll.setOnClickListener(this);
        sweep_iv.setOnClickListener(this);
        roomSelect_rll.setOnClickListener(this);
        deviceLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Intent intent = new Intent();
                intent.putExtra("deviceId", deviceList.get(i).getDeviceId());
                intent.putExtra("deviceType", deviceList.get(i).getDeviceType());
                //intent.putExtra("deviceName",deviceList.get(i).getDeviceName())
                String dType = deviceList.get(i).getDeviceType();
                if ("33001".equals(dType)) {//普通灯\
                    intent.setClass(getActivity(), LightActivity.class);
                    startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                } else if ("33008".equals(dType)) {//空气质量监测仪
                    intent.setClass(getActivity(), AirQualityActivity.class);
                    startActivity(intent);
                } else if ("33010".equals(dType)) {//摄像头
                    cameraName = deviceList.get(i).getDeviceName();
                    cameraAccount = deviceList.get(i).getThirdAccount();
                    cameraSerial = deviceList.get(i).getSerialNumber();
                    thirdAccount = deviceList.get(i).getThirdAccount();
                    sharedPreferencesManager.save("thirdAccount", thirdAccount);
                    cameraPosition = i;
                    if (sharedPreferencesManager.has("EZToken")) {
                        OgeApplication.getOpenSDK().setAccessToken(sharedPreferencesManager.get("EZToken"));
                        getCameraDeviceInfo(i);
                    } else {
                        getTime();
                    }
                } else if ("33006".equals(dType)) {//电视
                    intent.setClass(getActivity(), TelevisionActivity.class);
                    startActivity(intent);
                } else if ("33007".equals(dType)) {//空调
                    intent.setClass(getActivity(), AirConditionActivity.class);
                    startActivity(intent);
                } else if ("33004".equals(dType)) {//电动窗帘
                    intent.setClass(getActivity(), ElectricityCurtainActivity.class);
                    startActivity(intent);
                } else if ("33009".equals(dType)) {//智能插座
                    intent.setClass(getActivity(), OutletActivity.class);
                    startActivityForResult(intent, Activity.RESULT_FIRST_USER);
                } else if ("33005".equals(dType)) {//门磁
                    intent.setClass(getActivity(), DoorSecsorActivity.class);
                    startActivity(intent);
                } else if ("33003".equals(dType)) {//红外人体感应
                    intent.setClass(getActivity(), InfraredInductionActivity.class);
                    startActivity(intent);
                }else if ("33011".equals(dType)) {//净水器
                    intent.setClass(getActivity(), WaterFuntainActivity.class);
                    startActivity(intent);
                } else if ("33012".equals(dType)) {//音箱
                    intent.putExtra("deviceName", deviceList.get(i).getDeviceName());
                    intent.putExtra("deviceRoom", deviceList.get(i).getDeviceRoom());
                    intent.putExtra("serialNum",deviceList.get(i).getSerialNumber());
                    intent.putExtra("roomId",deviceList.get(i).getDeviceRoomId());
                    intent.putExtra("thirdaccount", deviceList.get(i).getThirdAccount());
                    intent.setClass(getActivity(), HOPE_SongListActivity.class);
                    startActivity(intent);
                }

            }
        });
        sceneGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                controlScene(sceneItemBeanList.get(position).getSceneId(), controlMethod, "ffff");
                sceneItemBeanList.get(position).setSceneStatus(1);
                sceneAdapter.setList(sceneItemBeanList);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Activity.RESULT_FIRST_USER) {
                if (data.getIntExtra("tag", 10) == 1) {
                    isFirstRefresh = false;
                    getDeviceList(1);
                }
            }
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.roomfragment_alert_rll://警报
                Intent intent = new Intent(getActivity(), AlertTwoActivity.class);
                startActivity(intent);
                break;
            case R.id.roomfragment_add_ivbtn://扫一扫
                showPopuWindowTwo();
                break;
            case R.id.roomfragment_roomname_rll://选择房间
               // showPopuWindow();
                getHomeList();
                break;
            default:
                break;
        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, String> {
        /**
         * 这里的Void参数对应AsyncTask中的第一个参数
         * 这里的String返回值对应AsyncTask的第三个参数
         * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
         * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
         */
        @Override
        protected String doInBackground(Void... voids) {
           /* try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            return null;
        }

        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pullToRefreshScrollView.onRefreshComplete();
        }
    }

    /**
     * 首页列表
     * @param order
     */
    private void getDeviceList(int order) {
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
            Log.e(TAG + "account", account);
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
            Log.e(TAG + "token", token);
        }
        if (sharedPreferencesManager.has("engine_id")) {
            Utils.showWaitDialog("加载中...", getActivity(),mWaitDialog);
            engine_id = sharedPreferencesManager.get("engine_id");
            Log.e(TAG + "engineId", engine_id);
            try {
                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                String sign = MD5Utils.MD5Encode(aesAccount + engine_id + METHOD + token + URLUtils.MD5_SIGN, "");
                XutilsHelper xutil = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
                xutil.add("account", aesAccount);
                xutil.add("engine_id", engine_id);
                xutil.add("method", METHOD);
                xutil.add("token", token);
                xutil.add("sign", sign);
                xutil.sendPost(order, getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {//没有控制主机需要选择控制主机 或添加控制主机
            deviceLv.setVisibility(View.GONE);
            noDevice_rll.setVisibility(View.VISIBLE);
            sceneGv.setVisibility(View.GONE);
            noScene_rll.setVisibility(View.VISIBLE);
        }
    }

    private RelativeLayout quanBu;

    private void showPopuWindowTwo() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.selectpopuwindow, null);
        Button addDevice = (Button) view.findViewById(R.id.selectpopuwindow_adddevice_btn);
        Button addScene = (Button) view.findViewById(R.id.selectpopuwindow_addscene_btn);
        Button addShare = (Button) view.findViewById(R.id.selectpopuwindow_shareaccount_btn);
        PopupWindow popupWindow = new PopupWindow(view);
        //设置SelectPicPopupWindow弹出窗体的宽——>匹配不同机型的适配
        int mWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();

        popupWindow.setWidth((mWidth * 5) / 15);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        final Intent intent = new Intent();
        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    intent.setClass(getActivity(), CaptureActivity.class);
                    startActivity(intent);
                    getActivity().finish();
            }
        });
        addScene.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sharedPreferencesManager.has("engine_id")) {
                    intent.setClass(getActivity(), AddSceneActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Utils.showDialog(getActivity(), "请添加控制主机");
                }
            }
        });
        addShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    intent.setClass(getActivity(), AddShareCodeActivity.class);
                    startActivity(intent);
            }
        });
        popupWindow.showAsDropDown(sweep_iv);
    }
    private void showPopuWindow(int tag) {
        if (popupWindow == null) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.shouye_popuwindow, null);
            popuW_lv = (ListView) view.findViewById(R.id.shouye_popuwindow_lv);
            popuW_fl = (RelativeLayout) view.findViewById(R.id.shouye_popuwindow_addhome_fl);
            quanBu = (RelativeLayout) view.findViewById(R.id.shouye_popuwindow_quanbu_rll);
            popuW_lv.setAdapter(sy_popuWAdapter);
            WindowManager windowManager = getActivity().getWindowManager();
            int width = windowManager.getDefaultDisplay().getWidth();
            int height = windowManager.getDefaultDisplay().getHeight();
            popupWindow = new PopupWindow(view);
            popupWindow.setWidth(width/3);
            popupWindow.setHeight(height/4);
            popuW_fl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), AddRoomActivity.class);
                    intent.putExtra("type", 1);
                    startActivity(intent);
                }
            });
        } else {
            sy_popuWAdapter.notifyDataSetChanged();
        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //在选择帐号控件下方弹出
        popupWindow.showAsDropDown(roomSelect_rll, 0, 0);
        //设置popuwindow的位置
        popuW_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                for (int i = 0; i < listSY.size(); i++) {
                    if (i == position) {
                        listSY.get(i).setIsSelect(1);
                        roomName_tv.setText(listSY.get(i).getHomeName());
                    } else {
                        listSY.get(position).setIsSelect(0);
                    }

                }
                sy_popuWAdapter.setList(listSY);
                sy_popuWAdapter.notifyDataSetChanged();
                try {
                    String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                    String roomId = listSY.get(position).getHomeId();
                    Log.e(TAG + "房间", roomId);
                    String sign = MD5Utils.MD5Encode(aesAccount + engine_id + QUERYLIST + roomId + token + URLUtils.MD5_SIGN, "");
                    XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET, handler);
                    xutilsHelper.add("account", aesAccount);
                    xutilsHelper.add("engine_id", engine_id);
                    xutilsHelper.add("room_id", roomId);
                    xutilsHelper.add("token", token);
                    xutilsHelper.add("method", QUERYLIST);
                    xutilsHelper.add("sign", sign);
                    xutilsHelper.sendPost(4, getActivity());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                listSY.clear();
                popupWindow.dismiss();
            }
        });
        quanBu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceList(1);
                roomName_tv.setText("全部房间");
                popupWindow.dismiss();
            }
        });
    }


    /**
     * 获取房间列表
     */
    private void getHomeList() {
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
            Log.e(TAG + "token", token);
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engine_id = sharedPreferencesManager.get("engine_id");
            Log.e(TAG + "engineId", engine_id);
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + QUERYHOMELIST + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engine_id);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", QUERYHOMELIST);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(6, getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    /**
     *  情景模式操作id
     * @param sceneId  情景模式id
     * @param method
     * @param sceneOrder   操作指令
     */
    private void controlScene(String sceneId,String method,String sceneOrder){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
            Log.e(TAG + "token", token);
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engine_id = sharedPreferencesManager.get("engine_id");
            Log.e(TAG + "engineId", engine_id);
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + "2" + engine_id + method + sceneOrder + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engine_id);
            xutilsHelper.add("device_id", sceneId);
            xutilsHelper.add("device_type", "2");
            xutilsHelper.add("order_id", sceneOrder);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(7, getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取场景列表
     */
    private void getSceneList(){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engine_id = sharedPreferencesManager.get("engine_id");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + methodSceneList + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL, handler);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engine_id);
            xutils.add("token", token);
            xutils.add("method", methodSceneList);
            xutils.add("sign", sign);
            xutils.sendPost(10, getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取萤石服务器时间
     */
    public void getTime(){
        XutilsHelper xutil = new XutilsHelper(URLUtils.EZTIME_URL, handler);
        xutil.add("id", "12345646");
        xutil.add("appKey", OgeApplication.AppKey);
        xutil.sendPost(8, getActivity());
    }
    /**
     * 	第三方获取accesstoken值
     * @param phoneNumber 用户电话号码
     */
    public void getAccessToken(String phoneNumber,String EzTime){
        String sign = MD5Utils.MD5Encode("phone:" + phoneNumber + ",method:" + method_getAccessToken + ",time:" + EzTime + ",secret:" + OgeApplication.screct, "");
        try {
            JSONObject json1 = new JSONObject();
            json1.put("key", OgeApplication.AppKey);
            json1.put("sign", sign);
            json1.put("time", EzTime);
            json1.put("ver", version);

            JSONObject json2 = new JSONObject();
            json2.put("phone", phoneNumber);

            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("system", json1);
            json.put("method", method_getAccessToken);
            json.put("params", json2);
            XutilsHelper xutil = new XutilsHelper(URLUtils.EZ_URL, handler);
            xutil.addRequestParams(json);
            xutil.sendPost(9, getActivity());

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void getCameraDeviceInfo(final int position) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    EZDeviceInfo ezDeviceInfo = OgeApplication.getOpenSDK().getDeviceInfo(deviceList.get(position).getSerialNumber());
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 2;
                    msg.obj = ezDeviceInfo;
                    handler.sendMessage(msg);
                } catch (BaseException e) {
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 3;
                    msg.obj = e.getErrorCode();
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * speech moudle
     */
    private int mTouchSlop;  //防抖动
    private CircleImageView mFAB;//悬浮球
    /**
     * 拖动事件的坐标
     */
    private int startX;
    private int startY;
    /**
     * 屏幕宽高
     */
    private int screenWidth;
    private int screenHeight;

    private Toast mToast;

    private boolean isSaying = false;//说话还是拖拽的标志位

    private SpeechRecognizer mIat;

    /**
     * 语音标志位
     */
//    private static final int SOUND_FLAG = 5;

    /**
     * 初始化悬浮球
     */
    private void initFAB() {
        int location_x = (int) SPUtils.get(getActivity(), ConstantValue.LOCATION_X, 0);
        int location_y = (int) SPUtils.get(getActivity(), ConstantValue.LOCATION_Y, 0);
        mFAB = (CircleImageView) view.findViewById(R.id.floatingActionButton);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = location_x;
        layoutParams.topMargin = location_y;
        mFAB.setLayoutParams(layoutParams);
        mTouchSlop = ViewConfiguration.get(getActivity()).getScaledTouchSlop();
        mFAB.setOnTouchListener(this);
        mFAB.setOnLongClickListener(this);
        initOthers();
    }

    private void initOthers() {
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        screenWidth = ScreenUtils.getScreenWidth(getActivity().getApplicationContext());
        screenHeight = ScreenUtils.getScreenHeight(getActivity().getApplicationContext());
        //初始化语音听写类
        mIat = SpeechRecognizer.createRecognizer(getActivity(), mInitListener);
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTos("初始化失败，错误码：" + code);
            }


        }
    };

    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        private EditText editContent;
        private RelativeLayout pager3;
        private ImageButton huatong;
        private Button btn_sound_send;
        private Button btn_sound_cancel;
        private RelativeLayout pager2;
        private String mSpeechText;
        private TextView soundText;
        //        private LinearLayout ll_sound;
//        private LinearLayout ll_bottom;
        StringBuilder sb = new StringBuilder();  //todo 放在外面 暂定使用sbuilder

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTos("开始说话,请勿移动语音按钮");
            isSaying = true;
        }

        @Override
        public void onError(SpeechError error) {
            showTos("您好像没有说话哦");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            //            printResult(results);
//            results 得到的结果存放地址  isLast  判断是否是最后的内容，已经回调到参数中
            Log.e("长按结果：", results.getResultString());//输出语音转换结果
            Gson gson = new Gson();
            Result result = gson.fromJson(results.getResultString(), Result.class);
            List<Result.WsBean> ws = result.getWs();
            for (Result.WsBean temp :
                    ws) {
                List<Result.WsBean.CwBean> cw = temp.getCw();
                String content = cw.get(0).getW();//这是说的语音内容
                sb.append(content);
            }
            if (isLast) {
                //该方法运行在主线程
                // TODO: 2017/7/24 弹出结果页
                setNewPager2();
                btn_sound_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pager2.setVisibility(View.GONE);
                        mFAB.setVisibility(View.VISIBLE);
                    }
                });
                btn_sound_send.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String sayResult = soundText.getText().toString();
                        mFAB.setVisibility(View.VISIBLE);
                        pager2.setVisibility(View.GONE);
                        //需要发送的指令需要从文本框获取
                        sendInstruction(sayResult);

                    }
                });
                sb.delete(0, sb.length());

            }
        }


        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTos("正在说话中，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }

        /**
         * 会话结束打开新界面的参数
         */
        private void setNewPager2() {
            pager2 = (RelativeLayout) getActivity().findViewById(R.id.pager2);
            pager2.setVisibility(View.VISIBLE);
            pager2.setOnClickListener(null); //让后面的页面不可点击
            mFAB.setVisibility(View.INVISIBLE);
            soundText = (TextView) getActivity().findViewById(R.id.ed_sound_text);
            soundText.setText("");
            mSpeechText = sb.toString();
            soundText.setText(mSpeechText);
            btn_sound_cancel = (Button) getActivity().findViewById(R.id.btn_sound_cancel);
            btn_sound_send = (Button) getActivity().findViewById(R.id.btn_sound_send);
            huatong = (ImageButton) getActivity().findViewById(R.id.ib_huatong);
            huatong.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    setParam4TTS();
                    showTos("准备听写");
                    mIat.startListening(mRecognizerListener2);
                    return true;  //拦截点击
                }
            });
            huatong.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    if (action == MotionEvent.ACTION_UP) {
                        if (isSaying) {
                            mIat.stopListening();
                            showTos("停止说话");
                            isSaying = false;
                        }
                    }
                    return false; //不拦截长按
                }
            });
            /**
             * 点击文本框，打开完整界面
             */
            soundText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: 2017/8/3 打开page3
//                    pager2.setVisibility(View.GONE);
                    setNewPager3();
                }
            });
        }

        private void setNewPager3() {
            pager3 = (RelativeLayout) getActivity().findViewById(R.id.pager3);
            pager3.setVisibility(View.VISIBLE);
            editContent = (EditText) getActivity().findViewById(R.id.et_edit_sound);
            editContent.setText(mSpeechText);
            editContent.setFocusable(true);
            editContent.setFocusableInTouchMode(true);
            editContent.requestFocus();
            KeyBoardUtils.openKeybord(editContent, getActivity());
            //收起
            getActivity().findViewById(R.id.btn_retract).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = editContent.getText().toString();
                    soundText.setText(s);
//                    pager2.setVisibility(View.VISIBLE);
                    KeyBoardUtils.closeKeybord(editContent, getActivity());
                    pager3.setVisibility(View.GONE);
                }
            });
            editContent.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        //发送 关闭pager2，3。调用发送指令
                        sendInstruction(editContent.getText().toString());
                        KeyBoardUtils.closeKeybord(editContent, getActivity());
                        pager3.setVisibility(View.GONE);
                        pager2.setVisibility(View.GONE);
                        //悬浮按钮继续显示
                        mFAB.setVisibility(View.VISIBLE);
                        return true;
                    }
                    return false;
                }
            });
        }
    };


    /**
     * 根据语音文字发送指令给服务器
     *
     * @param sayResult
     */
    private void sendInstruction2(String sayResult) {
        int deviceNum = SpeechUtil.getDeviceNum(sayResult);
        switch (deviceNum) {
            case 1:
                if (sayResult.contains("全部")) {
                    List<RoomItemBean> allLightList = getAllNamedDeviceslist("33001");
                    if (!allLightList.isEmpty()) {
                        //如果语音输入拿到的设备列表非空，查看是开还是关
                        if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                            //开
                            isSpeechOpera = true;  //这个标志位用来表示是语音操作，true时候会重新加载数据，保证操作完成后能够刷新界面
                            operateDevices(allLightList, "1", "灯");
                        } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                            isSpeechOpera = true;
                            operateDevices(allLightList, "0", "灯");
                        } else {
                            showTos(getString(R.string.no_operation));
                        }
                    }
                } else {
                    List<RoomItemBean> soundList = getSoundList(sayResult);
                    if (!soundList.isEmpty()) {
                        //如果语音输入拿到的设备列表非空，查看是开还是关
                        if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                            //开
                            isSpeechOpera = true;
                            operateDevices(soundList, "1", "灯");
                        } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                            isSpeechOpera = true;
                            operateDevices(soundList, "0", "灯");
                        } else {
                            showTos(getString(R.string.no_operation));
                        }
                    } else {
                        //没有找到设备
                        showTos(getResources().getString(R.string.no_devices));
                    }
                }
                break;
            case 2:
                List<RoomItemBean> allElectricityCurtainList = getSoundList(sayResult);
                if (!allElectricityCurtainList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                        //开
                        operateDevices(allElectricityCurtainList, "1", "窗帘");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        operateDevices(allElectricityCurtainList, "0", "窗帘");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.pause))) {
                        operateDevices(allElectricityCurtainList, "2", "窗帘");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
            case 3: //插座
                List<RoomItemBean> chazuoList = getSoundList(sayResult);
                if (!chazuoList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                        //开
                        isSpeechOpera = true;
                        operateDevices(chazuoList, "1", "插座");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        isSpeechOpera = true;
                        operateDevices(chazuoList, "0", "插座");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
            case 4: //红外人体感应：00-关闭 01-布防 02-触发
                List<RoomItemBean> infraredList = getAllNamedDeviceslist("33003");
                if (!infraredList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on)) || SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.deploy)) || SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.install))) {
                        //开
                        operateDevices(infraredList, "1", "红外");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        operateDevices(infraredList, "0", "红外");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.trigger))) {
                        operateDevices(infraredList, "2", "红外");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
           /* case 5: //空气质量检测仪33008 这个暂时不做 太复杂
                break;*/
            case 5://电视
                List<RoomItemBean> tvList = getSoundList(sayResult);
                if (!tvList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on)) || sayResult.contains(getResources().getString(R.string.turn_off))) {
                        //开 或者关
                        operateTV(tvList, "1", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.mute)) || sayResult.contains(getResources().getString(R.string.voice))) {
                        //声音 静音或不静音
                        operateTV(tvList, "2", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.menu))) {
                        //菜单
                        operateTV(tvList, "3", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.up))) {
                        //上键
                        operateTV(tvList, "4", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.down))) {
                        operateTV(tvList, "5", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.left))) {
                        operateTV(tvList, "6", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.right))) {
                        operateTV(tvList, "7", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.back))) {
                        operateTV(tvList, "8", "电视");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }

                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
            case 6://空调
                // TODO: 2017/8/7 中断
                List<RoomItemBean> airConditionList = getSoundList(sayResult);
                if (!airConditionList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (sayResult.contains(getResources().getString(R.string.turn_on)) || sayResult.contains(getResources().getString(R.string.turn_on))) {
                        //制冷 26度 中风
//                        operateAirCondition(airConditionList, "空调", "0", "26", "1");
                        operateAirCondition(airConditionList, "空调", "", "", "");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        operateAirCondition(airConditionList, "空调", "0", "0", "0");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
            default:
                showTos(getResources().getString(R.string.no_devices));
                break;
        }
    }


    /**
     * 根据语音文字发送指令给服务器
     *
     * @param sayResult
     */
    private void sendInstruction(String sayResult) {
        //1.拿到语音设备集合 实际上只有一个 因为设备名是唯一的
        List<RoomItemBean> soundList = getSoundList(sayResult);
        if(soundList.isEmpty()){
            showTos(getResources().getString(R.string.no_devices));
            return;
        }
        //2.拿到唯一的设备
        RoomItemBean device = soundList.get(0);
        //3.设备类型数字化
        String type = device.getDeviceType();
        //i:设备型号
        int deviceNum = SpeechUtil.getDeviceNumByDeviceType(type);
        switch (deviceNum) {
            case 1:
                if (sayResult.contains("全部")) {
                    List<RoomItemBean> allLightList = getAllNamedDeviceslist("33001");
                    if (!allLightList.isEmpty()) {
                        //如果语音输入拿到的设备列表非空，查看是开还是关
                        if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                            //开
                            isSpeechOpera = true;  //这个标志位用来表示是语音操作，true时候会重新加载数据，保证操作完成后能够刷新界面
                            operateDevices(allLightList, "1", "灯");
                        } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                            isSpeechOpera = true;
                            operateDevices(allLightList, "0", "灯");
                        } else {
                            showTos(getString(R.string.no_operation));
                        }
                    }
                } else {
                    if (!soundList.isEmpty()) {
                        //如果语音输入拿到的设备列表非空，查看是开还是关
                        if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                            //开
                            isSpeechOpera = true;
                            operateDevices(soundList, "1", "灯");
                        } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                            isSpeechOpera = true;
                            operateDevices(soundList, "0", "灯");
                        } else {
                            showTos(getString(R.string.no_operation));
                        }
                    } else {
                        //没有找到设备
                        showTos(getResources().getString(R.string.no_devices));
                    }
                }
                break;
            case 2:
                if (!soundList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                        //开
                        operateDevices(soundList, "1", "窗帘");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        operateDevices(soundList, "0", "窗帘");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.pause))) {
                        operateDevices(soundList, "2", "窗帘");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
            case 3: //插座
                if (!soundList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on))) {
                        //开
                        isSpeechOpera = true;
                        operateDevices(soundList, "1", "插座");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        isSpeechOpera = true;
                        operateDevices(soundList, "0", "插座");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
            case 4: //红外人体感应：00-关闭 01-布防 02-触发
                if (!soundList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on)) || SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.deploy)) || SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.install))) {
                        //开
                        operateDevices(soundList, "1", "红外");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        operateDevices(soundList, "0", "红外");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.trigger))) {
                        operateDevices(soundList, "2", "红外");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
           /* case 5: //空气质量检测仪33008 这个暂时不做 太复杂
                break;*/
            case 5://电视
                if (!soundList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_on)) || sayResult.contains(getResources().getString(R.string.turn_off))) {
                        //开 或者关
                        operateTV(soundList, "1", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.mute)) || sayResult.contains(getResources().getString(R.string.voice))) {
                        //声音 静音或不静音
                        operateTV(soundList, "2", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.menu))) {
                        //菜单
                        operateTV(soundList, "3", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.up))) {
                        //上键
                        operateTV(soundList, "4", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.down))) {
                        operateTV(soundList, "5", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.left))) {
                        operateTV(soundList, "6", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.right))) {
                        operateTV(soundList, "7", "电视");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.back))) {
                        operateTV(soundList, "8", "电视");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }

                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
            case 6://空调
                // TODO: 2017/8/7 中断
                if (!soundList.isEmpty()) {
                    //如果语音输入拿到的设备列表非空，查看是开还是关
                    if (sayResult.contains(getResources().getString(R.string.turn_on)) || sayResult.contains(getResources().getString(R.string.turn_on))) {
                        //制冷 26度 中风
//                        operateAirCondition(soundList, "空调", "0", "26", "1");
                        operateAirCondition(soundList, "空调", "", "", "");
                    } else if (SpeechUtil.isContainsKeyWords(sayResult, getResources().getString(R.string.turn_off))) {
                        operateAirCondition(soundList, "空调", "0", "0", "0");
                    } else {
                        showTos(getString(R.string.no_operation));
                    }
                } else {
                    showTos(getResources().getString(R.string.no_devices));
                }
                break;
            default:
                showTos(getResources().getString(R.string.no_devices));
                break;
        }
    }

    /**
     * 听写监听器2。
     */
    private RecognizerListener mRecognizerListener2 = new RecognizerListener() {

        private String mSpeechText;
        private TextView soundText;
        StringBuilder sb = new StringBuilder();  //todo 放在外面 暂定使用sbuilder

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTos("开始说话");
            isSaying = true;
        }

        @Override
        public void onError(SpeechError error) {
            showTos("您好像没有说话哦");
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            Gson gson = new Gson();
            Result result = gson.fromJson(results.getResultString(), Result.class);
            List<Result.WsBean> ws = result.getWs();
            for (Result.WsBean temp :
                    ws) {
                List<Result.WsBean.CwBean> cw = temp.getCw();
                String content = cw.get(0).getW();//这是说的语音内容
                sb.append(content);
            }
            if (isLast) {
                //该方法运行在主线程
                setNewPager();
                sb.delete(0, sb.length());
            }
        }


        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTos("正在说话中，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }

        private void setNewPager() {
            soundText = (TextView) getActivity().findViewById(R.id.ed_sound_text);
            soundText.setText("");
            mSpeechText = sb.toString();
            soundText.setText(mSpeechText);
        }
    };

    @Override
    public boolean onLongClick(View v) {
        //1.拿到当前控件坐标
        int before_move_x = (int) SPUtils.get(getActivity(), ConstantValue.BEFORE_MOVE_X, 0);
        int before_move_y = (int) SPUtils.get(getActivity(), ConstantValue.BEFORE_MOVE_Y, 0);
        //2.拿到保存的控件坐标
        int location_x = (int) SPUtils.get(getActivity(), ConstantValue.LOCATION_X, 0);
        int location_y = (int) SPUtils.get(getActivity(), ConstantValue.LOCATION_Y, 0);
        //3.比较是否有改变，发生改变表示经过了拖动 不执行点击事件
        if (before_move_x == location_x && before_move_y == location_y) {
            setParam4TTS();
            mIat.startListening(mRecognizerListener);
        }
        return true; //执行完长按 拦截点击
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startX = (int) event.getRawX();
                startY = (int) event.getRawY();
                //按下时保存控件坐标，供后面的点击事件做判断
                SPUtils.put(getActivity(), ConstantValue.BEFORE_MOVE_X, v.getLeft());
                SPUtils.put(getActivity(), ConstantValue.BEFORE_MOVE_Y, v.getTop());
//                Log.i("mFAB位置", "ontouch x: " + startX + ",y: " + startY);
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - startX;
                int dy = (int) event.getRawY() - startY; //偏移量
                int absX = Math.abs(dx);
                int absY = Math.abs(dy);
                if (absX >= mTouchSlop || absY >= mTouchSlop) {
                    int left = v.getLeft() + dx;
                    int top = v.getTop() + dy;
                    int right = v.getRight() + dx;
                    int bottom = v.getBottom() + dy; //原先位置+偏移量 = 新位置坐标

                    if (left < 0) {
                        left = 0;
                        right = left + v.getWidth();
                    }
                    if (right > screenWidth) {
                        right = screenWidth;
                        left = right - v.getWidth();
                    }
                    if (top < 0) {
                        top = 0;
                        bottom = top + v.getHeight();
                    }
                    //底部=屏幕高度-底部按钮高度-状态栏高度
                    if (bottom > (screenHeight - DensityUtils.dp2px(getActivity(), 55f) - ScreenUtils.getStatusHeight(getActivity()))) {
                        bottom = screenHeight - DensityUtils.dp2px(getActivity(), 55f) - ScreenUtils.getStatusHeight(getActivity());
                        top = bottom - v.getHeight();
                    }
                    v.layout(left, top, right, bottom); //设置view的位置
//                    Log.i("mFAB位置", "position = " + left + ", " + top + ", " + right + ", " + bottom);
                    startX = (int) event.getRawX();
                    startY = (int) event.getRawY(); //放置完后重新获取位置
                    //为了响应长按，只能在ACTION_MOVE中对控件位置进行实时保存
                    SPUtils.put(getActivity(), ConstantValue.LOCATION_X, v.getLeft());
                    SPUtils.put(getActivity(), ConstantValue.LOCATION_Y, v.getTop());
                }

                break;
            case MotionEvent.ACTION_UP:
                //将最后拖拽的位置定下来，否则页面刷新渲染后按钮会自动回到初始位置
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.width = v.getWidth();
                layoutParams.height = v.getHeight();
                layoutParams.leftMargin = v.getLeft();
                layoutParams.topMargin = v.getTop();
                mFAB.setLayoutParams(layoutParams);
                if (isSaying) {
                    mIat.stopListening();
                    showTos("停止说话");
                    isSaying = false;
                }
                break;
        }
        return false; //必须返回false 否则长按、点击事件无法执行
    }

    /**
     * 参数设置
     *
     * @param
     * @return
     */
    public void setParam4TTS() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        // 设置语言
        mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
        // 设置语言区域
        mIat.setParameter(SpeechConstant.ACCENT, "mandarin");


        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        mIat.setParameter("nunum","1");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, getActivity().getExternalCacheDir() + "sud.wav");
    }

    /**
     * 设置所有命名的设备集合
     * 例 打开所有灯  此时根据设备类型去拿到所有的设备
     *
     * @return 设备集合
     */
    public List<RoomItemBean> getAllNamedDeviceslist(String typeId) {
        //所有灯集合
        List<RoomItemBean> namedList = new ArrayList<>();
        if (!deviceList.isEmpty()) {
            for (RoomItemBean temp :
                    deviceList) {
                if (TextUtils.equals(temp.getDeviceType(), typeId)) {
                    namedList.add(temp);
                }
            }
            return namedList;
        }
        return null;
    }

    /**
     * 拿到语音输入灯的集合
     * 如果语音包含设备名称，把设备加入到新的集合
     * 例 开打厨房灯一  此时根据设备名字拿到关联的设备 返回的集合大概率是单个的设备 不排除同名情况
     *
     * @param soundInput 输入的语音
     * @return List<RoomItemBean> 命令的集合
     */
    public List<RoomItemBean> getSoundList(String soundInput) {
        List<RoomItemBean> namedList = new ArrayList<>();
        if ((deviceList!=null)&&(!deviceList.isEmpty())) {
            for (RoomItemBean temp :
                    deviceList) {
                if (soundInput.contains(temp.getDeviceName())) {
                    namedList.add(temp);
                }
            }
        }
        return namedList;

    }

    /**
     * 拿到相关房间灯的集合
     * 暂时不用
     *
     * @param room 灯的关联名字,需要设置包含常用房间名,比如客厅 厨房 卧室etc
     * @return List<RoomItemBean> 命令的集合
     */
    public List<RoomItemBean> getNamedLightList(String room) {
        List<RoomItemBean> namedLightList = new ArrayList<>();
        List<RoomItemBean> lightList = getAllNamedDeviceslist("33001");
        if (!lightList.isEmpty()) {
            for (RoomItemBean temp :
                    lightList) {
                if (temp.getDeviceName().contains(room)) {
                    namedLightList.add(temp);
                }
            }
            return namedLightList;
        }
        return null;
    }

    /**
     * 调用方法打开设备
     *
     * @param operateDevicesList 操作的设备集合
     * @param operation          操作的指令id 如果是灯需要加一个site位置01 ,11,10 etc 传入值不变
     */
    public void operateDevices(List<RoomItemBean> operateDevicesList, String operation, String operateType) {
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());
        }
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engine_id = sharedPreferencesManager.get("engine_id");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (operateType.equals("灯")) {
            //灯操作
            for (RoomItemBean temp :
                    operateDevicesList) {
                try {
                    String order_id = temp.getDeviceSite() + operation;
                    String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                    String sign = MD5Utils.MD5Encode(aesAccount
                            + temp.getDeviceId()
                            + "1"
                            + engine_id
                            + "DoOrders"
                            + order_id
                            + token
                            + URLUtils.MD5_SIGN, "");
                    XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL, handler);
                    xutilsHelper.add("account", aesAccount);
                    xutilsHelper.add("engine_id", engine_id);
                    xutilsHelper.add("device_id", temp.getDeviceId());
                    xutilsHelper.add("device_type", "1");
                    xutilsHelper.add("order_id", order_id);
                    xutilsHelper.add("token", token);
                    xutilsHelper.add("method", "DoOrders");
                    xutilsHelper.add("sign", sign);
                    xutilsHelper.sendPost(5, getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (operateType.equals("窗帘") || operateType.equals("插座") || operateType.equals("红外")) {
            for (RoomItemBean temp :
                    operateDevicesList) {
                try {
                    //可能存在问题  不是很了解需要传什么，貌似除了灯全部传0
                    String order_id = "0" + operation;
                    String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                    String sign = MD5Utils.MD5Encode(aesAccount
                            + temp.getDeviceId()
                            + "1"
                            + engine_id
                            + "DoOrders"
                            + order_id
                            + token
                            + URLUtils.MD5_SIGN, "");
                    XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL, handler);
                    xutilsHelper.add("account", aesAccount);
                    xutilsHelper.add("engine_id", engine_id);
                    xutilsHelper.add("device_id", temp.getDeviceId());
                    xutilsHelper.add("device_type", "1");
                    xutilsHelper.add("order_id", order_id);
                    xutilsHelper.add("token", token);
                    xutilsHelper.add("method", "DoOrders");
                    xutilsHelper.add("sign", sign);
                    xutilsHelper.sendPost(5, getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 电视的操作方法
     *
     * @param operateDevicesList 设备列表
     * @param operateType        设备类型
     * @param tvKeyId            电视按键
     */
    public void operateTV(List<RoomItemBean> operateDevicesList, String tvKeyId, String operateType) {
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());
        }
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engine_id = sharedPreferencesManager.get("engine_id");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (operateType.equals("电视")) {
            //灯操作
            for (RoomItemBean temp :
                    operateDevicesList) {
                try {
                    Utils.showWaitDialog(getString(R.string.loadtext_load), getActivity(), mWaitDialog);
                    String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                    String sign = MD5Utils.MD5Encode(aesAccount + temp.getDeviceId() + engine_id + tvKeyId + ConstantValue.REMOTE_CONTROL_TV + token + URLUtils.MD5_SIGN, "");
                    XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
                    xutilsHelper.add("account", aesAccount);
                    xutilsHelper.add("engine_id", engine_id);
                    xutilsHelper.add("controlled_device_id", temp.getDeviceId());
                    xutilsHelper.add("key_id", tvKeyId);
                    xutilsHelper.add("token", token);
                    xutilsHelper.add("method", ConstantValue.REMOTE_CONTROL_TV);
                    xutilsHelper.add("sign", sign);
                    xutilsHelper.sendPost(5, getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 空调的操作方法
     *
     * @param operateDevicesList 设备列表
     * @param operateType        设备类型
     * @param modeType           模式 0-制冷  1-制热  默认制冷
     * @param temperature        18摄氏度--30摄氏度(分度值为1摄氏度)
     * @param wind_speed         0-高 1-中 2-低
     */
    public void operateAirCondition(List<RoomItemBean> operateDevicesList, String operateType, String modeType, String temperature, String wind_speed) {
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());
        }
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engine_id = sharedPreferencesManager.get("engine_id");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (operateType.equals("空调")) {
            //灯操作
            for (RoomItemBean temp :
                    operateDevicesList) {
                try {
                    Utils.showWaitDialog(getString(R.string.loadtext_load), getActivity(), mWaitDialog);
                    String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                    String sign = MD5Utils.MD5Encode(aesAccount + temp.getDeviceId() + engine_id + ConstantValue.REMOTE_CONTROL_AC + modeType + temperature + token + wind_speed + URLUtils.MD5_SIGN, "");
                    XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
                    xutilsHelper.add("account", aesAccount);
                    xutilsHelper.add("engine_id", engine_id);
                    xutilsHelper.add("controlled_device_id", temp.getDeviceId());
                    xutilsHelper.add("mode_type", modeType);
                    xutilsHelper.add("temperature", temperature);
                    xutilsHelper.add("wind_speed", wind_speed);
                    xutilsHelper.add("token", token);
                    xutilsHelper.add("method", ConstantValue.REMOTE_CONTROL_AC);
                    xutilsHelper.add("sign", sign);
                    xutilsHelper.sendPost(5, getActivity());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void showTos(String str) {
        mToast.setText(str);
        mToast.show();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mIat) {
            // 退出时释放连接
            mIat.cancel();
            mIat.destroy();
        }
    }

    private void reslutShow() {
        showTos("操作成功");

        if (isSpeechOpera) {
            getDeviceList(1);
            isSpeechOpera = false;
        }
    }
}