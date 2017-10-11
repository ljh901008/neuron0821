package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.HostManagerDataItemBean;
import neuron.com.comneuron.R;

import static neuron.com.comneuron.R.id.hostmanagerdata_item_bottom_view;

/**
 * Created by ljh on 2017/3/9.
 */
public class HostManagerDataAdapter extends BaseAdapter {
    private List<HostManagerDataItemBean> list;
    private Context context;

    public HostManagerDataAdapter(List<HostManagerDataItemBean> list, Context context) {
        this.list = list;
        this.context = context;
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.hostmanagerdata_item, null);
            holder.photo = (ImageView) view.findViewById(R.id.hostmanagerdata_item_photo_iv);
            holder.userName = (TextView) view.findViewById(R.id.hostmanagerdata_item_name_tv);
            holder.account = (TextView) view.findViewById(R.id.hostmanagerdata_item_account_tv);
            holder.bottomview = view.findViewById(hostmanagerdata_item_bottom_view);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.userName.setText(list.get(position).getAccountName());
        holder.account.setText(list.get(position).getAccount());
        return view;
    }
    class ViewHolder{
        ImageView photo;
        TextView userName, account;
        View bottomview;
    }
}
