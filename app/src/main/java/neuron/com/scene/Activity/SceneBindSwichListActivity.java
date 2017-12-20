package neuron.com.scene.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
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
 * Created by ljh on 2017/5/17.  场景  可  绑定开关列表
 */
public class SceneBindSwichListActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private String TAG = "SceneBindSwichListActivity";
    private ImageView back_iv;
    private Button button;
    private ListView listView;

    private Intent intent;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token,engineId;
    private List<SwichBean> list;
    private SceneSwichListAdapter adapter;
    private String sceneId;
    private String swichMethod = "GetSwitchList";
    private String bindSwichMethod = "AddConditionTriggering";
    private TextView title_tv;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scenebindswichlist);

        init();
        setListener();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getSwichList(sceneId);
    }
    private void init() {
        intent = getIntent();
        sceneId = intent.getStringExtra("sceneId");
        back_iv = (ImageView) findViewById(R.id.sceneswichlist_back_iv);
        button = (Button) findViewById(R.id.sceneswichlist_queding_bnt);
        title_tv = (TextView) findViewById(R.id.sceneswichlist_title_tv);
        button.setText("确定");
        title_tv.setText("开关列表");
        listView = (ListView) findViewById(R.id.sceneswichlist_lv);
    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        button.setOnClickListener(this);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sceneswichlist_back_iv://返回
                finish();
                break;
            case R.id.sceneswichlist_queding_bnt://确定
                JSONArray jsonA = new JSONArray();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).isSelect()) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("contextual_model_id", sceneId);
                            jsonObject.put("neuron_id", list.get(i).getSwichId());
                            jsonObject.put("key_id", list.get(i).getSwichKeyId());
                            jsonA.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
                if (jsonA.length() > 0) {
                    bindSwich(bindSwichMethod, jsonA.toString());
                } else {
                    Utils.showDialog(SceneBindSwichListActivity.this,"请选择要绑定的开关");
                }
                break;
            default:
                break;
        }
    }
    /**
     * 获取没有被绑定的开关列表
     */
    private void getSwichList(String sceneId){
        setAccount();
        try {
            Utils.showWaitDialog("加载中...", SceneBindSwichListActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + engineId + swichMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("contextual_model_id", sceneId);
            xutils.add("token", token);
            xutils.add("method", swichMethod);
            xutils.add("sign", sign);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String swichResult) {
                    Log.e(TAG + "开关列表", swichResult);
                    Utils.dismissWaitDialog(mWaitDialog);
                    try {
                        JSONObject jsonObject = new JSONObject(swichResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONArray jsonArray = jsonObject.getJSONArray("msg");
                            int length = jsonArray.length();
                            if (length > 0) {
                                list = new ArrayList<SwichBean>();
                                SwichBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject msgjson = jsonArray.getJSONObject(i);
                                    bean = new SwichBean();
                                    bean.setSwichId(msgjson.getString("neuron_id"));
                                    bean.setSwichKeyName(msgjson.getString("key_name"));
                                    bean.setSwichKeyId(msgjson.getString("key_id"));
                                    if (msgjson.has("room_name")) {
                                        bean.setSwichRoom(msgjson.getString("room_name"));
                                    }
                                    bean.setSelect(false);
                                    list.add(bean);
                                }
                                adapter = new SceneSwichListAdapter(SceneBindSwichListActivity.this, list);
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
     *   添加场景开关
     * @param Method
     *
     */
    private void bindSwich(String Method,String swichList){
        setAccount();
        Utils.showWaitDialog("加载中...",SceneBindSwichListActivity.this,mWaitDialog);
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);

            Log.e(TAG + "绑定开关",swichList.toString());
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + Method + swichList + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("msg", swichList);
            xutils.add("token", token);
            xutils.add("method", Method);
            xutils.add("sign", sign);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    try {
                        Log.e(TAG + "绑定开关", result);
                        JSONObject json = new JSONObject(result);
                        if (json.getInt("status") == 9999) {
                            final JSONArray jsonArray = new JSONArray();
                            for (int i = 0; i < list.size(); i++) {
                                if (list.get(i).isSelect()) {
                                    JSONObject jsonObject = new JSONObject();
                                    try {
                                        jsonObject.put("key_name", list.get(i).getSwichKeyName());
                                        jsonObject.put("neuron_id", list.get(i).getSwichId());
                                        jsonObject.put("key_id", list.get(i).getSwichKeyId());
                                        jsonObject.put("room_name", list.get(i).getSwichRoom());
                                        jsonArray.put(jsonObject);
                                        final AlertDialog builder = new AlertDialog.Builder(SceneBindSwichListActivity.this).create();
                                        View view = View.inflate(SceneBindSwichListActivity.this, R.layout.dialog_textview, null);
                                        TextView title = (TextView) view.findViewById(R.id.textView1);
                                        Button button = (Button) view.findViewById(R.id.button1);
                                        title.setText("绑定成功");
                                        builder.setView(view);
                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                intent.putExtra("swichlist", jsonArray.toString());
                                                setResult(RESULT_OK, intent);
                                                builder.dismiss();
                                                SceneBindSwichListActivity.this.finish();
                                            }
                                        });
                                        builder.show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        } else {
                            Utils.showDialog(SceneBindSwichListActivity.this, json.getString("error"));
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
        if (list.get(position).isSelect()) {
            list.get(position).setSelect(false);
        } else {
            list.get(position).setSelect(true);
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }
}
