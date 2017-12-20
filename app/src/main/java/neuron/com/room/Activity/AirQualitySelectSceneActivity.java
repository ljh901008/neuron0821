package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
 * Created by ljh on 2017/4/12. 设定空气质量检测仪的场景
 */
public class AirQualitySelectSceneActivity extends BaseActivity implements AdapterView.OnItemClickListener,View.OnClickListener{
    private String TAG = "AirQualitySelectSceneActivity";
    private ImageButton back;
    private Button queding_btn;
    private EditText value_ed;
    private ListView sceneListview;
    private List<AirQualityBean> list;
    private AirQualityAdapter adapter;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private String methodSceneList = "GetNSContextualModelList";
    private String methodSetScene = "SetNeuron";
    private String methodDeleteScene = "DelNeuronSetting";
    private int index = 14;
    private String value;
    private Intent intent;
    private String deviceId;
    private int tag;
    //提示，单位
    private TextView tishi_tv, unit_tv;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airqualityscene);
        init();
        setListener();
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        getSceneList();
    }

    private void init() {
        intent = getIntent();
        deviceId = intent.getStringExtra("deviceId");
        tag = intent.getIntExtra("tag", 10);
        //设置删除和警报
        list = new ArrayList<AirQualityBean>();
        AirQualityBean bean;
        bean = new AirQualityBean();
        bean.setSceneName("删除");
        bean.setSelect(false);
        list.add(bean);
        bean = new AirQualityBean();
        bean.setSceneName("消息警报");
        bean.setSelect(false);
        list.add(bean);
        back = (ImageButton) findViewById(R.id.airqualityscene_back_iv);
        queding_btn = (Button) findViewById(R.id.airqualityscene_fonfirm_btn);
        value_ed = (EditText) findViewById(R.id.airqualityscene_value_tv);
        sceneListview = (ListView) findViewById(R.id.airqualityscene_lv);
        tishi_tv = (TextView) findViewById(R.id.airqualityscene_tishiyu_tv);
        unit_tv = (TextView) findViewById(R.id.airqualityscene_unit_tv);
        value_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        switch (tag) {
            case 1://温度高于
                tishi_tv.setText("温度高于:");
                unit_tv.setText("℃");
                break;
            case 2://温度低于
                tishi_tv.setText("温度低于:");
                unit_tv.setText("℃");
                break;
            case 3://湿度高于
                tishi_tv.setText("湿度高于:");
                unit_tv.setText("%");
                break;
            case 4://湿度低于
                tishi_tv.setText("湿度低于:");
                unit_tv.setText("%");
                break;
            case 5://甲醛
                tishi_tv.setText("TVOC:");
                unit_tv.setText("mg/m³");
                break;
            case 6://PM2.5
                tishi_tv.setText("PM2.5值:");
                unit_tv.setText("ug/m³");
                break;
            default:
                break;
        }

    }
    private void setListener(){
        back.setOnClickListener(this);
        queding_btn.setOnClickListener(this);
        sceneListview.setOnItemClickListener(this);
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

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.airqualityscene_back_iv://返回键
                finish();
                break;
            case R.id.airqualityscene_fonfirm_btn://确定键
                value = value_ed.getText().toString().trim();
                if (list.size() != 0) {

                    if (index == 0) {//删除
                        switch (tag) {
                            case 1://温度高于
                                deleteScene("05");
                                break;
                            case 2://温度低于
                                deleteScene("06");
                                break;
                            case 3://湿度高于
                                deleteScene("07");
                                break;
                            case 4://湿度低于
                                deleteScene("08");
                                break;
                            case 5://甲醛
                                deleteScene("02");
                                break;
                            case 6://PM2.5
                                deleteScene("01");
                                break;
                            default:
                                break;
                        }
                    } else if (index == 1) {//设定警报
                        if (!TextUtils.isEmpty(value)) {
                            switch (tag) {
                                case 1://温度高于
                                    Utils.showDialog(AirQualitySelectSceneActivity.this, "只有甲醛和PM2.5可以设置警报");
                                    break;
                                case 2://温度低于
                                    Utils.showDialog(AirQualitySelectSceneActivity.this, "只有甲醛和PM2.5可以设置警报");
                                    break;
                                case 3://湿度高于
                                    Utils.showDialog(AirQualitySelectSceneActivity.this, "只有甲醛和PM2.5可以设置警报");
                                    break;
                                case 4://湿度低于
                                    Utils.showDialog(AirQualitySelectSceneActivity.this, "只有甲醛和PM2.5可以设置警报");
                                    break;
                                case 5://甲醛设警报
                                    //int d = Integer.parseInt(value);
                                    float d = Float.parseFloat(value);
                                    if (d > 0.01 && d < 9.99) {
                                        String s = String.valueOf(d * 100);
                                        updateScene("2", "", "02." + s.substring(0, s.lastIndexOf(".")));//服务器直接收整型的数据
                                    } else {
                                        Utils.showDialog(AirQualitySelectSceneActivity.this, "请输入0.01-9.99范围内的数值");
                                    }
                                    break;
                                case 6://pm2.5设置警报
                                    int i = Integer.parseInt(value);
                                    if (i > 0 && i < 999) {
                                        updateScene("2", "", "01." + value);
                                    } else {
                                        Utils.showDialog(AirQualitySelectSceneActivity.this, "请输入0-999范围内的数值");
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } else {
                            Utils.showDialog(AirQualitySelectSceneActivity.this, "请输入合法数据");
                        }
                    } else if (index == 14) {
                        Utils.showDialog(AirQualitySelectSceneActivity.this, "请选择场景");
                    } else {
                        if (!TextUtils.isEmpty(value)) {
                            switch (tag) {
                                case 1://温度高于
                                    updateScene("1", list.get(index).getSceneId(), "05." + value);
                                    break;
                                case 2://温度低于
                                    updateScene("1", list.get(index).getSceneId(), "06." + value);
                                    break;
                                case 3://湿度高于
                                    updateScene("1", list.get(index).getSceneId(), "07." + value);
                                    break;
                                case 4://湿度低于
                                    updateScene("1", list.get(index).getSceneId(), "08." + value);
                                    break;
                                case 5://甲醛
                                    float d = Float.parseFloat(value);
                                    if (d > 0.01 && d < 9.99) {
                                        String s = String.valueOf(d * 100);
                                        updateScene("1", list.get(index).getSceneId(), "04." + s.substring(0, s.lastIndexOf(".")));//服务器直接收整型的数据
                                    } else {
                                        Utils.showDialog(AirQualitySelectSceneActivity.this, "请输入0.01-9.99范围内的数值");
                                    }
                                    break;
                                case 6://pm2.5
                                    int i = Integer.parseInt(value);
                                    if (i > 0 && i < 999) {
                                        updateScene("1", list.get(index).getSceneId(), "03." + value);
                                    } else {
                                        Utils.showDialog(AirQualitySelectSceneActivity.this, "请输入0-999范围内的数值");
                                    }

                                    break;

                                default:
                                    break;
                            }
                        } else {
                            Utils.showDialog(AirQualitySelectSceneActivity.this, "请输入合法数据");
                        }
                    }
                }
                if (index != 14) {
                    intent.putExtra("value", value);
                    intent.putExtra("sceneName", list.get(index).getSceneName());
                    setResult(RESULT_OK, intent);
                }
                break;
            default:
                break;
        }

    }

    /**
     *  删除绑定的场景
     * @param deleteType
     */
    private void deleteScene(String deleteType){
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
            String sign = MD5Utils.MD5Encode(aesAccount + deleteType + engineId + methodDeleteScene + deviceId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", deviceId);
            xutilsHelper.add("condition", deleteType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodDeleteScene);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(3,this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    Log.e(TAG + "删除场景，警报",deleteResult);
                    try {
                        JSONObject jsonObject = new JSONObject(deleteResult);
                        if (jsonObject.getInt("status") == 9999) {
                            final AlertDialog builder = new AlertDialog.Builder(AirQualitySelectSceneActivity.this).create();
                            View view = View.inflate(AirQualitySelectSceneActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("删除成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                    builder.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Utils.showDialog(AirQualitySelectSceneActivity.this, jsonObject.getString("error"));
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
     *   设定空气质量检测仪 场景
     * @param setType
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
            Utils.showWaitDialog("加载中...", AirQualitySelectSceneActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contextual_model_id", sceneId);
            jsonObject.put("triggering_condition", triggering);
            Log.e(TAG + "空气设定场景", aesAccount + "," + engineId + "," + methodSetScene + "," + jsonObject.toString() + "," + deviceId + "," + setType + "," + token);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + methodSetScene + jsonObject.toString() + deviceId + setType + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("neuron_id", deviceId);
            xutilsHelper.add("set_type", setType);
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", methodSetScene);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(2,this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        if (jsonObject.getInt("status") == 9999) {
                            final AlertDialog builder = new AlertDialog.Builder(AirQualitySelectSceneActivity.this).create();
                            View view = View.inflate(AirQualitySelectSceneActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("设置成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    finish();
                                    builder.dismiss();
                                }
                            });
                            builder.show();
                        } else {
                            Utils.showDialog(AirQualitySelectSceneActivity.this, jsonObject.getString("error"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(AirQualitySelectSceneActivity.this,"网络不通", Toast.LENGTH_LONG).show();
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
                public void onSuccess(String s) {
                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONArray jsonMsg = jsonObject.getJSONArray("contextual_model_list");
                            int length = jsonMsg.length();
                            if (length > 0) {
                                AirQualityBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject j = jsonMsg.getJSONObject(i);
                                    bean = new AirQualityBean();
                                    bean.setSceneId(j.getString("contextual_model_id"));
                                    bean.setSceneName(j.getString("contextual_model_name"));
                                    bean.setSelect(false);
                                    list.add(bean);
                                }
                                adapter = new AirQualityAdapter(list, AirQualitySelectSceneActivity.this);
                                sceneListview.setAdapter(adapter);
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
