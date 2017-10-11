package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.bean.EZProbeDeviceInfo;

import neuron.com.app.OgeApplication;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.util.Utils;

/**
 * Created by ljh on 2017/2/15. 扫描摄像头结果页面
 */
public class EZ_CameraResultActiviry extends BaseActivity implements View.OnClickListener{
    private String TAG = "EZ_CameraResultActiviry";
    private ImageView back_iv;
    private Button connectionWifi_btn;
    private Intent intent;
    private String cameraSerial, cameraVerification;
     //萤石设备状态
    private EZProbeDeviceInfo ezProbeDeviceInfo;
    private int addDeviceCode = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == 0) {//验证摄像头信息
                int errCode = msg.arg1;
                Log.e(TAG + "错误码", String.valueOf(errCode));
                switch (errCode) {
                    case ErrorCode.ERROR_WEB_DEVICE_OFFLINE_NOT_ADD://设备不在线 未被添加 120023 可以进行一键配置wifi
                        addDeviceCode = 120023;
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_ONLINE_ADDED://120022
                        Utils.showDialog(EZ_CameraResultActiviry.this, "设备在线，已经被别的用户添加");
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_OFFLINE_ADDED://120024
                        Utils.showDialog(EZ_CameraResultActiviry.this, "设备不在线，已经被别的用户添加");
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_ADD_OWN_AGAIN://120029
                        Utils.showDialog(EZ_CameraResultActiviry.this, "设备不在线，已经被自己添加");
                        break;
                    case ErrorCode.ERROR_WEB_NET_EXCEPTION://网络异常 120006
                        Utils.showDialog(EZ_CameraResultActiviry.this, "请检查您的网络");
                        break;
                    case ErrorCode.ERROR_WEB_SERVER_EXCEPTION://服务器异常 150000
                        Utils.showDialog(EZ_CameraResultActiviry.this, "服务器异常");
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_VERIFY_CODE_ERROR://验证码错误 150002
                        Utils.showDialog(EZ_CameraResultActiviry.this, "请输入正确的验证码");
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_SO_TIMEOUT://设备请求响应超时 102009
                        Utils.showDialog(EZ_CameraResultActiviry.this, "设备请求响应超时");
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT://设备不存在 120002
                        Utils.showDialog(EZ_CameraResultActiviry.this, "设备不存在");
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE://设备不在线
                        Utils.showDialog(EZ_CameraResultActiviry.this, "设备不在线");
                        break;
                    case ErrorCode.ERROR_WEB_DEVICE_NOT_ADD://设备在线，已被自己添加
                        Utils.showDialog(EZ_CameraResultActiviry.this, "设备在线，已被自己添加");
                        break;
                    default:
                        break;
                }
            } else if (what == 1) {//返回成功　　　可以调用adddevice添加设备
                addDeviceCode = 1;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ezcaptureresult);
        init();
    }
    private void init(){
        intent = getIntent();
        cameraSerial = intent.getStringExtra("cameraSerial");
        Log.e(TAG + "cameraSerial", cameraSerial);
        cameraVerification = intent.getStringExtra("cameraVerification");
        Log.e(TAG + "cameraVerification", cameraVerification);
        back_iv = (ImageView) findViewById(R.id.ezcaptureresult_back);
        connectionWifi_btn = (Button) findViewById(R.id.ezcaptureresult_connectionnet_btn);
        back_iv.setOnClickListener(this);
        connectionWifi_btn.setOnClickListener(this);
        Utils.detectionEZToken(EZ_CameraResultActiviry.this);
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    ezProbeDeviceInfo = OgeApplication.getOpenSDK().probeDeviceInfo(cameraSerial);
                    Message msg = Message.obtain();
                    msg.what = 1;
                    handler.sendMessage(msg);
                } catch (BaseException e) {
                    Message msg = Message.obtain();
                    msg.what = 0;
                    msg.arg1 = e.getErrorCode();//返回错误码 根据错误码进行判断
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }

            }
        }.start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ezcaptureresult_connectionnet_btn://
                Log.e(TAG + "addDeviceCode", String.valueOf(addDeviceCode));
                if (addDeviceCode == 120023 || addDeviceCode == 1) {//可以进行下一步
                    Intent intent = new Intent(this, EZ_CameraFirstActivity.class);
                    intent.putExtra("cameraSerial", cameraSerial);
                    intent.putExtra("cameraVerification", cameraVerification);
                    startActivity(intent);
                }
                break;
            case R.id.ezcaptureresult_back:
                finish();
                break;
            default:
                break;
        }
    }
}
