package com.karics.library.zxing.android;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.karics.library.zxing.camera.CameraManager;
import com.karics.library.zxing.view.ViewfinderView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import cn.nbhope.smarthome.smartlib.model.common.RequestModel;
import cn.nbhope.smarthome.smartlib.net.APIService;
import cn.nbhope.smarthome.smartlib.net.AppCommandType;
import cn.nbhope.smarthome.smartlib.net.RetrofitFactory;
import neuron.com.app.OgeApplication;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.room.Activity.AddDeviceActivity;
import neuron.com.room.Activity.EZ_CameraResultActiviry;
import neuron.com.room.Activity.HandAddDeviceActivity;
import neuron.com.util.RGBLuminanceSource;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 这个activity打开相机，在后台线程做常规的扫描；它绘制了一个结果view来帮助正确地显示条形码，在扫描的时候显示反馈信息，
 * 然后在扫描成功的时候覆盖扫描结果
 * 
 */
public final class CaptureActivity extends Activity implements
		SurfaceHolder.Callback {

	private static final String TAG = CaptureActivity.class.getSimpleName();

	// 相机控制
	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private IntentSource source;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	// 电量控制
	private InactivityTimer inactivityTimer;
	// 声音、震动控制
	private BeepManager beepManager;

	private ImageButton imageButton_back;
	private Button handAdd_btn;

	private ImageView light,photo;
	private boolean isOpenLight = false;
	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}
	private RequestModel requestModel = RequestModel.getInstance();
	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	/**
	 * OnCreate中初始化一些辅助类，如InactivityTimer（休眠）、Beep（声音）以及AmbientLight（闪光灯）
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// 保持Activity处于唤醒状态
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);
		OgeApplication.addActivity(this);
		hasSurface = false;

		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);

		imageButton_back = (ImageButton) findViewById(R.id.capture_imageview_back);
		imageButton_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CaptureActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		});
		handAdd_btn = (Button) findViewById(R.id.capture_hand_add_btn);
		handAdd_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(CaptureActivity.this, HandAddDeviceActivity.class);
				startActivity(intent);
			}
		});

		light = (ImageView) findViewById(R.id.capture_light_ib);
		photo = (ImageView) findViewById(R.id.capture_photo_ib);
		light.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean b = cameraManager.flashHandler();
				if (b) {
					light.setImageResource(R.mipmap.home_scan_light_close);
				} else {
					light.setImageResource(R.mipmap.home_scan_light_open);
				}

			}
		});
		photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				photo();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager必须在这里初始化，而不是在onCreate()中。
		// 这是必须的，因为当我们第一次进入时需要显示帮助页，我们并不想打开Camera,测量屏幕大小
		// 当扫描框的尺寸不正确时会出现bug
		cameraManager = new CameraManager(getApplication());
		
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// activity在paused时但不会stopped,因此surface仍旧存在；
			// surfaceCreated()不会调用，因此在这里初始化camera
			initCamera(surfaceHolder);
		} else {
			// 重置callback，等待surfaceCreated()来初始化camera
			surfaceHolder.addCallback(this);
		}

		beepManager.updatePrefs();
		inactivityTimer.onResume();

		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		beepManager.close();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	/**
	 * 扫描成功，处理反馈信息
	 * 
	 * @param rawResult
	 * @param barcode
	 * @param scaleFactor
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();

		boolean fromLiveScan = barcode != null;
		//这里处理解码完成后的结果，此处将参数回传到Activity处理
		if (fromLiveScan) {
			beepManager.playBeepSoundAndVibrate();
			String result = rawResult.getText();
			//Toast.makeText(CaptureActivity.this, result, Toast.LENGTH_SHORT).show();
			Log.e(TAG+"扫描结果",result);
			String[] s = result.split("\r");
			String title = result.substring(0, 2);
			Log.e(TAG+"扫描结果serial",title);
			SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
			if (title.contains("ys")) {//添加摄像头
				if (sharedPreferencesManager.get("is_belong").equals("1")) {
					Utils.showDialog(CaptureActivity.this, "子帐号，分享帐号无权限添加摄像头！");
				} else {
					Intent intent1 = new Intent(this, EZ_CameraResultActiviry.class);
					intent1.putExtra("cameraSerial", s[1]);
					intent1.putExtra("cameraVerification", s[2]);
					startActivity(intent1);
				}
			} else if (title.contains("Neuron")) {//添加设备
				//String serial = s[1];
				Intent intent = new Intent(CaptureActivity.this, AddDeviceActivity.class);
				intent.putExtra("serial", result);
				startActivity(intent);
			} else if (title.contains("HO")){//音响
                if (sharedPreferencesManager.get("is_belong").equals("1")) {
                    Utils.showDialog(CaptureActivity.this, "子帐号，分享帐号无权限添加音响！");
                } else {
                    APIService service = RetrofitFactory.getInstance().createRetrofit(URLUtils.BASE_URL).create(APIService.class);
                    Observable.just(null)
                            .map(object -> requestModel.generateServiceTimeCmd()) //取得请求服务器时间请求包
                            .flatMap(cmd -> service.getServerTime(cmd)) //请求服务器获取时间
                            .flatMap((timeResponse) -> {
                                String time = timeResponse.getData().getTime(); //返回服务器时间
                                return service.login(requestModel.generateVerifyExternalUser(time, sharedPreferencesManager.get("account"), URLUtils.AppKey, URLUtils.SecretKey));  //请求登录
                            })
                            .doOnNext(response -> { //返回数据进行处理
								if (AppCommandType.SUCCESS.equals(response.getResult())) {     //访问成功，保存用户
									sharedPreferencesManager.save("HopeToken", response.getData().getToken());
									Log.e(TAG + "音响扫描序列号", result);
									service.addDevice(requestModel.generateAddDeviceCmd(result))
											.observeOn(AndroidSchedulers.mainThread())
											.subscribeOn(Schedulers.io())
											.subscribe(cmdRequest -> {
												//结果处理
												String hopeResult = cmdRequest.getResult();
												Log.e(TAG + "音响添加结果", hopeResult);
												if (hopeResult.equals("Success")) {
                                                    Intent intent3 = new Intent(CaptureActivity.this, AddDeviceActivity.class);
                                                    intent3.putExtra("serial", cmdRequest.getData().getDeviceId());
                                                    intent3.putExtra("type", 3);
                                                    startActivity(intent3);
												}
											});
								} else {
									Log.e(TAG + "音响登录失败", "00000000000");
								}
							})
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(response -> {

                            }, throwable -> {

                            });
                }

			} else {//添加设备 暂时使用
				Intent intent2 = new Intent(CaptureActivity.this, AddDeviceActivity.class);
				intent2.putExtra("serial", result);
				intent2.putExtra("type", 2);
				startActivity(intent2);
			}
		}
	}

	/**
	 * 初始化Camera
	 * 
	 * @param surfaceHolder
	 */
	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			return;
		}
		try {
			// 打开Camera硬件设备
			cameraManager.openDriver(surfaceHolder);
			// 创建一个handler来打开预览，并抛出一个运行时异常
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						decodeHints, characterSet, cameraManager);
			}
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	/**
	 * 显示底层错误信息并退出应用
	 */
	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}
	private static final int REQUEST_CODE = 234;
	private Bitmap scanBitmap;
	private String photo_path;
	private void photo() {

		Intent innerIntent = new Intent(); // "android.intent.action.GET_CONTENT"
		if (Build.VERSION.SDK_INT < 19) {
			innerIntent.setAction(Intent.ACTION_GET_CONTENT);
		} else {
			innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
		}
		// innerIntent.setAction(Intent.ACTION_GET_CONTENT);

		innerIntent.setType("image/*");

		Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");

		CaptureActivity.this
				.startActivityForResult(wrapperIntent, REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {

			switch (requestCode) {

				case REQUEST_CODE:

					String[] proj = { MediaStore.Images.Media.DATA };
					// 获取选中图片的路径
					Cursor cursor = getContentResolver().query(data.getData(),
							proj, null, null, null);

					if (cursor.moveToFirst()) {

						int column_index = cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
						photo_path = cursor.getString(column_index);
						if (photo_path == null) {
							photo_path = Utils.getPath(getApplicationContext(),
									data.getData());
							Log.i("123path  Utils", photo_path);
						}
						Log.i("123path", photo_path);

					}

					cursor.close();

					new Thread(new Runnable() {

						@Override
						public void run() {

							Result result = scanningImage(photo_path);
							// String result = decode(photo_path);
							if (result == null) {
								Log.i("123", "   -----------");
								Looper.prepare();
								Toast.makeText(CaptureActivity.this, "图片格式有误", Toast.LENGTH_LONG)
										.show();
								Looper.loop();
							} else {
								Log.i("123result", result.toString());
								// Log.i("123result", result.getText());
								// 数据返回
								String recode = recode(result.toString());
								Intent data = new Intent();
								data.putExtra("result", recode);
								setResult(300, data);
								finish();
							}
						}
					}).start();
					break;

			}

		}
	}
	// TODO: 解析部分图片
	protected Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {

			return null;

		}
		// DecodeHintType 和EncodeHintType
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小

		int sampleSize = (int) (options.outHeight / (float) 200);

		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);

		// --------------测试的解析方法---PlanarYUVLuminanceSource-这几行代码对project没作功----------

		LuminanceSource source1 = new PlanarYUVLuminanceSource(
				rgb2YUV(scanBitmap), scanBitmap.getWidth(),
				scanBitmap.getHeight(), 0, 0, scanBitmap.getWidth(),
				scanBitmap.getHeight(), false);
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
				source1));
		MultiFormatReader reader1 = new MultiFormatReader();
		Result result1;
		try {
			result1 = reader1.decode(binaryBitmap);
			String content = result1.getText();
			Log.i("123content", content);
		} catch (NotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// ----------------------------

		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {

			return reader.decode(bitmap1, hints);

		} catch (NotFoundException e) {

			e.printStackTrace();

		} catch (ChecksumException e) {

			e.printStackTrace();

		} catch (FormatException e) {

			e.printStackTrace();

		}
		return null;
	}
	/**
	 * 中文乱码
	 *
	 * 暂时解决大部分的中文乱码 但是还有部分的乱码无法解决 .
	 *
	 * 如果您有好的解决方式 请联系 2221673069@qq.com
	 *
	 * 我会很乐意向您请教 谢谢您
	 *
	 * @return
	 */
	private String recode(String str) {
		String formart = "";

		try {
			boolean ISO = Charset.forName("ISO-8859-1").newEncoder()
					.canEncode(str);
			if (ISO) {
				formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
				Log.i("1234      ISO8859-1", formart);
			} else {
				formart = str;
				Log.i("1234      stringExtra", str);
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return formart;
	}
	/**
	 * //TODO: TAOTAO 将bitmap由RGB转换为YUV //TOOD: 研究中
	 *
	 * @param bitmap
	 *            转换的图形
	 * @return YUV数据
	 */
	public byte[] rgb2YUV(Bitmap bitmap) {
		// 该方法来自QQ空间
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		int len = width * height;
		byte[] yuv = new byte[len * 3 / 2];
		int y, u, v;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int rgb = pixels[i * width + j] & 0x00FFFFFF;

				int r = rgb & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb >> 16) & 0xFF;

				y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
				u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
				v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

				y = y < 16 ? 16 : (y > 255 ? 255 : y);
				u = u < 0 ? 0 : (u > 255 ? 255 : u);
				v = v < 0 ? 0 : (v > 255 ? 255 : v);

				yuv[i * width + j] = (byte) y;
				// yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
				// yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
			}
		}
		return yuv;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(CaptureActivity.this, MainActivity.class);
			startActivity(intent);
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}
}
