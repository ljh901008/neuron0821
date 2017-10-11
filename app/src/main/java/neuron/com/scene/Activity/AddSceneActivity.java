package neuron.com.scene.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/11.
 */
public class AddSceneActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AddSceneActivity";
    private ImageButton back;
    private EditText sceneName_ed;
    private RelativeLayout selectImg_rll;
    private ImageView sceneImg_iv;

    private Button confirm_btn;
    private SharedPreferencesManager sharedPreferencesManager;
    private String neuronId,account,token,engineId;
    private String addShareMethod = "AddContextualModel";
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1:
                    if (msg.what == 102) {
                        try {
                            String result = (String) msg.obj;
                            Log.e(TAG + "result", result);
                            JSONObject json = new JSONObject(result);
                            if (json.getInt("status") == 9999) {
                                Toast.makeText(AddSceneActivity.this, "添加成功", Toast.LENGTH_LONG).show();
                                JSONObject jsmsg = json.getJSONObject("msg");
                                String sceneId = jsmsg.getString("contextual_model_id");
                                Intent intent = new Intent(AddSceneActivity.this, SceneEditActivity.class);
                                intent.putExtra("tag", 1);
                                intent.putExtra("sceneId", sceneId);
                                startActivity(intent);
                            } else {
                                Utils.showDialog(AddSceneActivity.this, json.getString("error"));
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
    private String sceneImg,sceneName;
    private int simg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addscene);
        init();
        setListener();
    }

    private void init() {
        back = (ImageButton) findViewById(R.id.addscene_back_iv);
        sceneName_ed = (EditText) findViewById(R.id.addscene_sharecode_ed);
        selectImg_rll = (RelativeLayout) findViewById(R.id.addscene_selectImg_rll);
        confirm_btn = (Button) findViewById(R.id.addscene_confirm_btn);
        sceneImg_iv = (ImageView) findViewById(R.id.addscene_sceneImg_iv);
        sceneName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }
    private void setListener(){
        back.setOnClickListener(this);
        selectImg_rll.setOnClickListener(this);
        confirm_btn.setOnClickListener(this);
    }

    /**
     * 添加情景模式
     * @param sceneImg   场景图标id
     * @param sceneName  场景名称
     * @param desc   场景描述
     */
    private void addScene(String sceneImg,String sceneName,String desc){
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
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneImg + sceneName + desc + engineId + addShareMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL, handler);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("contextual_model_name", sceneName);
            xutils.add("contextual_model_img", sceneImg);
            xutils.add("desc", desc);
            xutils.add("token", token);
            xutils.add("method", addShareMethod);
            xutils.add("sign", sign);
            xutils.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.addscene_back_iv://返回
                Intent intent1 = new Intent(AddSceneActivity.this, MainActivity.class);
                startActivity(intent1);
                finish();
                break;
            case R.id.addscene_confirm_btn://确定
                sceneName = sceneName_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(sceneName)) {
                    if (TextUtils.isEmpty(sceneImg)) {
                        addScene("0", sceneName, "");
                    } else {
                        addScene(sceneImg, sceneName, "");
                    }
                }
                break;
            case R.id.addscene_selectImg_rll://选图标
                Intent intent = new Intent(this, SceneImgListActivity.class);
                startActivityForResult(intent, 10);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                if (data != null) {
                    simg = data.getIntExtra("sceneImg",10);
                    sceneImg = String.valueOf(simg);
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
                }

            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(AddSceneActivity.this, MainActivity.class);
            startActivity(intent);
            return false;
        }
        return super.onKeyDown(keyCode, event);

    }
}
