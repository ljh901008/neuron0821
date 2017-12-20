package neuron.com.scene.Activity;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.WaitDialog;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2017/5/10.
 */
public class SceneTimingActivity extends BaseActivity implements View.OnClickListener,TimePicker.OnTimeChangedListener{
    private String TAG = "SceneTimingActivity";
    private ImageButton back;
    private Button confirm_btn;
    private RelativeLayout openTime_rll, cf_rll,delTime_rll;
    private TextView openTime_tv, cfName_tv;
    private String isCf = "";
    private String account,token, engineId;
    private SharedPreferencesManager sharedPreferencesManager;
    private String sceneId;
    private String setTimeMethod = "SetTimer";
    private String delTimeMethod = "DelTimer";
    private String cfDay = "";
    private String days = "";
    private String openTime;
    private Intent intent;
    private String sceneTime;
    private String mIsCf, mCfTime;
    private WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scenetiming);
        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        init();
        setListener();
    }

    private void setListener() {
        back.setOnClickListener(this);
        confirm_btn.setOnClickListener(this);
        openTime_rll.setOnClickListener(this);
        cf_rll.setOnClickListener(this);
        delTime_rll.setOnClickListener(this);
    }

    private void init() {
        intent = getIntent();
        sceneId = intent.getStringExtra("sceneId");
        sceneTime = intent.getStringExtra("sceneTime");
        back = (ImageButton) findViewById(R.id.scenetiming_back_iv);
        confirm_btn = (Button) findViewById(R.id.scenetiming_fonfirm_btn);
        openTime_rll = (RelativeLayout) findViewById(R.id.scenetiming_open_rll);
        cf_rll = (RelativeLayout) findViewById(R.id.scenetiming_cf_rll);
        openTime_tv = (TextView) findViewById(R.id.scenetiming_time_tv);
        cfName_tv = (TextView) findViewById(R.id.scenetiming_cfname_tv);
        delTime_rll = (RelativeLayout) findViewById(R.id.scenetiming_deltime_rll);
        timeData(sceneTime);
    }

    /**
     * 解析已经设定的时间
     * @param time
     */
    private void timeData(String time){
        try {
            JSONObject jsonObject = new JSONObject(time);
            String oTime = jsonObject.getString("open_time");
            if (!TextUtils.isEmpty(oTime)) {
                openTime_tv.setText(oTime.substring(0, oTime.lastIndexOf(":")));
            }
            int repeatTime = jsonObject.getInt("repeat_sign");
            mIsCf = String.valueOf(repeatTime);
            if (repeatTime == 0) {//重复
                StringBuffer sceneRepeat = new StringBuffer();
                JSONArray jsonRepeat = jsonObject.getJSONArray("repeat_days");
                mCfTime = jsonObject.getString("repeat_days");
                for (int i = 0; i < jsonRepeat.length(); i++) {
                    String s = jsonRepeat.getString(i);
                    if (s.equals("1")) {
                        sceneRepeat.append("周一、");
                    } else if (s.equals("2")) {
                        sceneRepeat.append("周二、");
                    } else if (s.equals("3")) {
                        sceneRepeat.append("周三、");
                    } else if (s.equals("4")) {
                        sceneRepeat.append("周四、");
                    } else if (s.equals("5")) {
                        sceneRepeat.append("周五、");
                    } else if (s.equals("6")) {
                        sceneRepeat.append("周六、");
                    } else if (s.equals("0")) {
                        sceneRepeat.append("周日、");
                    }
                }
                cfName_tv.setText("重复(" + sceneRepeat.substring(0, sceneRepeat.length() - 1)+ ")");
            } else if (repeatTime == 1){
                mCfTime = jsonObject.getString("specified_day");
                cfName_tv.setText(jsonObject.getString("specified_day"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.scenetiming_back_iv://返回键
                finish();
                break;
            case R.id.scenetiming_fonfirm_btn://确定
                openTime = openTime_tv.getText().toString().trim();
                String cfTime = cfName_tv.getText().toString().trim();
                if (!TextUtils.isEmpty(openTime)) {
                    if (!TextUtils.isEmpty(cfTime)) {
                        if (TextUtils.isEmpty(isCf)) {
                            if (mIsCf.equals("0")) {
                                sceneSetTime(sceneId, openTime, mCfTime, mIsCf, "");
                            } else {
                                sceneSetTime(sceneId, openTime, "", mIsCf, mCfTime);
                            }
                        } else {
                            sceneSetTime(sceneId, openTime, days, isCf, cfDay);
                        }
                    } else {
                        Utils.showDialog(SceneTimingActivity.this, "请设置定时模式");
                    }
                } else {
                    Utils.showDialog(SceneTimingActivity.this, "请设置触发时间");
                }
                break;
            case R.id.scenetiming_open_rll: //开启时间
               SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                openTime_tv.setText(simpleDateFormat.format(new Date()));
                //TimeDialog(openTime_tv);
                getTime(openTime_tv);
                break;
            case R.id.scenetiming_cf_rll: //重复时间
                Intent intent = new Intent(SceneTimingActivity.this, SceneTimingSelectActivity.class);
                startActivityForResult(intent, 10);
                break;
            case R.id.scenetiming_deltime_rll://删除定时
                delTime(sceneId);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 10) {
                if (data != null) {
                    isCf = data.getStringExtra("cf");
                    if ("0".equals(isCf)) {//重复
                        days = data.getStringExtra("days");
                        try {
                            JSONArray jsonArray = new JSONArray(days);
                            StringBuffer stringBuffer = new StringBuffer();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                if ("1".equals(jsonArray.getString(i))) {
                                    stringBuffer.append("周一、");
                                } else if ("2".equals(jsonArray.getString(i))) {
                                    stringBuffer.append("周二、");
                                }else if ("3".equals(jsonArray.getString(i))) {
                                    stringBuffer.append("周三、");
                                }else if ("4".equals(jsonArray.getString(i))) {
                                    stringBuffer.append("周四、");
                                }else if ("5".equals(jsonArray.getString(i))) {
                                    stringBuffer.append("周五、");
                                }else if ("6".equals(jsonArray.getString(i))) {
                                    stringBuffer.append("周六、");
                                }else if ("0".equals(jsonArray.getString(i))) {
                                    stringBuffer.append("周日、");
                                }
                            }
                            String str = String.valueOf(stringBuffer);
                            if (str.endsWith("、")) {
                                String str1 = str.substring(0, str.length() - 1);
                                String str2 = "重复("+str1+")";
                                cfName_tv.setText(str2);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else if ("1".equals(isCf)) {//自定义
                        cfDay = data.getStringExtra("days");
                        cfName_tv.setText("自定义"+cfDay);
                    }

                }
            }
        }
    }

    /**
     *
     * @param sceneId  场景Id
     * @param openTime  触发时间
     * @param repeatDays  重复和不重复状态下的时间
     * @param repeatId   0重复2自定义
     * @param cfDay    自定义状态 触发的日期
     */
    private void sceneSetTime(String sceneId,String openTime,String repeatDays,String repeatId,String cfDay){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engineId = sharedPreferencesManager.get("engine_id");
        }
        Utils.showWaitDialog("加载中...", SceneTimingActivity.this,mWaitDialog);
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + engineId + setTimeMethod + openTime + repeatDays + repeatId + cfDay + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("contextual_model_id", sceneId);
            xutils.add("repeat_sign", repeatId);
            xutils.add("repeat_days", repeatDays);
            xutils.add("specified_day", cfDay);
            xutils.add("open_time", openTime);
            xutils.add("token", token);
            xutils.add("method", setTimeMethod);
            xutils.add("sign", sign);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    try {
                        Log.e(TAG + "场景设置时间", result);
                        JSONObject json = new JSONObject(result);
                        if (json.getInt("status") == 9999) {

                            final AlertDialog.Builder builder = new AlertDialog.Builder(SceneTimingActivity.this);
                            View view = View.inflate(SceneTimingActivity.this, R.layout.dialog_textview, null);
                            TextView title = (TextView) view.findViewById(R.id.textView1);
                            Button button = (Button) view.findViewById(R.id.button1);
                            title.setText("设置成功");
                            builder.setView(view);
                            button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    intent.putExtra("iscf", isCf);
                                    if ("0".equals(isCf)) {
                                        intent.putExtra("days", days);
                                    } else {
                                        intent.putExtra("days", cfDay);
                                    }
                                    setResult(RESULT_OK, intent);
                                    finish();
                                    builder.create().dismiss();
                                }
                            });
                            builder.create().show();
                        } else {
                            Utils.showDialog(SceneTimingActivity.this, json.getString("error"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Toast.makeText(SceneTimingActivity.this, "网络不通", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancelled(CancelledException e) {

                }

                @Override
                public void onFinished() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private TimePicker timePicker;
    private void TimeDialog(final TextView textView){
        LinearLayout dateTimeLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.timepickdialog, null);
        timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.timepickerdialog_time);
        Calendar calendar = Calendar.getInstance();
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
                if (hourOfDay < 10 && minute >= 10) {
                    textView.setText("0" + hourOfDay + ":" + minute + ":00");
                } else if (hourOfDay < 10 && minute < 10) {
                    textView.setText("0" + hourOfDay + ":" + "0" + minute + ":10");
                } else if (hourOfDay >= 10 && minute < 10) {
                    textView.setText(hourOfDay + ":" + "0" + minute + ":15");
                } else {
                    textView.setText(hourOfDay + ":" + minute + ":00");
                }
            }
        });
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setView(dateTimeLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        dialog.show();
    }

    /**
     * 删除情景模式Id
     * @param sceneId
     */
    private void delTime(String sceneId){
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
        if (sharedPreferencesManager.has("account")) {
            account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        if (sharedPreferencesManager.has("engine_id")) {
            engineId = sharedPreferencesManager.get("engine_id");
        }
        Utils.showWaitDialog("加载中...", SceneTimingActivity.this,mWaitDialog);
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + sceneId + engineId + delTimeMethod + token + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.GETHOMELIST_URL);
            xutils.add("account", aesAccount);
            xutils.add("engine_id", engineId);
            xutils.add("contextual_model_id", sceneId);
            xutils.add("token", token);
            xutils.add("method", delTimeMethod);
            xutils.add("sign", sign);
            xutils.sendPost2(new Callback.CommonCallback<String>() {
                @Override
                public void onSuccess(String updateResult) {
                    Utils.dismissWaitDialog(mWaitDialog);
                    Log.e(TAG + "删除情景模式时间", updateResult);
                    try {
                        JSONObject jsonObject = new JSONObject(updateResult);
                        if (jsonObject.getInt("status") != 9999) {
                            Utils.showDialog(SceneTimingActivity.this, jsonObject.getString("error"));
                        } else {
                            Utils.showDialog(SceneTimingActivity.this, "删除成功");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable, boolean b) {
                    Utils.dismissWaitDialog(mWaitDialog);
                }

                @Override
                public void onCancelled(CancelledException e) {

                }

                @Override
                public void onFinished() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getTime(final TextView input){
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(SceneTimingActivity.this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // TODO Auto-generated method stub
                if (hourOfDay < 10 && minute >= 10) {
                    input.setText("0" + hourOfDay + ":" + minute);
                } else if (hourOfDay < 10 && minute < 10) {
                    input.setText("0" + hourOfDay + ":" + "0" + minute);
                } else if (hourOfDay >= 10 && minute < 10) {
                    input.setText(hourOfDay + ":" + "0" + minute);
                } else {
                    input.setText(hourOfDay + ":" + minute);
                }
            }
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int i, int i1) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH),
                timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        openTime = new SimpleDateFormat("HH:mm").format(calendar.getTime());
    }
}
