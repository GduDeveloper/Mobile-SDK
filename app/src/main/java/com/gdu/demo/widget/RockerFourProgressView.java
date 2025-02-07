package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;


/**
 * @Author fuchi
 * @Date 2023/4/21-14:06
 * @Package com.gdu.debugger.view
 * @Description 十字架型，四个progress view
 */
public class RockerFourProgressView extends RelativeLayout {
    private Context     mContext;

    private TextView    top_progress_tv;
    private TextView    bottom_progress_tv;
    private TextView    left_progress_tv;
    private TextView    right_progress_tv;

    private VerticalProgressBar top_progress_view;
    private VerticalProgressBar bottom_progress_view;
    private ProgressBar left_progress_view;
    private ProgressBar right_progress_view;

    public RockerFourProgressView(Context context) {
        this(context, null);
    }

    public RockerFourProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RockerFourProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_rocker_vertical, this);
        top_progress_tv = findViewById(R.id.top_progress_tv);
        bottom_progress_tv = findViewById(R.id.bottom_progress_tv);
        left_progress_tv = findViewById(R.id.left_progress_tv);
        right_progress_tv = findViewById(R.id.right_progress_tv);
        top_progress_view = findViewById(R.id.top_progress_view);
        bottom_progress_view = findViewById(R.id.bottom_progress_view);
        left_progress_view = findViewById(R.id.left_progress_view);
        right_progress_view = findViewById(R.id.right_progress_view);
    }

    /**
     * 设置最大值
     * @param max
     */
    public void setMax(int max){
        top_progress_view.setMax(max);
        bottom_progress_view.setMax(max);
        left_progress_view.setMax(max);
        right_progress_view.setMax(max);
    }

    /**
     * 设置当前左值
     * @param value
     */
    public void setLeftValue(int value, int progress){
        left_progress_view.setProgress(progress);
        right_progress_view.setProgress(0);
        left_progress_tv.setText(progress + "%");
        right_progress_tv.setText("0%");
    }

    /**
     * 设置当前右值
     * @param value
     */
    public void setRightValue(int value, int progress){
        right_progress_view.setProgress(progress);
        left_progress_view.setProgress(0);
        right_progress_tv.setText(progress + "%");
        left_progress_tv.setText("0%");
    }

    /**
     * 设置当前top值
     * @param value
     */
    public void setTopValue(int value, int progress){
        top_progress_view.setProgress(progress);
        bottom_progress_view.setProgress(0);
        top_progress_tv.setText(progress + "%");
        bottom_progress_tv.setText("0%");
    }

    /**
     * 设置当前bottom值
     * @param value
     */
    public void setBottomValue(int value, int progress){
        bottom_progress_view.setProgress(progress);
        bottom_progress_tv.setText(progress + "%");
        top_progress_view.setProgress(0);
        top_progress_tv.setText("0%");
    }

    public void resetAllView(){
        left_progress_view.setProgress(0);
        right_progress_view.setProgress(0);
        top_progress_view.setProgress(0);
        bottom_progress_view.setProgress(0);
    }
}
