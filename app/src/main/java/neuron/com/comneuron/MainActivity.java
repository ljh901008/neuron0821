package neuron.com.comneuron;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import neuron.com.adapter.ViewPagerAdapter;
import neuron.com.app.JPushBroadcastReceiver;
import neuron.com.app.OgeApplication;
import neuron.com.fragment.MeFragment;
import neuron.com.fragment.RoomFragment;
import neuron.com.fragment.SceneFragment;
import neuron.com.view.MyViewpage;

public class MainActivity extends BaseActivity {
    private String TAG = "MainActivity";
    //底部imageView,  textView
    private ImageView room_iv,scene_iv,mine_iv;
    private TextView room_tv,scene_tv,mine_tv;
    private RelativeLayout roomRlly,secneRlly,mineRlly;

    //fragement
    MeFragment meFragment;
    RoomFragment roomFragment;
    SceneFragment sceneFragment;
    //管理器
    private FragmentManager fragmanager;
    //自定义viewPage 和 adapter
    private MyViewpage myPager;
    private ViewPagerAdapter viewPagerAdapter;
    private ArrayList<Fragment> fragmentlist;
    MyOnclick myOnclick;
    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;
    private Intent intent;
    JPushBroadcastReceiver jPushBroadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initView();
        setOnclick();
    }
    //初始化控件
    private void init(){
        intent = getIntent();

        //底部bottom布局   图片  文字
        room_iv = (ImageView) findViewById(R.id.bottom_room_iv);
        scene_iv = (ImageView) findViewById(R.id.bottom_scene_iv);
        mine_iv = (ImageView) findViewById(R.id.bottom_me_iv);

        room_tv = (TextView) findViewById(R.id.bottom_room_tv);
        scene_tv = (TextView) findViewById(R.id.bottom_scene_tv);
        mine_tv = (TextView) findViewById(R.id.bottom_me_tv);

        roomRlly = (RelativeLayout) findViewById(R.id.bottom_room_rlly);
        secneRlly = (RelativeLayout) findViewById(R.id.bottom_scene_rlly);
        mineRlly = (RelativeLayout) findViewById(R.id.bottom_me_rlly);

        meFragment = new MeFragment();
        roomFragment = new RoomFragment();
        sceneFragment = new SceneFragment();

        myPager = (MyViewpage) findViewById(R.id.activity_main_myviewpager);
        myPager.setOffscreenPageLimit(3);  //设置有三个fragment
        myPager.setOnPageChangeListener(null);//设置不可以滑动

    }

    /**
     * 初始化UI
     */
    private void initView(){
        fragmentlist = new ArrayList<Fragment>();
        fragmentlist.add(roomFragment);
        fragmentlist.add(sceneFragment);
        fragmentlist.add(meFragment);
        fragmanager = getSupportFragmentManager();
        viewPagerAdapter = new ViewPagerAdapter(fragmanager,fragmentlist);
        myPager.setAdapter(viewPagerAdapter);
        iconchange(R.id.bottom_room_rlly);
    }
    //设置点击事件
    private void setOnclick(){
        myOnclick = new MyOnclick();
        roomRlly.setOnClickListener(myOnclick);
        secneRlly.setOnClickListener(myOnclick);
        mineRlly.setOnClickListener(myOnclick);
    }



    private class MyOnclick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            iconchange(view.getId());
        }
    }
    /**
     *
     * @param id :点击底部bottom的id
     */
    private void iconchange(int id){
        clearChoice();
        switch (id){
            case R.id.bottom_room_rlly://房间，首页
                room_iv.setBackgroundResource(R.mipmap.button_home);
               // room_tv.setTextColor(getResources().getColor(R.color.white));
                myPager.setCurrentItem(0);
                break;
            case R.id.bottom_scene_rlly://场景
                scene_iv.setBackgroundResource(R.mipmap.button_scene);
               // scene_tv.setTextColor(getResources().getColor(R.color.white));
                myPager.setCurrentItem(1);
                break;
            case R.id.bottom_me_rlly://我的
                mine_iv.setBackgroundResource(R.mipmap.button_my);
               // mine_tv.setTextColor(getResources().getColor(R.color.white));
                myPager.setCurrentItem(2);
                break;
            default:
                break;
        }
    }
    private Bitmap bitmap;
    /**
     * 初始化布局
     */
    private void clearChoice(){
        room_iv.setBackgroundResource(R.mipmap.button_home_unchecked);
       // room_tv.setTextColor(getResources().getColor(R.color.white));
        scene_iv.setBackgroundResource(R.mipmap.button_scene_unchecked);
       // scene_tv.setTextColor(getResources().getColor(R.color.white));
        mine_iv.setBackgroundResource(R.mipmap.button_my_unchecked);
        //mine_tv.setTextColor(getResources().getColor(R.color.white));

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            RelativeLayout pager2 = (RelativeLayout) findViewById(R.id.pager2);
            RelativeLayout pager3 = (RelativeLayout) findViewById(R.id.pager3);
            if(pager3.getVisibility() == View.VISIBLE){
                pager3.setVisibility(View.GONE);
            }else {
                if(pager2.getVisibility() == View.VISIBLE){
                    pager2.setVisibility(View.GONE);
                    findViewById(R.id.floatingActionButton).setVisibility(View.VISIBLE);
                }else {
                    exit();
                }
            }

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出程序",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 1300);
        } else {
            OgeApplication.quiteApplication();

        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

}
