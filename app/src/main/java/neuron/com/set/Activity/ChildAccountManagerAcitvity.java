package neuron.com.set.Activity;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/3/23.  分享帐号列表
 */
public class ChildAccountManagerAcitvity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener,
        SwipeMenuListView.OnMenuItemClickListener{
    private String TAG = "ChildAccountManagerAcitvity";
    private ImageView back_iv;
    private TextView tishi;
    private TextView titileName_tv;
    private Button edit_btn,allselect_btn, delete_btn;
    private SwipeMenuListView listView;
    private FrameLayout bottom_flyt;
    //编辑按钮标记
    private int selectTag = 0;
    //是否全选
    private int isAllSelect = 0;

    private int listBeanIndex = -1;
    private List<AccountDataBean> listBean;
    private ChildAccountManagerAdapter adapter;
    private SharedPreferencesManager sharedPreferencesManager = null;
    private String account, token,engine_id;
    private String share_method = "ShowSharedAccounts";
    private String recycle_method = "RecycleSharedAccounts";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.childaccountmanageractivity);
        init();
        setListener();
        getShareAccountList();
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
        titileName_tv.setText("分享账号管理");
        edit_btn.setText("编辑");
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(ChildAccountManagerAcitvity.this);
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
    private void setListener(){
        back_iv.setOnClickListener(this);
        edit_btn.setOnClickListener(this);
        allselect_btn.setOnClickListener(this);
        delete_btn.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        if (selectTag == 0) {
            //默认状态下才可以左滑删除
            listView.setOnMenuItemClickListener(this);
        }
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.childaccountmanageractivity_back_iv://返回键
                finish();
                break;
            case R.id.childaccountmanageractivity_edit_btn://编辑键
                if (selectTag == 0) {//正常状态
                    selectTag = 1;
                    bottom_flyt.setVisibility(View.VISIBLE);
                } else if (selectTag == 1) {
                    selectTag = 0;//编辑状态下
                    bottom_flyt.setVisibility(View.GONE);
                }
                if (listBean != null) {
                    for (int i = 0; i < listBean.size(); i++) {
                        listBean.get(i).setSelect(false);
                        listBean.get(i).setEdit(false);
                    }
                    adapter.setList(listBean);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.childaccountmanageractivity_allselect_btn://全选
                if (listBean != null) {
                    if (isAllSelect == 0) {//未全选
                        for (int i = 0; i < listBean.size(); i++) {
                            listBean.get(i).setSelect(false);
                        }
                        isAllSelect = 1;
                    } else if (isAllSelect == 1) {//全选
                        isAllSelect = 0;
                        for (int i = 0; i < listBean.size(); i++) {
                            listBean.get(i).setSelect(true);
                        }
                    }

                    adapter.setList(listBean);
                    adapter.notifyDataSetChanged();
                }
                break;
            case R.id.childaccountmanageractivity_delete_btn://删除
                if (isAllSelect == 0) {//单独删除
                    if (listBeanIndex != -1) {//选中要删除的数据
                        deleteShareAccount(listBeanIndex, 1);
                    }
                } else if (isAllSelect == 1) {//全选
                    deleteShareAccount(0,2);
                }
                break;
            default:
            break;
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        if (selectTag == 0) {//正常状态下

        } else if (selectTag == 1) {//编辑状态下
            listBeanIndex = position;
            listBean.get(position).setSelect(true);
            adapter.setList(listBean);
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取分享帐号列表
     */
    private void getShareAccountList(){
        Log.e(TAG + "控制主机", engine_id + "," + account + URLUtils.SHAREHOSMANAGER);
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + share_method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.SHAREHOSMANAGER);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engine_id);
            xutilsHelper.add("method", share_method);
            xutilsHelper.add("token", token);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String shareAccountListResult) {
                    Log.e(TAG + "分享帐号列表", shareAccountListResult);
                    try {
                        JSONObject jsonObject = new JSONObject(shareAccountListResult);
                        JSONArray msgJson = jsonObject.getJSONArray("msg");
                        int length = msgJson.length();
                        if (length > 0) {
                            listBean = new ArrayList<AccountDataBean>();
                            AccountDataBean bean;
                            for (int i = 0; i < length; i++) {
                                bean = new AccountDataBean();
                                JSONObject jsonBean = msgJson.getJSONObject(i);
                                bean.setAccoungNumber(jsonBean.getString("account"));
                                bean.setUserName(jsonBean.getString("username"));
                                JSONObject pathJson = jsonBean.getJSONObject("photo_path");
                                bean.setPhotoPath(pathJson.getString("Android"));
                                Log.e(TAG + "头像路径", pathJson.getString("Android"));
                                bean.setEdit(true);
                                bean.setSelect(false);
                                bean.setShow(true);
                                listBean.add(bean);
                            }
                            adapter = new ChildAccountManagerAdapter(listBean, ChildAccountManagerAcitvity.this);
                            listView.setAdapter(adapter);
                        } else {
                            tishi.setVisibility(View.VISIBLE);
                            tishi.setText("您没有分享控制主机");
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
     *
     * @param index 单选删除的下标 如果全选 可以随意写
     * @param tag  单个删除和全选删除的标记 1单条删除 2全选删除
     */
    private void deleteShareAccount(int index,int tag){
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            JSONArray js = new JSONArray();
            if (tag == 1) {//单独删除
                js.put(listBean.get(index).getAccoungNumber());
            } else if (tag == 2) {//全选删除
                for (int i = 0; i < listBean.size(); i++) {
                    js.put(listBean.get(i).getAccoungNumber());
                }
            }
            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + recycle_method + js.toString() + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.SHAREHOSMANAGER);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("engine_id", engine_id);
            xutilsHelper.add("method", recycle_method);
            xutilsHelper.add("msg", js.toString());
            xutilsHelper.add("token", token);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String deleteResult) {
                    Log.e(TAG + "删除", deleteResult);
                    try {
                        JSONObject jsonDelete = new JSONObject(deleteResult);
                        if (jsonDelete.getInt("status") == 9999) {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(ChildAccountManagerAcitvity.this, "删除成功",Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ChildAccountManagerAcitvity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
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
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onMenuItemClick(int position, SwipeMenu menu, int index) {
        switch(index){
            case 0:
                Log.e(TAG + "左滑删除", "");
                deleteShareAccount(index,1);
                listBean.remove(position);
                adapter.setList(listBean);

                break;
            default:
                break;
        }
    }
}
