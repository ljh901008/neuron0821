package neuron.com.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import neuron.com.bean.AccountDataBean;
import neuron.com.comneuron.R;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/3/23.
 */
public class ChildAccountManagerAdapter extends BaseAdapter {
    private List<AccountDataBean> list;
    private Context context;

    public ChildAccountManagerAdapter(List<AccountDataBean> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setList(List<AccountDataBean> list) {
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
            view = LayoutInflater.from(context).inflate(R.layout.childaccountmanager_item, null);
            holder.photo = (ImageView) view.findViewById(R.id.childaccountmanager_photo_civ);
            holder.isSelect = (ImageView) view.findViewById(R.id.childaccountmanager_check_iv);
            holder.userName = (TextView) view.findViewById(R.id.childaccountmanager_name_tv);
            holder.accountNumber = (TextView) view.findViewById(R.id.childaccountmanager_account_tv);
            holder.right_iv = (ImageView) view.findViewById(R.id.childaccountmanager_right_iv);
            holder.bottom = view.findViewById(R.id.childaccountmanager_bottom);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        boolean tag = list.get(i).isEdit();
        if (tag) {//初始化状态
            holder.isSelect.setVisibility(View.GONE);
        } else {//编辑状态
            holder.isSelect.setVisibility(View.VISIBLE);
            if (list.get(i).isSelect()) {
                holder.isSelect.setImageResource(R.mipmap.share_account_hook);
            } else {
                holder.isSelect.setImageResource(R.mipmap.share_account_uncheck);
            }
        }
        boolean rightIsShow = list.get(i).isShow();
        if (rightIsShow) {
            holder.right_iv.setVisibility(View.VISIBLE);
        } else {
            holder.right_iv.setVisibility(View.GONE);
        }
        boolean setText = list.get(i).isSetText();
        if (setText) {
            holder.userName.setTextColor(Color.rgb(255, 255, 255));
            holder.accountNumber.setTextColor(Color.rgb(203, 203, 203));
        } else {
            XutilsHelper xutilsHelper = new XutilsHelper();
            xutilsHelper.downloadPhoto(holder.photo, context, list.get(i).getPhotoPath());
        }
        holder.accountNumber.setText(list.get(i).getAccoungNumber());
        holder.userName.setText(list.get(i).getUserName());

        return view;
    }
    class ViewHolder{
        TextView userName,accountNumber;
        ImageView isSelect,right_iv;
        ImageView photo;
        View bottom;
    }
}
