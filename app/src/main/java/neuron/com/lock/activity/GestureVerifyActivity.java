package neuron.com.lock.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.login.Activity.LoginActivity;
import neuron.com.util.XutilsHelper;

/**
 * 
 * 手势绘制/校验界面
 *
 */
public class GestureVerifyActivity extends BaseActivity implements android.view.View.OnClickListener {
	/** 手机号码*/
	public static final String PARAM_PHONE_NUMBER = "PARAM_PHONE_NUMBER";
	/** 意图 */
	public static final String PARAM_INTENT_CODE = "PARAM_INTENT_CODE";
	private RelativeLayout mTopLayout;
	private TextView mTextTitle;
	private TextView mTextCancel;
	private ImageView mImgUserLogo;
	private TextView mTextPhoneNumber;
	private TextView mTextTip;
	private FrameLayout mGestureContainer;
	private GestureContentView mGestureContentView;
	private TextView mTextForget;
	private TextView mTextOther;
	private String mParamPhoneNumber;
	private long mExitTime = 0;
	private int mParamIntentCode;
	private TextView otherLogin_tv;

	private SharedPreferencesManager sfManager;
	
	private String handPassword;
	
	private Intent intent;

	private int type;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_verify);
		ObtainExtraData();
		setUpViews();
		setUpListeners();
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		String imagePath = sfManager.get("imagePath");
		 if (null != imagePath) {
			File file = new File(imagePath);
			Uri imageUri = Uri.fromFile(file);
			try {
				Bitmap b =  BitmapFactory.decodeStream(this.getContentResolver().openInputStream(imageUri));
				if (b!=null) {
					mImgUserLogo.setImageBitmap(b);
				}else {
					mImgUserLogo.setImageResource(R.mipmap.avatar);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void ObtainExtraData() {

		mParamPhoneNumber = getIntent().getStringExtra(PARAM_PHONE_NUMBER);
		mParamIntentCode = getIntent().getIntExtra(PARAM_INTENT_CODE, 0);
	}
	
	private void setUpViews() {
		intent = getIntent();
		type = intent.getIntExtra("type", 3);
		mTopLayout = (RelativeLayout) findViewById(R.id.top_layout);
		mTextTitle = (TextView) findViewById(R.id.text_title);
		mTextCancel = (TextView) findViewById(R.id.text_cancel);
		mImgUserLogo = (ImageView) findViewById(R.id.user_logo);
		otherLogin_tv = (TextView) findViewById(R.id.gesture_veridy_otherlogin_tv);
		mTextPhoneNumber = (TextView) findViewById(R.id.text_phone_number);
		mTextTip = (TextView) findViewById(R.id.text_tip);
		mGestureContainer = (FrameLayout) findViewById(R.id.gesture_veridy_container);
		mTextForget = (TextView) findViewById(R.id.text_forget_gesture);
		sfManager = SharedPreferencesManager.getInstance(this);
		//获取 SharedPreferencesManager 保存的手势密码
		if (sfManager.has("handlock")) {
			handPassword = sfManager.get("handlock");
		}
			// 初始化一个显示各个点的viewGroup
			mGestureContentView = new GestureContentView(this, true, handPassword,
					new GestureDrawline.GestureCallBack() {

						@Override
						public void onGestureCodeInput(String inputCode) {

						}
						@Override
						public void checkedSuccess() {
							mGestureContentView.clearDrawlineState(0L);
							//Toast.makeText(GestureVerifyActivity.this, "密码正确", Toast.LENGTH_SHORT).show();
							if (type == 2) {//设置页面跳转过来
								Intent intent = new Intent(getApplication(), GestureEditActivity.class);
								startActivity(intent);
							} else {//登录或者别的页面跳转过来的
								Intent intent1 = new Intent(GestureVerifyActivity.this, MainActivity.class);
								startActivity(intent1);
							}
							GestureVerifyActivity.this.finish();
						}
						@Override
						public void checkedFail() {
							mGestureContentView.clearDrawlineState(1300L);
							mTextTip.setVisibility(View.VISIBLE);
							mTextTip.setText(Html
									.fromHtml("<font color='#c70c1e'>密码错误</font>"));
							// 左右移动动画
							Animation shakeAnimation = AnimationUtils.loadAnimation(GestureVerifyActivity.this, R.anim.shake);
							mTextTip.startAnimation(shakeAnimation);
						}
					});
			// 设置手势解锁显示到哪个布局里面
			mGestureContentView.setParentView(mGestureContainer);
		if (sfManager.has("paoto_path")) {
			XutilsHelper xutilsHelper = new XutilsHelper();
			xutilsHelper.downloadPhoto(mImgUserLogo,this,sfManager.get("paoto_path"));
		}

	}
	
	private void setUpListeners() {
		mTextCancel.setOnClickListener(this);
		mTextForget.setOnClickListener(this);
		otherLogin_tv.setOnClickListener(this);
//		mTextOther.setOnClickListener(this);
	}
	/**
	 * 
	 * @param phoneNumber 电话号码
	 * @return
	 */
	private String getProtectedMobile(String phoneNumber) {
		if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < 11) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(phoneNumber.subSequence(0,3));
		builder.append("****");
		builder.append(phoneNumber.subSequence(7,11));
		return builder.toString();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.text_cancel:
				this.finish();
				break;
			case R.id.text_forget_gesture: //忘记密码
				sfManager.remove("handlock");
				Intent intent = new Intent(GestureVerifyActivity.this, LoginActivity.class);
				intent.putExtra("type", 0);
				startActivity(intent);
				break;
			case R.id.gesture_veridy_otherlogin_tv://用其他方式登陆
				Intent intent1 = new Intent(GestureVerifyActivity.this, LoginActivity.class);
				intent1.putExtra("type", 1);
				startActivity(intent1);
				break;
			default:
				break;
		}
	}
}
