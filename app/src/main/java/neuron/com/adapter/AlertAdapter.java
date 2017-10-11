package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/3/28.
 */
public class AlertAdapter extends BaseAdapter {
    private List<Map<String, String>> list;
    private Context context;

    public AlertAdapter(List<Map<String, String>> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(List<Map<String, String>> list) {
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
            view = LayoutInflater.from(context).inflate(R.layout.alert_item, null);
            holder.time = (TextView) view.findViewById(R.id.alert_item_time_tv);
            holder.content = (TextView) view.findViewById(R.id.alert_item_content_tv);
            holder.bottom = view.findViewById(R.id.alert_item_view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.time.setText(list.get(i).get("time"));
        holder.content.setText(list.get(i).get("content"));
        return view;
    }

    class ViewHolder {
        TextView time, content;
        View bottom;
    }
}
