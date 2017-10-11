package neuron.com.room.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.RoomListAdapter;
import neuron.com.bean.SY_PopuWBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/11/15. 房间列表页面
 */
public class RoomListActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private String TAG = "RoomListActivity";
    private SharedPreferencesManager sharedPreferencesManager;
    private String account, token,engine_id;
    private String QUERYHOMELIST = "QueryRoomList";
    private String delRoomMethod = "DelRoom";
    private String updateRoomMethod = "UpdateRoom";
    private List<SY_PopuWBean> roomList;
    private RoomListAdapter adapter;

    private SwipeMenuListView room_lv;
    private Button addRoom_fl;
    private ImageView back_iv;
    private Intent intent;
    private int tag;
    private int isUpdate = 1;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1://房间列表
                    if (msg.what == 102) {
                        try {
                            String homeListResult = (String) msg.obj;
                            Log.e(TAG+"3333", homeListResult);
                            JSONObject jsonObject = new JSONObject(homeListResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONArray jsonArray = jsonObject.getJSONArray("room_list");
                                int length = jsonArray.length();
                                if (length > 0) {
                                    roomList = new ArrayList<SY_PopuWBean>();
                                    SY_PopuWBean sy_popuWBean;
                                    for (int i = 0; i < length; i++) {
                                        sy_popuWBean = new SY_PopuWBean();
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        String roomname = json.getString("room_name");
                                        sy_popuWBean.setHomeName(roomname);
                                        sy_popuWBean.setHomeId(json.getString("room_id"));
                                        roomList.add(sy_popuWBean);
                                    }
                                    Log.e(TAG + "roomList.size", String.valueOf(roomList.size()));
                                    if (isUpdate == 1) {
                                        adapter = new RoomListAdapter(RoomListActivity.this, roomList);
                                        room_lv.setAdapter(adapter);
                                        isUpdate = 2;
                                    } else {
                                        adapter.setList(roomList);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case 2://删除房间
                    if (msg.what == 102) {
                        String deleteResult = (String) msg.obj;
                        Log.e(TAG + "删除房间", deleteResult);
                        try {
                            JSONObject jsonObject = new JSONObject(deleteResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(RoomListActivity.this, jsonObject.getString("error"));
                            } else {
                                Utils.showDialog(RoomListActivity.this, "删除成功");
                                getHomeList();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 3://更新房间信息
                    if (msg.what == 102) {
                        String deleteResult = (String) msg.obj;
                        Log.e(TAG + "修改房间", deleteResult);
                        try {
                            JSONObject jsonObject = new JSONObject(deleteResult);
                            if (jsonObject.getInt("status") != 9999) {
                                Utils.showDialog(RoomListActivity.this, jsonObject.getString("error"));
                            } else {
                                Utils.showDialog(RoomListActivity.this, "修改成功");
                                getHomeList();
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
        setContentView(R.layout.roomlist);
        init();
        getHomeList();
    }

    private void init() {
        intent = getIntent();
        tag = intent.getIntExtra("type", 10);
        room_lv = (SwipeMenuListView) findViewById(R.id.roomlist_lv);
        addRoom_fl = (Button) findViewById(R.id.roomlist_add_btn);
        back_iv = (ImageView) findViewById(R.id.roomlist_back_iv);
        back_iv.setOnClickListener(this);
        addRoom_fl.setOnClickListener(this);
        room_lv.setOnItemClickListener(this);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(RoomListActivity.this);
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
        room_lv.setMenuCreator(creator);
        room_lv.setOnItemClickListener(this);
        room_lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        delRoom(roomList.get(position).getHomeId());
                        roomList.remove(position);
                        adapter.setList(roomList);
                        adapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
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
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engine_id = sharedPreferencesManager.get("engine_id");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + QUERYHOMELIST + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET, handler);
            xutilsHelper.add("account",aesAccount);
            xutilsHelper.add("engine_id",engine_id);
            xutilsHelper.add("token",token);
            xutilsHelper.add("method",QUERYHOMELIST);
            xutilsHelper.add("sign",sign);
            xutilsHelper.sendPost(1,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除房间
     * @param roomId
     */
    private void delRoom(String roomId){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
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
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(roomId);
            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + delRoomMethod + jsonArray.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET, handler);
            xutilsHelper.add("account",aesAccount);
            xutilsHelper.add("engine_id",engine_id);
            xutilsHelper.add("msg", jsonArray.toString());
            xutilsHelper.add("token",token);
            xutilsHelper.add("method",delRoomMethod);
            xutilsHelper.add("sign",sign);
            xutilsHelper.sendPost(2,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *   修改房间名称
     * @param roomId  房间id
     * @param roomName 房间名
     */
    private void updateRoom(String roomId,String roomName){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
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
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("room_id", roomId);
            jsonObject.put("room_name", roomName);
            jsonObject.put("desc", "");
            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + updateRoomMethod + jsonObject.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET, handler);
            xutilsHelper.add("account",aesAccount);
            xutilsHelper.add("engine_id",engine_id);
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token",token);
            xutilsHelper.add("method",updateRoomMethod);
            xutilsHelper.add("sign",sign);
            xutilsHelper.sendPost(3,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.roomlist_back_iv://返回键
                finish();
                break;
            case R.id.roomlist_add_btn:
                Intent intent = new Intent(RoomListActivity.this, AddRoomActivity.class);
                intent.putExtra("type", 2);
                startActivityForResult(intent, 100);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                if (data != null) {
                    if (data.getIntExtra("tag", 10) == 1) {
                        isUpdate = 2;
                        getHomeList();
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SY_PopuWBean bean = roomList.get(i);
        if (tag == 1) {//我的页面跳转到此页面
            changeRoomDialog(i);
        } else {
            intent.putExtra("roomName", bean.getHomeName());
            intent.putExtra("roomId", bean.getHomeId());
            setResult(RESULT_OK,intent);
            finish();
        }
    }

    /**
     * 修改房间名称
     */
    private void changeRoomDialog(final int position){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.dialog_input, null));
        dialog.show();
        dialog.getWindow().setContentView(R.layout.dialog_input);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_add);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_cancel);
        final EditText etContent = (EditText) dialog.findViewById(R.id.et_content);
        TextView titleName = (TextView) dialog.findViewById(R.id.name_tv);
        etContent.setHint("请输入房间名称");
        titleName.setText("修改房间名称");
        btnPositive.setText("确定");
        btnPositive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String str = etContent.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    etContent.setError("输入内如不能为空");
                } else {
                    updateRoom(roomList.get(position).getHomeId(), str);
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
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

}
