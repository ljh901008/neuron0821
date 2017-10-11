package neuron.com.room.Activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.nbhope.smarthome.smartlib.bean.home.HopeDevice;
import cn.nbhope.smarthome.smartlib.bean.music.HopeMusic;
import cn.nbhope.smarthome.smartlib.bean.net.response.CmdResponse;
import cn.nbhope.smarthome.smartlib.bean.net.response.music.SongResponse;
import cn.nbhope.smarthome.smartlib.model.common.RequestModel;
import cn.nbhope.smarthome.smartlib.net.APIService;
import cn.nbhope.smarthome.smartlib.net.AppCommandType;
import cn.nbhope.smarthome.smartlib.net.RetrofitFactory;
import cn.nbhope.smarthome.smartlib.service.SocketService;
import cn.nbhope.smarthome.smartlib.socket.HopeSocketApi;
import cn.nbhope.smarthome.smartlib.socket.SocketResultEvent;
import neuron.com.adapter.ChildAccountManagerAdapter;
import neuron.com.app.OgeApplication;
import neuron.com.bean.AccountDataBean;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ljh on 2017/9/8.
 */

public class HOPE_SongListActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener{
    private String TAG = "HOPE_SongListActivity";
    private ImageButton back_ibtn, edit_ibtn;
    private TextView deviceName_tv, deviceRoom_tv;
    //蓝牙  本地   外接线
    private ImageButton bluetooth_ibtn,local_itbn, dl_ibtn;
    //歌曲数目
    private TextView songNum_tv;
    //播放模式
    private TextView playPattern_tv;
    private ImageView playPattern_iv;
    private RelativeLayout playPattern_rll;
    private PullToRefreshListView pullToRefreshListView;
    //歌曲开始和结束时间
    private TextView startTime_tv, endTime_tv;
    private SeekBar seekBar;
    //歌名 和 歌手
    private TextView songName_tv, singerName_tv;
    //播放  上一曲 下一曲 音量加 音量减
    private ImageButton play_ibtn,upSong_itbn,nextSong_ibtn,soundAdd_ibtn, soundJian_ibtn;
    private APIService service;
    private RequestModel requestModel = RequestModel.getInstance();
    private String serialNum,deviceName,deviceRoom,account,deviceId,deviceType,deviceRoomId;
    private Intent intent;
    private SharedPreferencesManager sharedPreferencesManager;
    private ChildAccountManagerAdapter adapter;
    private List<AccountDataBean> listBean = new ArrayList<AccountDataBean>();
    //歌曲页码
    private int songPage = 1;
    //歌曲总数
    private String songNum;
    private int musicMode;
    //最大音量，当前音量
    private int maxVol, currentVol;
    private int songPro;//当前进度
    private int duration;//总时长进度
    private Gson gson = new Gson();
    //歌曲播放状态  1暂停，2播放
    private int songState = 1;

