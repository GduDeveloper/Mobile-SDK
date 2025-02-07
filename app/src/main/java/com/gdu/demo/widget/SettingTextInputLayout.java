package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;
import com.gdu.util.TextUtil;

/**
 * @Author: lixiqiang
 * @Date: 2022/10/25
 */
public class SettingTextInputLayout extends RelativeLayout {


    private final Context context;

    private TextView tv_name;

    public TextView tv_send;
    public EditText editText;


    public SettingTextInputLayout(Context context) {
        this(context,null);
    }

    public SettingTextInputLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SettingTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_text_input, this);
        tv_name = findViewById(R.id.tv_name);
        tv_send = findViewById(R.id.tv_send);
        editText = findViewById(R.id.et_input);
    }

    public void setName(String title) {

        if (!TextUtil.isEmptyString(title)) {
            tv_name.setText(title);
        }
    }
}
