package neuron.com.adapter;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import neuron.com.bean.RoomItemBean;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/11/24.
 */
public class BindSetAdapter extends BaseAdapter {
    private Context context;
    private List<RoomItemBean> list;
    private Handler handler;
    private String deviceId;
    public BindSetAdapter(Context context, List<RoomItemBean> list, Handler handler, String deviceId) {

        this.context = context;
        this.list = list;
        this.handler = handler;
        this.deviceId = deviceId;
    }

    public List<RoomItemBean> getList() {
        return list;
    }

    public void setList(List<RoomItemBean> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.bindset_item, null);
            viewHolder.deviceName_tv = (TextView) view.findViewById(R.id.bindset_item_devicename_tv);
            viewHolder.delete_btn = (Button) view.findViewById(R.id.bindset_item_delete_btn);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.deviceName_tv.setText(list.get(i).getDeviceName());
        viewHolder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
                String account = null, token = null;
                if (sharedPreferencesManager.has("account")) {
                     account = sharedPreferencesManager.get("account");
                }
                if (sharedPreferencesManager.has("token")) {
                     token = sharedPreferencesManager.get("token");
                }
                String association_devices = null, bind_device = null;
                JSONArray json;
                if (list.get(i).getSign() == 1) {//关联
                    try {
                        bind_device = new JSONArray().toString();
                        Log.e("BindsetAdapter", bind_device);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("branch_id", list.get(i).getBranchId());
                        jsonObject.put("controled_deviceid", list.get(i).getDeviceId());
                        json = new JSONArray();
                        json.put(jsonObject);
                        association_devices = json.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {//绑定
                    association_devices = new JSONArray().toString();
                    JSONArray jsonBind = new JSONArray();
                    jsonBind.put(list.get(i).getDeviceId());
                    bind_device = jsonBind.toString();
                }
                try {
                    String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                    String sign = MD5Utils.MD5Encode(aesAccount + association_devices + bind_device
                            + deviceId + "FreedControlledDevice" + token + URLUtils.MD5_SIGN, "");
                    XutilsHelper xutil = new XutilsHelper(URLUtils.GETDEVICELIST_URL, handler);
                    xutil.add("account", aesAccount);
                    xutil.add("association_devices", association_devices);
                    xutil.add("bind_devices", bind_device);
                    xutil.add("deviceid", deviceId);
                    xutil.add("method", "FreedControlledDevice");
                    xutil.add("token", token);
                    xutil.add("sign", sign);
                    xutil.sendPost(1, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }
    class ViewHolder{
        TextView deviceName_tv;
        Button delete_btn;
    }
}
