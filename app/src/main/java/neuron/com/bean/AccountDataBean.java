package neuron.com.bean;

/**
 * Created by ljh on 2017/3/23.
 */
public class AccountDataBean {
    /**
     * 头像路径
     */
    private String photoPath;
    /**
     * 帐号昵称
     */
    private String userName;
    /**
     * 帐号
     */
    private String accoungNumber;
    /**
     * 是否在编辑
     */
    private boolean isEdit;

    /**
     * 是否选中
     */
    private boolean isSelect;
    /**
     * 右边箭头是否显示
     */
    private boolean isShow;
    private boolean setText;
    public String getPhotoPath() {
        return photoPath;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccoungNumber() {
        return accoungNumber;
    }

    public void setAccoungNumber(String accoungNumber) {
        this.accoungNumber = accoungNumber;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public boolean isSetText() {
        return setText;
    }

    public void setSetText(boolean setText) {
        this.setText = setText;
    }
}
