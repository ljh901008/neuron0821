package neuron.com.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.common.util.DensityUtil;
import org.xutils.ex.HttpException;
import org.xutils.http.RequestParams;
import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.io.UnsupportedEncodingException;

import neuron.com.comneuron.R;

/**
 * 
 * @ClassName: XUtilsHelper.java
 * @Description: 开发框架XUtils帮助类，此类封装了该框架的各种网络请求
 * @author 
 * @Date 
 */
public class XutilsHelper {
	private Handler handler;//Handler对象
	private String url;//请求URL
//	private HttpUtils httpUtils;//网络连接对象
	// 编码与服务器端字符编码一致为utf-8
	private final String CHARSET = "utf-8";
	private RequestParams params;//post请求参数
	private String filename;//文件名称
	// 网络数据返回结果状态码（ 0：返回数据为null，1：发生异常；2：正常）
//	private int status = 2;
	public static final int SUCCESS=102;//请求结果成功
	public static final int FAILURE=101;//请求结果失败
	public static final int NULL=100;//请求结果为空
	//日志tag
	private final String LOG_TAG="will";
	

	/**
	 * 构造方法
	 * 
	 * @param url
	 *            网络资源地址
	 * @param handler
	 *            消息处理对象，用于请求完成后的怎么处理返回的结果数据
	 */
	public XutilsHelper(String url, Handler handler) {
		// 保存网络资源文件名，要在转码之前保存，否则是乱码
		filename = url.substring(url.lastIndexOf("/") + 1, url.length());
		// 解决中文乱码问题，地址中有中文字符造成乱码问题
		 try {
			this.url = new String(url.getBytes(), CHARSET).replace(" ", "%20");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
//		this.httpUtils = XutilsHttpClient.getInstence();
		this.handler = handler;
		this.params = new RequestParams(url);
	}

	public XutilsHelper() {
	}

	/**
	 * 异步的
	 * 
	 * @param order
	 *            网络请求顺序标识符 ，该方法的handler返回的数据有三种情况 0：返回数据为null，1：发生异常；2：正常
	 */
	public void sendGet(final int order,Context context) {
		if (NetWorkUtil.checkEnable(context)) {
			x.http().get(params, new Callback.CommonCallback<String>() {

				@Override
				public void onCancelled(CancelledException arg0) {
					// TODO Auto-generated method stub
					
				}
				 // 注意:如果是自己onSuccess回调方法里写了一些导致程序崩溃的代码，也会回调道该方法，
//				因此可以用以下方法区分是网络错误还是其他错误  
	            // 还有一点，网络超时也会也报成其他错误，还需具体打印出错误内容比较容易跟踪查看  
				@Override
				public void onError(Throwable ex, boolean isOnCallback) {
					// TODO Auto-generated method stub
					Message msg1 = handler.obtainMessage();
					if (ex instanceof HttpException) { // 网络错误
	                    HttpException httpEx = (HttpException) ex;
	                    int responseCode = httpEx.getCode();  
	                    String responseMsg = httpEx.getMessage();  
	                    String errorResult = httpEx.getResult();  
	                    
	                    msg1.what = FAILURE;
	                    msg1.obj = responseMsg;
	                    msg1.arg1 = order;
	                    handler.sendMessage(msg1);
	                    // ...  
	                } else { // 其他错误  
	                    // ...  
	                	msg1.arg1 = order;
	                	msg1.obj = "网络超时";
	                	msg1.what = FAILURE;
	                	handler.sendMessage(msg1);
	                }  
				}

				@Override
				public void onFinished() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onSuccess(String result) {
					// TODO Auto-generated method stub
					Log.e("++get++url++++", "Suceess_url: " + url);
					Message msg = handler.obtainMessage();
					Log.i(LOG_TAG, "result: " + result);
					if (result == null || result.equals("null")
							|| result == "") {
						msg.arg1 = order;
						msg.what = NULL;
					} else {
						msg.what = SUCCESS;
						msg.arg1 = order;
						msg.obj = result;
					}
					handler.sendMessage(msg);
				}
			});
		}else{
			Toast.makeText(context, "网络不通，请重试", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * POST请求时需要添加提交参数
	 * @param json JSONObject对象
	 */
	public void addRequestParams(JSONObject json) {
		params.setCharset(CHARSET);
		params.setAsJsonContent(true);
		params.setBodyContent(json.toString());
		//设置请求时长
		params.setConnectTimeout(25000);
	}


	public void add(String name,String value){
		//设置请求时长
		params.setConnectTimeout(25000);
		params.addBodyParameter(name, value);

	}
	/**
	 * 异步的
	 * 
	 * @param order
	 *            网络请求顺序标识符 ，该方法的handler返回的数据有三种情况 0：返回数据为null，1：发生异常；2：正常
	 */
	public void sendPost(final int order,Context context) {

		x.http().post(params, new Callback.CommonCallback<String>() {
  
			@Override
			public void onCancelled(CancelledException arg0) {
				// TODO Auto-generated method stub
				
			}
			 // 注意:如果是自己onSuccess回调方法里写了一些导致程序崩溃的代码，也会回调道该方法，
//			因此可以用以下方法区分是网络错误还是其他错误  
            // 还有一点，网络超时也会也报成其他错误，还需具体打印出错误内容比较容易跟踪查看  
			@Override
			public void onError(Throwable ex, boolean arg1) {
				// TODO Auto-generated method stub
				Message msg1 = handler.obtainMessage();
				if (ex instanceof HttpException) { // 网络错误
                    HttpException httpEx = (HttpException) ex;
                    int responseCode = httpEx.getCode();  
                    String responseMsg = httpEx.getMessage();  
                    String errorResult = httpEx.getResult();  
                    msg1.what = FAILURE;
                    msg1.obj = responseMsg;
                    msg1.arg1 = order;
                    handler.sendMessage(msg1);
                } else { // 其他错误  
                	msg1.arg1 = order;
                	msg1.obj = "网络超时";
                	msg1.what = FAILURE;
                	handler.sendMessage(msg1);
                }
			}
			@Override
			public void onFinished() {
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(String result) {
				// TODO Auto-generated method stub
				Log.i(LOG_TAG, "Suceess_url: " + url);
				Message msg = handler.obtainMessage();
				if (result == null || result.equals("null")) {
					msg.arg1 = order;
					msg.what = NULL;
				} else {
					msg.arg1 = order;
					msg.what = SUCCESS;
					msg.obj = result;
					Log.v(LOG_TAG, "result: " + result);
				}
					handler.sendMessage(msg);
			}
		});
	}

	public void sendPost2(Callback.CommonCallback<String> callback) {

		x.http().post(params, callback);
	}
	/**
	 *    图片加载
	 * @param imageView
	 * @param context
	 * @param photoPath   可以是本地路径也是可以是网络uri
     */
	public static void downloadPhoto(ImageView imageView, Context context, String photoPath){
		if (NetWorkUtil.checkEnable(context)) {

			ImageOptions imageOptions = new ImageOptions.Builder()
					.setSize(DensityUtil.dip2px(90), DensityUtil.dip2px(90))//图片大小
					.setCrop(true)//如果ImageView的大小不是定义为wrap_content, 不要crop.
					.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
					.setLoadingDrawableId(R.mipmap.avatar)//加载中默认显示图片
					.setFailureDrawableId(R.mipmap.avatar)//加载失败后默认显示图片
					.setCircular(true)
					.build();

			x.image().bind(imageView, photoPath, imageOptions, new Callback.CommonCallback<Drawable>() {
				@Override
				public void onSuccess(Drawable drawable) {
					Log.e("下载头像成功提示", "下载头像成功");
					//成功提示
				}

				@Override
				public void onError(Throwable throwable, boolean b) {
					//下载失败提示
					Log.e("下载头像失败提示", "下载头像失败");
				}

				@Override
				public void onCancelled(CancelledException e) {

				}

				@Override
				public void onFinished() {

				}
			});
		} else {
			Toast.makeText(context, "网络不通，请重试", Toast.LENGTH_LONG).show();
		}
	}
	
	
}
