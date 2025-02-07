package com.gdu.demo.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gdu.demo.R;


/**
 * 感知高级设置
 */
public class PerceivingHighSettingsView extends LinearLayout {

    private final Context mContext;

    /**
     * 设置相关监听
     */
    private OnPerceiveHighListener mOnPerceiveListener;

    /**
     * 视觉定位开关
     */
    private ImageView mVisualLocationSwitchImageView;

    /**
     * 精准降落开关
     */
    private ImageView mPreciseLandingSwitchImageView;
    private TextView mVisualLocationHintTextView;

    public PerceivingHighSettingsView(Context context) {
        this(context, null);
    }

    public PerceivingHighSettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PerceivingHighSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initListener();
    }

    private void initView() {
        LayoutInflater.from(mContext).inflate(R.layout.view_perceiving_high_setting, this);
        mVisualLocationSwitchImageView = findViewById(R.id.start_visual_location_switch);
        mPreciseLandingSwitchImageView = findViewById(R.id.start_precise_landing_switch);
        mVisualLocationHintTextView = findViewById(R.id.tv_visual_location_hint);
//        mVisualLocationHintTextView.setText(CommonUtils.getVisionLocationHintString(mContext));
    }

    private void initListener() {
        mVisualLocationSwitchImageView.setOnClickListener(v -> {
            boolean isOpen = mVisualLocationSwitchImageView.isSelected();
            mOnPerceiveListener.onVisualLocationSwitch(isOpen);
        });
        mPreciseLandingSwitchImageView.setOnClickListener(v -> {
            boolean isOpen = mPreciseLandingSwitchImageView.isSelected();
            mOnPerceiveListener.onPreciseLandingSwitch(isOpen);
        });
    }

    public interface OnPerceiveHighListener{
        void onVisualLocationSwitch(boolean isOpen);
        void onPreciseLandingSwitch(boolean isOpen);
    }

    public void setOnPerceiveListener(OnPerceiveHighListener onPerceiveHighListener){
        mOnPerceiveListener = onPerceiveHighListener;
    }
}
