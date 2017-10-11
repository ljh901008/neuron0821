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

import neuron.com.bean.SceneItemBean;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2016/9/13.
 */
public class SceneAdapter extends BaseAdapter {

    private List<SceneItemBean> list;

    private Context context;

    public SceneAdapter(List<SceneItemBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public List<SceneItemBean> getList() {
        return list;
    }

    public void setList(List<SceneItemBean> list) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.roomfragment_sceneitem, null);
            holder.sceneType = (TextView) convertView.findViewById(R.id.roomfragment_sceneitem_scenetype_tv);
            holder.sceneName = (TextView) convertView.findViewById(R.id.roomfragment_sceneitem_scenename_tv);
            holder.sceneBg = (RelativeLayout) convertView.findViewById(R.id.roomfragment_sceneitem_scenebg_rll);
            holder.scene_iv = (ImageView) convertView.findViewById(R.id.roomfragment_sceneitem_image_iv);
            holder.yuandian = (ImageView) convertView.findViewById(R.id.roomfragment_sceneitem_yuandian_iv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.sceneName.setText(list.get(position).getSceneName());
        int status = list.get(position).getSceneStatus();
        int sceneImg = list.get(position).getSceneImg();
        if (status == 01) {//开启状态
            holder.sceneType.setText("启用中");
            holder.yuandian.setVisibility(View.VISIBLE);
            holder.sceneBg.setBackgroundResource(R.mipmap.scene_bg);
            //Log.e("场景图标,启用", String.valueOf(sceneImg) + list.get(position).getSceneName());
            switch (sceneImg) {
                case 0:
                    holder.scene_iv.setImageResource(R.mipmap.scene_back);
                    break;
                case 1:
                    holder.scene_iv.setImageResource(R.mipmap.scene_leave);
                    break;
                case 2:
                    holder.scene_iv.setImageResource(R.mipmap.scene_meeting);
                    break;
                case 3:
                    holder.scene_iv.setImageResource(R.mipmap.scene_movie);
                    break;
                case 4:
                    holder.scene_iv.setImageResource(R.mipmap.scene_night);
                    break;
                case 5:
                    holder.scene_iv.setImageResource(R.mipmap.scene_party);
                    break;
                case 6:
                    holder.scene_iv.setImageResource(R.mipmap.scene_reading);
                    break;
                case 7:
                    holder.scene_iv.setImageResource(R.mipmap.scene_get_up);
                    break;
                default:
                    break;
            }
        } else {
            holder.sceneType.setText("未启用");
            holder.sceneBg.setBackgroundResource(R.mipmap.scene_bg_unchecked);
            holder.yuandian.setVisibility(View.GONE);
            // Log.e("场景图标，未启用", String.valueOf(sceneImg) + list.get(position).getSceneName());
            switch (sceneImg) {
                case 0:
                    holder.scene_iv.setImageResource(R.mipmap.scene_back_not);
                    break;
                case 1:
                    holder.scene_iv.setImageResource(R.mipmap.scene_leave_not);
                    break;
                case 2:
                    holder.scene_iv.setImageResource(R.mipmap.scene_meeting_not);
                    break;
                case 3:
                    holder.scene_iv.setImageResource(R.mipmap.scene_movie_not);
                    break;
                case 4:
                    holder.scene_iv.setImageResource(R.mipmap.scene_night_not);
                    break;
                case 5:
                    holder.scene_iv.setImageResource(R.mipmap.scene_party_not);
                    break;
                case 6:
                    holder.scene_iv.setImageResource(R.mipmap.scene_reading_not);
                    break;
                case 7:
                    holder.scene_iv.setImageResource(R.mipmap.scene_get_up_not);
                    break;
                default:
                    break;
            }
        }
        return convertView;
    }
    class ViewHolder{
        TextView sceneName,sceneType;
        ImageView scene_iv,yuandian;
        RelativeLayout sceneBg;
    }
}
