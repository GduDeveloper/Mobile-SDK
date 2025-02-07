package com.gdu.demo.widget.live;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import androidx.annotation.IdRes;


public abstract class GduBasePop extends PopupWindow {

    public View mRootView;
    public Context mContext;
    public GduBasePop(Context context, int w , int h){
        super(context);
        mContext = context;
        setWidth(w);
        setHeight(h);
        setOutsideTouchable(true);
        setBackgroundDrawable(null);
        setFocusable(true);
        int layoutId = getLayoutId();
        mRootView = View.inflate(context, layoutId, null);
        setContentView(mRootView);
        initView();
        initListener();
    }

    protected abstract int getLayoutId();

    protected abstract void initListener();

    public abstract void initView();

    public final <T extends View> T  findViewById(@IdRes int id){
        return mRootView.findViewById(id);
    }

    public Context getContext(){
        return mContext;
    }

}
