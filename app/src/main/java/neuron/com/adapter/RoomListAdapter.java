package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.SY_PopuWBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2016/11/15.  房间选择界面的activity
 */
public class RoomListAdapter extends BaseAdapter {

    private Context context;
    private List<SY_PopuWBean> list;

    public RoomListAdapter(Context context, List<SY_PopuWBean> list) {
        this.context = context;
        this.list = list;
    }

    public List<SY_PopuWBean> getList() {
        return list;
    }

    public void setList(List<SY_PopuWBean> list) {
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.roomlist_item, null);
            holder.homeName = (TextView) view.findViewById(R.id.roomlist_item_tv);
            holder.seclect_iv = (ImageView) view.findViewById(R.id.roomlist_item_iv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.homeName.setText(list.get(i).getHomeName());
        if (list.get(i).getIsSelect() == 0) {//0表示未选中
            holder.seclect_iv.setVisibility(View.GONE);
        } else {// 1表示选中
            holder.seclect_iv.setVisibility(View.VISIBLE);
        }
        return view;
    }
    class ViewHolder{
        TextView homeName;
        ImageView seclect_iv;
    }
}
