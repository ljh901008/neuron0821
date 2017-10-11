package neuron.com.util;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;




/**
 * Created by ljh on 2016/9/21.
 */
public class AESOperator {


    /*
   * 加密用的Key 可以用26个字母和数字组成 此处使用AES-128-CBC加密模式，key需要为16位。
   */
    private static String sKey = "ed16b1f8a9e648d4";
    private static String ivParameter = "ed16b1f8a9e648d4";
    private static AESOperator instance = null;

    private AESOperator() {

    }

    public static AESOperator getInstance() {
        if (instance == null)
            instance = new AESOperator();
        return instance;
    }

    /**
     *  aes加密方法
     * @param sSrc 需要加密的数据
     * @param password  密钥
     * @return　aes加密后的数据
     * @throws Exception
     */
    public static String encrypt(String sSrc, String password) throws Exception {
        byte[] raw = password.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());// 使用CBC模式，需要一个向量iv，可增加加密算法的强度
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        byte[] encrypted = cipher.doFinal(sSrc.getBytes("utf-8"));
        //return new BASE64Encoder().encode(encrypted);// 此处使用BASE64做转码。
        return Base64.encodeToString(encrypted, Base64.DEFAULT);
    }

    // 解密
    public static String decrypt(String sSrc,String password) throws Exception {
        byte[] raw = sKey.getBytes();
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec iv = new IvParameterSpec(ivParameter.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
       // byte[] encrypted1 = new BASE64Decoder().decodeBuffer(sSrc);// 先用base64解密
        byte[] encrypted1 = Base64.decode(sSrc, Base64.DEFAULT);
        byte[] original = cipher.doFinal(encrypted1);
        String originalString = new String(original, "utf-8");
        return originalString;
    }


    public static String encodeBytes(byte[] bytes) {
        StringBuffer strBuf = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
            strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
        }

        return strBuf.toString();
    }

}
