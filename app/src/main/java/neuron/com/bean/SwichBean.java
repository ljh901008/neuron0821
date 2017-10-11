package neuron.com.bean;

/**
 * Created by ljh on 2017/5/10.
 */
public class SwichBean {
    private String swichNama;

    private String swichId;

    private String swichRoom;

    private String swichKeyId;

    private String swichKeyName;

    private boolean isSelect;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getSwichId() {
        return swichId;
    }

    public void setSwichId(String swichId) {
        this.swichId = swichId;
    }

    public String getSwichKeyId() {
        return swichKeyId;
    }

    public void setSwichKeyId(String swichKeyId) {
        this.swichKeyId = swichKeyId;
    }

    public String getSwichKeyName() {
        return swichKeyName;
    }

    public void setSwichKeyName(String swichKeyName) {
        this.swichKeyName = swichKeyName;
    }

    public String getSwichNama() {
        return swichNama;
    }

    public void setSwichNama(String swichNama) {
        this.swichNama = swichNama;
    }

    public String getSwichRoom() {
        return swichRoom;
    }

    public void setSwichRoom(String swichRoom) {
        this.swichRoom = swichRoom;
    }
}
