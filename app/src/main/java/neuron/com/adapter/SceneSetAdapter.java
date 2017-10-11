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
 * Created by ljh on 2017/5/9. 场景设置fragment的 适配器
 */
public class SceneSetAdapter extends BaseAdapter {

    private List<AirQualityBean> list;
    private Context context;

    public SceneSetAdapter(Context context, List<AirQualityBean> list) {
        this.context = context;
        this.list = list;
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
        ViewHolder viewHolder = null;
        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.sceneset_item, null);
            viewHolder.sceneImg = (ImageView) view.findViewById(R.id.sceneset_item_img);
            viewHolder.sceneName = (TextView) view.findViewById(R.id.sceneset_item_scenename_tv);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.sceneName.setText(list.get(i).getSceneName());
        String sceneImg = list.get(i).getSceneImg();
        switch(sceneImg){
            case "1":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_leave);
                break;
            case "2":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_meeting);
                break;
            case "3":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_movie);
                break;
            case "4":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_night);
                break;
            case "5":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_party);
                break;
            case "6":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_reading);
                break;
            case "7":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_up);
                break;
            case "0":
                viewHolder.sceneImg.setImageResource(R.mipmap.set_up_scene_back);
                break;
            default:
                break;
        }
        return view;
    }
    class ViewHolder{
        TextView sceneName;
        ImageView sceneImg;
    }
}
