package neuron.com.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

import neuron.com.bean.SceneItemBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/5/11.
 */
public class SceneImgListAdapter extends BaseAdapter {
    private List<SceneItemBean> list;
    private Context context;

    public SceneImgListAdapter(Context context, List<SceneItemBean> list) {
        this.context = context;
        this.list = list;
    }

    public void setList(List<SceneItemBean> list) {
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
            view = LayoutInflater.from(context).inflate(R.layout.sceneimglist_item, null);
            holder.seclect_iv = (ImageView) view.findViewById(R.id.sceneimglist_item_select_iv);
            holder.sceneImg_iv = (ImageView) view.findViewById(R.id.sceneimglist_item_sceneimg_iv);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (list.get(i).isSelect()) {
            holder.seclect_iv.setVisibility(View.VISIBLE);
        } else {
            holder.seclect_iv.setVisibility(View.GONE);
        }
        int sceneImg = list.get(i).getSceneImg();
        switch(sceneImg){
            case 1:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_leave_not);
                break;
            case 2:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_meeting_not);
                break;
            case 3:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_movie_not);
                break;
            case 4:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_night_not);
                break;
            case 5:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_party_not);
                break;
            case 6:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_reading_not);
                break;
            case 7:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_get_up_not);
                break;
            case 0:
                holder.sceneImg_iv.setImageResource(R.mipmap.scene_back_not);
                break;
            default:
                break;
        }
        return view;
    }
    class ViewHolder{
        ImageView seclect_iv,sceneImg_iv;
    }
}
