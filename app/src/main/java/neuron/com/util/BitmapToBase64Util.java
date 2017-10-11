package neuron.com.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.util.Base64;
/**
 * 
 * @author  刘俊行
 * @时间：2016年1月13日下午12:21:13
 * @类描述： bitmap 转换成Base64的工具类
 * @版本：1.0
 * @修改人：
 * @修改地址：
 */
public class BitmapToBase64Util {
	/**
	 *  把Bitmap转换成Base64 的方法
	 * @param bitmap 对象
	 * @return Base64字符串
	 */
	public static String getBase64(Bitmap bitmap){
		
		String result = null;  
	    ByteArrayOutputStream baos = null;  
	    try {  
	        if (bitmap != null) {  
	            baos = new ByteArrayOutputStream();  
	            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);  
	            
	            baos.flush();  
	            baos.close();  
	  
	            byte[] bitmapBytes = baos.toByteArray();  
	            result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);  
	        }  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    } finally {  
	        try {  
	            if (baos != null) {  
	                baos.flush();  
	                baos.close();  
	            }  
	        } catch (IOException e) {  
	            e.printStackTrace();  
	        }  
	    }  
	    return result;

	}
	/**
	 *  把文件转换成base64
	 * @param path  文件路径
	 * @return  String
	 * @throws Exception
	 */
	public static String encodeBase64File(File  file) throws Exception {
		FileInputStream inputFile = new FileInputStream(file);
		byte[] buffer = new byte[(int)file.length()];
		inputFile.read(buffer);
		        inputFile.close();
		        return Base64.encodeToString(buffer,Base64.DEFAULT);
		}
}
