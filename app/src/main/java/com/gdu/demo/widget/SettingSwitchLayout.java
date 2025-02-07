package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.demo.R;


/**
 * @Author: lixiqiang
 * @Date: 2022/10/25
 */
public class SettingSwitchLayout extends RelativeLayout {


    private final Context context;

    private TextView tv_name;
    public ImageView iv_switch;

    public SettingSwitchLayout(Context context) {
        this(context, null);
    }

    public SettingSwitchLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingSwitchLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingSwitchLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {

        LayoutInflater.from(context).inflate(R.layout.layout_setting_switch, this);
        tv_name = findViewById(R.id.tv_name);
        iv_switch = findViewById(R.id.iv_switch);
    }

    public void setName(String title) {
        tv_name.setText(title);
    }
}
