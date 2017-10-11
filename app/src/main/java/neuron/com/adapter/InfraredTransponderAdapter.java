package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.DeviceSetFragmentBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/5/4.
 */
public class InfraredTransponderAdapter extends BaseAdapter{
    private List<DeviceSetFragmentBean> list;
    private Context context;

    public InfraredTransponderAdapter(Context context, List<DeviceSetFragmentBean> list) {
        this.context = context;
        this.list = list;
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
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.infraredtransponder_item, null);
            holder.deviceName = (TextView) view.findViewById(R.id.infraredtransponder_item_devicename_tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.deviceName.setText(list.get(i).getDeviceName());
        return view;
    }
    class ViewHolder{
        TextView deviceName;
    }
}
