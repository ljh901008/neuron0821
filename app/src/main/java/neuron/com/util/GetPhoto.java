package neuron.com.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.Date;

import neuron.com.bean.UploadFileBean;
import neuron.com.comneuron.R;

/**
 * 
 * @author 刘俊行
 * @时间：2016年1月12日上午10:31:07
 * @类描述： 包含调用系统相册 和 照相机的 图片压缩
 *                          图片截取的方法
 * @版本：1.0
 * @修改人：
 * @修改地址：
 */
public class GetPhoto {
	private static final String imageDir = "/NueronPhoto";
	private static File file;
	/**
	 * 系统相册Uri
	 */
	private final static Uri imageUri = Uri
			.fromFile(new File(Environment.getExternalStorageDirectory() + "/", "pic.jpg"));
	private final static int TAKE_PHOTO = 1;
	private final static int PHOTO_RESULT = 2;
	public static Uri photoUri;
	/**
	 * 调用系统相册和 照相机的方法
	 * 
	 * @param context
	 *            activity
	 */
	public static void getPhotoZoom(final Activity context) {
		Builder builder = new Builder(context);
		builder.setTitle("请选择图片方式");
		// setItems 第二个参数
		builder.setItems(context.getResources().getStringArray(R.array.itemArray),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:// 调用系统相册
							Intent intent = new Intent("android.intent.action.PICK");
							intent.setType("image/*");
							intent.putExtra("crop", "true");
							intent.putExtra("aspectX", 1);
							intent.putExtra("aspectY", 1);
							intent.putExtra("outputX", 500);
							intent.putExtra("outputY", 500);
							intent.putExtra("scale", true);
							intent.putExtra("return-data", false);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
							intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
							intent.putExtra("noFaceDetection", false); // no//
																		// face//
																		// detection
							context.startActivityForResult(intent, TAKE_PHOTO);

							break;
						case 1:// 调用系统相机
							Intent intent1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用android自带的照相机
							String fname = "blp_" + DateFormat.format("yyyyMMddhhmmss", new Date()).toString() + ".jpg";
							String path = Environment.getExternalStorageDirectory() + imageDir;
							file = new File(path, fname);
							File filePath = new File(path);
							if (!filePath.exists()) {
								filePath.mkdir();
							}
							photoUri = Uri.fromFile(file);
							intent1.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
							context.startActivityForResult(intent1, PHOTO_RESULT);
							break;
						default:
							break;
						}
					}
				});
		builder.show();
	}

	/**
	 * 对返回的结果码进行判断 如果请求码正确 则
	 * 
	 * @param requestCode
	 *            请求码
	 * @param resultCode
	 *            结果码
	 * @param context
	 *            activity
	 * @return UploadFileBean 对象
	 */
	public static UploadFileBean getResultCode(int requestCode, int resultCode, Activity context) {
		if (resultCode == -1) {
			switch (requestCode) {
			case TAKE_PHOTO:
				UploadFileBean bean=new UploadFileBean();
				bean.setBm(getPhoto(imageUri, context));
				bean.setFilePath(new File(imageUri.getPath()));
				
				return bean;
			case PHOTO_RESULT:
				Uri photoUri = Uri.fromFile(file);
				UploadFileBean upBean=new UploadFileBean();
				upBean.setBm(getPhoto(photoUri, context));
				upBean.setFilePath(file);
				return upBean;
			default:
				break;
			}
		}
		return null;
	}

	/**
	 *
	 * 
	 * @param uri
	 *            系统相册路径
	 * @return
	 */
	public static Bitmap getPhoto(Uri uri, Activity context) {
		Bitmap bm = null;
		try {
			ContentResolver cr = context.getContentResolver();
			bm = BitmapFactory.decodeStream(cr.openInputStream(uri));
			bm = calculateInSampleSize(uri.getPath(), 450, 450);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bm;
	}

	/**
	 * 按指定大小压缩图片 照相时可用
	 * 
	 * @param path
	 *            图片路径
	 * @param w
	 *            图片宽
	 * @param h
	 *            图片高
	 * @return
	 */
	private static Bitmap calculateInSampleSize(String path, int w, int h) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			// 设置为ture只获取图片大小
			opts.inJustDecodeBounds = true;
			opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
			// 返回为空
			BitmapFactory.decodeFile(path, opts);
			int width = opts.outWidth;
			int height = opts.outHeight;
			float scaleWidth = 0.f, scaleHeight = 0.f;
			if (width > w || height > h) {
				// 缩放
				scaleWidth = ((float) width) / w;
				scaleHeight = ((float) height) / h;
			}
			opts.inJustDecodeBounds = false;
			float scale = Math.max(scaleWidth, scaleHeight);
			opts.inSampleSize = (int) scale;
			WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));

			Bitmap bMapRotate = Bitmap.createBitmap(weak.get(), 0, 0, weak.get().getWidth(), weak.get().getHeight(),
					null, true);
			if (bMapRotate != null) {
				return bMapRotate;
			}
			return null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

}
