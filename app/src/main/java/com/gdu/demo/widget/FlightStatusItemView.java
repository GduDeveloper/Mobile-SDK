package com.gdu.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gdu.demo.R;


public class FlightStatusItemView extends ConstraintLayout {

    private AppCompatTextView mTitle;
    private AppCompatTextView mContent;

    public FlightStatusItemView(@NonNull Context context) {
        this(context, null);
    }

    public FlightStatusItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlightStatusItemView);
        String title = ta.getString(R.styleable.FlightStatusItemView_ifcsv_title);
        String content = ta.getString(R.styleable.FlightStatusItemView_ifcsv_content);
        ta.recycle();

        View view = LayoutInflater.from(context).inflate(R.layout.item_flight_check_status, this);
        mTitle = view.findViewById(R.id.flight_status_title);
        mContent = view.findViewById(R.id.flight_status_content);

        mTitle.setText(title);
        mContent.setText(content);
    }

    public void setTitleTextSize(int size) {
        mTitle.setTextSize(size);
    }

    public void setTitle(int title) {
        mTitle.setText(title);
    }

    public void setTitle(String title) {
        mTitle.setText(title);

    }

    public void setContentTextSize(int size) {
        mContent.setTextSize(size);
    }

    public void setContent(int content) {
        mContent.setText(content);
    }

    public void setContent(String content) {
        mContent.setText(content);
    }

    public void setContentSelect(boolean isSelect) {
        mContent.setSelected(isSelect);
    }

    public void setContentEnable(boolean isEnable) {
        mContent.setEnabled(isEnable);
    }

    public void setContentTextColor(int color) {
        mContent.setTextColor(color);
    }

}
