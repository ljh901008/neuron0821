package neuron.com.lock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import neuron.com.app.OgeApplication;
import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;


/**
 *  
 * 手势密码设置界面  用此类就可以设置 手势密码  主代码在setUpViews()方法中
 *
 */
public class GestureEditActivity extends BaseActivity implements OnClickListener {
	/** 手机号码*/
	public static final String PARAM_PHONE_NUMBER = "PARAM_PHONE_NUMBER";
	/** 意图 */
	public static final String PARAM_INTENT_CODE = "PARAM_INTENT_CODE";
	/** 首次提示绘制手势密码，可以选择跳过 */
	public static final String PARAM_IS_FIRST_ADVICE = "PARAM_IS_FIRST_ADVICE";
	private TextView mTextTitle;
	private TextView mTextCancel;
	private LockIndicator mLockIndicator;
	private TextView mTextTip;
	private FrameLayout mGestureContainer;
	private GestureContentView mGestureContentView;
	private TextView mTextReset;
	private String mParamSetUpcode = null;
	private String mParamPhoneNumber;
	private boolean mIsFirstInput = true; //判断是否是第一次设置密码
	private String mFirstPassword = null;//设置的手势密码
	private String mConfirmPassword = null;
	private int mParamIntentCode;
	private SharedPreferencesManager sfManager;
	
	//titlebar  信息
	private ImageButton left_iv;
	private TextView titile_tv,right_tv;
	private Intent intent;
	private int type;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_edit);
		sfManager = SharedPreferencesManager.getInstance(this);
		setUpViews();
		setUpListeners();
		OgeApplication.addActivity(this);
	}
	
	private void setUpViews() {
		intent = getIntent();
		type = intent.getIntExtra("type", 10);
		left_iv = (ImageButton) findViewById(R.id.activity_gesture_edit_back_iv);
		right_tv = (TextView) findViewById(R.id.activity_gesture_edit_fonfirm_btn);
		if (type == 2) {
			right_tv.setVisibility(View.GONE);
		} else {
			left_iv.setVisibility(View.GONE);
		}
		mTextTitle = (TextView) findViewById(R.id.text_title);
		mTextCancel = (TextView) findViewById(R.id.text_cancel);
		mTextReset = (TextView) findViewById(R.id.text_reset);
		mTextReset.setClickable(false);
		mLockIndicator = (LockIndicator) findViewById(R.id.lock_indicator);
		mTextTip = (TextView) findViewById(R.id.text_tip);
		mGestureContainer = (FrameLayout) findViewById(R.id.gesture_container);
		// 初始化一个显示各个点的viewGroup
		mGestureContentView = new GestureContentView(this, false, "", new GestureDrawline.GestureCallBack() {
			@Override
			public void onGestureCodeInput(String inputCode) {
				if (!isInputPassValidate(inputCode)) {
					mTextTip.setText(Html.fromHtml("<font color='#c70c1e'>最少链接4个点, 请重新输入</font>"));
					mGestureContentView.clearDrawlineState(0L);
					return;
				}
				if (mIsFirstInput) {
					mFirstPassword = inputCode;
					updateCodeList(inputCode);
					mGestureContentView.clearDrawlineState(0L);
					mTextReset.setClickable(true);
					mTextReset.setText(getString(R.string.reset_gesture_code));
				} else {
					if (inputCode.equals(mFirstPassword)) {
						//保存密码到SharedPreferences
						if (sfManager.has("isFirstLogin")) {
							sfManager.save("isFirstLogin", "1");
						} else {
							sfManager.save("isFirstLogin", "1");
						}
						sfManager.save("handlock", mFirstPassword);
						mGestureContentView.clearDrawlineState(0L);
						GestureEditActivity.this.finish();
						Toast.makeText(GestureEditActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
						Intent intent = new Intent(GestureEditActivity.this, MainActivity.class);
						startActivity(intent);
					} else {
						mTextTip.setText(Html.fromHtml("<font color='#c70c1e'>与上一次绘制不一致，请重新绘制</font>"));
						// 左右移动动画
						Animation shakeAnimation = AnimationUtils.loadAnimation(GestureEditActivity.this, R.anim.shake);
						mTextTip.startAnimation(shakeAnimation);
						// 保持绘制的线，1.5秒后清除
						mGestureContentView.clearDrawlineState(1300L);
					}
				}
				mIsFirstInput = false;
			}

			@Override
			public void checkedSuccess() {
				
			}

			@Override
			public void checkedFail() {
				
			}
		});
		// 设置手势解锁显示到哪个布局里面
		mGestureContentView.setParentView(mGestureContainer);
		updateCodeList("");
	}
	
	private void setUpListeners() {
//		mTextCancel.setOnClickListener(this);
		mTextReset.setOnClickListener(this);
		right_tv.setOnClickListener(this);
		left_iv.setOnClickListener(this);
	}
	
	private void updateCodeList(String inputCode) {
		// 更新选择的图案
		mLockIndicator.setPath(inputCode);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.activity_gesture_edit_back_iv://fanhuijian
				finish();
				break;
			case R.id.activity_gesture_edit_fonfirm_btn://跳过按钮
				sfManager.save("isFirstLogin", "0");
				Intent intent = new Intent(this, MainActivity.class);
				startActivity(intent);
				break;
			case R.id.text_cancel:
				this.finish();
				break;
			case R.id.text_reset:
				mIsFirstInput = true;
				updateCodeList("");
				mTextTip.setText(getString(R.string.set_gesture_pattern));
				break;
			default:
				break;
		}
	}
	/**
	 * 判断密码长度  不能小于4位
	 * @param inputPassword 密码 
	 * @return
	 */
	private boolean isInputPassValidate(String inputPassword) {
		if (TextUtils.isEmpty(inputPassword) || inputPassword.length() < 4) {
			return false;
		}
		return true;
	}

	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(GestureEditActivity.this, LoginActivity.class);
			startActivity(intent);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}*/
}
