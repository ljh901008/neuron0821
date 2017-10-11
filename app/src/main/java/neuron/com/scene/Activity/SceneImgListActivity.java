package neuron.com.scene.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import neuron.com.adapter.SceneImgListAdapter;
import neuron.com.bean.SceneItemBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.util.Utils;

/**
 * Created by ljh on 2017/5/11.
 */
public class SceneImgListActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemClickListener{
    private ImageButton back;
    private Button confirm;
    private ListView listView;
    private List<SceneItemBean> list;
    private SceneImgListAdapter adapter;
    private int index = 20;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sceneimglist);
        init();
    }

    private void init() {
        intent = getIntent();
        back = (ImageButton) findViewById(R.id.sceneimglist_back_iv);
        confirm = (Button) findViewById(R.id.sceneimglist_confirm_btn);
        listView = (ListView) findViewById(R.id.sceneimglist_lv);
        back.setOnClickListener(this);
        confirm.setOnClickListener(this);
        listView.setOnItemClickListener(this);
        setList();
        adapter = new SceneImgListAdapter(SceneImgListActivity.this, list);
        listView.setAdapter(adapter);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sceneimglist_back_iv://返回
                finish();
                break;
            case R.id.sceneimglist_confirm_btn://确定
                if (index == 20) {
                    Utils.showDialog(SceneImgListActivity.this, "请选择图标");
                } else {
                    intent.putExtra("sceneImg", list.get(index).getSceneImg());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        for (int i = 0; i < list.size(); i++) {
            if (i == position) {
                list.get(i).setSelect(true);
            } else {
                list.get(i).setSelect(false);
            }
            adapter.setList(list);
            adapter.notifyDataSetChanged();
            index = position;
        }
    }
    private void setList(){
        SceneItemBean bean = new SceneItemBean(0, false);
        SceneItemBean bean1 = new SceneItemBean(1, false);
        SceneItemBean bean2 = new SceneItemBean(2, false);
        SceneItemBean bean3 = new SceneItemBean(3, false);
        SceneItemBean bean4 = new SceneItemBean(4, false);
        SceneItemBean bean5 = new SceneItemBean(5, false);
        SceneItemBean bean6 = new SceneItemBean(6, false);
        SceneItemBean bean7 = new SceneItemBean(7, false);
        list = new ArrayList<SceneItemBean>();
        list.add(bean);
        list.add(bean1);
        list.add(bean2);
        list.add(bean3);
        list.add(bean4);
        list.add(bean5);
        list.add(bean6);
        list.add(bean7);
    }
}
