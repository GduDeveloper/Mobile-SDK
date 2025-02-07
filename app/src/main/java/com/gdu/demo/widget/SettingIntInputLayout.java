package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;


/**
 * @Author: lixiqiang
 * @Date: 2022/10/25
 */
public class SettingIntInputLayout extends RelativeLayout {


    private final Context context;
    private TextView tv_name;
    public EditText editText;

    public SettingIntInputLayout(Context context) {
        this(context,null);
    }

    public SettingIntInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingIntInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public SettingIntInputLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_int_input, this);
        tv_name = findViewById(R.id.tv_name);
        editText = findViewById(R.id.et_input);
    }

    public void setName(String title) {
        tv_name.setText(title);
    }
}
