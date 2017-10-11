package neuron.com.room.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/2/15.
 */
public class EZ_CameraSecondActivity extends BaseActivity implements View.OnClickListener{
    private ImageView back_iv;
    private TextView wifiName_tv;
    private EditText wifiPwd_ed;
    private Button next_btn;
    private WifiManager mWiFi;
    //wifi名称，密码
    private String wifiSSID = null;
    //sdk版本号
    private int SDKVersion;
    private String ssid;
    private Intent intent;
    private String cameraSerial, cameraVerification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ezcamerasecond);
        init();
        setListener();
        getWiFi();
    }
    private void setListener() {
        back_iv.setOnClickListener(this);
        next_btn.setOnClickListener(this);
    }
    private void init() {
        intent = getIntent();
        cameraSerial = intent.getStringExtra("cameraSerial");
        cameraVerification = intent.getStringExtra("cameraVerification");
        back_iv = (ImageView) findViewById(R.id.ezcamerasecond_back_iv);
        wifiName_tv = (TextView) findViewById(R.id.ezcamerasecond_wifiname_tv);
        wifiPwd_ed = (EditText) findViewById(R.id.ezcamerasecond_wifipassword_ed);
        next_btn = (Button) findViewById(R.id.ezcamerasecond_next_btn);
        wifiPwd_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ezcamerasecond_back_iv:
                finish();
                break;
            case R.id.ezcamerasecond_next_btn://下一步
                Intent intent = new Intent(this, EZ_CameraThirdActivity.class);
                intent.putExtra("cameraSerial", cameraSerial);
                intent.putExtra("cameraVerification", cameraVerification);
                intent.putExtra("support_Wifi", true);
                intent.putExtra("support_net_work", false);//是否是有线设备
                intent.putExtra("SSID", SDKVersion >= 17 ? ssid : wifiSSID);
                intent.putExtra("wifipwd", TextUtils.isEmpty(wifiPwd_ed.getText().toString()) ? "smile"
                        : wifiPwd_ed.getText().toString());
                intent.putExtra("type", 1);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
    /**
     * 获取本机所连wifi名称（SSID）
     */
    private void getWiFi(){
        mWiFi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWiFi.isWifiEnabled()) {
            mWiFi.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = mWiFi.getConnectionInfo();
        if (wifiInfo.getSSID() != null) {
            wifiSSID = wifiInfo.getSSID();
            SDKVersion = Build.VERSION.SDK_INT;
            //Android 4.2（API Level = 17）及其以上的版本，获取到的SSID名称是有双引号的，4.2一下版本没有引号
            if (SDKVersion >= 17) {
                ssid = wifiSSID.substring(1,wifiSSID.length()-1);
                wifiName_tv.setText("网络：" + ssid);
            }else {
                wifiName_tv.setText("网络：" + wifiSSID);
            }
        }else {
            Toast.makeText(EZ_CameraSecondActivity.this, "请连接wifi", Toast.LENGTH_SHORT).show();
        }
    }
}
