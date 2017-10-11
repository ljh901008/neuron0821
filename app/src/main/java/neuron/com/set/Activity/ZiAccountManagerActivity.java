package neuron.com.set.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
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
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/20.  自账号列表
 */
public class ZiAccountManagerActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener,SwipeMenuListView.OnMenuItemClickListener{
    private String TAG = "ChildAccountManagerAcitvity";
    private ImageView back_iv;
    private TextView titileName_tv;
    private Button edit_btn,allselect_btn, delete_btn;
    private SwipeMenuListView listView;
    private FrameLayout bottom_flyt;
    private List<AccountDataBean> listBean;
    private ChildAccountManagerAdapter adapter;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private String account, token,engine_id;
    private String ziMethod = "GetKidAccountList";
    private String delZiMethod = "DelKidAccount";
    private TextView tishi;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://子账号列表
                    if (msg.what == 102) {
                        String ziResult = (String) msg.obj;
                        try {
                            JSONObject jsonObject = new JSONObject(ziResult);
                            if (jsonObject.getInt("status") == 9999) {
                                JSONArray jsonArray = jsonObject.getJSONArray("kid_list");
                                int length = jsonArray.length();
                                if (length > 0) {
                                    listBean = new ArrayList<AccountDataBean>();
                                    AccountDataBean bean;
                                    for (int i = 0; i < length; i++) {
                                        bean = new AccountDataBean();
                                        JSONObject jsonBean = jsonArray.getJSONObject(i);
                                        bean.setAccoungNumber(jsonBean.getString("account"));
                                        Log.e("zi账号", jsonBean.getString("account"));
                                        bean.setUserName(jsonBean.getString("username"));
                                        JSONObject pathJson = jsonBean.getJSONObject("photo_path");
                                        bean.setPhotoPath(pathJson.getString("Android"));
                                        Log.e(TAG + "头像路径", pathJson.getString("Android"));
                                        bean.setEdit(true);
                                        bean.setSelect(false);
                                        bean.setShow(true);
                                        listBean.add(bean);
                                    }
                                    adapter = new ChildAccountManagerAdapter(listBean, ZiAccountManagerActivity.this);
                                    listView.setAdapter(adapter);
                                    tishi.setVisibility(View.GONE);
                                } else {
                                    tishi.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 2://删除
                    if (msg.what == 102) {
                        String deleteResult = (String) msg.obj;
                        Log.e(TAG + "删除", deleteResult);
                        try {
                            JSONObject jsonDelete = new JSONObject(deleteResult);
                            if (jsonDelete.getInt("status") == 9999) {
                                Toast.makeText(ZiAccountManagerActivity.this, "删除成功",Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ZiAccountManagerActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.childaccountmanageractivity);
        init();
        setListener();
        getZiAccount();
    }
    private void init() {
        back_iv = (ImageView) findViewById(R.id.childaccountmanageractivity_back_iv);
        titileName_tv = (TextView) findViewById(R.id.childaccountmanageractivity_titlename_tv);
        edit_btn = (Button) findViewById(R.id.childaccountmanageractivity_edit_btn);
        allselect_btn = (Button) findViewById(R.id.childaccountmanageractivity_allselect_btn);
        delete_btn = (Button) findViewById(R.id.childaccountmanageractivity_delete_btn);
        bottom_flyt = (FrameLayout) findViewById(R.id.childaccountmanageractivity_framely);
        listView = (SwipeMenuListView) findViewById(R.id.childaccountmanageractivity_lv);
        tishi = (TextView) findViewById(R.id.childaccountmanageractivity_tishi_tv);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(ZiAccountManagerActivity.this);
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
        listView.setMenuCreator(creator);

    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
        allselect_btn.setOnClickListener(this);
        delete_btn.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        listView.setOnMenuItemClickListener(this);
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.childaccountmanageractivity_back_iv://返回键
                finish();
                break;
            case R.id.childaccountmanageractivity_edit_btn://添加子账号
                Intent intent = new Intent(ZiAccountManagerActivity.this, AddZiAccountActivity.class);
                startActivityForResult(intent,100);
                break;
            case R.id.childaccountmanageractivity_allselect_btn://全选

                break;
            case R.id.childaccountmanageractivity_delete_btn://删除

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
                        getZiAccount();
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(ZiAccountManagerActivity.this, ShareHostManagerListActivity.class);
        intent.putExtra("type", 2);
        intent.putExtra("accountc", listBean.get(i).getAccoungNumber());
        startActivity(intent);
    }

    @Override
    public void onMenuItemClick(int position, SwipeMenu menu, int index) {
        switch(index){
            case 0:
                Log.e(TAG + "左滑删除", "");
                try {
                    String aesZiAccount = AESOperator.encrypt(listBean.get(position).getAccoungNumber(), URLUtils.AES_SIGN);
                    delZiAccount(aesZiAccount);
                    listBean.remove(position);
                    adapter.setList(listBean);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;
            default:
                break;
        }
    }
    private void getZiAccount(){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + ziMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.USERNAME_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", ziMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void delZiAccount(String ziAccount){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + ziAccount + delZiMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.USERNAME_URL, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("accountc", ziAccount);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", delZiMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(2, this);
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
            engine_id = sharedPreferencesManager.get("engine_id");
        }
    }
}
