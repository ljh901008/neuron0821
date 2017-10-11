package neuron.com.set.Activity;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenuListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.HostManageAdapter;
import neuron.com.bean.HostManagerItemBean;
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
 * Created by ljh on 2017/4/27.
 */
public class ShareHostManagerListActivity extends BaseActivity implements OnClickListener ,AdapterView.OnItemClickListener{
    private String TAG = "ShareHostManagerListActivity";
    private ImageView back_iv;
    private Button titleRight_btn;
    private TextView titleName_tv;
    private HostManageAdapter adapter;
    private List<HostManagerItemBean> list;
    private SwipeMenuListView hostList_lv;
    private ListView listView;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private String method = "GetBindEngines";
    private String makeShareMethod = "ShareEngines";
    private String account = null, token = null;

    private int index;
    private Intent intent;
    private int type = 1;//子帐号设置控制主机的标记
    private String accountc;//子帐号
    private WaitDialog mWaitDialog;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1://控制主机列表
                    if (msg.what == 102) {
                        String hostManagerListResult = (String) msg.obj;
                        Utils.dismissWaitDialog(mWaitDialog);
                        Log.e(TAG + "控制主机列表",hostManagerListResult);
                        try {
                            JSONObject jsonObject = new JSONObject(hostManagerListResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONArray jsonArray = jsonObject.getJSONArray("msg");
                                int length = jsonArray.length();
                                if (length > 0) {
                                    list = new ArrayList<HostManagerItemBean>();
                                    HostManagerItemBean bean;
                                    for (int i = 0; i < length; i++) {
                                        bean = new HostManagerItemBean();
                                        JSONObject json = jsonArray.getJSONObject(i);
                                        //  Log.e(TAG + "控制主机名称",s);
                                        bean.setHostManagerName(json.getString("engine_name"));
                                        bean.setEngineId(json.getString("engine_id"));
                                        //   Log.e(TAG + "控制主机Id",json.getString("engine_id"));
                                        //bean.setHostSerialNumber(json.getString("serial_number"));
                                        // Log.e(TAG + "控制主机序列号",json.getString("serial_number"));
                                        //bean.setHostState(json.getInt("_default"));
                                        if (type == 2) {//子帐号不显示文字
                                            bean.setIsVisible(0);
                                        } else {
                                            bean.setIsVisible(1);
                                        }
                                        list.add(bean);
                                    }
                                    Log.e(TAG + "列表长度", String.valueOf(list.size()));
                                    adapter = new HostManageAdapter(list, ShareHostManagerListActivity.this);
                                    listView.setAdapter(adapter);
                                }
                            } else {
                                Utils.dismissWaitDialog(mWaitDialog);
                                Log.e(TAG + "error", jsonObject.getString("error"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2://生成分享码
                    if (msg.what == 102) {
                        String shareCodeREsult = (String) msg.obj;
                        try {
                            JSONObject jsonShareCode = new JSONObject(shareCodeREsult);
                            if (jsonShareCode.getInt("status") == 9999) {
                                makeShareDialog(ShareHostManagerListActivity.this, jsonShareCode.getString("share_code"));
                            } else {
                                Toast.makeText(ShareHostManagerListActivity.this, jsonShareCode.getString("error"),Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 3://子帐号设定控制主机
                    if (msg.what == 102) {
                        String shareCodeREsult = (String) msg.obj;
                        try {
                            JSONObject jsonShareCode = new JSONObject(shareCodeREsult);
                            if (jsonShareCode.getInt("status") == 9999) {
                                Utils.showDialog(ShareHostManagerListActivity.this, "设置成功！");
                            } else {
                                Toast.makeText(ShareHostManagerListActivity.this, jsonShareCode.getString("error"),Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.hostmanager);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
    }
    private void init(){
        intent = getIntent();
        type = intent.getIntExtra("type", 5);
        accountc = intent.getStringExtra("accountc");
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        titleName_tv = (TextView) findViewById(R.id.hostmanager_room_name_tv);
        back_iv = (ImageView) findViewById(R.id.hostmanager_back_iv);
        titleRight_btn = (Button) findViewById(R.id.hostmanager_finish_btn);
        if (type == 2) {
            titleName_tv.setText("设置控制主机");
            titleRight_btn.setText("确定");
        } else {
            titleName_tv.setText("选择控制主机");
            titleRight_btn.setText("生成分享码");
        }
        hostList_lv = (SwipeMenuListView) findViewById(R.id.hostmanager_listview_lv);
        listView = (ListView) findViewById(R.id.hostmanager_lv);
        listView.setVisibility(View.VISIBLE);
        hostList_lv.setVisibility(View.GONE);
        getHostManagerList();
        listView.setOnItemClickListener(this);
        back_iv.setOnClickListener(this);
        titleRight_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.hostmanager_back_iv:
                finish();
                break;
            case R.id.hostmanager_finish_btn://确定
                if (type == 2) {
                    setHost(accountc, "DeliveryEngines");
                } else {
                    makeShare();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取控制主机列表
     */
    private void getHostManagerList() {

        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.SHAREHOSMANAGER, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", method);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void makeShare(){
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(list.get(index).getEngineId());
            String sign = MD5Utils.MD5Encode(aesAccount + jsonArray.toString() + makeShareMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.SHAREHOSMANAGER, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_list", jsonArray.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", makeShareMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(2, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (type == 2) {
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).getHostState() == 0) {
                    list.get(j).setHostState(1);
                } else {
                    list.get(j).setHostState(0);
                }
            }
        } else {
            for (int j = 0; j < list.size(); j++) {
                if (position == j) {
                    list.get(j).setHostState(0);
                } else {
                    list.get(j).setHostState(1);
                }
            }
        }
        adapter.setList(list);
        adapter.notifyDataSetChanged();
        index = position;
    }
    private void setHost(String accountc,String method){
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String aesAccountc = AESOperator.encrypt(accountc, URLUtils.AES_SIGN);
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getHostState() == 0) {//被选择的控制主机
                    jsonArray.put(list.get(i).getEngineId());
                }
            }
            String sign = MD5Utils.MD5Encode(aesAccount + aesAccountc + jsonArray.toString() + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.SHAREHOSMANAGER, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("accountc", aesAccountc);
            xutilsHelper.add("engine_list", jsonArray.toString());
            xutilsHelper.add("method", method);
            xutilsHelper.add("token", token);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(3, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     *          复制内容
     * @param context
     * @param content
     */
    public void makeShareDialog(final Context context, final String content){
        final AlertDialog builder = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.dialog_textview, null);
        TextView title = (TextView) view.findViewById(R.id.textView1);
        Button button = (Button) view.findViewById(R.id.button1);
        button.setText("复制");
        title.setText(content);
        builder.setView(view);
        button.setOnClickListener(new OnClickListener() {
           @Override
           public void onClick(View view) {
               // 从API11开始android推荐使用android.content.ClipboardManager
               // 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
               ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
               // 将文本内容放到系统剪贴板里。
               cm.setText(content);
               Toast.makeText(context,"复制成功",Toast.LENGTH_LONG).show();
               builder.dismiss();
           }
        });
        builder.show();
    }
}
