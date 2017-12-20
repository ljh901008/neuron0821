package neuron.com.scene.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/10.  场景编辑页面
 */
public class SceneEditActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "SceneEditActivity";
    private ImageButton back;
    private RelativeLayout timing_rll,sceneName_rll,sceneDeviceList_rll,swich_rll, sceneImg_rll;
    private TextView timing_tv,sceneDeviceList_tv,swich_tv,sceneName_tv;
    private ImageView sceneImg_iv;
    private Intent intent;
    private String sceneId,sceneName,sceneType;
    private int sceneImg;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token,engineId;
    private String sceneMethod = "GetContextualModelDetail";
    private String updateSceneMethod = "UpdateContextualModel";
    private String deviceList, swichList;
    private String sceneTime;
    private WaitDialog mWaitDialog;
    private int tag = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sceneedit);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
        setListener();
    }

    private void init() {
        intent = getIntent();
        sceneId = intent.getStringExtra("sceneId");
        tag = intent.getIntExtra("tag", 3);
        back = (ImageButton) findViewById(R.id.sceneedit_back_ibtn);
        timing_rll = (RelativeLayout) findViewById(R.id.sceneedit_timing_rll);
        sceneName_rll = (RelativeLayout) findViewById(R.id.sceneedit_scenename_rll);
        sceneDeviceList_rll = (RelativeLayout) findViewById(R.id.sceneedit_devicelist_rll);
        swich_rll = (RelativeLayout) findViewById(R.id.sceneedit_swich_rll);
        sceneImg_rll = (RelativeLayout) findViewById(R.id.sceneedit_sceneimg_rll);
        timing_tv = (TextView) findViewById(R.id.sceneedit_timing_tv);
        sceneName_tv = (TextView) findViewById(R.id.sceneedit_scenename_ed);
        sceneDeviceList_tv = (TextView) findViewById(R.id.sceneedit_devicelistnumber_tv);
        swich_tv = (TextView) findViewById(R.id.sceneedit_switch_tv);
        sceneImg_iv = (ImageView) findViewById(R.id.sceneedit_sceneimg_iv);
        getSceneData(sceneId, sceneMethod);
    }
    private void setListener(){
        timing_rll.setOnClickListener(this);
        sceneDeviceList_rll.setOnClickListener(this);
        swich_rll.setOnClickListener(this);
        sceneImg_rll.setOnClickListener(this);
        back.setOnClickListener(this);
        sceneName_rll.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sceneedit_back_ibtn://返回键
                if (tag == 1) {
                    Intent intent4 = new Intent(this, MainActivity.class);
                    startActivity(intent4);
                    finish();
                } else {
                    finish();
                }
                break;
            case R.id.sceneedit_timing_rll://设置时间
                Intent intent1 = new Intent(SceneEditActivity.this, SceneTimingActivity.class);
                intent1.putExtra("sceneTime", sceneTime);
                intent1.putExtra("sceneId", sceneId);
                startActivityForResult(intent1, 10);
                break;
            case R.id.sceneedit_devicelist_rll: //设备组合
                Intent intent3 = new Intent(SceneEditActivity.this, SceneBindDeviceListActivity.class);
                intent3.putExtra("sceneId", sceneId);
                intent3.putExtra("deviceList", deviceList);
                startActivityForResult(intent3,12);
                break;
            case R.id.sceneedit_swich_rll: //开关绑定
                Intent intent2 = new Intent(SceneEditActivity.this, SceneSwichListActivity.class);
                intent2.putExtra("sceneId", sceneId);
                intent2.putExtra("swichList", swichList);
                startActivityForResult(intent2, 11);
                break;
            case R.id.sceneedit_sceneimg_rll: //场景图标
                Intent intent = new Intent(SceneEditActivity.this, SceneImgListActivity.class);
                startActivityForResult(intent,14);
                break;
            case R.id.sceneedit_scenename_rll: //场景名称
                showDialog();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch(requestCode){
                case 10://设定时间
                    if (data != null) {
                        String iscf = data.getStringExtra("iscf");
                        if ("0".equals(iscf)) {
                            try {
                                String days = data.getStringExtra("days");
                                JSONArray jsonArray = new JSONArray(days);
                                StringBuffer stringBuffer = new StringBuffer();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    if ("1".equals(jsonArray.getString(i))) {
                                        stringBuffer.append("周一、");
                                    } else if ("2".equals(jsonArray.getString(i))) {
                                        stringBuffer.append("周二、");
                                    }else if ("3".equals(jsonArray.getString(i))) {
                                        stringBuffer.append("周三、");
                                    }else if ("4".equals(jsonArray.getString(i))) {
                                        stringBuffer.append("周四、");
                                    }else if ("5".equals(jsonArray.getString(i))) {
                                        stringBuffer.append("周五、");
                                    }else if ("6".equals(jsonArray.getString(i))) {
                                        stringBuffer.append("周六、");
                                    }else if ("0".equals(jsonArray.getString(i))) {
                                        stringBuffer.append("周日、");
                                    }
                                }
                                timing_tv.setText("重复("+stringBuffer.substring(0,stringBuffer.length()-1)+")");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else if ("2".equals(iscf)) {
                            timing_tv.setText("自定义" + data.getStringExtra("days"));
                        }
                    }
                    break;
               case 12://设备组合
                   getSceneData(sceneId, sceneMethod);
                    break;
                case 11://开关
                       /* Log.e(TAG + "开关长度", String.valueOf(SceneSwichListActivity.list.size()));
                        swich_tv.setText(String.valueOf(SceneSwichListActivity.list.size()));*/
                    getSceneData(sceneId, sceneMethod);
                    break;
                case 14: //选图标
                    if (data != null) {
                       int simg = data.getIntExtra("sceneImg",10);
                        switch(simg){
                            case 1:
                                sceneImg_iv.setImageResource(R.mipmap.scene_leave_not);
                                break;
                            case 2:
                                sceneImg_iv.setImageResource(R.mipmap.scene_meeting_not);
                                break;
                            case 3:
                                sceneImg_iv.setImageResource(R.mipmap.scene_movie_not);
                                break;
                            case 4:
                                sceneImg_iv.setImageResource(R.mipmap.scene_night_not);
                                break;
                            case 5:
                                sceneImg_iv.setImageResource(R.mipmap.scene_party_not);
                                break;
                            case 6:
                                sceneImg_iv.setImageResource(R.mipmap.scene_reading_not);
                                break;
                            case 7:
                                sceneImg_iv.setImageResource(R.mipmap.scene_get_up_not);
                                break;
                            case 0:
                                sceneImg_iv.setImageResource(R.mipmap.scene_back_not);
                                break;
                            default:
                                break;
                        }
                        updateScene(sceneId, sceneName, String.valueOf(simg));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     *      获取场景详情
     * @param sceneid
     * @param method
     */
    private void getSceneData(String sceneid,String method){
        setAccount();
        try {
            Utils.showWaitDialog("加载中...", SceneEditActivity.this,mWaitDialog);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneid + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("contextual_model_id", sceneid);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String sceneResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "场景详情", sceneResult);
                    try {
                        JSONObject jsonObject = new JSONObject(sceneResult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONObject sceneJs = jsonObject.getJSONObject("msg");
                            sceneId = sceneJs.getString("contextual_model_id");
                            sceneName = sceneJs.getString("contextual_model_name");
                            sceneType = sceneJs.getString("contextual_model_type");
                            sceneImg = sceneJs.getInt("contextual_model_img");
                            sceneTime = sceneJs.getString("timmer");

                            sceneName_tv.setText(sceneName);
                            JSONArray cdl = sceneJs.getJSONArray("controlled_device_list");
                            deviceList = cdl.toString();
                            sceneDeviceList_tv.setText(String.valueOf(cdl.length()));
                            JSONArray sList = sceneJs.getJSONArray("switch_list");
                            swichList = sList.toString();
                            swich_tv.setText(String.valueOf(sList.length()));
                            switch (sceneImg) {
                                case 1:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_leave_not);
                                    break;
                                case 2:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_meeting_not);
                                    break;
                                case 3:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_movie_not);
                                    break;
                                case 4:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_night_not);
                                    break;
                                case 5:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_party_not);
                                    break;
                                case 6:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_reading_not);
                                    break;
                                case 7:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_get_up_not);
                                    break;
                                case 0:
                                    sceneImg_iv.setImageResource(R.mipmap.scene_back_not);
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(SceneEditActivity.this, "网络不通", Toast.LENGTH_SHORT).show();
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
            sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        }
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
     *   修改场景名称和图片
     * @param sceneId
     * @param sceneName
     * @param sceneImg
     */
    private void updateScene(String sceneId,String sceneName,String sceneImg){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("contextual_model_id", sceneId);
            jsonObject.put("contextual_model_name", sceneName);
            jsonObject.put("contextual_model_img", sceneImg);
            jsonObject.put("desc", "");
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + updateSceneMethod + jsonObject.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("msg", jsonObject.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", updateSceneMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    try {
                        Log.e(TAG + "result", result);
                        JSONObject json = new JSONObject(result);
                        if (json.getInt("status") == 9999) {
                            Utils.showDialog(SceneEditActivity.this,"修改成功");
                        } else {
                            Utils.showDialog(SceneEditActivity.this, json.getString("error"));
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
    private void showDialog(){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.dialog_input, null));
        dialog.show();
        dialog.getWindow().setContentView(R.layout.dialog_input);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_add);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_cancel);
        TextView title = (TextView) dialog.findViewById(R.id.name_tv);
        title.setText("场景名称");
        btnPositive.setText("确定");
        final EditText etContent = (EditText) dialog.findViewById(R.id.et_content);
        etContent.setHint("请输入名称");
        btnPositive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String str = etContent.getText().toString().trim();
                if (TextUtils.isEmpty(str)) {
                    etContent.setError("输入内如不能为空");
                } else {
                    sceneName_tv.setText(str);
                    updateScene(sceneId, str, String.valueOf(sceneImg));
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
}
