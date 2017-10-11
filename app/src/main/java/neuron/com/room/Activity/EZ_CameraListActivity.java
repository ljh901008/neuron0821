package neuron.com.room.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.videogo.constant.IntentConsts;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.LogUtil;

import java.util.List;

import neuron.com.app.OgeApplication;
import neuron.com.comneuron.R;
import neuron.com.util.EZUtils;
import neuron.com.util.Utils;

/**
 * Created by ljh on 2017/2/22.
 */
public class EZ_CameraListActivity extends Activity {
    private String TAG = "EZ_CameraListActivity";
    private Button cameraList_btn;
    private List<EZDeviceInfo> result = null;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                result = (List<EZDeviceInfo>) msg.obj;
                EZDeviceInfo ezDeviceInfo = result.get(0);
                if (ezDeviceInfo.getCameraNum() == 1 && ezDeviceInfo.getCameraInfoList() != null && ezDeviceInfo.getCameraInfoList().size() == 1) {
                    LogUtil.d(TAG, "cameralist have one camera");
                    final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(ezDeviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }
                    Intent intent = new Intent(EZ_CameraListActivity.this, EZ_CameraPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, ezDeviceInfo);
                    startActivity(intent);
                    return;
                }
            } else if (msg.what == 2) {
                EZDeviceInfo deviceInfo = (EZDeviceInfo) msg.obj;
                if (deviceInfo.getCameraNum() == 1 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() == 1) {
                    LogUtil.d(TAG, "cameralist have one camera");
                    final EZCameraInfo cameraInfo = EZUtils.getCameraInfoFromDevice(deviceInfo, 0);
                    if (cameraInfo == null) {
                        return;
                    }
                    Intent intent = new Intent(EZ_CameraListActivity.this, EZ_CameraPlayActivity.class);
                    intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo);
                    intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo);
                    startActivity(intent);
                    return;
                }
            } else if (msg.what == 3) {
                String errorCode = (String) msg.obj;
                Log.e("ErrorCode", errorCode);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ezcameralist);
        cameraList_btn = (Button) findViewById(R.id.ezcameralist_list_btn);
        cameraList_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getCameraList();
                Utils.detectionEZToken(EZ_CameraListActivity.this);
               getCameraInfo();
            }
        });
    }
    private void getCameraList(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    List<EZDeviceInfo> list = OgeApplication.getOpenSDK().getDeviceList(0, 10);
                    if (list.size() == 0) {
                        return;
                    }
                    Message msg = handler.obtainMessage();
                    msg.what = 1;
                    msg.obj = list;
                    handler.sendMessage(msg);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

    }
    private void getCameraInfo(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    EZDeviceInfo ezDeviceInfo = OgeApplication.getOpenSDK().getDeviceInfo("684962533");
                    Message msg = handler.obtainMessage();
                    msg.what = 2;
                    msg.obj = ezDeviceInfo;
                    handler.sendMessage(msg);
                } catch (BaseException e) {
                    Message msg = handler.obtainMessage();
                    msg.what = 3;
                    msg.obj = e.getErrorCode();
                    handler.sendMessage(msg);
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
