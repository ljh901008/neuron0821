package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.SwichBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/5/10.
 */
public class SceneSwichListAdapter extends BaseAdapter {
    private List<SwichBean> list;
    private Context context;

    public SceneSwichListAdapter(Context context, List<SwichBean> list) {
        this.context = context;
        this.list = list;
    }

    public List<SwichBean> getList() {
        return list;
    }

    public void setList(List<SwichBean> list) {
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
            view = LayoutInflater.from(context).inflate(R.layout.sceneswichlist_item, null);
            holder.swichName = (TextView) view.findViewById(R.id.sceneswichlist_item_swichname_tv);
            holder.swichRoom = (TextView) view.findViewById(R.id.sceneswichlist_item_swichroom_tv);
            holder.seclect_iv = (ImageView) view.findViewById(R.id.sceneswichlist_item_select_iv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.swichName.setText(list.get(i).getSwichKeyName());
        holder.swichRoom.setText(list.get(i).getSwichRoom());
        if (list.get(i).isSelect()) {
            holder.seclect_iv.setVisibility(View.VISIBLE);
        } else {
            holder.seclect_iv.setVisibility(View.GONE);
        }
        return view;
    }
    class ViewHolder{
        TextView swichName,swichRoom;
        ImageView seclect_iv;
    }
}
