package neuron.com.set.Activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/5/24. 联系我们
 */
public class AboutWeActivity extends BaseActivity implements View.OnClickListener{
    private ImageButton back;
    private RelativeLayout relation;
    private TextView version;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aboutwe);
        init();
        setListener();

    }

    private void setListener() {
        back.setOnClickListener(this);
        relation.setOnClickListener(this);
    }

    private void init() {
        back = (ImageButton) findViewById(R.id.aboutwe_back_iv);
        relation = (RelativeLayout) findViewById(R.id.aboutwe_relation_rll);
        version = (TextView) findViewById(R.id.aboutwe_vision_tv);
        version.setText("版本号:"+getVersion());
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.aboutwe_back_iv://返回
                finish();
                break;
            case R.id.aboutwe_relation_rll://联系我们
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse("tel:" + "4000560199");
                intent.setData(data);
                startActivity(intent);
                break;

            default:
                break;
        }
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
            return "版本号:" + version;
        } catch (Exception e) {
            e.printStackTrace();
            return "版本号: 1.0.0";
        }
    }
}
