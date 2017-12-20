package neuron.com.room.Activity;

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
 * Created by ljh on 2017/5/2.  红外人体感应设置场景
 */
public class SceneListActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private String TAG = "SceneListActivity";
    private ImageView back_iv;
    private Button button;
    private ListView listView;
    private Intent intent;
    private String neuronId,account,token,engineId;
    private String methodSetScene = "SetNeuron";
    private String methodSceneList = "GetNSContextualModelList";
    private List<AirQualityBean> list;
    private AirQualityAdapter adapter;
    private int condition;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private int index;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scenelist);
        init();
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getSceneList();
    }
    private void init(){
        intent = getIntent();
        neuronId = intent.getStringExtra("neuronId");
        condition = intent.getIntExtra("tag", 10);
        back_iv = (ImageView) findViewById(R.id.scenelist_back_iv);
        button = (Button) findViewById(R.id.scenelist_queding_bnt);
        listView = (ListView) findViewById(R.id.scenelist_listview);
        back_iv.setOnClickListener(this);
        button.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scenelist_back_iv://返回
                finish();
                break;
            case R.id.scenelist_queding_bnt://确定
                updateScene("1",list.get(index).getSceneId(),String.valueOf(condition));

                break;
            default:
                break;
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
        index = position;
    }
    /**
     *   设定红外人体感应 场景
     * @param setType   要设置的类型1 设定场景 2设定警报 3设定时间
     * @param sceneId
     * @param triggering
     */
    private void updateScene(String setType,String sceneId,String triggering){
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
            Utils.showWaitDialog("加载中...", SceneListActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contextual_model_id", sceneId);
            jsonObject.put("triggering_condition", triggering);

            Log.e(TAG + "数据", aesAccount + ";" + engineId + ";" + methodSetScene + ";" + jsonObject.toString() + ";" + neuronId + ";" + setType + ";" + token);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + methodSetScene + jsonObject.toString() + neuronId + setType + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", neuronId);
            xutilsHelper.add("set_type", setType);
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodSetScene);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String setResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "设定场景，警报", setResult);
                    try {
                        JSONObject jsonObject = new JSONObject(setResult);
                        if (jsonObject.getInt("status") == 9999) {
                               /* Utils.showDialogTwo(SceneListActivity.this, "设置成功", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        intent.putExtra("sceneName", list.get(index).getSceneName());
                                        setResult(RESULT_OK, intent);
                                        finish();
                                        dialogInterface.dismiss();
                                    }
                                });*/
                            final AlertDialog builder = new AlertDialog.Builder(SceneListActivity.this).create();
                            View view = View.inflate(SceneListActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("设置成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    intent.putExtra("sceneName", list.get(index).getSceneName());
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    builder.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Utils.showDialog(SceneListActivity.this, jsonObject.getString("error"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(SceneListActivity.this, "网络不通", Toast.LENGTH_LONG).show();
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
            engineId = sharedPreferencesManager.get("engine_id");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + methodSceneList + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("token", token);
            xutils.add("method", methodSceneList);
            xutils.add("sign", sign);
            //xutils.sendPost(1, this);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String sceneListResult) {
                    Log.e(TAG + "场景列表",sceneListResult);
                    try {
                        JSONObject jsonObject = new JSONObject(sceneListResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONArray jsonMsg = jsonObject.getJSONArray("contextual_model_list");
                            int length = jsonMsg.length();
                            if (length > 0) {
                                list = new ArrayList<AirQualityBean>();
                                AirQualityBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject j = jsonMsg.getJSONObject(i);
                                    bean = new AirQualityBean();
                                    bean.setSceneId(j.getString("contextual_model_id"));
                                    bean.setSceneName(j.getString("contextual_model_name"));
                                    bean.setSelect(false);
                                    list.add(bean);
                                }
                                adapter = new AirQualityAdapter(list, SceneListActivity.this);
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
}
