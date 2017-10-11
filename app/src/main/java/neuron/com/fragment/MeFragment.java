package neuron.com.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.lock.activity.GestureEditActivity;
import neuron.com.lock.activity.GestureVerifyActivity;
import neuron.com.room.Activity.RoomListActivity;
import neuron.com.set.Activity.AboutUsActivity;
import neuron.com.set.Activity.ChangePassword;
import neuron.com.set.Activity.ChildAccountManagerAcitvity;
import neuron.com.set.Activity.HostManagerActivity;
import neuron.com.set.Activity.SelectPhotoActivity;
import neuron.com.set.Activity.ShareHostManagerListActivity;
import neuron.com.set.Activity.ZiAccountManagerActivity;
import neuron.com.util.AESOperator;
import neuron.com.util.BitmapToBase64Util;
import neuron.com.util.GetPhoto;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;
import neuron.com.view.CircleImageView;

/**
 * Created by ljh on 2016/8/28.
 */
public class MeFragment extends Fragment implements View.OnClickListener{
    //  控制主机管理家庭成员，分享帐号管理，家庭成员管理，房间管理，修改密码，手势密码，修改手势密码，其他的相对布局
    private RelativeLayout hostManager_rllfamily_rll,shareAccount_rll,family_rll,room_rll,modifyPassword_rll,
            handPassword_rll,modifyHandPassword_rll,other_rll;
    //头像
    private CircleImageView photo_iv;
    private ImageView photoTwo_iv;
    private ImageButton share_ibtn;
    //昵称 帐号
    private TextView userName_tv,account_tv,shareAccount_tv;
    private ScrollView scrollView;
    //手势密码开关
    private ImageView handPassword_iv;
    private View view;
    private View family_view;
    private SharedPreferencesManager sharedPreferencesManager;
    private String account=null, token = null;
    private String changePhoto = "ChangePhoto";
    private boolean isSetHandpwd = false;
    //头像的imageview显示的标记
    private int isVisibilePhoto = 0;
    /**
     * 系统相册Uri
     */
    public static Uri imageUri = Uri
            .fromFile(new File(Environment.getExternalStorageDirectory() + "/", "pic.jpg"));
    private String photoPath = null;
    private String str;//昵称
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
           switch(arg1){
               case 1:
                   if (msg.what == 102) {
                       String changePhotoResult = (String) msg.obj;
                       try {
                           JSONObject jsonObject = new JSONObject(changePhotoResult);
                           if (jsonObject.getInt("status") == 9999) {
                               Toast.makeText(getActivity(), "修改头像成功",Toast.LENGTH_LONG).show();
                           } else {
                               Toast.makeText(getActivity(), jsonObject.getString("error"),Toast.LENGTH_LONG).show();
                           }
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }
                    break;
               case 2:
                   if (msg.what == 102) {
                       String result = (String) msg.obj;
                       Log.e("修改昵称", result);
                       try {
                           JSONObject jsonObject = new JSONObject(result);
                           if (jsonObject.getInt("status") == 9999) {
                               Toast.makeText(getActivity(), "修改成功", Toast.LENGTH_SHORT).show();
                               userName_tv.setText("昵称:" + str);
                               sharedPreferencesManager.save("username", str);
                           } else {
                               Toast.makeText(getActivity(), jsonObject.getString("error"), Toast.LENGTH_SHORT).show();
                           }
                       } catch (JSONException e) {
                           e.printStackTrace();
                       }
                   }

                   break;
               default:
                    break;
           }
        }
    };
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mefragment , container , false);
        init();
        setListener();
        return view;
    }

    private void init() {
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getContext());
        hostManager_rllfamily_rll = (RelativeLayout) view.findViewById(R.id.mefragment_hostmanager_rll);
        shareAccount_rll = (RelativeLayout) view.findViewById(R.id.mefragment_shareaccount_rll);
        family_rll = (RelativeLayout) view.findViewById(R.id.mefragment_family_rll);
        room_rll = (RelativeLayout) view.findViewById(R.id.mefragment_room_rll);
        modifyPassword_rll = (RelativeLayout) view.findViewById(R.id.mefragment_password_rll);
        handPassword_rll = (RelativeLayout) view.findViewById(R.id.mefragment_gesture_password_rll);
        modifyHandPassword_rll = (RelativeLayout) view.findViewById(R.id.mefragment_modify_gesture_password_rll);
        other_rll = (RelativeLayout) view.findViewById(R.id.mefragment_other_rll);
        handPassword_iv = (ImageView) view.findViewById(R.id.mefragment_gesture_password_right_iv);
        photo_iv = (CircleImageView) view.findViewById(R.id.mefragment_avatar_iv);
        photoTwo_iv = (ImageView) view.findViewById(R.id.mefragment_photo_iv);
        scrollView = (ScrollView) view.findViewById(R.id.mefragment_pullsv);
        share_ibtn = (ImageButton) view.findViewById(R.id.mefragment_share_ibtn);
        userName_tv = (TextView) view.findViewById(R.id.mefragment_username_tv);
        account_tv = (TextView) view.findViewById(R.id.mefragment_account_tv);
        shareAccount_tv = (TextView) view.findViewById(R.id.mefragment_shareaccount_tv);
        family_view = view.findViewById(R.id.mefragment_view4);
        if (sharedPreferencesManager.has("photo_path")) {
            photoPath = sharedPreferencesManager.get("photo_path");
            Log.e("我的页面头像路径", photoPath);
            //加载头像
            XutilsHelper xutilsHelper = new XutilsHelper();
            xutilsHelper.downloadPhoto(photoTwo_iv,getActivity(),photoPath);
        }
        if (sharedPreferencesManager.has("isFirstLogin")) {
            if ("0".equals(sharedPreferencesManager.get("isFirstLogin"))) {
                handPassword_iv.setImageResource(R.mipmap.kaiguan_guan);
                modifyHandPassword_rll.setVisibility(View.GONE);
            } else {
                handPassword_iv.setImageResource(R.mipmap.kaiguan_kai);
                modifyHandPassword_rll.setVisibility(View.VISIBLE);
            }
        } else {
            handPassword_iv.setImageResource(R.mipmap.kaiguan_guan);
            modifyHandPassword_rll.setVisibility(View.GONE);
        }
        if (sharedPreferencesManager.has("account")) {
            account_tv.setText("帐号: "+sharedPreferencesManager.get("account"));
        }
        if (sharedPreferencesManager.has("username")) {
            userName_tv.setText("昵称: "+sharedPreferencesManager.get("username"));
        }
        if (sharedPreferencesManager.has("userType")) {
            if (sharedPreferencesManager.get("userType").equals("02")) {//子帐号为02，主账号权限为01
                family_rll.setVisibility(View.GONE);
                family_view.setVisibility(View.GONE);
                shareAccount_tv.setTextColor(getResources().getColor(R.color.text_gray));
            } else {
                shareAccount_rll.setOnClickListener(this);
            }
        }
    }
    private void setListener(){
        hostManager_rllfamily_rll.setOnClickListener(this);
        family_rll.setOnClickListener(this);
        room_rll.setOnClickListener(this);
        modifyPassword_rll.setOnClickListener(this);
        handPassword_rll.setOnClickListener(this);
        modifyHandPassword_rll.setOnClickListener(this);
        other_rll.setOnClickListener(this);
        handPassword_iv.setOnClickListener(this);
        photoTwo_iv.setOnClickListener(this);
        photo_iv.setOnClickListener(this);
        share_ibtn.setOnClickListener(this);
        userName_tv.setOnClickListener(this);
       // XutilsHelper xutilsHelper = new XutilsHelper();
        //xutilsHelper.downloadPhoto(photoTwo_iv,getActivity(),sharedPreferencesManager.get("photo_path"));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mefragment_username_tv://昵称
                showDialog();
                break;
            case R.id.mefragment_hostmanager_rll://控制主机管理
                Intent intent = new Intent(getActivity(), HostManagerActivity.class);
                startActivity(intent);
                break;
            case R.id.mefragment_shareaccount_rll://分享帐号管理
                Intent intent2 = new Intent(getActivity(), ChildAccountManagerAcitvity.class);
                intent2.putExtra("type", 1);
                startActivity(intent2);
                break;
            case R.id.mefragment_family_rll://家庭成员管理
                Intent intent6 = new Intent(getActivity(), ZiAccountManagerActivity.class);
                intent6.putExtra("type", 2);
                startActivity(intent6);
                break;
            case R.id.mefragment_room_rll://房间管理
                Intent intent4 = new Intent(getActivity(), RoomListActivity.class);
                intent4.putExtra("type", 1);
                startActivity(intent4);
                break;
            case R.id.mefragment_password_rll://修改密码
                Intent in = new Intent(getActivity(), ChangePassword.class);
                startActivity(in);
                break;
            case R.id.mefragment_gesture_password_rll://手势密码
                if (sharedPreferencesManager.has("isFirstLogin")) {
                    if ("0".equals(sharedPreferencesManager.get("isFirstLogin"))) {
                        handPassword_iv.setImageResource(R.mipmap.kaiguan_kai);
                        modifyHandPassword_rll.setVisibility(View.VISIBLE);
                        sharedPreferencesManager.save("isFirstLogin", "1");
                    } else {
                        handPassword_iv.setImageResource(R.mipmap.kaiguan_guan);
                        modifyHandPassword_rll.setVisibility(View.GONE);
                        sharedPreferencesManager.save("isFirstLogin", "0");
                    }
                }
                break;
            case R.id.mefragment_gesture_password_right_iv://手势密码按钮
                if (sharedPreferencesManager.has("isFirstLogin")) {
                    Log.e("isFirstLogin", sharedPreferencesManager.get("isFirstLogin"));
                    if ("0".equals(sharedPreferencesManager.get("isFirstLogin"))) {
                        handPassword_iv.setImageResource(R.mipmap.kaiguan_kai);
                        modifyHandPassword_rll.setVisibility(View.VISIBLE);
                        sharedPreferencesManager.save("isFirstLogin", "1");
                        if (!sharedPreferencesManager.has("handlock")) {
                            Intent intent9 = new Intent(getActivity(), GestureEditActivity.class);
                            intent9.putExtra("type", 2);
                            startActivity(intent9);
                        }
                    } else {
                        handPassword_iv.setImageResource(R.mipmap.kaiguan_guan);
                        modifyHandPassword_rll.setVisibility(View.GONE);
                        sharedPreferencesManager.save("isFirstLogin", "0");
                    }
                }
                break;
            case R.id.mefragment_modify_gesture_password_rll://修改手势密码
                if (sharedPreferencesManager.has("handlock")) {
                    Intent intent8 = new Intent(getActivity(), GestureVerifyActivity.class);
                    intent8.putExtra("type", 2);
                    startActivity(intent8);
                } else {
                    Intent intent8 = new Intent(getActivity(), GestureEditActivity.class);
                    intent8.putExtra("type", 2);
                    startActivity(intent8);
                }
                break;
            case R.id.mefragment_other_rll://其他
                Intent mIntent = new Intent(getActivity(), AboutUsActivity.class);
                startActivity(mIntent);
                break;
            case R.id.mefragment_avatar_iv://自定义圆形头像
                Log.e("我点击了头像1111", "dfafa");
                Intent intent3 = new Intent(getActivity(), SelectPhotoActivity.class);
                startActivityForResult(intent3, Activity.RESULT_FIRST_USER);
                break;
            case R.id.mefragment_photo_iv://头像
                Log.e("我点击了头像22222", String.valueOf(isVisibilePhoto));
                    Intent intent9 = new Intent(getActivity(), SelectPhotoActivity.class);
                    startActivityForResult(intent9, Activity.RESULT_FIRST_USER);
                break;
            case R.id.mefragment_share_ibtn://右上角分享
                Intent intent1 = new Intent(getActivity(), ShareHostManagerListActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Activity.RESULT_FIRST_USER) {
                String path = data.getStringExtra("path");
                Uri uri = Uri.fromFile(new File(path));
                Bitmap bitmap = GetPhoto.getPhoto(uri, getActivity());
                photo_iv.setImageBitmap(bitmap);
                photoTwo_iv.setVisibility(View.GONE);
                photo_iv.setVisibility(View.VISIBLE);
                UploadPhoto(bitmap);

            }
        }
    }

    /**
     *  上传头像
     * @param bitmap
     */
    private void UploadPhoto(Bitmap bitmap){
        String base64 = null;
        if (bitmap != null) {
            base64 = BitmapToBase64Util.getBase64(bitmap);
            if (sharedPreferencesManager.has("account")) {
                account = sharedPreferencesManager.get("account");
            }
            if (sharedPreferencesManager.has("token")) {
                token = sharedPreferencesManager.get("token");
            }
            try {
                String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                String sign = MD5Utils.MD5Encode(aesAccount + changePhoto + base64 + token + URLUtils.MD5_SIGN, "");
                XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.USERNAME_URL, handler);
                xutilsHelper.add("account", aesAccount);
                xutilsHelper.add("photo", base64);
                xutilsHelper.add("token", token);
                xutilsHelper.add("method", changePhoto);
                xutilsHelper.add("sign", sign);
                xutilsHelper.sendPost(1, getActivity());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Utils.showDialog(getActivity(),"图片不存在");
        }
    }

    /**
     *   修改帐号昵称
     * @param userName  昵称
     * @param method  方法名
     */
    private void updateUserName(String userName,String method){
        if (sharedPreferencesManager.has("account")) {
           account = sharedPreferencesManager.get("account");
        }
        if (sharedPreferencesManager.has("token")) {
            token = sharedPreferencesManager.get("token");
        }
        try {
            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
            String sign = MD5Utils.MD5Encode(aesAccount + method + token + userName + URLUtils.MD5_SIGN, "");
            XutilsHelper xutils = new XutilsHelper(URLUtils.USERNAME_URL, handler);
            xutils.add("account", aesAccount);
            xutils.add("username", userName);
            xutils.add("token", token);
            xutils.add("method", method);
            xutils.add("sign", sign);
            xutils.sendPost(2, getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showDialog(){
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        dialog.setView(LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input, null));
        dialog.show();
        dialog.getWindow().setContentView(R.layout.dialog_input);
        Button btnPositive = (Button) dialog.findViewById(R.id.btn_add);
        Button btnNegative = (Button) dialog.findViewById(R.id.btn_cancel);
        final EditText etContent = (EditText) dialog.findViewById(R.id.et_content);
        TextView titleName = (TextView) dialog.findViewById(R.id.name_tv);
        titleName.setText("修改昵称");
        etContent.setHint("请输入昵称(1-10个字符)");
        btnPositive.setText("确定");
        btnPositive.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                 str = etContent.getText().toString();
                if (TextUtils.isEmpty(str) || str.length() > 10) {
                    etContent.setError("请输入1-10个字符");
                } else {
                    updateUserName(str, "ChangeUserName");
                    dialog.dismiss();

                }
            }
        });
        btnNegative.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
            }
        });
    }

}
