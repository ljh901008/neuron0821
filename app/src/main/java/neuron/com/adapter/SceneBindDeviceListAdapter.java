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
 * Created by ljh on 2017/5/12.
 */
public class SceneBindDeviceListAdapter extends BaseAdapter {
    private List<DeviceSetFragmentBean> list;
    private Context context;

    public SceneBindDeviceListAdapter(Context context, List<DeviceSetFragmentBean> list) {
        this.context = context;
        this.list = list;
    }

    public List<DeviceSetFragmentBean> getList() {
        return list;
    }

    public void setList(List<DeviceSetFragmentBean> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        if (list.size() != 0) {

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
        ViewHolder viewHolder;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.scenebinddevicelist_item, null);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.scenebinddevicelist_item_devicename_tv);
            viewHolder.roomName = (TextView) view.findViewById(R.id.scenebinddevicelist_item_deviceroom_tv);
            viewHolder.deviceStatus = (TextView) view.findViewById(R.id.scenebinddevicelist_item_devicestatus_tv);
            viewHolder.deviceImg = (ImageView) view.findViewById(R.id.scenebinddevicelist_item_sceneimg_iv);
            viewHolder.select = (ImageView) view.findViewById(R.id.scenebinddevicelist_item_select_iv);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.deviceName.setText(list.get(i).getDeviceName());
        viewHolder.roomName.setText(list.get(i).getRoomName());

        if (list.get(i).isSelect()) {
            viewHolder.select.setVisibility(View.VISIBLE);
        } else {
            viewHolder.select.setVisibility(View.GONE);
        }
        String deviceType = list.get(i).getDeviceType();
        if ("33001".equals(deviceType)) {//普通灯
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_lights);
            if ("00".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("关闭");
            } else if ("01".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("开启");
            }
        } else if ("33002".equals(deviceType)) {//调光灯
            if ("00".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("关闭");
            } else if ("01".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("开启");
            }
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_lights);
        } else if ("33003".equals(deviceType)) {//红外人体感应
            if ("00".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("布防");
            } else if ("01".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("关闭");
            }else if ("02".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("触发");
            }
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_induction);
        } else if ("33005".equals(deviceType)) {//门磁
            if ("00".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("关闭");
            } else if ("01".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("开启");
            }
            viewHolder.deviceImg.setImageResource(R.mipmap.equipment_magnetometer);
        } else if ("33004".equals(deviceType)) {//电动窗帘
            if ("00".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("关闭");
            } else if ("01".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("开启");
            }else if ("02".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("暂停");
            }
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_window);
        } else if ("33008".equals(deviceType)) {//空气质量检测仪
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_air_quality);
        } else if ("2049".equals(deviceType)) {//摄像头
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_camera);
        } else if ("33007".equals(deviceType)) {//空调
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_binxiang);
        } else if ("33006".equals(deviceType)) {//电视
            viewHolder.deviceImg.setImageResource(R.mipmap.equ_tvshow);
        } else if ("33009".equals(deviceType)) {//插座
            if ("00".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("关闭");
            } else if ("01".equals(list.get(i).getDeviceStatus())) {
                viewHolder.deviceStatus.setText("开启");
            }
            viewHolder.deviceImg.setImageResource(R.mipmap.equipment_socket);
        }
        return view;
    }
    class ViewHolder {
        TextView deviceName, roomName, deviceStatus;
        ImageView deviceImg, select;
    }
}
