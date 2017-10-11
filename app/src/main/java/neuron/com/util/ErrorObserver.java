package neuron.com.util;

import cn.nbhope.smarthome.smartlib.net.observer.Observer;
import cn.nbhope.smarthome.smartlib.net.observer.Type;

/**
 * Created by ljh on 2017/9/8.
 */

public class ErrorObserver implements Observer {
    @Override
    public void notify(Type type) {
        switch (type) {
            case NO_OPERATE: //如果二十分钟不进行操作，就会无权操作，此时，可以重新进入登录界面重新登录(或者后台子线程进行登录)
                /*Toast.makeText(App.getInstance(), R.string.login_invalid, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(App.getInstance(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getInstance().startActivity(intent);*/

                break;
            default:
        }
    }
}
