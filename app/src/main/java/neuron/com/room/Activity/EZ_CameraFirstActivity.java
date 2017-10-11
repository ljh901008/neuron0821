package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.util.Utils;

/**
 * Created by ljh on 2017/2/15.
 */
public class EZ_CameraFirstActivity extends BaseActivity implements View.OnClickListener{
    private ImageView back_iv;
    //第一次配置网络，以前配置过网络
    private Button one_btn, two_btn;
    private Intent intent;
    private String cameraSerial, cameraVerification;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ezcamerafirst);
        init();
        setListener();
    }

    private void init() {
        intent = getIntent();
        cameraSerial = intent.getStringExtra("cameraSerial");
        cameraVerification = intent.getStringExtra("cameraVerification");
        back_iv = (ImageView) findViewById(R.id.ezcamerafirst_back);
        one_btn = (Button) findViewById(R.id.ezcamerafirst_one_btn);
        two_btn = (Button) findViewById(R.id.ezcamerafirst_two_btn);

    }
    private void setListener(){
        back_iv.setOnClickListener(this);
        one_btn.setOnClickListener(this);
        two_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ezcamerafirst_back://返回键
                finish();
                break;
            case R.id.ezcamerafirst_one_btn://第一次配置网络
                Intent intent = new Intent(this, EZ_CameraSecondActivity.class);
                intent.putExtra("cameraSerial", cameraSerial);
                intent.putExtra("cameraVerification", cameraVerification);
                startActivity(intent);
                break;
            case R.id.ezcamerafirst_two_btn://以前配置过网络
                Utils.showDialog(this,"长按设备上的reset键10秒后松开，并等待大约30秒直到设备启动完成");
                break;
            default:
                break;
        }

    }
}
