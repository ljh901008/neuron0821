package neuron.com.database;

import org.xutils.DbManager;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.List;

/**
 * Created by ljh on 2016/9/22.
 */
public class SQLiteUtils {
    private static DbManager.DaoConfig daoConfig;
    /**
     *      创建数据库的方法（xutils3.0）不设置setDbDir 数据库的路径默认存在/data/data/database/xxx.db 下
     * @return : DbManager.DaoConfig对象
     */
    public static synchronized DbManager.DaoConfig getDaoConfig(){
        if (daoConfig == null) {
            daoConfig = new DbManager.DaoConfig()
                    .setDbName("account.db")//数据库名称
                    .setDbVersion(1)//数据库版本
                    .setAllowTransaction(true)//是否开启事物
                    .setDbUpgradeListener(new DbManager.DbUpgradeListener() {//版本升级的监听方法
                        @Override
                        public void onUpgrade(DbManager dbManager, int i, int i1) {

                        }
                    });
        }
        return daoConfig;
    }

    /**
     *  插入数据
     * @param account 内容字段
     */
    public static void insert(String account){
        try {
            DbManager.DaoConfig daoConfig = SQLiteUtils.getDaoConfig();
        DbManager dbManager = x.getDb(daoConfig);
        UserDaoBean user = new UserDaoBean();
        user.setAccount(account);
            dbManager.saveOrUpdate(user);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    /**
     *   查询所有数据 帐号
     * @return 所有帐号的list集合
     */
    public static List<UserDaoBean> query() {
        List<UserDaoBean> list = null;
        try {
            DbManager.DaoConfig daoConfig = SQLiteUtils.getDaoConfig();
            DbManager dbManager = x.getDb(daoConfig);
             list = dbManager.findAll(UserDaoBean.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        return list;
    }
}
