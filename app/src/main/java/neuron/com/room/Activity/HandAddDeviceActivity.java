package neuron.com.room.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.Utils;

/**
 * Created by ljh on 2016/10/21.
 */
public class HandAddDeviceActivity extends BaseActivity implements View.OnClickListener{
    private ImageView back_iv;
    private EditText serial_ed;
    private Button queren_btn;
    private String serial;
    private String cameraVerifyCode;//摄像头验证码
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand_add_device_activity);
        init();
        setListener();
    }
    private void init(){
        back_iv = (ImageView) findViewById(R.id.hand_add_device_activity_back_iv);
        serial_ed = (EditText) findViewById(R.id.hand_add_device_activity_serial_ed);
        queren_btn = (Button) findViewById(R.id.hand_add_device_activity_queren_btn);
        serial_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        queren_btn.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.hand_add_device_activity_back_iv://返回键
                finish();
                break;
            case R.id.hand_add_device_activity_queren_btn://确认键
                    serial = serial_ed.getText().toString().trim();

                if (!TextUtils.isEmpty(serial)) {
                    if (serial.length() == 9) {//摄像头添加
                        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
                        if (sharedPreferencesManager.get("is_belong").equals("1")) {
                            Utils.showDialog(HandAddDeviceActivity.this, "子帐号，分享帐号无权限添加摄像头！");
                        } else {
                            showDialog();
                        }
                    } else if (serial.length() == 16) {
                        Intent intent = new Intent(HandAddDeviceActivity.this, AddDeviceActivity.class);
                        intent.putExtra("serial", serial);
                        intent.putExtra("type", 2);
                        startActivity(intent);
                    } else {
                        Utils.showDialog(HandAddDeviceActivity.this, "请输入正确的序列号");
                    }
                }
                break;
            default:
            break;
        }
    }
    private void showDialog(){
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setView(LayoutInflater.from(this).inflate(R.layout.dialog_input, null));
        dialog.show();
        dialog.getWindow().setContentView(R.layout.dialog_input);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_add);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_cancel);
        final EditText etContent = (EditText) dialog.findViewById(R.id.et_content);
        etContent.setHint("请输入验证码");
        btnPositive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String str = etContent.getText().toString();
                if (TextUtils.isEmpty(str)) {
                    etContent.setError("输入内如不能为空");
                } else {
                    cameraVerifyCode = str;
                    SharedPreferencesManager.getInstance(HandAddDeviceActivity.this).save(serial, cameraVerifyCode);
                    Intent intent1 = new Intent(HandAddDeviceActivity.this, EZ_CameraResultActiviry.class);
                    intent1.putExtra("cameraSerial", serial);
                    intent1.putExtra("cameraVerification", cameraVerifyCode);
                    startActivity(intent1);
                    dialog.dismiss();
                }
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }
}
