package com.gdu.demo.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.gdu.demo.R;


public class TextImageItemLayout extends ConstraintLayout {

    public TextImageItemLayout(@NonNull Context context) {
        this(context,null);
    }

    public TextImageItemLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TextImageItemLayout);
        String text = ta.getString(R.styleable.TextImageItemLayout_tii_text);
        Drawable icon = ta.getDrawable(R.styleable.TextImageItemLayout_tii_icon);
        float iconWidth = ta.getDimension(R.styleable.TextImageItemLayout_tii_icon_width, -1);
        float iconHeight = ta.getDimension(R.styleable.TextImageItemLayout_tii_icon_height, -1);
        boolean showLine = ta.getBoolean(R.styleable.TextImageItemLayout_tii_show_line, true);
        ta.recycle();
        View view = LayoutInflater.from(context).inflate(R.layout.text_image_item, this);
        AppCompatTextView tvTiiDes = view.findViewById(R.id.tv_tii_des);
        AppCompatImageView ivTiiIcon = view.findViewById(R.id.iv_tii_icon);
        View vLineTii = view.findViewById(R.id.v_line_tii);
        tvTiiDes.setText(text);
        ivTiiIcon.setImageDrawable(icon);
        if (iconWidth > 0 && iconHeight > 0) {
            LayoutParams lp = (LayoutParams) ivTiiIcon.getLayoutParams();
            lp.width = (int) iconWidth;
            lp.height = (int) iconHeight;
            ivTiiIcon.setLayoutParams(lp);
        }
        vLineTii.setVisibility(showLine ? View.VISIBLE : View.GONE);

    }


}
