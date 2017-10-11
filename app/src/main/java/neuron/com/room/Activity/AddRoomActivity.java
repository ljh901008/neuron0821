package neuron.com.room.Activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import neuron.com.comneuron.BaseActivity;
import neuron.com.comneuron.MainActivity;
import neuron.com.comneuron.R;
import neuron.com.database.SharedPreferencesManager;
import neuron.com.util.AESOperator;
import neuron.com.util.MD5Utils;
import neuron.com.util.URLUtils;
import neuron.com.util.Utils;
import neuron.com.util.XutilsHelper;

/**
 * Created by ljh on 2016/11/7.
 */
public class AddRoomActivity extends BaseActivity implements View.OnClickListener{
    private String TAG = "AddRoomActivity";
    //返回键
    private ImageView back_iv;
    //添加键
    private Button addRoom_tv;
    //房间名
    private EditText roomName_ed;
    private SharedPreferencesManager sharedPreferencesManager;
    private String roomName;
    private String account, token, engine_id;
    private String ADDROOM_METHOD = "AddRoom";
    private Intent intent;
    private int type;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int arg1 = msg.arg1;
            switch (arg1) {
                case 1://添加房间
                    if (msg.what == 102) {
                        String result = (String) msg.obj;
                        Log.e("添加房间", result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            if (jsonObject.getInt("status") == 9999) {
                               /* Utils.showDialogTwo(AddRoomActivity.this, "添加成功", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (type == 1) {//首页
                                            Intent intent = new Intent(AddRoomActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        } else if (type == 2) {//其他页面
                                            Intent i1 = new Intent(AddRoomActivity.this, RoomListActivity.class);
                                            startActivity(i1);
                                        }
                                        dialogInterface.dismiss();
                                    }
                                });*/
                                final AlertDialog.Builder builder = new AlertDialog.Builder(AddRoomActivity.this);
                                View view = View.inflate(AddRoomActivity.this, R.layout.dialog_textview, null);
                                TextView title = (TextView) view.findViewById(R.id.textView1);
                                Button button = (Button) view.findViewById(R.id.button1);
                                title.setText("添加成功");
                                builder.setView(view);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (type == 1) {//首页
                                            Intent intent = new Intent(AddRoomActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        } else if (type == 2) {//其他页面
                                            //Intent i1 = new Intent(AddRoomActivity.this, RoomListActivity.class);
                                            //startActivity(i1);
                                            intent.putExtra("tag", 1);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                        builder.create().dismiss();
                                    }
                                });
                                builder.create().show();
                            } else {
                                Utils.showDialog(AddRoomActivity.this, jsonObject.getString("error"));
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addroom);
        init();
    }

    private void init() {
        intent = getIntent();
        type = intent.getIntExtra("type", 3);
        back_iv = (ImageView) findViewById(R.id.addroom_back_iv);
        addRoom_tv = (Button) findViewById(R.id.addroom_add_tv);
        roomName_ed = (EditText) findViewById(R.id.addroom_roomname_ed);
        roomName_ed.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });
        back_iv.setOnClickListener(this);
        addRoom_tv.setOnClickListener(this);
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addroom_back_iv://返回
                this.finish();
                break;
            case R.id.addroom_add_tv://添加
                roomName = roomName_ed.getText().toString().trim();
                if (!TextUtils.isEmpty(roomName)) {
                    if (sharedPreferencesManager.has("account")) {
                        account = sharedPreferencesManager.get("account");
                    }
                    if (sharedPreferencesManager.has("token")) {
                        token = sharedPreferencesManager.get("token");
                    }
                    if (sharedPreferencesManager.has("engine_id")) {
                        engine_id = sharedPreferencesManager.get("engine_id");
                        try {

                            String aesAccount = AESOperator.encrypt(account, URLUtils.AES_SIGN);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("room_name", roomName);
                            jsonObject.put("desc", "");
                            Log.e(TAG + "添加房间数据流",aesAccount+"," + engine_id+"," + ADDROOM_METHOD+"," + jsonObject.toString()+"," + token+"," + URLUtils.MD5_SIGN);
                            String sign = MD5Utils.MD5Encode(aesAccount + engine_id + ADDROOM_METHOD + jsonObject.toString() + token + URLUtils.MD5_SIGN, "");
                            XutilsHelper xutilsHelper = new XutilsHelper(URLUtils.HOUSESET, handler);
                            xutilsHelper.add("account", aesAccount);
                            xutilsHelper.add("engine_id", engine_id);
                            xutilsHelper.add("msg", jsonObject.toString());
                            xutilsHelper.add("method", ADDROOM_METHOD);
                            xutilsHelper.add("token", token);
                            xutilsHelper.add("sign", sign);
                            xutilsHelper.sendPost(1,this);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Utils.showDialog(AddRoomActivity.this, "请添加控制主机");
                    }


                } else {
                    Utils.showDialog(AddRoomActivity.this, "请输入房间名称");
                }
                break;
            default:
                break;
        }
    }
    /**
     *      dialog  只有一个确定button
     * @param context
     * @param content    内容
     */
    private  void showDialog(Context context, String content){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.dialog_textview, null);
        TextView title = (TextView) view.findViewById(R.id.textView1);
        title.setText(content);
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (type == 1) {//首页
                    Intent intent = new Intent(AddRoomActivity.this, MainActivity.class);
                    startActivity(intent);
                } else if (type == 2) {//其他页面
                    Intent i1 = new Intent(AddRoomActivity.this, RoomListActivity.class);
                    startActivity(i1);
                }
            }
        });
        builder.show();
    }
}
