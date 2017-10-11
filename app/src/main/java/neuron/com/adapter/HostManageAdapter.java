package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.HostManagerItemBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/3/8.
 */
public class HostManageAdapter extends BaseAdapter {
    private List<HostManagerItemBean> list;
    private Context context;

    public HostManageAdapter(List<HostManagerItemBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(List<HostManagerItemBean> list) {
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
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.hostmanager_item, null);
            holder.hostImg_iv = (ImageView) view.findViewById(R.id.hostmanager_item_hostimage_iv);
            holder.select_iv = (ImageView) view.findViewById(R.id.hostmanager_item_select_iv);
            holder.hostName_tv = (TextView) view.findViewById(R.id.hostmanager_item_hostname_tv);
            holder.select_tv = (TextView) view.findViewById(R.id.hostmanager_item_selectname_tv);
            holder.select_rll = (RelativeLayout) view.findViewById(R.id.hostmanager_item_right_rll);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.hostName_tv.setText(list.get(position).getHostManagerName());
        int isSelect = list.get(position).getHostState();
        if (isSelect == 0) {//默认控制主机
            holder.select_rll.setVisibility(View.VISIBLE);
        } else if (isSelect == 1) {
            holder.select_rll.setVisibility(View.GONE);
        }
        int isVisible = list.get(position).getIsVisible();
        if (isVisible == 0) {//给子帐号设置控制主机时不显示文字
            holder.select_tv.setVisibility(View.GONE);
        } else if (isVisible == 1) {
            holder.select_tv.setVisibility(View.VISIBLE);
        }

        return view;
    }
    class ViewHolder{
        ImageView hostImg_iv, select_iv;
        TextView hostName_tv, select_tv;
        RelativeLayout select_rll;
    }
}
