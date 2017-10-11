package neuron.com.scene.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/5.  电视空调品牌  列表
 */
public class AirTVBrandListActivity extends BaseActivity implements AdapterView.OnItemClickListener,View.OnClickListener{
    private String TAG = "AirTVBrandListActivity";
    private ImageView back_iv;
    private TextView titleName_tv;
    private Button button;
    private ListView listView;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account,token, engineId;
    private List<AirQualityBean> list;
    private AirQualityAdapter adapter;
    private String brandMethod = "GetBrandList";
    private Intent intent;
    private String eletricId; // 区分电视空调的标记
    private int index = 1000;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1://品牌列表
                    if (msg.what == 102) {
                        String brandResult = (String) msg.obj;
                        Log.e(TAG + "品牌列表", brandResult);
                        try {
                            JSONObject jsr = new JSONObject(brandResult);
                            if (jsr.getInt("status") == 9999) {
                                JSONArray jsBrand = jsr.getJSONArray("brand_list");
                                int length = jsBrand.length();
                                if (length > 0) {
                                    list = new ArrayList<AirQualityBean>();
                                    AirQualityBean bean;
                                    for (int i = 0; i < length; i++) {
                                        JSONObject json = jsBrand.getJSONObject(i);
                                        bean = new AirQualityBean();
                                        bean.setSceneId("0&"+json.getString("brand_id"));
                                        bean.setSceneName(json.getString("brand_name"));
                                        bean.setSelect(false);
                                        list.add(bean);
                                    }
                                    if ("0".equals(eletricId)) {
                                        bean = new AirQualityBean();
                                        bean.setSceneId("1&");
                                        bean.setSceneName("自定义");
                                        list.add(bean);
                                    }
                                    adapter = new AirQualityAdapter(list, AirTVBrandListActivity.this);
                                    listView.setAdapter(adapter);
                                }
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
        setContentView(R.layout.devicelist);
        init();
        setListener();
    }
    private void init(){
        intent = getIntent();
        eletricId = intent.getStringExtra("electricId");
        back_iv = (ImageView) findViewById(R.id.devicelist_back_iv);
        button = (Button) findViewById(R.id.devicelist_queding_bnt);
        listView = (ListView) findViewById(R.id.devicelist_listview);
        titleName_tv = (TextView) findViewById(R.id.devicelist_titlename_tv);
        titleName_tv.setText("选择品牌");
        getBrandList(eletricId);

    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        button.setOnClickListener(this);
        listView.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        for (int i = 0; i < list.size(); i++) {
            if (i == position) {
                list.get(position).setSelect(true);
            } else {
                list.get(i).setSelect(false);
            }
            adapter.setList(list);
            adapter.notifyDataSetChanged();
            index = position;
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.devicelist_back_iv://返回键
                finish();
                break;
            case R.id.devicelist_queding_bnt://确定
                Log.e(TAG + "系列Id和名称", list.get(index).getSceneName() + list.get(index).getSceneId());
                intent.putExtra("brandName", list.get(index).getSceneName());
                intent.putExtra("brandId", list.get(index).getSceneId());
                setResult(RESULT_OK, intent);
                finish();
                break;
            default:
            break;
        }
    }

    /**
     *      获取品牌列表
     * @param electricType     0电视  1空调
     */
    private void getBrandList(String electricType){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        Log.e(TAG + "电视/空调", electricType);
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + electricType + brandMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.OPERATION, handler);
            xutilsHelper.add("account", aesAccount);
            xutilsHelper.add("electric_type", electricType);
            xutilsHelper.add("token", token);
            xutilsHelper.add("method", brandMethod);
            xutilsHelper.add("sign", sign);
            xutilsHelper.sendPost(1, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
