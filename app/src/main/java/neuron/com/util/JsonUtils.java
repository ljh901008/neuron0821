package neuron.com.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import neuron.com.bean.RoomItemBean;

/**
 * Created by ljh on 2016/11/14.
 */
public class JsonUtils {

    public static List<RoomItemBean> getRoomItemBean(JSONArray jsonArray){
        try {
            List<RoomItemBean> listRoomBean;
                int length = jsonArray.length();
                if (length > 0) {
                    listRoomBean = new ArrayList<RoomItemBean>();
                    RoomItemBean bean;
                    for (int i = 0; i < length; i++) {
                        bean = new RoomItemBean();
                        JSONObject bjson = jsonArray.getJSONObject(i);
                        bean.setBranchId(bjson.getString("branch_id"));
                        bean.setBranchName(bjson.getString("branch_name"));
                        JSONArray bjsonA = bjson.getJSONArray("controled_devicelist");
                        int length1 = bjsonA.length();
                        if (length1 > 0) {
                            List<RoomItemBean> list = new ArrayList<RoomItemBean>();
                            RoomItemBean roomItemBean;
                            for (int j = 0; j < length1; j++) {
                                roomItemBean = new RoomItemBean();
                                JSONObject jsonRoomItemBean = bjsonA.getJSONObject(i);
                                roomItemBean.setDeviceId(jsonRoomItemBean.getString("device_id"));
                                roomItemBean.setDeviceName(jsonRoomItemBean.getString("device_name"));
                               // roomItemBean.setRoomId(jsonRoomItemBean.getString("roomid"));
                               // roomItemBean.setRoomName(jsonRoomItemBean.getString("roomname"));
                                roomItemBean.setSerialNumber(jsonRoomItemBean.getString("device_serial"));
                                roomItemBean.setDeviceType(jsonRoomItemBean.getString("device_brand"));
                                roomItemBean.setSign(jsonRoomItemBean.getInt("sign"));
                                list.add(roomItemBean);
                            }
                            bean.setRoomItemBeanList(list);
                        }
                        listRoomBean.add(bean);
                    }
                    return listRoomBean;
                }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
