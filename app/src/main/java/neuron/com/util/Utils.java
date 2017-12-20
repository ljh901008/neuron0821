package neuron.com.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import neuron.com.app.OgeApplication;
import neuron.com.bean.RoomItemBean;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.room.Activity.EZ_OpenServiceActivity;

/**
 * Created by ljh on 2016/10/19.
 */
public class Utils {
    /**
     *   正则匹配手机号的方法
     * @param mobiles 手机号
     * @return
     */
    public static boolean isMobileNO(String mobiles){
        Pattern p = Pattern.compile("1[3|4|5|7|8][0-9]{9}");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
    /**
     * 正则表达式验证密码
     * @param input
     * @return
     */
    public static boolean rexCheckPassword(String input) {
        // 6-12 位，字母、数字、字符
        String regStr = "^[0-9a-zA-Z]{6,12}$";
        return input.matches(regStr);
    }

    /**
     *  匹配全是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    /**
     *  匹配是小数
     * @param str
     * @return
     */
    public static boolean isDouble(String str){
            Pattern pattern = Pattern.compile("^\\d+(\\.\\d+)?$");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    /**
     *  匹配用户名
     * @param str
     * @return
     */
    public static boolean isUserName(String str){
        Pattern pattern = Pattern.compile("/^[a-zA-Z]\\w{3,9}$/ig");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    /**
     *  匹配设备名称只能是2-8个汉字
     * @param str
     * @return
     */
    public static boolean isDeviceName(String str){
        Pattern pattern = Pattern.compile("^[\\u4e00-\\u9fa5]{1,8}$");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }
    /**
     *      dialog  只有一个确定button
     * @param context
     * @param content    内容
     */
    public static void showDialog(Context context,String content){
        final AlertDialog builder = new AlertDialog.Builder(context).create();
        View view = View.inflate(context, R.layout.dialog_textview, null);
        TextView title = (TextView) view.findViewById(R.id.textView1);
        Button button = (Button) view.findViewById(R.id.button1);
        title.setText(content);
        builder.setView(view);
        button.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               builder.dismiss();
           }
        });
        builder.show();
    }
    /**
     *      dialog  只有一个确定button
     * @param context
     * @param content    内容
     */
    public static void showDialogTwo(Context context, String content){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.dialog_textview, null);
        TextView title = (TextView) view.findViewById(R.id.textView1);
        Button button = (Button) view.findViewById(R.id.button1);
        title.setText(content);
        builder.setView(view);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                builder.create().dismiss();
            }
        });

        builder.create().show();
    }
    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };
                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }



    //创建文件夹及文件
    public static void createText(String SDPath,String dirName) throws IOException {
        File file = new File(SDPath,dirName);
        if (!file.exists()) {
            try {
                //按照指定的路径创建文件夹
                file.mkdirs();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }



    /**
     *    向已创建的文件中写入数据,帐号用的写入拼接上了\n分割符  ，此方法不能用在帐号写入本地
     * @param content  内容
     * @param SDPath  文件夹的路径
     * @param fileName  文件的名称
     */
    public static void print(String content,String SDPath,String fileName) {
        BufferedWriter bw = null;
        File file = new File(SDPath ,fileName);
        try {
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
            bw.write(content);
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *       读取本地存文件
     * @param filePath　根路径
     * @param dirName   文件夹名称
     * @param fileName  文件名称
     * @return
     */
    public static String input(String filePath,String dirName,String fileName){
        StringBuffer stringBuffer = null;
        try {
            File file = new File(filePath + dirName, fileName);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String readline = "";
                stringBuffer = new StringBuffer();
                while ((readline = br.readLine()) != null) {
                    stringBuffer.append(readline);
                }
                br.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String listAS = stringBuffer.toString();
        return listAS;
    }
    /**
     *   查询设备详情
     * @param aesAccount
     * @param token
     * @param method
     * @param serial
     * @param deviceId
     * @param xutilsHelper
     * @param order
     * @param context
     */
    public static void getDeviceDetail(String aesAccount, String token,String method,
                                String serial, String deviceId, XutilsHelper xutilsHelper,int order,Context context){
        String sign = MD5Utils.MD5Encode(aesAccount + deviceId + method + serial + token + URLUtils.MD5_SIGN, "");
        xutilsHelper.add("account", aesAccount);
        xutilsHelper.add("deviceid", deviceId);
        xutilsHelper.add("serial_number", serial);
        xutilsHelper.add("token", token);
        xutilsHelper.add("method", method);
        xutilsHelper.add("sign", sign);
       // xutilsHelper.sendPost2(order,context);

    }

    /**
     *   更新设备详情  device 字段数据的解析
     * @param deviceId  设备id
     * @param deviecName 设备名称
     * @param serial   序列号
     * @param roomId    房间id
     * @param listRoom
     * @return
     */
    public static JSONObject updateDebiceJson(String deviceId, String deviecName, String serial, String roomId, List<RoomItemBean> listRoom){
        try {
            JSONObject json = new JSONObject();
            json.put("deviceid", deviceId);
            json.put("serial_number", serial);
            json.put("device_name", deviecName);
            json.put("roomid", roomId);
            JSONArray jsonArray = new JSONArray();
            if (listRoom != null) {
                for (int i = 0; i < listRoom.size(); i++) {
                    RoomItemBean bean = listRoom.get(i);
                    JSONObject json1 = new JSONObject();
                    json1.put("controled_deviceid", bean.getDeviceId());
                    json1.put("name", bean.getDeviceName());
                    //json1.put("roomid", bean.getRoomId());
                    json1.put("brand", bean.getDeviceType());
                    json1.put("serial", bean.getSerialNumber());
                    jsonArray.put(json1);
                }
            }
            json.put("control_devicelist", jsonArray);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param context  此方法检测萤石token可用性 可在需要的地方调用
     */
    public static void detectionEZToken(Context context){
        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(context);
        if (!sharedPreferencesManager.has("EZToken")) {//判断本地是否有accesstoken，是否开启了萤石云服务
            Intent intent = new Intent(context, EZ_OpenServiceActivity.class);
            context.startActivity(intent);
        } else {
            if (sharedPreferencesManager.has("EZTime")) {
                Date date = new Date();
                long time = date.getTime()/1000;//获取当前时间  。以秒为单位
                String t = sharedPreferencesManager.get("EZTime");
                long EZTime = Integer.parseInt(t);
                long differ = time - EZTime;
                if (differ >= 86400 * 7) {//判断保存accesstokentime  是否大于7天，大于7天需要重新获取accesstoken
                    Toast.makeText(context, "accesstoken过期，请重新获取", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(context, EZ_OpenServiceActivity.class);
                    context.startActivity(intent);
                } else {
                    if (sharedPreferencesManager.has("EZToken")) {
                        OgeApplication.getOpenSDK().setAccessToken(sharedPreferencesManager.get("EZToken"));
                    } else {
                        Toast.makeText(context, "accesstoken不存在，请重新获取，摄像头才能正常使用", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
        if (sharedPreferencesManager != null) {
            sharedPreferencesManager = null;
        }
    }

    private static  WaitDialog mWaitDlg;

    /**
     * 等待框加载
     * @param text
     * @param context
     */
    public static void showWaitDialog(String text,Activity context,WaitDialog mWaitDlg){
        mWaitDlg.setWaitText(text);
        mWaitDlg.setCancelable(false);
        if (!mWaitDlg.isShowing()) {
            mWaitDlg.show();
        }
    }
    /**
     * dismiss进度圈
     *
     * @see
     * @since V1.8.2
     */
    public static void dismissWaitDialog(WaitDialog mWaitDlg) {
        if (mWaitDlg != null && mWaitDlg.isShowing()) {
            mWaitDlg.dismiss();
        }
    }


   /* *//**
     * 写入本地文件
     * @param context
     * @param obj
     * @param fileName
     *//*
    public static void write(Context context, Object obj, String fileName) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream oout = new ObjectOutputStream(bout);
            oout.writeObject(obj);
            oout.flush();
            oout.close();
            bout.close();
            byte[] b = bout.toByteArray();
            File file = new File(context.getFilesDir(), fileName);
            FileOutputStream out = new FileOutputStream(file);
            out.write(b);
            out.flush();
            out.close();
        } catch (Exception e) {
        } finally {

        }
    }

    *//**
     * 从本地文件读取
     * @param context
     * @param fileName
     * @return
     *//*
    public static Object read(Context context, String fileName) {
        // 拿出持久化数据
        Object obj = null;
        try {
            File file = new File(context.getFilesDir(), fileName);
            FileInputStream in = new FileInputStream(file);
            ObjectInputStream oin = new ObjectInputStream(in);
            obj = oin.readObject();
            in.close();
            oin.close();
        } catch (Exception e) {
        }
        return obj;
    }*/

}
