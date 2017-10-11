package neuron.com.bean;

/**
 * Created by ljh on 2016/9/29. 首页popuWindow 所需实体类
 */
public class SY_PopuWBean {
    /**
     * 房间名称
     */
    private String homeName;
    /**
     * 是否选中此房间
     */
    private int isSelect;
    /**
     * 房间ID
     */
    private String homeId;

    /**
     * 住宅
     */
    private String houseId;

    public SY_PopuWBean() {
    }

    public SY_PopuWBean(String homeName, int isSelect, String homeId, String houseId) {
        this.homeName = homeName;
        this.isSelect = isSelect;
        this.homeId = homeId;
        this.houseId = houseId;
    }

    public String getHomeName() {
        return homeName;
    }

    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    public int getIsSelect() {
        return isSelect;
    }

    public void setIsSelect(int isSelect) {
        this.isSelect = isSelect;
    }

    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(String homeId) {
        this.homeId = homeId;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }
}
