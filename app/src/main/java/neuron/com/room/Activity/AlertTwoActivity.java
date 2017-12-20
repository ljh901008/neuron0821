package neuron.com.room.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neuron.com.adapter.AlertAdapter;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/8/8.
 */
public class AlertTwoActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AlertTwoActivity";
    private SwipeMenuListView listView;
    private ImageView back_iv;
    private Button clear_btn;
    private PullToRefreshScrollView pullToRefreshScrollView;
    private String account, token;
    private String getMsgMethod = "GetMessageList";
    private String delMsgMethod = "DelMessage";
    private AlertAdapter adapter;
    private List<Map<String, String>> list;
    private boolean isFirst = true;
    private int mPosition = 100;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alerttwo);
        init();
        setListener();
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(AlertTwoActivity.this);
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
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        delMsg(list.get(position).get("msgId"),2);
                        mPosition = position;
                        break;
                    default:
                        break;
                }
            }
        });
        getMsg("", "",1);
        pullToRefreshScrollView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
       // pullToRefreshScrollView.getLoadingLayoutProxy().setTextTypeface(Typeface.DEFAULT);
        pullToRefreshScrollView.getLoadingLayoutProxy().setReleaseLabel("松开即可刷新");

        pullToRefreshScrollView.setMode(PullToRefreshBase.Mode.BOTH);//设置刷新样式，可上拉 可下拉
        pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ScrollView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                getMsg("", "",1);//下拉刷新
                new GetDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ScrollView> refreshView) {
                //上拉加载
                if (list.size() > 0) {
                    getMsg(list.get(list.size() - 1).get("msgId"),list.get(list.size() - 1).get("time"),3);
                }
                new GetDataTask().execute();
            }
        });
    }

    private void init() {
        intent = getIntent();
        back_iv = (ImageView) findViewById(R.id.alerttwoactivity_back_iv);
        clear_btn = (Button) findViewById(R.id.alerttwoactivity_clear_btn);
        listView = (SwipeMenuListView) findViewById(R.id.alerttwoactivity_alertlistview);
        pullToRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.alerttwo_pullsv);
    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        clear_btn.setOnClickListener(this);
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    /**
     *   获取警报消息列表
     * @param lastMsgId  当前警报列表的最后一条消息的id，第一次获取为空
     * @param lastMsgTime 当前警报列表的最后一条消息的时间
     */
    private void getMsg(String lastMsgId,String lastMsgTime,int arg1){
        getAccount();
        try {
            Log.e(TAG + "最后一条消息的id和时间", "id" + lastMsgId + "时间" + lastMsgTime);
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + lastMsgId + lastMsgTime + getMsgMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.Other);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("last_msg_id", lastMsgId);
            xutilsHelper.add("last_msg_time", lastMsgTime);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", getMsgMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String msgResult) {
                    switch(arg1){
                        case 1:
                            try {
                                Log.e(TAG + "消息列表", msgResult);
                                JSONObject jsonObject = new JSONObject(msgResult);
                                if (jsonObject.getInt("status") == 9999) {
                                    JSONArray jsonArray = jsonObject.getJSONArray("msg");
                                    int length = jsonArray.length();
                                    if (length > 0) {
                                        list = new ArrayList<Map<String, String>>();
                                        Map<String, String> map;
                                        for (int i = 0; i < length; i++) {
                                            map = new HashMap<String, String>();
                                            JSONObject jsonMsg = jsonArray.getJSONObject(i);
                                            map.put("time", jsonMsg.getString("msg_time"));
                                            map.put("content", jsonMsg.getString("msg_content"));
                                            map.put("msgId", jsonMsg.getString("msg_id"));
                                            list.add(map);
                                        }
                                        adapter = new AlertAdapter(list, AlertTwoActivity.this);
                                        listView.setAdapter(adapter);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 3:
                            try {
                                JSONObject jsonObject = new JSONObject(msgResult);
                                if (jsonObject.getInt("status") == 9999) {
                                    JSONArray jsonArray = jsonObject.getJSONArray("msg");
                                    int length = jsonArray.length();
                                    if (length > 0) {
                                        list.clear();
                                        Map<String, String> map;
                                        for (int i = 0; i < length; i++) {
                                            map = new HashMap<String, String>();
                                            JSONObject jsonMsg = jsonArray.getJSONObject(i);
                                            map.put("time", jsonMsg.getString("msg_time"));
                                            map.put("content", jsonMsg.getString("msg_content"));
                                            map.put("msgId", jsonMsg.getString("msg_id"));
                                            list.add(map);
                                        }
                                        adapter.setList(list);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                        break;
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
     *  删除警报
     * @param msgId
     */
    private void delMsg(String msgId,int arg1){
        getAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + delMsgMethod + msgId + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.Other);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("msg_id", msgId);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", delMsgMethod);
            xutilsHelper.add("sign", sign);
            //xutilsHelper.sendPost(arg1,this);
            xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    Log.e(TAG + "删除消息",s);
                    switch(arg1){
                        case 2://删除
                            try {
                                JSONObject jsonDelete = new JSONObject(s);
                                if (jsonDelete.getInt("status") == 9999) {
                                    Toast.makeText(AlertTwoActivity.this, "删除成功",Toast.LENGTH_LONG).show();
                                    list.remove(mPosition);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Toast.makeText(AlertTwoActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        break;
                        case 4://清空
                            try {
                                JSONObject jsonDelete = new JSONObject(s);
                                if (jsonDelete.getInt("status") == 9999) {
                                    if (list != null) {
                                        Toast.makeText(AlertTwoActivity.this, "删除成功",Toast.LENGTH_LONG).show();
                                        list.clear();
                                        adapter.notifyDataSetChanged();
                                    }
                                } else {
                                    Toast.makeText(AlertTwoActivity.this, jsonDelete.getString("error"),Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                        break;
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
    private void getAccount(){
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
            Log.e(TAG + "account", account);
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
            Log.e(TAG + "token", token);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.alerttwoactivity_back_iv://返回
                intent.putExtra("tag", 1);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.alerttwoactivity_clear_btn://清空
              delMsg("all",4);
                break;
            default:
                break;
        }
    }

    private class GetDataTask extends AsyncTask<Void, Void, String> {
        /**
         * 这里的Void参数对应AsyncTask中的第一个参数
         * 这里的String返回值对应AsyncTask的第三个参数
         * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
         * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
         */
        @Override
        protected String doInBackground(Void... voids) {
           /* try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            return null;
        }

        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pullToRefreshScrollView.onRefreshComplete();
        }
    }
}
