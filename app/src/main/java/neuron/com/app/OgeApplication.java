package neuron.com.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.videogo.openapi.EZOpenSDK;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.x;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import cn.jpush.android.api.JPushInterface;
import cn.nbhope.smarthome.smartlib.net.RetrofitFactory;
import cn.nbhope.smarthome.smartlib.socket.SocketRequestEvent;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.ErrorObserver;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/8/28.
 */
public class OgeApplication extends Application {
    private String TAG = "OgeApplication";
    /**
     * 开发者申请的AppKey填充    萤石
     */
    public static String AppKey = "f0982dc875cd47deb2b254b49c52af6d";
    public static String screct = "0d5590e98c3ab9e0f6248b69bf6cb5ea";
    private SharedPreferencesManager sharedPreferencesManager;
    private static Context context;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch(arg1){
                case 1:
                    if (msg.what == 102) {
                        String result = (String) msg.obj;
                        Log.e("Application", result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("status") == 9999) {
                                String versionId = jsonObject.getString("version_id");
                                String url = jsonObject.getString("url");
                                sharedPreferencesManager.save("versionUrl", url);
                                sharedPreferencesManager.save("versionCode", versionId);
                                if (!TextUtils.isEmpty(versionId) && !versionId.equals(getVersion())) {
                                    sharedPreferencesManager.save("isUpdate", URLUtils.needUpdate);
                                } else {
                                    sharedPreferencesManager.save("isUpdate", URLUtils.noUpdate);
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
    public static EZOpenSDK getOpenSDK() {
        return EZOpenSDK.getInstance();
    }
    @Override
    public void onCreate() {
        super.onCreate();
        activities = new LinkedList<>();
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        x.Ext.init(this);//xutils3.0 初始化
        x.Ext.setDebug(true);//是否输出debug日志，开启debug会影响性能。
        initSDK();
        //初始化极光推送服务
        JPushInterface.init(this);
        JPushInterface.setDebugMode(true);//开启打印日志
        //检测内存泄漏，发布时请删除
       /* if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/
        checkAppVersion("CheckAndUpdate");//检测版本
        //初始化语音功能
        SpeechUtility.createUtility(OgeApplication.this, SpeechConstant.APPID + "=596c597b");
        context = getApplicationContext();
        //音响初始化
        RetrofitFactory.init(this); //进行RetrofitFactory的初始化
        RetrofitFactory.register(new ErrorObserver()); //注册 网络响应 观察者

    }


    /**
     * 获取全局的context
     * @author xuhuanli
     * @time 2017/8/17  13:32
     */
    public static Context getContext(){
        return context;
    }
    private void initSDK() {
        /**********国内版本初始化EZOpenSDK**************/
            /**
             * sdk日志开关，正式发布需要设置未false
             */
            EZOpenSDK.showSDKLog(true);
            /**
             * 设置是否支持P2P取流,详见api
             */
            EZOpenSDK.enableP2P(false);
            /**
             * APP_KEY请替换成自己申请的
             */
            EZOpenSDK.initLib(OgeApplication.this, AppKey, null);
            if (sharedPreferencesManager.has("EZToken")) {
                getOpenSDK().setAccessToken(sharedPreferencesManager.get("EZToken"));
            }
    }
    private static List<Activity> activities;

    /**
     * 结束当前所有Activity
     */
    public static void clearActivities() {
        ListIterator<Activity> iterator = activities.listIterator();
        Activity activity;
        while (iterator.hasNext()) {
            activity = iterator.next();
            if (activity != null) {
                activity.finish();
            }
        }
    }
    /**
     * 添加一个Activity
     *
     * @param activity
     */
    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    /**
     * 结束一个Activity
     *
     * @param activity
     */
    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }
    /**
     * 退出应运程序
     */
    public static void quiteApplication() {
        clearActivities();
        System.exit(0);
    }
    private void checkAppVersion(String method){
        String sign = MD5Utils.MD5Encode(method + URLUtils.MD5_SIGN, "");
        XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.Other, handler);
        xutilsHelper.add("method", method);
        xutilsHelper.add("sign", sign);
        xutilsHelper.sendPost(1,getApplicationContext());
    }
    /**
     * 2  * 获取版本号
     * 3  * @return 当前应用的版本号
     * 4
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.1";
        }
    }
    /**
     *   音响发送指令  所有的结果都在SocketResultEvent 这里边拿到
     * @param sendstr
     */
    public static void HopeSendData(String sendstr){
        EventBus.getDefault().post(new SocketRequestEvent(sendstr));
    }
}
