package neuron.com.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.util.List;

import neuron.com.bean.RoomItemBean;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/9/30. 设备列表的适配器
 */
public class RoomAdapter extends BaseAdapter {
    private String lightType = "33001";
    private Context context;
    private List<RoomItemBean> list;
    private Handler handler;
    private SharedPreferencesManager sharedPreferencesManager;

    public RoomAdapter(Context context, List<RoomItemBean> list, Handler handler, SharedPreferencesManager sharedPreferencesManager) {
        this.context = context;
        this.list = list;
        this.handler = handler;
        this.sharedPreferencesManager = sharedPreferencesManager;
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
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.roomdevicelist_item, null);
            holder.devicePhoto_iv = (ImageView) view.findViewById(R.id.roomdevicelist_item_devicephoto_iv);
            holder.deviceKaiGuan_iv = (ImageView) view.findViewById(R.id.roomdevicelist_item_devicetype_iv);
            holder.deviceName = (TextView) view.findViewById(R.id.roomdevicelist_item_devicename_tv);
            holder.deviceRoom = (TextView) view.findViewById(R.id.roomdevicelist_item_deviceRoom_tv);
            holder.bg_rll = (RelativeLayout) view.findViewById(R.id.roomdevicelist_item_bg_rll);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.deviceRoom.setText(list.get(i).getDeviceRoom());
        String deviceType = list.get(i).getDeviceType();
        int kaiguan = list.get(i).getDeviceStatu();
        if ("33001".equals(deviceType) || "33009".equals(deviceType)) {
            if (kaiguan == 00) {//关闭状态
                holder.deviceKaiGuan_iv.setImageResource(R.mipmap.kaiguan_guan);
                holder.deviceKaiGuan_iv.setTag(4);
            } else if (kaiguan == 01) {//开启状态
                holder.deviceKaiGuan_iv.setImageResource(R.mipmap.kaiguan_kai);
                holder.deviceKaiGuan_iv.setTag(0);
            }
        }
        holder.deviceName.setText(list.get(i).getDeviceName());
        //holder.devicePhoto_iv.setImageResource(R.mipmap.equ_air);
        if ("33001".equals(deviceType)) {//普通灯
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_light);
            holder.deviceKaiGuan_iv.setVisibility(View.VISIBLE);

            holder.deviceKaiGuan_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String site = list.get(i).getDeviceSite();
                    if (holder.deviceKaiGuan_iv.getTag().equals(0)) {
                        list.get(i).setDeviceStatu(0);
                        holder.deviceKaiGuan_iv.setTag(4);
                        holder.deviceKaiGuan_iv.setImageResource(R.mipmap.kaiguan_guan);
                        if (lightType.equals(list.get(i).getDeviceType())) {//普通灯
                            setDevice(i, "03", site + "0");
                        }
                    } else if (holder.deviceKaiGuan_iv.getTag().equals(4)) {
                        list.get(i).setDeviceStatu(4);
                        holder.deviceKaiGuan_iv.setTag(0);
                        holder.deviceKaiGuan_iv.setImageResource(R.mipmap.kaiguan_kai);
                        if (lightType.equals(list.get(i).getDeviceType())) {//普通灯
                            setDevice(i, "03", site + "1");
                        }
                    }
                }
            });
        } else if ("33003".equals(deviceType)){//红外人体感应
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_hongwaiganyin);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);

        } else if ("33004".equals(deviceType)){//智能窗帘
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_chuanglian);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);

        }else if ("33005".equals(deviceType)){//门磁
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_menci);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);

        }else if ("33006".equals(deviceType)){//电视
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_tv);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);
        }else if ("33007".equals(deviceType)){//空调
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_air);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);
        }else if ("33008".equals(deviceType)){//空气质量检测仪
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_kongqi);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);
        }else if ("33009".equals(deviceType)){//智能插座
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_chazuo);
            holder.deviceKaiGuan_iv.setVisibility(View.VISIBLE);

            holder.deviceKaiGuan_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   // String site = list.get(i).getDeviceSite();
                    if (holder.deviceKaiGuan_iv.getTag().equals(0)) {
                        list.get(i).setDeviceStatu(0);
                        holder.deviceKaiGuan_iv.setTag(4);
                        holder.deviceKaiGuan_iv.setImageResource(R.mipmap.kaiguan_guan);
                        if ("33009".equals(list.get(i).getDeviceType())) {//
                            setDevice(i, "03", "00");
                        }
                    } else if (holder.deviceKaiGuan_iv.getTag().equals(4)) {
                        list.get(i).setDeviceStatu(4);
                        holder.deviceKaiGuan_iv.setTag(0);
                        holder.deviceKaiGuan_iv.setImageResource(R.mipmap.kaiguan_kai);
                        if ("33009".equals(list.get(i).getDeviceType())) {//普通灯
                            setDevice(i, "03", "01");
                        }
                    }
                }
            });
        }else if ("33010".equals(deviceType)){//摄像头
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_shexiangtou);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);
        }else if ("33011".equals(deviceType)){//净水器
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_yinshuiji);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);
        }else if ("33012".equals(deviceType)){//音响
            holder.devicePhoto_iv.setImageResource(R.mipmap.equ_yinxiang);
            holder.deviceKaiGuan_iv.setVisibility(View.GONE);
        }
        return view;
    }
    class ViewHolder{
        ImageView devicePhoto_iv;
        ImageView deviceKaiGuan_iv;
        TextView deviceName,deviceRoom;
        RelativeLayout bg_rll;
    }
    private String account, engine_id,token;
    public void setDevice(int position,String method_type,String order_id){
        if (sharedPreferencesManager == null) {
            sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        }
            if (sharedPreferencesManager.has("account")) {
                account = sharedPreferencesManager.get("account");
            }
            if (sharedPreferencesManager.has("engine_id")) {
                engine_id = sharedPreferencesManager.get("engine_id");
            }
            if (sharedPreferencesManager.has("token")) {
                token = sharedPreferencesManager.get("token");
            }
            Log.e("RoomAdapter", account + ":" + engine_id + ":" + token);
            try {
                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                String sign = MD5Utils.MD5Encode(aesAccount
                        + list.get(position).getDeviceId()
                        + "1"
                        + engine_id
                        + "DoOrders"
                        + method_type
                        + order_id
                        + token
                        + URLUtils.MD5_SIGN, "");
                XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.GETHOMELIST_URL);
                xutilsHelper.add("account", aesAccount);
                xutilsHelper.add("engine_id", engine_id);
                xutilsHelper.add("device_id", list.get(position).getDeviceId());
                xutilsHelper.add("device_type", "1");
                xutilsHelper.add("method_type", method_type);
                xutilsHelper.add("order_id", order_id);
                xutilsHelper.add("token", token);
                xutilsHelper.add("method","DoOrders" );
                xutilsHelper.add("sign",sign);
                //xutilsHelper.sendPost(5, context);
                xutilsHelper.sendPost2(new Callback.CommonCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject json = new JSONObject(result);
                            int status1 = json.getInt("status");
                            if (status1 == 9999) {
                                Toast.makeText(context, "操作成功", Toast.LENGTH_LONG).show();
                            } else if (status1 == 1000 || status1 == 1001) {
                                Intent intent = new Intent(context, LoginActivity.class);
                                context.startActivity(intent);
                            } else {
                                Toast.makeText(context, json.getString("error"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable, boolean b) {
                        Toast.makeText(context, "网络不通", Toast.LENGTH_LONG).show();
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
}
