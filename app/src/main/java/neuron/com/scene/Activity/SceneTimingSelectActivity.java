package neuron.com.scene.Activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.Calendar;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;

/**
 * Created by ljh on 2017/5/10.
 */
public class SceneTimingSelectActivity extends BaseActivity implements View.OnClickListener,DatePickerDialog.OnDateSetListener {
    private String TAG = "SceneTimingSelectActivity";
    private ImageButton back;
    private Button confirm;
    private RelativeLayout cf_rll,sunday_rll,saturday_rll,friday_rll,thursday_rll,wednesday_rll,tuesday_rll, monday_rll;
    private ImageView sunday_iv,saturday_iv,friday_iv,thursday_iv,wednesday_iv,tuesday_iv, monday_iv;
    private RelativeLayout custom_rll;
    private TextView cf_tv,custom_tv;
    private boolean isSun = false;
    private boolean isSat = false;
    private boolean isFri = false;
    private boolean isTHu = false;
    private boolean isWed = false;
    private boolean isTue = false;
    private boolean isMon = false;
    private int isCf = 0;
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scenetimingselect);
        init();
        setListener();
    }
    private void setListener() {
        cf_rll.setOnClickListener(this);
        sunday_rll.setOnClickListener(this);
        saturday_rll.setOnClickListener(this);
        friday_rll.setOnClickListener(this);
        thursday_rll.setOnClickListener(this);
        wednesday_rll.setOnClickListener(this);
        tuesday_rll.setOnClickListener(this);
        monday_rll.setOnClickListener(this);
        custom_rll.setOnClickListener(this);
        back.setOnClickListener(this);
        confirm.setOnClickListener(this);
    }
    private void init() {
        intent = getIntent();
        back = (ImageButton) findViewById(R.id.scenetimingselect_back_iv);
        confirm = (Button) findViewById(R.id.scenetimingselect_fonfirm_btn);
        custom_tv = (TextView) findViewById(R.id.scenetimingselect_custom_tv);
        cf_tv = (TextView) findViewById(R.id.scenetiming_cf_tv);
        custom_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_custom_rll);
        cf_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_pattern_rll);
        sunday_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_sunday_rll);
        saturday_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_saturday_rll);
        friday_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_friday_rll);
        thursday_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_thursday_rll);
        wednesday_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_wednesday_rll);
        tuesday_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_tuesday_rll);
        monday_rll = (RelativeLayout) findViewById(R.id.scenetimingselect_monday_rll);
        sunday_iv = (ImageView) findViewById(R.id.scenetimingselect_sunday_iv);
        saturday_iv = (ImageView) findViewById(R.id.scenetimingselect_saturday_iv);
        friday_iv = (ImageView) findViewById(R.id.scenetimingselect_friday_iv);
        thursday_iv = (ImageView) findViewById(R.id.scenetimingselect_thursday_iv);
        wednesday_iv = (ImageView) findViewById(R.id.scenetimingselect_wednesday_iv);
        tuesday_iv = (ImageView) findViewById(R.id.scenetimingselect_tuesday_iv);
        monday_iv = (ImageView) findViewById(R.id.scenetimingselect_monday_iv);

    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.scenetimingselect_pattern_rll://定时模式
                if (isCf == 0) {//重复状态
                    isCf = 1;
                    cf_tv.setText("自定义");
                    sunday_rll.setVisibility(View.GONE);
                    saturday_rll.setVisibility(View.GONE);
                    friday_rll.setVisibility(View.GONE);
                    thursday_rll.setVisibility(View.GONE);
                    wednesday_rll.setVisibility(View.GONE);
                    tuesday_rll.setVisibility(View.GONE);
                    monday_rll.setVisibility(View.GONE);
                    custom_rll.setVisibility(View.VISIBLE);
                } else if (isCf == 1) {//自定义状态
                    isCf = 0;
                    cf_tv.setText("重复");
                    sunday_rll.setVisibility(View.VISIBLE);
                    saturday_rll.setVisibility(View.VISIBLE);
                    friday_rll.setVisibility(View.VISIBLE);
                    thursday_rll.setVisibility(View.VISIBLE);
                    wednesday_rll.setVisibility(View.VISIBLE);
                    tuesday_rll.setVisibility(View.VISIBLE);
                    monday_rll.setVisibility(View.VISIBLE);
                    custom_rll.setVisibility(View.GONE);
                }
                break;
            case R.id.scenetimingselect_sunday_rll://周日
                if (isSun) {//选择状态
                    isSun = false;
                    sunday_iv.setVisibility(View.GONE);
                } else {//未选状态
                    isSun = true;
                    sunday_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.scenetimingselect_saturday_rll: //周六
                if (isSat) {//选择状态
                    isSat = false;
                    saturday_iv.setVisibility(View.GONE);
                } else {//未选状态
                    isSat = true;
                    saturday_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.scenetimingselect_friday_rll: //周五
                if (isFri) {//选择状态
                    isFri = false;
                    friday_iv.setVisibility(View.GONE);
                } else {//未选状态
                    isFri = true;
                    friday_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.scenetimingselect_thursday_rll: //周四
                if (isTHu) {//选择状态
                    isTHu = false;
                    thursday_iv.setVisibility(View.GONE);
                } else {//未选状态
                    isTHu = true;
                    thursday_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.scenetimingselect_wednesday_rll://周三
                if (isWed) {//选择状态
                    isWed = false;
                    wednesday_iv.setVisibility(View.GONE);
                } else {//未选状态
                    isWed = true;
                    wednesday_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.scenetimingselect_tuesday_rll: //周二
                if (isTue) {//选择状态
                    isTue = false;
                    tuesday_iv.setVisibility(View.GONE);
                } else {//未选状态
                    isTue = true;
                    tuesday_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.scenetimingselect_monday_rll: //周一
                if (isMon) {//选择状态
                    isMon = false;
                    monday_iv.setVisibility(View.GONE);
                } else {//未选状态
                    isMon = true;
                    monday_iv.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.scenetimingselect_back_iv: //返回
                    finish();
                break;
            case R.id.scenetimingselect_fonfirm_btn: //确定
                JSONArray jsonArray = new JSONArray();
                if (isMon) {
                    jsonArray.put("1");
                }
                if (isTue) {
                    jsonArray.put("2");
                }
                if (isWed) {
                    jsonArray.put("3");
                }
                if (isTHu) {
                    jsonArray.put("4");
                }
                if (isFri) {
                    jsonArray.put("5");
                }
                if (isSat) {
                    jsonArray.put("6");
                }
                if (isSun) {
                    jsonArray.put("0");
                }
                if (isCf == 0) {//重复
                    intent.putExtra("days", jsonArray.toString());
                    intent.putExtra("cf", String.valueOf(isCf));
                    setResult(RESULT_OK, intent);
                } else if (isCf == 1) {//自定义
                    String date = custom_tv.getText().toString().trim();
                    Log.e(TAG + "自定时场景时间",date);
                    intent.putExtra("cf", String.valueOf(isCf));
                    intent.putExtra("days", date);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            case R.id.scenetimingselect_custom_rll://选择日期
                getDate();
                break;
            default:
                break;
        }
    }
    private int years,month, day;
    private void getDate(){
        Calendar calendar = Calendar.getInstance();
        years = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        new DatePickerDialog(this, this, years, month, day).show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
        years = year;
        month = monthOfYear + 1;
        day = dayOfMonth;
        if (month < 10 && day < 10) {
            custom_tv.setText(years + "-" + "0" + month + "-" + "0" + day);
        } else if (month > 10 && day < 10) {
            custom_tv.setText(years + "-" + month + "-" + "0" + day);
        }else if (month < 10 && day > 10) {
            custom_tv.setText(years + "-" + "0" + month + "-" + day);
        }
    }
}
