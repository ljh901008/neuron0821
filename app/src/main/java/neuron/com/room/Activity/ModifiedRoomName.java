package neuron.com.room.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2016/11/15.修改名称页面
 */
public class ModifiedRoomName extends BaseActivity implements View.OnClickListener{
    private TextView titile_tv;
    private ImageView back_iv;
    private EditText roomName_ed;
    private Button queding_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hand_add_device_activity);
        init();
        setListener();
    }

    private void setListener() {
        back_iv.setOnClickListener(this);
        queding_btn.setOnClickListener(this);
    }

    private void init() {
        titile_tv = (TextView) findViewById(R.id.hand_add_device_activity_title_1_tv);
        titile_tv.setText("修改名称");
        back_iv = (ImageView) findViewById(R.id.hand_add_device_activity_back_iv);
        roomName_ed = (EditText) findViewById(R.id.hand_add_device_activity_serial_ed);
        roomName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        roomName_ed.setHint("请输入名称");
        queding_btn = (Button) findViewById(R.id.hand_add_device_activity_queren_btn);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.hand_add_device_activity_back_iv:
                finish();
                break;
            case R.id.hand_add_device_activity_queren_btn:
                String roomName = roomName_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(roomName)) {
                    Intent intent = getIntent();
                    intent.putExtra("roomName", roomName);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                finish();
                break;
            default:
            break;
        }
    }
}
