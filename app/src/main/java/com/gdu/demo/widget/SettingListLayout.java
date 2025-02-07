package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gdu.demo.R;

import java.util.List;

/**
 * @Author: lixiqiang
 * @Date: 2022/10/25
 */
public class SettingListLayout extends RelativeLayout {

    private final Context context;
    private TextView tv_name;

    public GduSpinner optionView;

    public SettingListLayout(Context context) {
        this(context, null);
    }

    public SettingListLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingListLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingListLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_list, this);
        tv_name = findViewById(R.id.tv_name);
        optionView = findViewById(R.id.ov_out_of_control);

    }

    public void setName(String title) {
        tv_name.setText(title);
    }

    public void setArrayData(List<String> list) {
        optionView.setData(list.toArray(new String[list.size()]));
    }
}
