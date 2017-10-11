package neuron.com.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
/**
 * viewpager 适配器
 * @author ljh
 *
 */
public class ViewPagerAdapter extends FragmentPagerAdapter{
    ArrayList<Fragment> afragment;
    public ViewPagerAdapter(FragmentManager fm,ArrayList<Fragment> afragment) {
        super(fm);
        this.afragment=afragment;
    }

    @Override
    public Fragment getItem(int arg0) {
        // TODO Auto-generated method stub
        return afragment.get(arg0);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return afragment.size();
    }

}
