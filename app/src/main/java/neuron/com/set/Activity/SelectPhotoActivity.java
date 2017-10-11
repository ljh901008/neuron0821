package neuron.com.set.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.LubanOptions;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.model.TakePhotoOptions;

import java.io.File;

import neuron.com.app.OgeApplication;
import neuron.com.comneuron.R;


/**
 * Created by ljh on 2017/3/22.
 */
public class SelectPhotoActivity extends TakePhotoActivity implements View.OnClickListener{
    private Button xiangce,paizhao, back;
    private TakePhoto takePhoto;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_photo);
        OgeApplication.addActivity(this);
        init();
    }
    private void init(){
        xiangce = (Button) findViewById(R.id.pic_photo_xc_btn);
        paizhao = (Button) findViewById(R.id.pic_photo_pz_btn);
        back = (Button) findViewById(R.id.pic_photo_clear_btn);
        xiangce.setOnClickListener(this);
        paizhao.setOnClickListener(this);
        back.setOnClickListener(this);

        takePhoto = getTakePhoto();
        configCompress(takePhoto);
        configTakePhotoOption(takePhoto);

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.pic_photo_xc_btn://相册
                takePhoto.onPickFromGalleryWithCrop(getUri(), getCropOptions());
                break;
            case R.id.pic_photo_pz_btn://拍照
                takePhoto.onPickFromCaptureWithCrop(getUri(),getCropOptions());
                break;
            case R.id.pic_photo_clear_btn://取消
                finish();
                break;
            default:
            break;
        }
    }
    private void showImg(TImage image){
        Intent intent = getIntent();
        String path = image.getCompressPath();
        Log.e("picPath", path);
        intent.putExtra("path", path);
        this.setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        showImg(result.getImage());
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
    }

    //压缩图片
    private void configCompress(TakePhoto takePhoto){
        Log.e("压缩图片", "111");
        int width = 200;
        int height = 220;
        int maxSize = 50 * 1024;
        LubanOptions option=new LubanOptions.Builder()
                .setMaxHeight(height)
                .setMaxWidth(width)
                .setMaxSize(maxSize)
                .create();
        CompressConfig config= CompressConfig.ofLuban(option);
        takePhoto.onEnableCompress(config, false);
    }
    //裁剪图片
    private CropOptions getCropOptions(){
        Log.e("裁剪图片", "222");
        CropOptions.Builder builder=new CropOptions.Builder();
        builder.setOutputY(200);
        builder.setOutputX(200);
        //builder.setAspectX(220);
        //builder.setAspectY(220);
        builder.setWithOwnCrop(true);
        return builder.create();
    }
    /**
     * 给出文件的保存路径
     * @return
     */
    private Uri getUri(){
        Log.e("图片URL", "333");
        File file=new File(Environment.getExternalStorageDirectory(), "/Acc/"+System.currentTimeMillis() + ".jpg");
        if (!file.getParentFile().exists())file.getParentFile().mkdirs();
        Uri imageUri = Uri.fromFile(file);
        return imageUri;
    }

    /**
     * 设置takephoto参数
     * @param takePhoto
     */
    private void configTakePhotoOption(TakePhoto takePhoto){
        Log.e("设置TakePhoto", "444");
        TakePhotoOptions.Builder builder=new TakePhotoOptions.Builder();
        builder.setWithOwnGallery(true);//是否使用takephoto自带图库
        takePhoto.setTakePhotoOptions(builder.create());
    }

}
