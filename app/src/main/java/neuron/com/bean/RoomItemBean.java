package neuron.com.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ljh on 2016/9/14.设备列表的实体类
 */
public class RoomItemBean implements Serializable {
    /**
     * 设备序列号
     */
    private String serialNumber;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备状态
     */
    private int deviceStatu;
    /**
     * 设备类型
     */
    private String deviceType;

    private List<RoomItemBean> roomItemBeanList;
    /**
     * 设备id
     */
    private String deviceId;
    private String deviceRoom;
    private String deviceRoomId;
    //private String roomId;
    //private String roomName;
    /**
     * 开关A,B,C键的id
     */
    private String branchId;
    /**
     * 开关ABC键的名称
     */
    private String branchName;

    private int sign;
    /**
     * 灯的按键位置
     */
    private String deviceSite;
    private String thirdAccount;

    public List<RoomItemBean> getRoomItemBeanList() {
        return roomItemBeanList;
    }

    public void setRoomItemBeanList(List<RoomItemBean> roomItemBeanList) {
        this.roomItemBeanList = roomItemBeanList;
    }

    public String getDeviceRoomId() {
        return deviceRoomId;
    }

    public void setDeviceRoomId(String deviceRoomId) {
        this.deviceRoomId = deviceRoomId;
    }

    public String getDeviceRoom() {
        return deviceRoom;
    }

    public void setDeviceRoom(String deviceRoom) {
        this.deviceRoom = deviceRoom;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String debiceId) {
        this.deviceId = debiceId;
    }


    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDeviceStatu() {
        return deviceStatu;
    }

    public void setDeviceStatu(int deviceStatu) {
        this.deviceStatu = deviceStatu;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceSite() {
        return deviceSite;
    }

    public void setDeviceSite(String deviceSite) {
        this.deviceSite = deviceSite;
    }

    public String getThirdAccount() {
        return thirdAccount;
    }

    public void setThirdAccount(String thirdAccount) {
        this.thirdAccount = thirdAccount;
    }
}
