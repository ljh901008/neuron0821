package neuron.com.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.videogo.exception.BaseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.List;

import cn.nbhope.smarthome.smartlib.model.common.RequestModel;
import cn.nbhope.smarthome.smartlib.net.APIService;
import cn.nbhope.smarthome.smartlib.net.RetrofitFactory;
import neuron.com.adapter.DeviceSetFragmentAdapter;
import neuron.com.adapter.SY_PopuWAdapter;
import neuron.com.app.OgeApplication;
import neuron.com.bean.DeviceSetFragmentBean;
import neuron.com.bean.SY_PopuWBean;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;
import neuron.com.room.Activity.AddRoomActivity;
import neuron.com.scene.Activity.InfraredTransponderActivity;
import neuron.com.scene.Activity.SwichEditActivity;
import neuron.com.scene.Activity.UpdateDeviceDataActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ljh on 2017/4/21.  设备模块  设备列表页面
 */
public class DeviceSetFragment extends Fragment implements AdapterView.OnItemClickListener,View.OnClickListener{
    private String TAG = "DeviceSetFragment";
    private SwipeMenuListView listView;
    private RelativeLayout selectRoom_rll;
    private View view;
    private DeviceSetFragmentAdapter adapter;
    private List<DeviceSetFragmentBean> list;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private String account,token, engineId;
    private String deviceDataMethod = "QueryNeuronList";
    private String deleteMethod = "DelDevices";
    private String QUERYHOMELIST = "QueryRoomList";
    private String getRoomDeviceListMethod = "QueryRoomNeuronlist";
    private PullToRefreshScrollView pullToRefreshScrollView;
    private ScrollView sv;
    private String deviceSerial, neuronId;
    private List<SY_PopuWBean> listSY;
    private SY_PopuWAdapter sy_popuWAdapter;
    private PopupWindow popupWindow;
    private TextView roomName_tv;
    private boolean isFirst = true;
    private int mIndex = 200;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 3://删除摄像头
                    if (msg.what == 1) {
                        boolean b = (boolean) msg.obj;
                        if (b) {
                            deleteDevice(deviceSerial, neuronId);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.devicesetfragment, container, false);
        init();
        return view;
    }
    private void init(){
        listView = (SwipeMenuListView) view.findViewById(R.id.devicesetfragment_listview);
        selectRoom_rll = (RelativeLayout) view.findViewById(R.id.devicesetfragment_selectroom_rll);
        selectRoom_rll.setOnClickListener(this);
        roomName_tv = (TextView) view.findViewById(R.id.devicesetfragment_roomname_tv);
        pullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.devicesetfragment_pullsv);
        pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                new GetDataTask().execute();
            }
        });
        sv = pullToRefreshScrollView.getRefreshableView();
        //设置scrollview在最顶端
        sv.smoothScrollTo(0, 0);

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
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
        listView.setOnItemClickListener(this);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        deviceSerial = list.get(position).getDeviceSerial();
                        neuronId = list.get(position).getNeuronId();
                        String deviceType = list.get(position).getDeviceType();
                        Log.e(TAG + "deviceType：", deviceType);
                        if ("2049".equals(deviceType)) {//摄像头
                            deleteCamera(deviceSerial);
                            mIndex = position;
                            //deleteDevice(deviceSerial, neuronId);
                        } else if ("8193".equals(deviceType)){//音响
                            APIService service = RetrofitFactory.getInstance().createRetrofit(URLUtils.BASE_URL).create(APIService.class);
                            RequestModel requestModel = RequestModel.getInstance();
                            service.deleteManager(requestModel.generateDeleteManager(list.get(position).getDeviceSerial()))
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(cmdRequest->{
                                        Log.e("删除结果", cmdRequest.getResult());
                                        if (cmdRequest.getResult().equals("Success")) {
                                            deleteDevice(deviceSerial,neuronId);
                                        }
                                    });

                        }else {//普通节点设备
                            deleteDevice(deviceSerial, neuronId);
                        }
                        mIndex = position;
                        break;
                    default:
                        break;
                }
            }
        });
        deviceList();
    }
    /**
     * 获取节点设备列表
     */
    private void deviceList(){
        //Utils.showWaitDialog("加载中...", getActivity());
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + deviceDataMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", deviceDataMethod);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(1, getActivity());
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deviceData) {
                    Log.e(TAG + "节点设备列表",deviceData);
                    try {
                        JSONObject jsonObject = new JSONObject(deviceData);
                        int status = jsonObject.getInt("status");
                        if (status == 9999) {
                            JSONArray jsonArray = jsonObject.getJSONArray("neuron_list");
                            int length = jsonArray.length();
                            if (length > 0) {
                                list = new ArrayList<DeviceSetFragmentBean>();
                                DeviceSetFragmentBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject deviceJson = jsonArray.getJSONObject(i);
                                    bean = new DeviceSetFragmentBean();
                                    bean.setNeuronId(deviceJson.getString("neuron_id"));
                                    bean.setDeviceSerial(deviceJson.getString("serial_number"));
                                    bean.setRoomId(deviceJson.getString("room_id"));
                                    bean.setRoomName(deviceJson.getString("room_name"));
                                    //bean.setDeviceStatus(deviceJson.getString("status"));
                                    bean.setDeviceName(deviceJson.getString("neuron_name"));
                                    bean.setDeviceType(deviceJson.getString("device_type_id"));
                                    list.add(bean);
                                }
                                if (isFirst) {
                                    adapter = new DeviceSetFragmentAdapter(getActivity(), list);
                                    listView.setAdapter(adapter);
                                    isFirst = false;
                                } else {
                                    adapter.setList(list);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }else if (status == 1000 || status == 1001) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
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
            sharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());
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
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String deviceType = list.get(i).getDeviceType();
        if ("258".equals(deviceType) || "257".equals(deviceType)) {//单键开关
            Intent intent = new Intent(getActivity(), SwichEditActivity.class);
            intent.putExtra("deviceType", deviceType);
            intent.putExtra("neuronId", list.get(i).getNeuronId());
            startActivity(intent);
        } else if ("259".equals(deviceType)||"260".equals(deviceType)||"261".equals(deviceType)) {//双键开关
            Intent intent1 = new Intent(getActivity(), SwichEditActivity.class);
            intent1.putExtra("deviceType", deviceType);
            intent1.putExtra("neuronId", list.get(i).getNeuronId());
            startActivity(intent1);
        } else if ("262".equals(deviceType)||"263".equals(deviceType)||"264".equals(deviceType)||"265".equals(deviceType)) {//三键开关
            Intent intent2 = new Intent(getActivity(), SwichEditActivity.class);
            intent2.putExtra("deviceType", deviceType);
            intent2.putExtra("neuronId", list.get(i).getNeuronId());
            startActivity(intent2);
        } else if ("1025".equals(deviceType)) {//红外转发
            Intent intent3 = new Intent(getActivity(), InfraredTransponderActivity.class);
            intent3.putExtra("deviceType", deviceType);
            intent3.putExtra("neuronId", list.get(i).getNeuronId());
            startActivity(intent3);
        } else {//剩余节点设备
            Intent intent4 = new Intent(getActivity(), UpdateDeviceDataActivity.class);
            intent4.putExtra("neuronId", list.get(i).getNeuronId());
            intent4.putExtra("deviceName", list.get(i).getDeviceName());
            intent4.putExtra("roomId", list.get(i).getRoomId());
            intent4.putExtra("roomName", list.get(i).getRoomName());
            intent4.putExtra("deviceType", deviceType);
            startActivityForResult(intent4, Activity.RESULT_FIRST_USER);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.devicesetfragment_selectroom_rll:
                getHomeList();
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Activity.RESULT_FIRST_USER) {
                if (data != null) {
                    int tag = data.getIntExtra("tag", 10);
                    Log.e(TAG + "data", String.valueOf(tag));
                    if (tag == 1) {
                        deviceList();//刷新列表
                    }
                }
            }
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
            /*try {
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
            deviceList();
            pullToRefreshScrollView.onRefreshComplete();
        }
    }

    /**
     *   删除节点设备
     * @param serial    节点设备序列号
     * @param neuronId  节点设备Id
     */
    private void deleteDevice(String serial, String neuronId) {
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("device_id", neuronId);
            jsonObject.put("serial_number", serial);
            JSONArray deviceList = new JSONArray();
            deviceList.put(jsonObject);
            String sign = MD5Utils.MD5Encode(aesAccount + deviceList.toString() + engineId + deleteMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("device_list", deviceList.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", deleteMethod);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(2, getActivity());
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    Log.e(TAG + "删除节点设备", deleteResult);
                    try {
                        JSONObject jsonObject = new JSONObject(deleteResult);
                        int status1 = jsonObject.getInt("status");
                        if (status1 != 9999) {
                            Utils.showDialog(getActivity(), jsonObject.getString("error"));
                        } else if (status1 == 1000 || status1 == 1001) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
                        } else {
                            Utils.showDialog(getActivity(), "删除成功");
                            list.remove(mIndex);
                            adapter.setList(list);
                            adapter.notifyDataSetChanged();
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
     * 获取房间列表
     */
    private void getHomeList() {
       setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + QUERYHOMELIST + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", QUERYHOMELIST);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(6, getActivity());
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String roomResult) {
                    Log.e(TAG + "房间列表", roomResult);
                    try {
                        JSONObject jsonObject = new JSONObject(roomResult);
                        int status2 = jsonObject.getInt("status");
                        if (status2 == 9999) {
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
                                    showPopuWindow(0);
                                } else {
                                    sy_popuWAdapter.setList(listSY);
                                    showPopuWindow(1);
                                }
                            }
                        }else if (status2 == 1000 || status2 == 1001) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
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
    private void deleteCamera(final String cameraSerial){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Utils.detectionEZToken(getActivity());
                    boolean b = OgeApplication.getOpenSDK().deleteDevice(cameraSerial);
                    Message msg = handler.obtainMessage();
                    msg.obj = b;
                    msg.arg1 = 3;
                    msg.what = 1;
                    handler.sendMessage(msg);

                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    private void getRoomDeviceList(String roomId){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + getRoomDeviceListMethod + roomId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("room_id", roomId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", getRoomDeviceListMethod);
            xutilsHelper.add("sign", sign);
           // xutilsHelper.sendPost(4, getActivity());
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        int status3 = jsonObject.getInt("status");
                        if (status3 == 9999) {
                            JSONArray jsonArray = jsonObject.getJSONArray("neuron_list");
                            int length = jsonArray.length();
                            if (length > 0) {
                                list = new ArrayList<DeviceSetFragmentBean>();
                                DeviceSetFragmentBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject deviceJson = jsonArray.getJSONObject(i);
                                    bean = new DeviceSetFragmentBean();
                                    bean.setNeuronId(deviceJson.getString("neuron_id"));
                                    bean.setDeviceSerial(deviceJson.getString("serial_number"));
                                    bean.setRoomId(deviceJson.getString("room_id"));
                                    bean.setRoomName(deviceJson.getString("room_name"));
                                    bean.setDeviceStatus(deviceJson.getString("status"));
                                    bean.setDeviceName(deviceJson.getString("neuron_name"));
                                    bean.setDeviceType(deviceJson.getString("device_type_id"));
                                    list.add(bean);
                                }
                                adapter.setList(list);
                                adapter.notifyDataSetChanged();
                            }
                        }else if (status3 == 1000 || status3 == 1001) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
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
    private ListView popuW_lv;
    private RelativeLayout popuW_fl;
    private RelativeLayout quanBu;

    /**
     * 根据房间显示设备列表
     */
    private void showPopuWindow(int tag) {
        if (popupWindow == null) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.shouye_popuwindow, null);
            popuW_lv = (ListView) view.findViewById(R.id.shouye_popuwindow_lv);
            popuW_fl = (RelativeLayout) view.findViewById(R.id.shouye_popuwindow_addhome_fl);
            quanBu = (RelativeLayout) view.findViewById(R.id.shouye_popuwindow_quanbu_rll);
            if (tag == 0) {
                popuW_lv.setAdapter(sy_popuWAdapter);
            } else {
                sy_popuWAdapter.notifyDataSetChanged();
            }
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
        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        //在选择帐号控件下方弹出
        popupWindow.showAsDropDown(roomName_tv, 0, 0);
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
                getRoomDeviceList(listSY.get(position).getHomeId());
                listSY.clear();
                popupWindow.dismiss();
            }
        });
        quanBu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deviceList();
                roomName_tv.setText("全部房间");
                popupWindow.dismiss();
            }
        });
    }
}
