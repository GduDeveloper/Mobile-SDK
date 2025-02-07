package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;


/**
 * @Author: lixiqiang
 * @Date: 2022/10/25
 */
public class SettingButtonLayout extends RelativeLayout {

    private final Context context;
    public TextView textView;

    public SettingButtonLayout(Context context) {
        this(context, null);
    }

    public SettingButtonLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingButtonLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingButtonLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_button, this);
        textView = findViewById(R.id.tv_text);

    }

    public void setText(String string) {
        textView.setText(string);
    }
}
