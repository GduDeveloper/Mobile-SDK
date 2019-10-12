package com.gdu.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;

/**
 * Created by zhangzhilai on 2018/4/15.
 */

public class GduGallery extends Gallery {

    private OnGduGalleryListener mOnGduGalleryListener;

    public GduGallery(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnItemSelectedListener(onItemSelectedListener);
    }

    //这个设计有问题。当快速滑动的时候，只检测了第一个MotionEvent.ACTION_UP---ron
    //Gallery的这个控件是完善的，不需要自己再对触摸事件进行处理---ron
  /* @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mOnGduGalleryListener != null) {
                mOnGduGalleryListener.onItemSelected(getSelectedItemPosition());
            }
        }
        return super.onTouchEvent(event);
    }*/

    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        super.setOnItemClickListener(listener);
    }


    public void setOnGduGalleryListener(OnGduGalleryListener onGduGalleryListener){
        mOnGduGalleryListener = onGduGalleryListener;


    }

    public interface OnGduGalleryListener{
        void onItemSelected(int position);
    }

    private OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(mOnGduGalleryListener != null)
             mOnGduGalleryListener.onItemSelected(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
}
