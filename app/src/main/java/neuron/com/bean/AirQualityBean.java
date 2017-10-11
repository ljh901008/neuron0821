package neuron.com.bean;

/**
 * Created by ljh on 2017/4/12.空气质量检测仪选择场景页面实体类
 */
public class AirQualityBean {
    /**
     * 场景名称
     */
    private String sceneName;
    /**
     * 场景id
     */
    private String sceneId;
    /**
     * 是否被选中
     */
    private boolean isSelect;

    private String sceneType;
    private String sceneImg;

    private String deviceType;

    public AirQualityBean() {
    }

    public String getSceneImg() {
        return sceneImg;
    }

    public void setSceneImg(String sceneImg) {
        this.sceneImg = sceneImg;
    }

    public String getSceneType() {
        return sceneType;
    }

    public void setSceneType(String sceneType) {
        this.sceneType = sceneType;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
