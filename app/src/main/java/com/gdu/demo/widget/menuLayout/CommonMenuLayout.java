package com.gdu.demo.widget.menuLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gdu.demo.R;


public class CommonMenuLayout extends ConstraintLayout {

    public CommonMenuLayout(@NonNull Context context) {
        super(context);
    }

    public CommonMenuLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        View view = LayoutInflater.from(context).inflate(R.layout.common_menu_layout, this);
        TextView tvLabel = view.findViewById(R.id.tv_menu_layout_label);
        ImageView ivMore = view.findViewById(R.id.iv_menu_layout_more);
        View line = view.findViewById(R.id.v_line_menu_layout);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CommonMenuLayout);
        String label = typedArray.getString(R.styleable.CommonMenuLayout_cml_label);
        boolean showLine = typedArray.getBoolean(R.styleable.CommonMenuLayout_cml_show_line, true);
        typedArray.recycle();
        tvLabel.setText(label);
        line.setVisibility(showLine?VISIBLE:GONE);
    }

}
