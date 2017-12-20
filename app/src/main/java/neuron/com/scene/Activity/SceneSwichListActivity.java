package neuron.com.scene.Activity;

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

import neuron.com.adapter.SceneSwichListAdapter;
import neuron.com.bean.SwichBean;
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
 * Created by ljh on 2017/5/10.  场景  已  绑定开关列表
 */
public class SceneSwichListActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private String TAG = "SceneSwichListActivity";
    private ImageView back_iv;
    private Button button;
    private SwipeMenuListView listView;
    private Intent intent;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token,engineId;
    private List<SwichBean> list;
    private SceneSwichListAdapter adapter;
    private String deleteSwichMethod = "DelConditionTriggering";
    private String sceneId,swichList;
    private int indexing = 500;
    private TextView warning_tv,title;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sceneswichlist);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
        setListener();
    }
    private void init() {
        intent = getIntent();
        sceneId = intent.getStringExtra("sceneId");
        swichList = intent.getStringExtra("swichList");
        back_iv = (ImageView) findViewById(R.id.sceneswichlist_back_iv);
        button = (Button) findViewById(R.id.sceneswichlist_queding_bnt);
        warning_tv = (TextView) findViewById(R.id.sceneswichlist_tishi_tv);
        title = (TextView) findViewById(R.id.sceneswichlist_title_tv);
        listView = (SwipeMenuListView) findViewById(R.id.sceneswichlist_listview);
        title.setText("已绑定开关列表");
        warning_tv.setText("您没有绑定开关。");
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(SceneSwichListActivity.this);
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
                        delSwich(list.get(position).getSwichId(),list.get(position).getSwichKeyId(),deleteSwichMethod);
                        indexing = position;
                        break;
                    default:
                        break;
                }
            }
        });
        getSwichList(swichList);
    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        button.setOnClickListener(this);
        listView.setOnItemClickListener(this);
    }

    /**
     *  解析开关列表
     * @param swichList 开关列表
     */
    private void getSwichList(String swichList){
        try {
                JSONArray jsonArray = new JSONArray(swichList);
                int length = jsonArray.length();
                if (length > 0) {

                    list = new ArrayList<SwichBean>();
                    SwichBean bean;
                    for (int i = 0; i < length; i++) {
                        JSONObject msgjson = jsonArray.getJSONObject(i);
                        bean = new SwichBean();
                        bean.setSwichId(msgjson.getString("neuron_id"));
                        bean.setSwichKeyName(msgjson.getString("neuron_name")+msgjson.getString("triggering_condition_desc"));
                        bean.setSwichKeyId(msgjson.getString("triggering_condition"));
                        bean.setSwichRoom(msgjson.getString("room_name"));
                        bean.setSelect(false);
                        list.add(bean);
                    }
                    adapter = new SceneSwichListAdapter(SceneSwichListActivity.this, list);
                    listView.setAdapter(adapter);
                } else {
                    warning_tv.setVisibility(View.VISIBLE);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                    warning_tv.setVisibility(View.GONE);
                    String swichList = data.getStringExtra("swichlist");
                    if (list != null) {
                        try {
                            JSONArray jsonArray = new JSONArray(swichList);
                            SwichBean bean;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                bean = new SwichBean();
                                bean.setSwichId(jsonObject.getString("neuron_id"));
                                bean.setSwichKeyName(jsonObject.getString("key_name"));
                                bean.setSwichKeyId(jsonObject.getString("key_id"));
                                bean.setSwichRoom(jsonObject.getString("room_name"));
                                bean.setSelect(false);
                                list.add(bean);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        adapter.setList(list);
                        adapter.notifyDataSetChanged();
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(swichList);
                            list = new ArrayList<SwichBean>();
                            SwichBean bean;
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                bean = new SwichBean();
                                bean.setSwichId(jsonObject.getString("neuron_id"));
                                bean.setSwichKeyName(jsonObject.getString("key_name"));
                                bean.setSwichKeyId(jsonObject.getString("key_id"));
                                bean.setSwichRoom(jsonObject.getString("room_name"));
                                bean.setSelect(false);
                                list.add(bean);
                            }
                            adapter = new SceneSwichListAdapter(SceneSwichListActivity.this, list);
                            listView.setAdapter(adapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
            }
        }
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
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sceneswichlist_back_iv://返回
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.sceneswichlist_queding_bnt://确定
                Intent intent1 = new Intent(SceneSwichListActivity.this, SceneBindSwichListActivity.class);
                intent1.putExtra("sceneId", sceneId);
                startActivityForResult(intent1, 10);
                break;
            default:
            break;
        }
    }


    /**
     *       删除场景绑定开关
     * @param neuronId   节点设备Id
     * @param swichKeyId  开关键位
     * @param Method  方法名
     *
     */
    private void delSwich(String neuronId,String swichKeyId,String Method){
        setAccount();
        Utils.showWaitDialog("加载中", SceneSwichListActivity.this, mWaitDialog);
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contextual_model_id", sceneId);
            jsonObject.put("neuron_id", neuronId);
            jsonObject.put("key_id", swichKeyId);
            Log.e(TAG + "删除开关", jsonObject.toString());
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + Method + jsonObject.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("msg", jsonObject.toString());
            xutils.add("token", token);
            xutils.add("method", Method);
            xutils.add("sign", sign);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    try {
                        Log.e(TAG + "删除开关", deleteResult);
                        JSONObject json = new JSONObject(deleteResult);
                        if (json.getInt("status") == 9999) {
                            Utils.showDialog(SceneSwichListActivity.this, "删除成功");
                            list.remove(indexing);
                            adapter.setList(list);
                            adapter.notifyDataSetChanged();
                        } else {
                            Utils.showDialog(SceneSwichListActivity.this, json.getString("error"));
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
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
       /* if (list.get(position).isSelect()) {
            list.get(position).setSelect(false);
        } else {
            list.get(position).setSelect(true);
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
        index = position;*/
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