    private Subscriber<Long> mSubscriber;
    private Observable<Long> mObservable;
    private Toast toast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hopeactivity);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        service = RetrofitFactory.getInstance().createRetrofit(URLUtils.BASE_URL).create(APIService.class);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        init();
        EventBus.getDefault().register(this);
        hopeLogin(account);
        setListener();
        pullToRefreshListView.getLoadingLayoutProxy().setPullLabel("下拉刷新");
        pullToRefreshListView.getLoadingLayoutProxy().setReleaseLabel("松开即可刷新");
        pullToRefreshListView.setMode(PullToRefreshBase.Mode.BOTH);//设置刷新样式，可上拉 可下拉
        pullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                songPage = 1;
                //下拉刷新
                getSongList(serialNum,songPage,10,"","","");
                new GetDataTask().execute();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                int songnum = Integer.valueOf(songNum);
                if (songnum % 10 > 0) {
                    if (songPage < (songnum / 10) + 1) {
                        //上拉加载
                        songPage = songPage+1;
                        getSongList(serialNum, songPage, 10, "", "", "");
                        new GetDataTask().execute();
                    } else {
                        Toast.makeText(HOPE_SongListActivity.this, "没有更多歌曲", Toast.LENGTH_LONG).show();
                        new GetDataTask().execute();
                    }
                } else if ((songnum % 10) == 0) {
                    if (songPage < songnum / 10) {
                        //上拉加载
                        songPage = songPage+1;
                        getSongList(serialNum, songPage, 10, "", "", "");
                        new GetDataTask().execute();
                    } else {
                        Toast.makeText(HOPE_SongListActivity.this, "没有更多歌曲", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        service.loadDevice(requestModel.generateLoadDevice(1,10)) //服务器请求设备列表 用requestModel生成请求数据包 请求第1页数据，每页10条
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(deviceResponse -> {
                    List<HopeDevice> deviceList = deviceResponse.getData().getDataList(); //返回设备列表
                    boolean isOline = deviceList.get(0).getIsOnline();
                    Log.e(TAG + "在线状态", String.valueOf(isOline));
                    if (!isOline) {
                        Utils.showDialog(HOPE_SongListActivity.this,"设备不在线");
                    }
                },throwable -> {
                });
    }
    private void init(){
        intent = getIntent();
        serialNum = intent.getStringExtra("serialNum");
        Log.e(TAG + "音响serial", serialNum);
        deviceName = intent.getStringExtra("deviceName");
        deviceRoom = intent.getStringExtra("deviceRoom");
        deviceId = intent.getStringExtra("deviceId");
        deviceType = intent.getStringExtra("deviceType");
        deviceRoomId = intent.getStringExtra("roomId");
        account = intent.getStringExtra("thirdaccount");
        back_ibtn = (ImageButton) findViewById(R.id.hopeactivity_back_ibtn);
        edit_ibtn = (ImageButton) findViewById(R.id.hopeactivity_edit_btn);
        deviceName_tv = (TextView) findViewById(R.id.hopeactivity_devicename_tv);
        deviceRoom_tv = (TextView) findViewById(R.id.hopeactivity_roomnama_tv);
        bluetooth_ibtn = (ImageButton) findViewById(R.id.hopeactivity_bt_ibtn);
        local_itbn = (ImageButton) findViewById(R.id.hopeactivity_local_ibtn);
        dl_ibtn = (ImageButton) findViewById(R.id.hopeactivity_dl_ibtn);
        songNum_tv = (TextView) findViewById(R.id.hopeactovity_songnum_tv);
        playPattern_tv = (TextView) findViewById(R.id.hopeactivity_palypattern_tv);
        playPattern_iv = (ImageView) findViewById(R.id.hopeactivity_palypattern_iv);
        playPattern_rll = (RelativeLayout) findViewById(R.id.hopeactivity_playpattern_rll);
        startTime_tv = (TextView) findViewById(R.id.hopeactovity_songstartime_tv);
        endTime_tv = (TextView) findViewById(R.id.hopeactovity_songendtime_tv);
        seekBar = (SeekBar) findViewById(R.id.hopeactiovity_song_pro);
        songName_tv = (TextView) findViewById(R.id.hopeactivity_songname_tv);
        singerName_tv = (TextView) findViewById(R.id.hopeactivity_singername_tv);
        play_ibtn = (ImageButton) findViewById(R.id.hopeactiovity_play_ibtn);
        upSong_itbn = (ImageButton) findViewById(R.id.hopeactivity_upsong_ibtn);
        nextSong_ibtn = (ImageButton) findViewById(R.id.hopeactivity_nextsong_ibtn);
        soundAdd_ibtn = (ImageButton) findViewById(R.id.hopeactivity_addsound_ibtn);
        soundJian_ibtn = (ImageButton) findViewById(R.id.hopeactivity_jiansound_ibtn);
        pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.hopeactivity_pulllistview);
        deviceName_tv.setText(deviceName);
        deviceRoom_tv.setText(deviceRoom);
        /*WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        int height = windowManager.getDefaultDisplay().getHeight();
        Rect rectangle= new Rect();
        Window window= getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight= rectangle.top;
        ViewGroup.LayoutParams viewGroup = pullToRefreshListView.getLayoutParams();
        viewGroup.height = height - 688 - statusBarHeight;
        pullToRefreshListView.setLayoutParams(viewGroup);*/
    }
    private void setListener(){
       back_ibtn.setOnClickListener(this);
       edit_ibtn.setOnClickListener(this);
       bluetooth_ibtn.setOnClickListener(this);
       local_itbn.setOnClickListener(this);
       dl_ibtn.setOnClickListener(this);
       playPattern_rll.setOnClickListener(this);

       play_ibtn.setOnClickListener(this);
       upSong_itbn.setOnClickListener(this);
       nextSong_ibtn.setOnClickListener(this);
       soundAdd_ibtn.setOnClickListener(this);
       soundJian_ibtn.setOnClickListener(this);
       pullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               OgeApplication.HopeSendData(HopeSocketApi.musicPlayEx(serialNum, String.valueOf(position-1), sharedPreferencesManager.get("HopeToken")));
           }
       });
   }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG + "SocketService", "onServiceConnected");
            SocketService.IBinderSocket iBinderSocket = (SocketService.IBinderSocket) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG + "SocketService", "onServiceDisconnected");
        }
    };

    /**
     *  登录hope服务器
     * @param userName
     */
    private void hopeLogin(String userName){
      //  service.getDeviceCommandActionParams(@Body )
        //登录方法需要3个参数，其中服务器时间需要从服务器取得，所以先用service取得服务器时间再进行登录请求
                Observable.just(null)
                .map(s -> requestModel.generateServiceTimeCmd()) //取得请求服务器时间请求包
                .flatMap(cmd -> service.getServerTime(cmd)) //请求服务器获取时间
                .flatMap((timeResponse) -> {
                    String time = timeResponse.getData().getTime(); //返回服务器时间
                    return service.login(requestModel.generateVerifyExternalUser(time, userName, URLUtils.AppKey, URLUtils.SecretKey));  //请求登录
                })
                .doOnNext(response -> { //返回数据进行处理
                            if (AppCommandType.SUCCESS.equals(response.getResult())) {     //访问成功，保存用户
                                Log.e("HOPE_login", response.getData().getMobileNo() + "," + response.getData().getToken());
                                sharedPreferencesManager.save("HopeToken", response.getData().getToken());
                                Intent intent1 = new Intent(this, SocketService.class);
                                intent1.putExtra("token", response.getData().getToken());
                                bindService(intent1, serviceConnection, Service.BIND_AUTO_CREATE);//登录以后启动Socket
                                getSongList(serialNum, songPage, 10, "", "", "");//登录成功获取歌曲列表
                            } else {
                                throw new IllegalStateException(response.getData().getMessage());  //访问失败，抛出异常}
                            }
                            })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {

                }, throwable -> {

                });
    }

    /**
     *  获取歌曲列表
     * @param serial  音响id
     * @param page       页码
     * @param size       歌曲数目
     * @param songName      歌名
     * @param artist        艺人
     * @param album         专辑
     */
    private void getSongList(String serial,int page,int size,String songName, String artist, String album){
        Log.v(TAG + "获取歌曲列表的数据", serial + "," + page + "," + size + "," + songName + "," + artist + "," + album);
        service.searchSong(requestModel.generateSearchSong(serial, page, size, songName, artist, album, false))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<SongResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SongResponse songResponse) {
                        songNum = songResponse.getData().getTotal();
                        songNum_tv.setText("全部歌曲共" + songNum + " 首");
                        List<HopeMusic> list= songResponse.getData().getSongList();
                        AccountDataBean bean;
                        if (list != null) {
                            for (int i = 0; i < list.size(); i++) {
                                bean = new AccountDataBean();
                                bean.setUserName(list.get(i).getTitle());
                                bean.setAccoungNumber(list.get(i).getArtist());
                                bean.setEdit(true);
                                bean.setShow(true);
                                bean.setSetText(true);
                                listBean.add(bean);
                            }
                            if (adapter == null) {
                                adapter = new ChildAccountManagerAdapter(listBean, HOPE_SongListActivity.this);
                                pullToRefreshListView.setAdapter(adapter);
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                        } else {

                        }
                    }
                });
        OgeApplication.HopeSendData(HopeSocketApi.initPlayerState(serialNum,sharedPreferencesManager.get("HopeToken")));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketResultEvent socketResultEvent) {
        CmdResponse cmdResponse = socketResultEvent.getResponse();
        JsonObject jsonObject = cmdResponse.getData();
        String cmd = cmdResponse.getCmd();
        Log.v(TAG + "Cmd：", cmd);
        switch(cmd){
            case HopeSocketApi.CMD_INIT_STATE://初始化状态
                    maxVol = jsonObject.get("MaxVol").getAsInt();
                    currentVol = jsonObject.get("CurrentVol").getAsInt();
                    songPro = jsonObject.get("Progress").getAsInt();
                    songState = jsonObject.get("State").getAsInt();
                    songName_tv.setText(jsonObject.get("title").getAsString());
                    singerName_tv.setText(jsonObject.get("artist").getAsString());
                    if (songState == 1) {//暂停
                        play_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_play));
                    } else if (songState == 2) {//播放
                        play_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_time_out));
                    }
                    if (songPro == 0) {
                        startTime_tv.setText("0:00");
                    } else {
                        int minute = songPro / 60;
                        int second = songPro % 60;
                        String m = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
                        String s = second < 10 ? "0" + String.valueOf(second) : String.valueOf(second);
                        startTime_tv.setText(m + ":" + s);
                    }
                    duration = jsonObject.get("Duration").getAsInt();
                    seekBar.setMax(duration);
                    seekBar.setProgress(songPro);
                    int mm = duration / 60;
                    int ss = duration % 60;
                    String m1 = mm < 10 ? "0" + String.valueOf(mm) : String.valueOf(mm);
                    String s1 = ss < 10 ? "0" + String.valueOf(ss) : String.valueOf(ss);
                    endTime_tv.setText(m1+ ":" + s1);
                    int source = jsonObject.get("Source").getAsInt();
                    switch (source) {
                        case 1://本地
                            bluetooth_ibtn.setImageResource(R.mipmap.sound_bt_not);
                            local_itbn.setImageResource(R.mipmap.sound_local);
                            dl_ibtn.setImageResource(R.mipmap.sound_dl_not);
                            break;
                        case 2://外接
                            bluetooth_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_bt_not));
                            local_itbn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_local_not));
                            dl_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_dl));
                            break;
                        case 3://蓝牙
                            bluetooth_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_bt));
                            local_itbn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_local_not));
                            dl_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_dl_not));
                            break;
                        default:
                            break;
                    }
                    musicMode = jsonObject.get("Mode").getAsInt();
                    switch (musicMode) {
                        case 1://随机播放
                            playPattern_tv.setText("随机播放");
                            // playPattern_iv.setImageResource(R.mipmap.sound_shuffle_playback);
                            playPattern_iv.setImageDrawable(getResources().getDrawable(R.mipmap.sound_shuffle_playback));
                            break;
                        case 2://全部循环
                            playPattern_tv.setText("全部循环");
                            playPattern_iv.setImageDrawable(getResources().getDrawable(R.mipmap.sound_play_order));
                            break;
                        case 3://单曲循环
                            playPattern_tv.setText("单曲循环");
                            playPattern_iv.setImageDrawable(getResources().getDrawable(R.mipmap.sound_single_cyclet));
                            break;
                        default:
                            break;
                    }


                break;
            case HopeSocketApi.CMD_MUSIC_PLAY://播放
                play_ibtn.setImageResource(R.mipmap.sound_time_out);
                maxVol = jsonObject.get("MaxVol").getAsInt();
                currentVol = jsonObject.get("CurrentVol").getAsInt();
                songPro = jsonObject.get("Progress").getAsInt();
                songName_tv.setText(jsonObject.get("title").getAsString());
                songState = jsonObject.get("State").getAsInt();
                singerName_tv.setText(jsonObject.get("artist").getAsString());
                if (songPro == 0) {
                    startTime_tv.setText("0:00");
                } else {
                    int minute = songPro / 60;
                    int second = songPro % 60;
                    String m = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
                    String s = second < 10 ? "0" + String.valueOf(second) : String.valueOf(second);
                    startTime_tv.setText(m + ":" + s);
                }
                duration = jsonObject.get("Duration").getAsInt();
                seekBar.setMax(duration);
                seekBar.setProgress(songPro);
                int mm1 = duration / 60;
                int ss1 = duration % 60;
                String m2 = mm1 < 10 ? "0" + String.valueOf(mm1) : String.valueOf(mm1);
                String s2 = ss1 < 10 ? "0" + String.valueOf(ss1) : String.valueOf(ss1);
                endTime_tv.setText(m2+ ":" + s2);
                    handleTimeProgress(songPro,duration);//开始进度条
                break;
            case HopeSocketApi.CMD_MUSIC_PAUSE://暂停
                songState = jsonObject.get("State").getAsInt();
                //play_ibtn.setImageResource(R.mipmap.sound_play);
                play_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_play));
                unSubscribe();//进度条停止
                break;
            case HopeSocketApi.CMD_MUSIC_PLAY_EX://切换歌曲

                break;
            case HopeSocketApi.CMD_MUSIC_VOLUME://声音设置
                toastShoW("音量:"+jsonObject.get("Volume").getAsString());
                break;
            case HopeSocketApi.CMD_MUSIC_LOOP_MODE://播放模式设置
                musicMode = jsonObject.get("Mode").getAsInt();
                switch (musicMode) {
                    case 1://随机播放
                        playPattern_tv.setText("随机播放");
                        playPattern_iv.setImageDrawable(getResources().getDrawable(R.mipmap.sound_shuffle_playback));
                        break;
                    case 2://全部循环
                        playPattern_tv.setText("全部循环");
                        playPattern_iv.setImageDrawable(getResources().getDrawable(R.mipmap.sound_play_order));
                        break;
                    case 3://单曲循环
                        playPattern_tv.setText("单曲循环");
                        playPattern_iv.setImageDrawable(getResources().getDrawable(R.mipmap.sound_single_cyclet));
                        break;
                    default:
                        break;
                }
                break;
            case HopeSocketApi.CMD_MUSIC_CHANGE_SOURCE://音源设置
                int SourceType = jsonObject.get("SourceType").getAsInt();
                switch (SourceType) {
                    case 1://本地
                        bluetooth_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_bt_not));
                        local_itbn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_local));
                        dl_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_dl_not));
                        break;
                    case 2://外接
                        bluetooth_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_bt_not));
                        local_itbn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_local_not));
                        dl_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_dl));
                        break;
                    case 3://蓝牙
                        bluetooth_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_bt));
                        local_itbn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_local_not));
                        dl_ibtn.setImageDrawable(getResources().getDrawable(R.mipmap.sound_dl_not));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.hopeactivity_back_ibtn://返回键
                finish();
                break;
            case R.id.hopeactivity_edit_btn://编辑
                Intent intent1 = new Intent(HOPE_SongListActivity.this, EditActivity.class);
                intent1.putExtra("deviceName", deviceName);
                intent1.putExtra("deviceRoom", deviceRoom);
                intent1.putExtra("brand", "");
                intent1.putExtra("serial", "");
                intent1.putExtra("roomId", deviceRoomId);
                intent1.putExtra("deviceId", deviceId);
                intent1.putExtra("deviceType", deviceType);
                startActivityForResult(intent1,100);
                break;
            case R.id.hopeactivity_bt_ibtn://蓝牙
                OgeApplication.HopeSendData(HopeSocketApi.musicChangeSource(serialNum, 3, sharedPreferencesManager.get("HopeToken")));
                break;
            case R.id.hopeactivity_local_ibtn://本地
                OgeApplication.HopeSendData(HopeSocketApi.musicChangeSource(serialNum, 1, sharedPreferencesManager.get("HopeToken")));
                break;
            case R.id.hopeactivity_dl_ibtn://外接
                OgeApplication.HopeSendData(HopeSocketApi.musicChangeSource(serialNum, 2, sharedPreferencesManager.get("HopeToken")));
                break;
            case R.id.hopeactivity_playpattern_rll://播放模式
                OgeApplication.HopeSendData(HopeSocketApi.musicChangeMode(serialNum,(musicMode%3)+1,sharedPreferencesManager.get("HopeToken")));
                break;
            case R.id.hopeactiovity_play_ibtn://播放
                if (songState == 1) {//暂停
                    OgeApplication.HopeSendData(HopeSocketApi.musicPlay(serialNum,sharedPreferencesManager.get("HopeToken")));
                } else if (songState == 2) {//播放,点击暂停时只返回指令不返回状态，需要自己更新状态
                    OgeApplication.HopeSendData(HopeSocketApi.musicPause(serialNum,sharedPreferencesManager.get("HopeToken")));
                }
                break;
            case R.id.hopeactivity_upsong_ibtn://上一曲，会返回播放状态的实时状态 需要在播放中更新ui
                OgeApplication.HopeSendData(HopeSocketApi.musicPrev(serialNum,sharedPreferencesManager.get("HopeToken")));
                break;
            case R.id.hopeactivity_nextsong_ibtn://下一曲
                OgeApplication.HopeSendData(HopeSocketApi.musicNext(serialNum,sharedPreferencesManager.get("HopeToken")));
                break;
            case R.id.hopeactivity_addsound_ibtn://音量加
                if (currentVol < maxVol) {
                    currentVol = currentVol + 1;
                    OgeApplication.HopeSendData(HopeSocketApi.MusicVolumeSet(serialNum, currentVol, sharedPreferencesManager.get("HopeToken")));
                } else {
                    toastShoW("音量已调至最大");
                }
                break;
            case R.id.hopeactivity_jiansound_ibtn://音量减
                if (currentVol > 0) {
                    currentVol = currentVol-1;
                    OgeApplication.HopeSendData(HopeSocketApi.MusicVolumeSet(serialNum, currentVol, sharedPreferencesManager.get("HopeToken")));
                } else {
                    toastShoW("静音");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        OgeApplication.HopeSendData(HopeSocketApi.seekMusicPosition(serialNum,seekBar.getProgress(),sharedPreferencesManager.get("HopeToken")));
    }

    private class GetDataTask extends AsyncTask<Void, Void, String> {
        /**
         * 这里的Void参数对应AsyncTask中的第一个参数
         * 这里的String返回值对应AsyncTask的第三个参数
         * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
         * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
         */
        @Override
        protected String doInBackground(Void... voids) {
            return null;
        }

        /**
         * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
         * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pullToRefreshListView.onRefreshComplete();

        }
    }
    //定时器，刷新ProgressBar
    private void handleTimeProgress(int mCurrentProgress,int progess) {
        int time = progess - mCurrentProgress < 0 ? 0 : progess - mCurrentProgress;
        unSubscribe();
        createSubcribe();
        mObservable = Observable.interval(1, TimeUnit.SECONDS);
        mObservable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .take(time)
                .map(x -> x + 1)
                .subscribe(mSubscriber);

    }
    //创建sekbar
    private void createSubcribe() {
        mSubscriber = new Subscriber<Long>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long response) {
                songPro++;
                seekBar.setProgress(songPro);
                int minute = songPro / 60;
                int second = songPro % 60;
                String m = minute < 10 ? "0" + String.valueOf(minute) : String.valueOf(minute);
                String s = second < 10 ? "0" + String.valueOf(second) : String.valueOf(second);
                startTime_tv.setText(m + ":" + s);
            }
        };
    }
    //停止seekbar
    protected void unSubscribe() {
        if (mSubscriber != null && !mSubscriber.isUnsubscribed()) {
            mSubscriber.unsubscribe();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 100) {
                if (data != null) {
                    deviceName = data.getStringExtra("deviceName");
                    deviceName_tv.setText(deviceName);
                    deviceRoom = data.getStringExtra("roomName");
                    deviceRoom_tv.setText(deviceRoom);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        EventBus.getDefault().unregister(this);
    }
    private void toastShoW(String str){
        toast.setText(str);
        toast.show();
    }
}
