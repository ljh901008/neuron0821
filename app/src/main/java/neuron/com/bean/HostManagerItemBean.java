package neuron.com.bean;

/**
 * Created by ljh on 2017/3/7.
 */
public class HostManagerItemBean {
    private String hostManagerName,hostSerialNumber,engineId;
    private int hostState;
    private int isVisible;
    public HostManagerItemBean() {
    }

    public String getHostManagerName() {
        return hostManagerName;
    }

    public void setHostManagerName(String hostManagerName) {
        this.hostManagerName = hostManagerName;
    }

    public String getHostSerialNumber() {
        return hostSerialNumber;
    }

    public void setHostSerialNumber(String hostSerialNumber) {
        this.hostSerialNumber = hostSerialNumber;
    }

    public String getEngineId() {
        return engineId;
    }

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    public int getHostState() {
        return hostState;
    }

    public void setHostState(int hostState) {
        this.hostState = hostState;
    }

    public int getIsVisible() {
        return isVisible;
    }

    public void setIsVisible(int isVisible) {
        this.isVisible = isVisible;
    }
}
