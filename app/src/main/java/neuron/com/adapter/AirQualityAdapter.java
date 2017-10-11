package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.AirQualityBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/4/12.
 */
public class AirQualityAdapter extends BaseAdapter {
    private List<AirQualityBean> list;
    private Context context;

    public AirQualityAdapter(List<AirQualityBean> list, Context context) {
        this.list = list;
        this.context = context;
    }
    public void setList(List<AirQualityBean> list) {
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
        ViewHolder holder = null;
        if (view == null) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.airqualityscene_item, null);
            holder.sceneName = (TextView) view.findViewById(R.id.airqualityscene_item_name_tv);
            holder.seclect_iv = (ImageView) view.findViewById(R.id.airqualityscene_item_scelet_iv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.sceneName.setText(list.get(i).getSceneName());
        boolean b = list.get(i).isSelect();
        if (b) {
            holder.seclect_iv.setVisibility(View.VISIBLE);
        } else {
            holder.seclect_iv.setVisibility(View.GONE);
        }
        return view;
    }
    class ViewHolder{
        TextView sceneName;
        ImageView seclect_iv;
    }
}
