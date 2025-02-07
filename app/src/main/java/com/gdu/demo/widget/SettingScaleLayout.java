package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.demo.R;


/**
 * @Author: lixiqiang
 * @Date: 2022/10/25
 */
public class SettingScaleLayout extends LinearLayout {


    private final Context context;

    public TextView tv_name;
    public SeekBar seekBar;
    public TextView tv_progress;

    public SettingScaleLayout(Context context) {
        this(context, null);
    }

    public SettingScaleLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingScaleLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SettingScaleLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_setting_scale, this);
        tv_name = findViewById(R.id.tv_name);
        seekBar = findViewById(R.id.seekbar);
        tv_progress = findViewById(R.id.tv_progress);

    }

    public void setName(String title) {
        tv_name.setText(title);
    }

    public void setProgress(int progress) {
        seekBar.setProgress(progress);
        tv_progress.setText(progress + "");
    }
}
