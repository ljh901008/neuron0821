package neuron.com.login.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/5/31.  用户协议页面
 */
public class UserAgreeMentActivity extends BaseActivity {
    private ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.useragreement);
        back = (ImageButton) findViewById(R.id.useragreement_back_ibtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
