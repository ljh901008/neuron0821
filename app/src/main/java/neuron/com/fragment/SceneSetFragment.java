package neuron.com.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ScrollView;

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
import java.util.List;

import neuron.com.adapter.SceneSetAdapter;
import neuron.com.bean.AirQualityBean;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;
import neuron.com.scene.Activity.SceneEditActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/4/21. 设置模块  场景
 */
public class SceneSetFragment extends Fragment implements AdapterView.OnItemClickListener{
    private String TAG = "SceneSetFragment";
    private SwipeMenuListView listView;
    private PullToRefreshScrollView pullToRefreshScrollView;
    private ScrollView sv;
    private View view;
    private List<AirQualityBean> list;
    private SceneSetAdapter adapter;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private String methodSceneList = "GetNSContextualModelList";
    private String delMethod = "DelContextualModels";
    private int mIndex;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://场景列表
                    if (msg.what == 102) {
                        String scenelistResult = (String) msg.obj;


                    }
                    break;
                case 2://删除场景
                    if (msg.what == 102) {
                        String updateResult = (String) msg.obj;

                    }
                    break;
                default:
                    break;
            }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scenesetfragment, container, false);
        init();
        return view;
    }
    private void init(){
        listView = (SwipeMenuListView) view.findViewById(R.id.scenesetfragment_listview);
        pullToRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.scenesetfragment_pullsv);
        pullToRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                new GetDataTask().execute();
            }
        });
        sv = pullToRefreshScrollView.getRefreshableView();
        //设置scrollview在最顶端
        sv.smoothScrollTo(0, 0);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
                // 设置菜单的背景
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
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
        listView.setOnItemClickListener(this);
        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch(index){
                    case 0:
                        Log.e(TAG+"场景Id", list.get(position).getSceneId());
                        delScene(list.get(position).getSceneId(), delMethod);
                        mIndex = position;
                        break;
                    default:
                        break;
                }
            }
        });
        getSceneList();
    }
    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(getActivity(), SceneEditActivity.class);
        intent.putExtra("sceneId", list.get(i).getSceneId());
        startActivity(intent);
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
            /*try {
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
            getSceneList();
            pullToRefreshScrollView.onRefreshComplete();
        }
    }
    /**
     * 获取场景列表
     */
    private void getSceneList(){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + engineId + methodSceneList + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("token", token);
            xutils.add("method", methodSceneList);
            xutils.add("sign", sign);
            //xutils.sendPost(1, getActivity());
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String scenelistResult) {
                    try {
                        JSONObject jsonObject = new JSONObject(scenelistResult);
                        int status = jsonObject.getInt("status");
                        if (status == 9999) {
                            JSONArray jsonArray = jsonObject.getJSONArray("contextual_model_list");
                            int length = jsonArray.length();
                            if (length > 0) {
                                list = new ArrayList<AirQualityBean>();
                                AirQualityBean bean;
                                for (int i = 0; i < length; i++) {
                                    JSONObject sceneJson = jsonArray.getJSONObject(i);
                                    bean = new AirQualityBean();
                                    bean.setSceneName(sceneJson.getString("contextual_model_name"));
                                    bean.setSceneId(sceneJson.getString("contextual_model_id"));
                                    bean.setSceneType(sceneJson.getString("contextual_model_type"));
                                    bean.setSceneImg(sceneJson.getString("contextual_model_img"));
                                    list.add(bean);
                                }
                                adapter = new SceneSetAdapter(getActivity(), list);
                                listView.setAdapter(adapter);
                            }
                        }else if (status == 1000 || status == 1001) {
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);
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
     *  删除场景
     * @param sceneId  场景Id
     * @param method  方法名
     */
    private void delScene(String sceneId,String method){
        setAccount();
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + engineId + method + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("contextual_model_id", sceneId);
            xutils.add("token", token);
            xutils.add("method", method);
            xutils.add("sign", sign);
            //xutils.sendPost(2, getActivity());
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String updateResult) {
                    Log.e("删除场景", updateResult);
                    try {
                        JSONObject jsonObject = new JSONObject(updateResult);
                        if (jsonObject.getInt("status") != 9999) {
                            Utils.showDialog(getActivity(),jsonObject.getString("error"));
                        } else {
                            Utils.showDialog(getActivity(),"操作成功");
                            list.remove(mIndex);
                            adapter.setList(list);
                            adapter.notifyDataSetChanged();
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
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(getActivity());
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
}
