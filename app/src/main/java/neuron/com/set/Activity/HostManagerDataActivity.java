package neuron.com.set.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import neuron.com.adapter.ChildAccountManagerAdapter;
import neuron.com.bean.AccountDataBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/3/9.修改控制主机名称
 */
public class HostManagerDataActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "HostManagerDataActivity";
    private ImageView back_iv;
    private TextView titleName,liebiao_tv;
    private ImageButton hostMgeNameDelete_ibtn;
    private Button save_btn;
    private EditText hostMgeName_ed;
    private SwipeMenuListView accountListView;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private Intent intent;
    private String engineName,engineId, engineSerial;
    private String account,token;
    private String shaareAccountMethod = "ShowEngineDetail";
    private String updateMethod = "UpdateDevices";
    private String deleteShareAccountMethod = "RecycleSharedAccounts";
    private List<AccountDataBean> shaerAccountlist;
    private ChildAccountManagerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hostmanagerdata);
        init();
        setListener();
        getHostMagShareList();
    }

    private void init() {
        intent = getIntent();
        engineName = intent.getStringExtra("hostMagName");
        engineId = intent.getStringExtra("hostMagId");
        engineSerial = intent.getStringExtra("hostMagSerial");

        back_iv = (ImageView) findViewById(R.id.hostmanagerdata_back_iv);
        titleName = (TextView) findViewById(R.id.hostmanagerdata_room_name_tv);
        liebiao_tv = (TextView) findViewById(R.id.hostmanagerdata_liebiao_tv);
        save_btn = (Button) findViewById(R.id.hostmanagerdata_finish_btn);
        hostMgeNameDelete_ibtn = (ImageButton) findViewById(R.id.hostmanagerdata_deleteed_ibtn);
        hostMgeName_ed = (EditText) findViewById(R.id.hostmanagerdata_hostname_ed);
        accountListView = (SwipeMenuListView) findViewById(R.id.hostmanagerdata_listview_lv);
        titleName.setText(engineName);
        hostMgeName_ed.setText(engineName);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(HostManagerDataActivity.this);
                // 设置菜单的背景
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,0x3F, 0x25)));
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
        accountListView.setMenuCreator(creator);
        accountListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        shaerAccountlist.remove(position);

                        adapter.notifyDataSetChanged();
                        deleteShareAccount(position);
                        break;
                    default:
                        break;
                }
            }
        });

    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        save_btn.setOnClickListener(this);
        hostMgeNameDelete_ibtn.setOnClickListener(this);
    }

    /**
     * 获取分享帐号的列表
     */
    private void getHostMagShareList(){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + shaareAccountMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.SHAREHOSMANAGER);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("method", shaareAccountMethod);
            xutilsHelper.add("token", token);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String accountListresult) {
                    Log.e(TAG + "分享帐号列表", accountListresult);
                    try {
                        JSONObject jsonObject = new JSONObject(accountListresult);
                        if (jsonObject.getInt("status") == 9999) {
                            JSONObject jsonMsg = jsonObject.getJSONObject("msg");
                            JSONArray jsonArray = jsonMsg.getJSONArray("account_list");
                            int length = jsonArray.length();
                            if (length > 0) {
                                shaerAccountlist = new ArrayList<AccountDataBean>();
                                AccountDataBean bean;
                                for (int i = 0; i < length; i++) {
                                    bean = new AccountDataBean();
                                    JSONObject json = jsonArray.getJSONObject(i);
                                    bean.setAccoungNumber(json.getString("account"));
                                    bean.setUserName(json.getString("username"));
                                    bean.setPhotoPath(json.getJSONObject("photo_path").getString("Android"));
                                    bean.setEdit(true);
                                    bean.setSelect(false);
                                    bean.setShow(false);
                                    shaerAccountlist.add(bean);
                                }
                                adapter = new ChildAccountManagerAdapter(shaerAccountlist, HostManagerDataActivity.this);
                                accountListView.setAdapter(adapter);
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

    /**
     * 修改控制主机名称
     *
     */
    private void ChangeHostMagName(String engineName){
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String hostMagName = hostMgeName_ed.getText().toString().trim();
            if (!TextUtils.isEmpty(hostMagName)) {
                JSONObject json = new JSONObject();
                json.put("device_id", engineId);
                //json.put("serial_number", engineSerial);
                json.put("device_name", engineName);
                json.put("room_id", "");
                String sign = MD5Utils.MD5Encode(aesAccount + json.toString() + updateMethod + token + URLUtils.MD5_SIGN, "");
                XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETDEVICELIST_URL);
                xutilsHelper.add("account", aesAccount);
                xutilsHelper.add("engine_id", "");
                xutilsHelper.add("device", json.toString());
                xutilsHelper.add("token", token);
                xutilsHelper.add("method", updateMethod);
                xutilsHelper.add("sign", sign);
                xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String updateResult) {
                        Log.e(TAG + "修改名称", updateResult);
                        try {
                            JSONObject jsonObject = new JSONObject(updateResult);
                            if (jsonObject.getInt("status") == 9999) {

                                final AlertDialog builder = new AlertDialog.Builder(HostManagerDataActivity.this).create();
                                View view = View.inflate(HostManagerDataActivity.this, R.layout.dialog_textview, null);
                                TextView title = (TextView) view.findViewById(R.id.textView1);
                                Button button = (Button) view.findViewById(R.id.button1);
                                title.setText("修改成功");
                                builder.setView(view);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        intent.putExtra("engineName", engineName);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                        builder.dismiss();
                                    }
                                });
                                builder.show();
                            } else {
                                Utils.showDialog(HostManagerDataActivity.this, jsonObject.getString("erroe"));
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
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     * 删除分享帐号
     * @param index
     */
    private void deleteShareAccount(int index){
        try {

            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(shaerAccountlist.get(index).getAccoungNumber());
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + deleteShareAccountMethod + jsonArray.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.SHAREHOSMANAGER);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engineId);
            xutilsHelper.add("msg", jsonArray.toString());
            xutilsHelper.add("method", deleteShareAccountMethod);
            xutilsHelper.add("token", token);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    Log.e(TAG + "删除",deleteResult);
                    try {
                        JSONObject jsonObject = new JSONObject(deleteResult);
                        if (jsonObject.getInt("status") == 9999) {
                            Toast.makeText(HostManagerDataActivity.this, jsonObject.getString("msg"), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(HostManagerDataActivity.this, jsonObject.getString("error"), Toast.LENGTH_LONG).show();
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

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.hostmanagerdata_back_iv://返回键
                finish();
                break;
            case R.id.hostmanagerdata_finish_btn://保存
                engineName = hostMgeName_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(engineName)) {
                    ChangeHostMagName( engineName);
                }
                break;
            case R.id.hostmanagerdata_deleteed_ibtn://清空名称
                hostMgeName_ed.setText("");
                break;
            default:
            break;
        }
    }
}
