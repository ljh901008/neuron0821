package neuron.com.database;

/**
 * Created by ljh on 2016/9/22.
 */
public class UserDaoBean {
    private String account;
    private boolean isDelete;
    public UserDaoBean(String account) {
        this.account = account;
    }

    public UserDaoBean() {
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
