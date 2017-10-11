package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.DeviceSetFragmentBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/5/3.
 */
public class DeviceSetFragmentAdapter extends BaseAdapter {
    private List<DeviceSetFragmentBean> list;
    private Context context;

    public DeviceSetFragmentAdapter(Context context, List<DeviceSetFragmentBean> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(List<DeviceSetFragmentBean> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list.size() > 0) {
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.devicesetfragment_item, null);
            holder.deviceName = (TextView) view.findViewById(R.id.devicesetfragment_item_devicename_tv);
            holder.deviceImg = (ImageView) view.findViewById(R.id.devicesetfragment_item_deviceimg_iv);
            holder.roomName = (TextView) view.findViewById(R.id.devicesetfragment_item_roomname_tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.deviceName.setText(list.get(i).getDeviceName());
        holder.roomName.setText(list.get(i).getRoomName());
        String deviceType = list.get(i).getDeviceType();
        switch (deviceType) {
            case "257"://单键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "258"://单键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "259"://双键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "260"://双键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "261"://双键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "262"://双键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "263"://双键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "264"://双键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "265"://三键开关
                holder.deviceImg.setImageResource(R.mipmap.set_up_switch);
                break;
            case "769"://红外人体感应
                holder.deviceImg.setImageResource(R.mipmap.set_up_induction);
                break;
            case "770"://门磁
                holder.deviceImg.setImageResource(R.mipmap.set_up_magnetometer);
                break;
            case "1025"://红外转发
                holder.deviceImg.setImageResource(R.mipmap.set_up_forward);
                break;
            case "1026"://电动窗帘
                holder.deviceImg.setImageResource(R.mipmap.set_up_window);
                break;
            case "1281"://空气质量检测仪
                holder.deviceImg.setImageResource(R.mipmap.set_up_air_quality);
                break;
            case "1537"://智能插座
                holder.deviceImg.setImageResource(R.mipmap.set_up_socket);
                break;
            case "2049"://摄像头
                holder.deviceImg.setImageResource(R.mipmap.set_up_scene_movie);
                break;
            case "4097"://净水器
                holder.deviceImg.setImageResource(R.mipmap.set_drinking);
                break;
            default:
                break;
        }
        return view;
    }
    class ViewHolder{
        TextView deviceName,roomName;
        ImageView deviceImg;
    }
}
