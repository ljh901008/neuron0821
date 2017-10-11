package neuron.com.database;


import android.content.Context;
import android.content.SharedPreferences;

public class    SharedPreferencesManager {

    private static SharedPreferences PREFERENCE;

    private static SharedPreferences.Editor editor;

    private static SharedPreferencesManager spm;

    private final static String APP_NAME = "com.neuron.sharedpreferencesmanager";

    private SharedPreferencesManager(){

    }
    public static synchronized SharedPreferencesManager getInstance(
            Context context) {
        if (spm == null) {
            spm = new SharedPreferencesManager();
            PREFERENCE = context.getSharedPreferences(APP_NAME,
                    Context.MODE_PRIVATE);
            editor = PREFERENCE.edit();
        }
        return spm;
    }
    /**
     * 保存String类型值
     * @param name
     * 			名字
     * @param value  值
     */
    public void save(String name, String value) {
        editor.putString(name, value);
        editor.commit();
    }
    /**
     * 保存int类型的值
     *
     * @param name
     *            名称
     * @param value
     *            值
     */
    public void saveInt(String name, int value) {
        editor.putInt(name, value);
        editor.commit();
    }

    /**
     * 获取数据
     *
     * @param name
     *            名称
     * @return 值
     */
    public String get(String name) {
        return PREFERENCE.getString(name, null);
    }
    /**
     * 获取
     *
     * @param name
     *            名称
     * @return 值
     */
    public int getInt(String name) {
        return PREFERENCE.getInt(name, 0);
    }
    /**
     * 判断是否包含
     *
     * @param name
     *            名称
     * @return 如果包含,返回true;否则返回false.
     */
    public boolean has(String name) {
        return PREFERENCE.contains(name);
    }
    /**
     * 根据名字删除数据
     *
     * @param name
     *            名称
     */
    public void remove(String name) {
        editor.remove(name);
        editor.commit();
    }
}
