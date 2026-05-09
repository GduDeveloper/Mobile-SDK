package com.gdu.demo.widget.psdk.widget;

import android.content.Context;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.views.DragLayout;


/**
 * @Author: lixiqiang
 * @Date: 2022/10/24
 */
public class CustomFloatWindow extends DragLayout {

    private final Context context;
    public TextView tv_text;
    public TextView tv_title;


    public CustomFloatWindow(Context context) {
        this(context, null);
    }

    public CustomFloatWindow(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomFloatWindow(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomFloatWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.layout_custom_float_window, this);
        tv_text = findViewById(R.id.tv_text);
        tv_text.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_title = findViewById(R.id.tv_title);

        ImageView close = findViewById(R.id.iv_close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                GlobalVariable.isShowCurrentData = false;
                setVisibility(GONE);
            }
        });
    }

    public void updateText(String string) {
        if (!TextUtils.isEmpty(GlobalVariable.psdkName)) {
            tv_title.setText(GlobalVariable.psdkName);
        }

//        String syn = "";
//        if (GlobalVariable.psdkSynStates == 5) {
//            syn = context.getString(R.string.str_has_syn);
//        } else {
//            syn = context.getString(R.string.str_no_syn);
//        }
//        String showStr = syn + "\r\n" + string;
        tv_text.setText(string);
    }
}
