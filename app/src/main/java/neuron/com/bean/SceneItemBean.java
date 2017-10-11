package neuron.com.bean;

/**
 * Created by ljh on 2016/9/13. 场景item 实体类
 */
public class SceneItemBean {
    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 场景Id
     */
    private String sceneId;

    /**
     * 场景状态
     */
    private int sceneStatus;
    /**
     * 定时时间：特定的日期
     */
    private String sceneSpecified;
    /**
     * 重复触发的日期
     */
    private String sceneRepeatDays;
    /**
     * 重复的标志
     */
    private int sceneRepeat;

    /**
     * 开启时间
     */
    private String openTime;

    /**
     * 是否是默认场景
     */
    private int isDefault;
    //场景图标
    private int sceneImg;

    private boolean isSelect;

    public SceneItemBean(int sceneImg, boolean isSelect) {
        this.sceneImg = sceneImg;
        this.isSelect = isSelect;
    }

    public SceneItemBean() {
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getSceneImg() {
        return sceneImg;
    }

    public void setSceneImg(int sceneImg) {
        this.sceneImg = sceneImg;
    }

    public String getSceneRepeatDays() {
        return sceneRepeatDays;
    }

    public void setSceneRepeatDays(String sceneRepeatDays) {
        this.sceneRepeatDays = sceneRepeatDays;
    }

    public String getOpenTime() {
        return openTime;
    }

    public void setOpenTime(String openTime) {
        this.openTime = openTime;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public int getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    public String getSceneId() {
        return sceneId;
    }

    public void setSceneId(String sceneId) {
        this.sceneId = sceneId;
    }

    public int getSceneStatus() {
        return sceneStatus;
    }

    public void setSceneStatus(int sceneStatus) {
        this.sceneStatus = sceneStatus;
    }

    public String getSceneSpecified() {
        return sceneSpecified;
    }

    public void setSceneSpecified(String sceneSpecified) {
        this.sceneSpecified = sceneSpecified;
    }


    public int getSceneRepeat() {
        return sceneRepeat;
    }

    public void setSceneRepeat(int sceneRepeat) {
        this.sceneRepeat = sceneRepeat;
    }
}
