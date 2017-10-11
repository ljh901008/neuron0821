package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import neuron.com.comneuron.R;

/**
 * Created by ljh on 2016/11/11. 设备详情页 item
 */
public class DeviceDetailAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;

    public DeviceDetailAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    public List<String> getList() {
        return list;
    }

    public void setList(List<String> list) {
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
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.devicedetail_item, null);
            holder.deviceName_tv = (TextView) view.findViewById(R.id.devicedetail_itemname_tv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.deviceName_tv.setText(list.get(position));
        return view;
    }
    class ViewHolder{
        TextView deviceName_tv;
    }
}
