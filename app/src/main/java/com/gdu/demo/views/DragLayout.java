package com.gdu.demo.views;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * @Author: lixiqiang
 * @Date: 2022/11/9
 */
public class DragLayout extends RelativeLayout {

    private Context mContext;
    private int mWidth;
    private int mHeight;
    private float mDownX;
    private float mDownY;
    private int mScreenWidth;
    private int mScreenHeight;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;
        init();
    }

    private void init() {
        mScreenWidth = getScreenWidth(mContext);
        mScreenHeight = getScreenHeight(mContext);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

    }

    public int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public int getScreenHeight(Context context) {
        WindowManager manager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float distanceX = event.getX() - mDownX;
                final float distanceY = event.getY() - mDownY;
                int l, r, t, b;
                //当水平或者垂直滑动距离大于10,才算是拖动事件
                if (Math.abs(distanceX) > 10 || Math.abs(distanceY) > 10) {
                    l = (int) (getLeft() + distanceX);
                    r = l + mWidth;
                    t = (int) (getTop() + distanceY);
                    b = t + mHeight;
                    //边界判断,不让布局滑出界面
                    if (l < 0) {
                        l = 0;
                        r = l + mWidth;
                    } else if (r > mScreenWidth) {
                        r = mScreenWidth;
                        l = r - mWidth;
                    }
                    if (t < 0) {
                        t = 0;
                        b = t + mHeight;
                    } else if (b > mScreenHeight) {
                        b = mScreenHeight;
                        t = b - mHeight;
                    }
                    setLayout(this, l, t, r, b);
                }
                break;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                break;
            default:
                break;
        }
        return true;
    }

    private  void setLayout(View view, int left, int top, int right, int bottom) {
        LayoutParams params = new LayoutParams(right - left, bottom - top);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        ViewParent parent = view.getParent();
        if (parent instanceof RelativeLayout) {
            RelativeLayout parentView = (RelativeLayout) parent;
            int marginRight = parentView.getWidth() - right;
            int marginBottom = parentView.getHeight() - bottom;
            params.setMargins(left, top, marginRight, marginBottom);
            view.setLayoutParams(params);
        }
    }
}
