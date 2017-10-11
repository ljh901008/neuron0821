package neuron.com.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
/**
 * 自定义的viewpager 实现禁止Fragment界面滑动的功能
 * @author oge01
 *
 */
public class MyViewpage extends ViewPager{
    private boolean mDisableSroll = true;
    public MyViewpage(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public MyViewpage(Context context, AttributeSet attrs){
        super(context, attrs);
    }
    public void setDisableScroll(boolean bDisable)
    {
        mDisableSroll = bDisable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mDisableSroll)
        {
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if(mDisableSroll)
        {
            return false;
        }
        return super.onTouchEvent(ev);
    }
}
