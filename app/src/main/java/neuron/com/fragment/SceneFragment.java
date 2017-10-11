package neuron.com.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import neuron.com.comneuron.R;
import neuron.com.view.ChildViewPager;

/**
 * Created by ljh on 2016/8/28.
 */
public class SceneFragment extends Fragment implements View.OnClickListener{
    private Button deviceSet_btn, scene_btn;
    private DeviceSetFragment deviceSetFragment;
    private SceneSetFragment sceneSetFragment;
    private View view;
    private ChildViewPager mPaper;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments = new ArrayList<Fragment>();
    private RelativeLayout fragment_rll;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scene, container, false);
        init();
        initView();
        return view;
    }
    private void init(){
        scene_btn = (Button) view.findViewById(R.id.scenefragment_scene_btn);
        deviceSet_btn = (Button) view.findViewById(R.id.scenefragment_set_btn);
        mPaper = (ChildViewPager) view.findViewById(R.id.scene_viewpage);
        fragment_rll = (RelativeLayout) view.findViewById(R.id.scene_fragment_rll);
        deviceSet_btn.setOnClickListener(this);
        scene_btn.setOnClickListener(this);
        sceneSetFragment = new SceneSetFragment();
        deviceSetFragment = new DeviceSetFragment();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        clearChoice();
        scene_btn.setBackgroundResource(R.drawable.whiteframe_1);
        scene_btn.setTextColor(getResources().getColor(R.color.text_scenetitle_blue));
        fragmentTransaction.add(R.id.scene_fragment_rll,sceneSetFragment);
        fragmentTransaction.add(R.id.scene_fragment_rll, deviceSetFragment);
        fragmentTransaction.hide(sceneSetFragment);
        fragmentTransaction.show(deviceSetFragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.scenefragment_scene_btn://设备页面
                clearChoice();
                scene_btn.setBackgroundResource(R.drawable.whiteframe_1);
                scene_btn.setTextColor(getResources().getColor(R.color.text_scenetitle_blue));
                FragmentTransaction fragmentTransaction1 = getChildFragmentManager().beginTransaction();
                fragmentTransaction1.hide(sceneSetFragment);
                fragmentTransaction1.show(deviceSetFragment);
                fragmentTransaction1.commit();
                break;
            case R.id.scenefragment_set_btn://场景设置页面
                clearChoice();
                deviceSet_btn.setBackgroundResource(R.drawable.whiteframe_2);
                deviceSet_btn.setTextColor(getResources().getColor(R.color.text_scenetitle_blue));
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                fragmentTransaction.hide(deviceSetFragment);
                fragmentTransaction.show(sceneSetFragment);
                fragmentTransaction.commit();
                break;
            default:
                break;
        }
    }


    private void clearChoice(){
        deviceSet_btn.setBackgroundResource(R.color.white_00000000);
        scene_btn.setBackgroundResource(R.color.white_00000000);
        deviceSet_btn.setTextColor(getResources().getColor(R.color.white));
        scene_btn.setTextColor(getResources().getColor(R.color.white));
    }
    private void initView(){
        deviceSet_btn.setBackgroundResource(R.color.white_00000000);
        scene_btn.setBackgroundResource(R.drawable.whiteframe_1);
        deviceSet_btn.setTextColor(getResources().getColor(R.color.white));
        scene_btn.setTextColor(getResources().getColor(R.color.text_scenetitle_blue));
    }
}



