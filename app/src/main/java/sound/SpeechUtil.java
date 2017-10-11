package sound;

import neuron.com.app.OgeApplication;
import neuron.com.comneuron.R;

/**
 * Created by xuhuanli on 2017/8/17.
 */

public class SpeechUtil {
    /**
     * 是否包含关键字
     *
     * @return
     */
    public static boolean isContainsKeyWords(String speechText, String keyWords) {
        if (speechText.contains(keyWords)) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param speechText 语音文本
     * @param ResId 资源id
     * @return
     */
    public static boolean isContainsKeyWords(String speechText, int ResId) {
        if (speechText.contains(OgeApplication.getContext().getString(ResId))) {
            return true;
        }
        return false;
    }

    /**
     * 获取设备的序号
     *
     * @param speechText
     * @param keyWords
     * @return
     */
    public static int getDeviceNum(String speechText, String keyWords) {
        if (isContainsKeyWords(speechText, keyWords)) {
            if (keyWords.equals(OgeApplication.getContext().getString(R.string.keyword_light))) {
                return 1;
            }
            if (keyWords.equals(OgeApplication.getContext().getString(R.string.chuanglian))) {
                return 2;
            }
            if (keyWords.equals(OgeApplication.getContext().getString(R.string.chazuo))) {
                return 3;
            }
            if (keyWords.equals(OgeApplication.getContext().getString(R.string.hongwai))) {
                return 4;
            }
            if (keyWords.equals(OgeApplication.getContext().getString(R.string.dianshi))) {
                return 5;
            }
            if (keyWords.equals(OgeApplication.getContext().getString(R.string.kongtiao))) {
                return 6;
            }

        }
        return 0;
    }

    /**
     * 获取设备的序号
     * 仅提供单句命令词打开单个设备
     *
     * @param speechText
     * @return
     */
    public static int getDeviceNum(String speechText) {
        if(speechText.contains(OgeApplication.getContext().getString(R.string.keyword_light))){
            return 1;
        }
        if(speechText.contains(OgeApplication.getContext().getString(R.string.chuanglian))){
            return 2;
        }
        if(speechText.contains(OgeApplication.getContext().getString(R.string.chazuo))){
            return 3;
        }
        if(speechText.contains(OgeApplication.getContext().getString(R.string.hongwai))){
            return 4;
        }
        if(speechText.contains(OgeApplication.getContext().getString(R.string.dianshi))){
            return 5;
        }
        if(speechText.contains(OgeApplication.getContext().getString(R.string.kongtiao))){
            return 6;
        }
        return 0;
    }
    /**
     * 获取设备的序号
     * 仅提供单句命令词打开单个设备
     *
     * @param deviceType  设备类型
     * @return
     */
    public static int getDeviceNumByDeviceType(String deviceType) {
        if (deviceType.equals("33001")){return 1;}
        if (deviceType.equals("33004")){return 2;}
        if (deviceType.equals("33009")){return 3;}
        if (deviceType.equals("33003")){return 4;}
        if (deviceType.equals("33006")){return 5;}
        if (deviceType.equals("33007")){return 6;}
        return 0;
    }


}
