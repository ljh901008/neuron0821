package neuron.com.util;

/**
 * Created by ljh on 2016/9/20.
 */
public class URLUtils {
    //楼上外网IP
    private static final String NEI_URL = "http://117.149.14.76:18080";
    //楼下外网IP
    private static final String WAI_URL= "http://117.149.14.76:18081";
    //阿里云IP
    private static final String ALI_URL= "http://ehooworld.com:8080";

    public static final String URL_HEAD = ALI_URL;

    public static final String MD5_SIGN = "b083df8ce6d2ad72";
    /**
     * aec 密钥
     */
    public static final String AES_SIGN = "ed16b1f8a9e648d4";
    /*
     * 账户类 URL
     */
    public static final String USERNAME_URL = URL_HEAD + "/Neuron/AccountAction.do";
    /**
     * 场景
     */
    public static final String GETHOMELIST_URL = URL_HEAD + "/Neuron/SceneAction.do";
    /**
     * 房间操作
     */
    public static final String HOUSESET = URL_HEAD + "/Neuron/HouseAction.do";

    /**
     * 获取设备列表
     */
    public static final String GETDEVICELIST_URL = URL_HEAD + "/Neuron/DeviceAction.do";
    /**
     * 场景类
     */
    public static final String SCENE_URL = URL_HEAD + "/Neuron/OrderAction.do";
    /**
     * 萤石获取时间url
     */
    public static final String EZTIME_URL= "https://open.ys7.com/api/time/get";
    /**
     * 萤石url
     */
    public static final String EZ_URL= "https://open.ys7.com/api/method";
    /**
     * 分享
     */
    public static final String SHAREHOSMANAGER = URL_HEAD + "/Neuron/ShareAction.do";
    /**
     * 短信，分享
     */
    public static final String Other = URL_HEAD + "/Neuron/OtherAction.do";
    /**
     * 电视空调操作接口
     */
    public static final String OPERATION = URL_HEAD + "/Neuron/InfradedAction.do";

    //音响服务器地址
    public static final String BASE_URL = "http://121.40.227.8:8088/";
    //音响AppKey
    public static final String AppKey = "0E803F8F6F0A4FC89EDD0D62C0B0A9C1";
    //音响SecretKey
    public static final String SecretKey = "4484A18D521140C3B211B9FABB687D7A";

    public static final String needUpdate = "1";
    public static final String noUpdate = "2";
}
