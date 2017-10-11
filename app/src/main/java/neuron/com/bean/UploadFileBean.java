package neuron.com.bean;

import android.graphics.Bitmap;

import java.io.File;
/**
 * 
 * @author
 * @时间：
 * @类描述：  文件上传api的实体类
 * @版本：1.0
 * @修改人：
 * @修改地址：
 */
public class UploadFileBean {
	
	
	private File filePath;
	
	private Bitmap bm;

	public UploadFileBean() {
		super();
	}


	public File getFilePath() {
		return filePath;
	}

	public void setFilePath(File filePath) {
		this.filePath = filePath;
	}

	public Bitmap getBm() {
		return bm;
	}

	public void setBm(Bitmap bm) {
		this.bm = bm;
	}
	
	
}
