package com.gdu.demo.widget;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.FormatConfig;
import com.gdu.util.ViewUtils;
import com.gdu.util.logger.MyLogUtils;

import java.text.DecimalFormat;

/**
 * 单方向感知避障设置
 */
public class PerceivingSettingsView extends RelativeLayout {

    /** 设置相关监听 */
    private OnPerceiveHorizontalListener mOnPerceiveHorizontalListener;
    private OnPerceiveTopListener mOnPerceiveTopListener;
    private OnPerceiveBottomListener mOnPerceiveBottomListener;

    /** 感知避障提示信息 */
    private TextView mHorizontalTipTv;
    private TextView mTopTipTv;
    private TextView mBottomTipTv;

    /** 感知避障类型图片 */
    private ImageView mPerceivingTypeImageView;
    private ImageView mPerceivingTypeImageViewTop;
    private ImageView mPerceivingTypeImageViewBottom;

    /** 避障类型名称 */
    private TextView mObstacleNameTextView;

    /** 感知避障开关 */
    private ImageView mObsHorSwitch;

    /** 刹停距离提示 */
    private TextView mBrakeDistanceHintTextView;
    private TextView mBrakeDistanceHintTextViewTop;
    private TextView mBrakeDistanceHintTextViewBottom;

    /** 刹停距离seekbar */
    private SeekBar mBrakeDistanceSeekBar;
    private SeekBar mBrakeDistanceSeekBarTop;
    private SeekBar mBrakeDistanceSeekBarBottom;

    /** 刹停距离 */
    private TextView mBrakeDistanceValueTextView;
    private TextView mBrakeDistanceValueTextViewTop;
    private TextView mBrakeDistanceValueTextViewBottom;

    /** 告警距离提示 */
    private TextView mWarnDistanceHintTextView;
    private TextView mWarnDistanceHintTextViewTop;
    private TextView mWarnDistanceHintTextViewBottom;

    /** 告警距离seekbar */
    private SeekBar mWarnDistanceSeekBar;
    private SeekBar mWarnDistanceSeekBarTop;
    private SeekBar mWarnDistanceSeekBarBottom;

    /** 告警距离 */
    private TextView mWarnDistanceValueTextView;
    private TextView mWarnDistanceValueTextViewTop;
    private TextView mWarnDistanceValueTextViewBottom;

    /** 水平避障 */
    private ConstraintLayout mHorizontalLayout;

    /** 上视避障 */
    private ConstraintLayout mTopLayout;
    /** 上视避障类型名称 */
    private TextView mTopObsName;
    /** 上视感知避障开关 */
    private ImageView mObsTopSwitch;

    /** 下视避障 */
    private ConstraintLayout mBottomLayout;
    /** 下视避障类型名称 */
    private TextView mBottomObsName;
    /** 下视感知避障开关 */
    private ImageView mBottomSwitch;

    // 替换下视避障
    private ConstraintLayout layout_downObstacle;

    /** 水平避障开关 */
    private boolean isHorSwitchSelected;
    /** 上视避障开关 */
    private boolean isTopSwitchSelected;
    /** 下视避障开关 */
    private boolean isBottomSwitchSelected;

    /** 水平刹停距离 */
    private int horSeekbarValue;
    /** 水平告警距离 */
    private int horizontalAlarmValue;
    /** 上视刹停距离 */
    private int topSeekbarValue;
    /** 上视告警距离 */
    private int topAlarmValue;
    /** 下视刹停距离 */
    private int bottomSeekbarValue;
    /** 下视告警距离 */
    private int bottomAlarmValue;
    private ImageView mIvLandingProtect;

    /** 辅助降落提示文案 */
    private TextView tvTip;
    /** 降落保护提示文案 */
    private TextView tvLandingProtectHint;

    public PerceivingSettingsView(Context context) {
        this(context, null);
    }

    public PerceivingSettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PerceivingSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initData();
        initListener();
    }

    private void initData() {
        String unit = "N/A" + UnitChnageUtils.getUnit();
        mBrakeDistanceValueTextView.setText(unit);
        mWarnDistanceValueTextView.setText(unit);
        mBrakeDistanceValueTextViewTop.setText( unit);
        mWarnDistanceValueTextViewTop.setText( unit);
        mBrakeDistanceValueTextViewBottom.setText(unit);
        mWarnDistanceValueTextViewBottom.setText(unit);

//        GduApplication.getSingleApp().gduCommunication.getLandingProtectState((code, bean) -> {
//            MyLogUtils.i("getLandingProtectState() code = " + code );
//            boolean isEmptyData = code != GduConfig.OK || bean == null || bean.frameContent == null
//                    || bean.frameContent.length < 2;
//            if (isEmptyData) {
//                return;
//            }
//            byte state = bean.frameContent[2];
//            post(() -> mIvLandingProtect.setSelected(state == 1));
//        });

    }

    /** 水平避障设置监听*/
    public void setOnPerceiveHorizontalListener(OnPerceiveHorizontalListener onPerceiveHorizontalListener){
        mOnPerceiveHorizontalListener = onPerceiveHorizontalListener;
    }

    /** 上视避障设置监听*/
    public void setOnPerceiveTopListener(OnPerceiveTopListener onPerceiveTopListener) {
        mOnPerceiveTopListener = onPerceiveTopListener;
    }

    /** 下视避障设置监听*/
    public void setOnPerceiveBottomListener(OnPerceiveBottomListener onPerceiveBottomListener) {
        mOnPerceiveBottomListener = onPerceiveBottomListener;
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_perceiving_setting, this);

        mHorizontalLayout = findViewById(R.id.incHorizontalObstacle);
        mHorizontalTipTv = mHorizontalLayout.findViewById(R.id.hintPerceivingTextview);
        mPerceivingTypeImageView = mHorizontalLayout.findViewById(R.id.ivPerceivingIcon);
        mObstacleNameTextView = mHorizontalLayout.findViewById(R.id.tvObstacleOrientationTip);
        mObsHorSwitch = mHorizontalLayout.findViewById(R.id.ivOrientationObstacleSwitch);
        mBrakeDistanceHintTextView = mHorizontalLayout.findViewById(R.id.tvObstacleBrakeDistanceTip);
        mBrakeDistanceSeekBar = mHorizontalLayout.findViewById(R.id.sbObstacleBrakeDistance);
        mBrakeDistanceValueTextView = mHorizontalLayout.findViewById(R.id.tvObstacleBrakeDistanceContent);
        mWarnDistanceHintTextView = mHorizontalLayout.findViewById(R.id.tvObstacleAlarmDistanceTip);
        mWarnDistanceSeekBar = mHorizontalLayout.findViewById(R.id.sbObstacleAlarmDistance);
        mWarnDistanceValueTextView = mHorizontalLayout.findViewById(R.id.tvObstacleAlarmDistanceContent);

        mHorizontalTipTv.setText(getContext().getResources().getString(R.string.horizontally_disposed_hint));
//        mPerceivingTypeImageView.setImageResource(CameraUtil.getVisionSettingHorizontalIcon());
        mBrakeDistanceSeekBar.setMax(1000);
        mWarnDistanceSeekBar.setMax(4000);
        //此方法只能在Android 26 版本以上使用，已在setOnSeekBarChangeListener中做了低版本适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBrakeDistanceSeekBar.setMin(100);
            mWarnDistanceSeekBar.setMin(110);
        }

        mTopLayout = findViewById(R.id.incUpObstacle);
        mTopTipTv = mTopLayout.findViewById(R.id.hintPerceivingTextview);
        mPerceivingTypeImageViewTop = mTopLayout.findViewById(R.id.ivPerceivingIcon);
        mTopObsName = mTopLayout.findViewById(R.id.tvObstacleOrientationTip);
        mObsTopSwitch = mTopLayout.findViewById(R.id.ivOrientationObstacleSwitch);
        mBrakeDistanceHintTextViewTop = mTopLayout.findViewById(R.id.tvObstacleBrakeDistanceTip);
        mBrakeDistanceSeekBarTop = mTopLayout.findViewById(R.id.sbObstacleBrakeDistance);
        mBrakeDistanceValueTextViewTop = mTopLayout.findViewById(R.id.tvObstacleBrakeDistanceContent);
        mWarnDistanceHintTextViewTop = mTopLayout.findViewById(R.id.tvObstacleAlarmDistanceTip);
        mWarnDistanceSeekBarTop = mTopLayout.findViewById(R.id.sbObstacleAlarmDistance);
        mWarnDistanceValueTextViewTop = mTopLayout.findViewById(R.id.tvObstacleAlarmDistanceContent);

        mTopTipTv.setText(getContext().getResources().getString(R.string.above_disposed_hint));
//        mPerceivingTypeImageViewTop.setImageResource(CameraUtil.getVisionSettingTopIcon());
        mBrakeDistanceSeekBarTop.setMax(1000);
        mWarnDistanceSeekBarTop.setMax(2000);
        //此方法只能在Android 26 版本以上使用，已在setOnSeekBarChangeListener中做了低版本适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBrakeDistanceSeekBarTop.setMin(100);
            mWarnDistanceSeekBarTop.setMin(110);
        }

        layout_downObstacle = findViewById(R.id.layoutDownObstacle);
        mIvLandingProtect = findViewById(R.id.ivLandingProtect);

        mBottomLayout = findViewById(R.id.incDownObstacle);
        mBottomTipTv = mBottomLayout.findViewById(R.id.hintPerceivingTextview);
        mPerceivingTypeImageViewBottom = mBottomLayout.findViewById(R.id.ivPerceivingIcon);
        mBottomObsName = mBottomLayout.findViewById(R.id.tvObstacleOrientationTip);
        mBottomSwitch = mBottomLayout.findViewById(R.id.ivOrientationObstacleSwitch);
        mBrakeDistanceHintTextViewBottom = mBottomLayout.findViewById(R.id.tvObstacleBrakeDistanceTip);
        mBrakeDistanceSeekBarBottom = mBottomLayout.findViewById(R.id.sbObstacleBrakeDistance);
        mBrakeDistanceValueTextViewBottom = mBottomLayout.findViewById(R.id.tvObstacleBrakeDistanceContent);
        mWarnDistanceHintTextViewBottom = mBottomLayout.findViewById(R.id.tvObstacleAlarmDistanceTip);
        mWarnDistanceSeekBarBottom = mBottomLayout.findViewById(R.id.sbObstacleAlarmDistance);
        mWarnDistanceValueTextViewBottom = mBottomLayout.findViewById(R.id.tvObstacleAlarmDistanceContent);
        tvTip = findViewById(R.id.tv_tip);
        tvTip.setText(getResources().getString(R.string.string_bottom_obstacle, UnitChnageUtils.getDecimalFormatUnit(3f),UnitChnageUtils.getDecimalFormatSpeedUnit(1.5f)));
        tvLandingProtectHint = findViewById(R.id.tvLandingProtectHint);
        tvLandingProtectHint.setText(getResources().getString(R.string.precise_landing_protect_hint, UnitChnageUtils.getDecimalFormatUnit(1f)));

        mBottomTipTv.setText(getContext().getResources().getString(R.string.below_disposed_hint));
        //因下视避障界面隐藏，这里暂不处理图片资源
//        mPerceivingTypeImageViewBottom.setImageResource(CameraUtil.getVisionSettingBottomIcon());
        if (DroneUtil.isSmallFlight()) {
            mBottomSwitch.setVisibility(INVISIBLE);
            mBrakeDistanceSeekBarBottom.setMax(200);
            mWarnDistanceSeekBarBottom.setMax(1000);
        } else {
            mBrakeDistanceSeekBarBottom.setMax(300);
            mWarnDistanceSeekBarBottom.setMax(2000);
        }
        //此方法只能在Android 26 版本以上使用，已在setOnSeekBarChangeListener中做了低版本适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBrakeDistanceSeekBarBottom.setMin(50);
            mWarnDistanceSeekBarBottom.setMin(60);
        }

        mBrakeDistanceSeekBar.setEnabled(false);
        mWarnDistanceSeekBar.setEnabled(false);
        mBrakeDistanceSeekBarTop.setEnabled(false);
        mWarnDistanceSeekBarTop.setEnabled(false);
        mBrakeDistanceSeekBarBottom.setEnabled(false);
        mWarnDistanceSeekBarBottom.setEnabled(false);
    }

    private void initListener() {
        // 水平避障开关
        mObsHorSwitch.setOnClickListener(v -> {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return;
            }

            isHorSwitchSelected = !isHorSwitchSelected;
            mObsHorSwitch.setSelected(isHorSwitchSelected);
            mBrakeDistanceSeekBar.setEnabled(isHorSwitchSelected);
            mWarnDistanceSeekBar.setEnabled(isHorSwitchSelected);
            if (mOnPerceiveHorizontalListener != null) {
                mOnPerceiveHorizontalListener.onSwitch(isHorSwitchSelected);
            }
//            mPerceivingTypeImageView.setAlpha(isHorSwitchSelected ? 1.0f : 0.5f);
        });
        // 水平避障刹停距离
        mBrakeDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < 100){
                    seekBar.setProgress(100);
                    horSeekbarValue = 100;
                }

//                String value = getScalePbValue(seekBar.getProgress());
//                horSeekbarValue = seekBar.getProgress();
//                mBrakeDistanceValueTextView.setText(value + "m");
                mBrakeDistanceValueTextView.setText(UnitChnageUtils.getDecimalFormatUnit(
                        seekBar.getProgress()/100f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mWarnDistanceSeekBar.setEnabled(false);
                mObsHorSwitch.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mWarnDistanceSeekBar.setEnabled(true);
                mObsHorSwitch.setEnabled(true);
                final int value = seekBar.getProgress() / 10 * 10;
                if (horizontalAlarmValue <= value) {
                    mBrakeDistanceSeekBar.setProgress(horSeekbarValue);
                    Toast.makeText(getContext(), R.string.Msg_StopDistanceToLargeTip, Toast.LENGTH_SHORT).show();
                    return;
                }
                horSeekbarValue = value;
                if (mOnPerceiveHorizontalListener != null) {
                    mOnPerceiveHorizontalListener.onBrake(value);
                }
            }
        });
        // 水平避障告警距离
        mWarnDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < 110 ){
                   seekBar.setProgress(110);
                    horizontalAlarmValue = 110;
                }
                mWarnDistanceValueTextView.setText(UnitChnageUtils
                        .getDecimalFormatUnit(seekBar.getProgress()/100f));


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mBrakeDistanceSeekBar.setEnabled(false);
                mObsHorSwitch.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBrakeDistanceSeekBar.setEnabled(true);
                mObsHorSwitch.setEnabled(true);
                final int value = seekBar.getProgress() / 10 * 10;
                if (horSeekbarValue >= value) {
                    Toast.makeText(getContext(), R.string.Msg_AlarmDistanceToSmallTip, Toast.LENGTH_SHORT).show();
                    mWarnDistanceSeekBar.setProgress(horizontalAlarmValue);
                    return;
                }
                horizontalAlarmValue = value;
                if (mOnPerceiveHorizontalListener != null) {
                    mOnPerceiveHorizontalListener.onWarn(value);
                }
            }
        });

        // 上视避障开关
        mObsTopSwitch.setOnClickListener(v -> {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return;
            }

            isTopSwitchSelected = !isTopSwitchSelected;
            mObsTopSwitch.setSelected(isTopSwitchSelected);
            mBrakeDistanceSeekBarTop.setEnabled(isTopSwitchSelected);
            mWarnDistanceSeekBarTop.setEnabled(isTopSwitchSelected);
            if (mOnPerceiveTopListener != null) {
                mOnPerceiveTopListener.onSwitch(isTopSwitchSelected);
            }
//            mPerceivingTypeImageViewTop.setAlpha(isTopSwitchSelected ? 1.0f : 0.5f);
        });
        // 上视避障刹停距离
        mBrakeDistanceSeekBarTop.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < 100){
                    seekBar.setProgress(100);
                    topSeekbarValue = 100;
                }
//                topSeekbarValue = seekBar.getProgress();
//                final String value = getScalePbValue(seekBar.getProgress());
//                mBrakeDistanceValueTextViewTop.setText(value + "m");
                mBrakeDistanceValueTextViewTop.setText(UnitChnageUtils
                        .getDecimalFormatUnit(progress/100f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mWarnDistanceSeekBarTop.setEnabled(false);
                mObsTopSwitch.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mWarnDistanceSeekBarTop.setEnabled(true);
                mObsTopSwitch.setEnabled(true);
                final int value = seekBar.getProgress() / 10 * 10;
                if (topAlarmValue <= value) {
                    mBrakeDistanceSeekBarTop.setProgress(topSeekbarValue);
                    Toast.makeText(getContext(), R.string.Msg_StopDistanceToLargeTip, Toast.LENGTH_SHORT).show();
                    return;
                }
                topSeekbarValue = value;
                if (mOnPerceiveTopListener != null) {
                    mOnPerceiveTopListener.onBrake(value);
                }
            }
        });
        // 上视避障告警距离
        mWarnDistanceSeekBarTop.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress < 110){
                    seekBar.setProgress(110);
                    topAlarmValue = 110;
                }
//                topAlarmValue = seekBar.getProgress();
//                final String value = getScalePbValue(seekBar.getProgress());
//                mWarnDistanceValueTextViewTop.setText(value + "m");
                mWarnDistanceValueTextViewTop.setText(UnitChnageUtils
                        .getDecimalFormatUnit(progress/100f));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mBrakeDistanceSeekBarTop.setEnabled(false);
                mObsTopSwitch.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBrakeDistanceSeekBarTop.setEnabled(true);
                mObsTopSwitch.setEnabled(true);
                final int value = seekBar.getProgress() / 10 * 10;
                if (value <= topSeekbarValue) {
                    Toast.makeText(getContext(), R.string.Msg_AlarmDistanceToSmallTip, Toast.LENGTH_SHORT).show();
                    mWarnDistanceSeekBarTop.setProgress(topAlarmValue);
                    return;
                }
                topAlarmValue = value;
                if (mOnPerceiveTopListener != null) {
                    mOnPerceiveTopListener.onWarn(value);
                }
            }
        });

        // 下视避障开关
        mBottomSwitch.setOnClickListener(v -> {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return;
            }

            isBottomSwitchSelected = !isBottomSwitchSelected;
            mBottomSwitch.setSelected(isBottomSwitchSelected);
            mBrakeDistanceSeekBarBottom.setEnabled(isBottomSwitchSelected);
            mWarnDistanceSeekBarBottom.setEnabled(isBottomSwitchSelected);
            if (mOnPerceiveBottomListener != null) {
                mOnPerceiveBottomListener.onSwitch(isBottomSwitchSelected);
            }
//            mPerceivingTypeImageViewBottom.setAlpha(isBottomSwitchSelected ? 1.0f : 0.5f);
        });
        // 下视避障刹停距离
        mBrakeDistanceSeekBarBottom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 50){
                    seekBar.setProgress(50);
                    bottomSeekbarValue = 50;

                }
//                bottomSeekbarValue = seekBar.getProgress();
//                final String value = getScalePbValue(seekBar.getProgress());
//                mBrakeDistanceValueTextViewBottom.setText(value + "m");
                mBrakeDistanceValueTextViewBottom.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mWarnDistanceSeekBarBottom.setEnabled(false);
                mBottomSwitch.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mWarnDistanceSeekBarBottom.setEnabled(true);
                mBottomSwitch.setEnabled(true);
                final int value = seekBar.getProgress() / 10 * 10;
                if (bottomAlarmValue <= value) {
                    mBrakeDistanceSeekBarBottom.setProgress(bottomSeekbarValue);
                    Toast.makeText(getContext(), R.string.Msg_StopDistanceToLargeTip, Toast.LENGTH_SHORT).show();
                    return;
                }
                bottomSeekbarValue = value;
                if (mOnPerceiveBottomListener != null) {
                    mOnPerceiveBottomListener.onBrake(value);
                }
            }
        });
        // 下视避障告警距离
        mWarnDistanceSeekBarBottom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 60 ){
                   seekBar.setProgress(60);
                    bottomAlarmValue = 60;
                }
//                bottomAlarmValue = seekBar.getProgress();
//                final String value = getScalePbValue(seekBar.getProgress());
//                mWarnDistanceValueTextViewBottom.setText(value + "m");

                mWarnDistanceValueTextViewBottom.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mBrakeDistanceSeekBarBottom.setEnabled(false);
                mBottomSwitch.setEnabled(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mBrakeDistanceSeekBarBottom.setEnabled(true);
                mBottomSwitch.setEnabled(true);
                final int value = seekBar.getProgress() / 10 * 10;
                if (value <= bottomSeekbarValue) {
                    Toast.makeText(getContext(), R.string.Msg_AlarmDistanceToSmallTip, Toast.LENGTH_SHORT).show();
                    mWarnDistanceSeekBarBottom.setProgress(bottomAlarmValue);
                    return;
                }
                bottomAlarmValue = value;
                if (mOnPerceiveBottomListener != null) {
                    mOnPerceiveBottomListener.onWarn(value);
                }
            }
        });
        mIvLandingProtect.setOnClickListener(v -> {
            // 降落保护
            if (!connStateToast()) {
                return;
            }
            if (mIvLandingProtect.isSelected()) {
                mIvLandingProtect.setSelected(false);
                switchLandingProtect(false);
            } else {
                mIvLandingProtect.setSelected(true);
                switchLandingProtect(true);
            }
        });
    }

    private void switchLandingProtect(final boolean openLandProtect) {
        MyLogUtils.i("switchLandingProtect() openLandProtect = " + openLandProtect);
//        GduApplication.getSingleApp().gduCommunication.switchLandingProtect((byte) (openLandProtect ? 1 : 2),
//                (code, bean) -> {
//            MyLogUtils.i("switchLandingProtect() code = " + code);
//            if (code != 0) {
//                post(() -> mIvLandingProtect.setSelected(!openLandProtect));
//            }
//            Toaster.show(code == GduConfig.OK ? getContext().getString(R.string.string_set_success) :
//                    getContext().getString(R.string.Label_SettingFail));
//        });
    }

    private boolean connStateToast() {
        MyLogUtils.i("connStateToast()");
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                Toast.makeText(getContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_MoreOne:
                Toast.makeText(getContext(), R.string.Label_ConnMore, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_Sucess:
                return true;
            default:
                break;
        }
        return false;
    }

    private String getScalePbValue(int progress) {
        final float value = progress / 100f;
        final DecimalFormat mFormat = FormatConfig.format_4;
        return mFormat.format(value);
    }

    /**
     * 控制水平，上视，下视布局显示和隐藏
     * @param type 1：水平 2：上视 3：下视/辅助降落
     */
    public void showHideSettingView(int type) {
        MyLogUtils.i("showHideSettingView() type = " + type);
        switch (type) {
            case 1:
                ViewUtils.setViewShowOrHide(mHorizontalLayout, true);
                ViewUtils.setViewShowOrHide(mTopLayout, false);
                ViewUtils.setViewShowOrHide(mBottomLayout, false);
                ViewUtils.setViewShowOrHide(layout_downObstacle, false);
                break;

            case 2:
                ViewUtils.setViewShowOrHide(mTopLayout, true);
                ViewUtils.setViewShowOrHide(mHorizontalLayout, false);
                ViewUtils.setViewShowOrHide(mBottomLayout, false);
                ViewUtils.setViewShowOrHide(layout_downObstacle, false);
                break;

            case 3:
                if (CommonUtils.curPlanIsSmallFlight()) {
                    ViewUtils.setViewShowOrHide(mBottomLayout, true);
                    ViewUtils.setViewShowOrHide(mHorizontalLayout, false);
                    ViewUtils.setViewShowOrHide(mTopLayout, false);
                    ViewUtils.setViewShowOrHide(layout_downObstacle, false);
                } else {
                    // 辅助降落
                    ViewUtils.setViewShowOrHide(mBottomLayout, false);
                    ViewUtils.setViewShowOrHide(mHorizontalLayout, false);
                    ViewUtils.setViewShowOrHide(mTopLayout, false);
                    ViewUtils.setViewShowOrHide(layout_downObstacle, true);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 设置水平视觉避障开关
     * @param type 1:水平 2:上视 3:下视
     */
    public void setObstacleOpen(boolean isOpen, int type) {
        MyLogUtils.i("setObstacleOpen() isOpen = " + isOpen + "; type = " + type);
        switch (type) {
            case 1:
                mObsHorSwitch.setSelected(isOpen);
                mBrakeDistanceSeekBar.setEnabled(isOpen);
                mWarnDistanceSeekBar.setEnabled(isOpen);
                isHorSwitchSelected = isOpen;
//                mPerceivingTypeImageView.setAlpha(isHorSwitchSelected ? 1.0f : 0.5f);
                break;
            case 2:
                mObsTopSwitch.setSelected(isOpen);
                mBrakeDistanceSeekBarTop.setEnabled(isOpen);
                mWarnDistanceSeekBarTop.setEnabled(isOpen);
                isTopSwitchSelected = isOpen;
//                mPerceivingTypeImageViewTop.setAlpha(isTopSwitchSelected ? 1.0f : 0.5f);
                break;
            case 3:
                mBottomSwitch.setSelected(isOpen);
                mBrakeDistanceSeekBarBottom.setEnabled(isOpen);
                mWarnDistanceSeekBarBottom.setEnabled(isOpen);
                isBottomSwitchSelected = isOpen;
//                mPerceivingTypeImageViewBottom.setAlpha(isHorSwitchSelected ? 1.0f : 0.5f);
                break;
            default:
                break;
        }
    }

    /** 设置水平刹停距离*/
    public void setBrakeSeekbarProgress(int progress) {
//        MyLogUtils.i("setBrakeSeekbarProgress() progress = " + progress);
//        final String value = getScalePbValue(progress);
        mBrakeDistanceSeekBar.setProgress(progress);
        horSeekbarValue = progress;
//        mBrakeDistanceValueTextView.setText(value + "m");

        mBrakeDistanceValueTextView.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));
    }

    /** 设置上视刹停距离*/
    public void setBrakeSeekbarTop(int progress) {
//        MyLogUtils.i("setBrakeSeekbarTop() progress = " + progress);
//        final String value = getScalePbValue(progress);
        mBrakeDistanceSeekBarTop.setProgress(progress);
        topSeekbarValue = progress;
//        mBrakeDistanceValueTextViewTop.setText(value + "m");
        mBrakeDistanceValueTextViewTop.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));
    }

    /** 设置下视刹停距离*/
    public void setBrakeSeekbarBottom(int progress) {
//        MyLogUtils.i("setBrakeSeekbarBottom() progress = " + progress);
//        final String value = getScalePbValue(progress);
        mBrakeDistanceSeekBarBottom.setProgress(progress);
        bottomSeekbarValue = progress;
//        mBrakeDistanceValueTextViewBottom.setText(value + "m");
        mBrakeDistanceValueTextViewBottom.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));
    }

    /** 设置水平告警距离*/
    public void setWarnSeekbarProgress(int progress) {
//        MyLogUtils.i("setWarnSeekbarProgress() progress = " + progress);
//        final String value = getScalePbValue(progress);
        mWarnDistanceSeekBar.setProgress(progress);
        horizontalAlarmValue = progress;
//        mWarnDistanceValueTextView.setText(value + "m");
        mWarnDistanceValueTextView.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));
    }

    /** 设置上视告警距离*/
    public void setWarnSeekbarTop(int progress) {
//        MyLogUtils.i("setWarnSeekbarTop() progress = " + progress);
//        final String value = getScalePbValue(progress);
        mWarnDistanceSeekBarTop.setProgress(progress);
        topAlarmValue = progress;
//        mWarnDistanceValueTextViewTop.setText(value + "m");
        mWarnDistanceValueTextViewTop.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));
    }

    /** 设置下视告警距离*/
    public void setWarnSeekbarBottom(int progress) {
//        MyLogUtils.i("setWarnSeekbarBottom() progress = " + progress);
//        final String value = getScalePbValue(progress);
        mWarnDistanceSeekBarBottom.setProgress(progress);
        bottomAlarmValue = progress;
//        mWarnDistanceValueTextViewBottom.setText(value + "m");
        mWarnDistanceValueTextViewBottom.setText(UnitChnageUtils.getDecimalFormatUnit(progress/100f));

    }

    /** 设置水平感知名*/
    public void setPerceiveName(String name, int type) {
        MyLogUtils.i("setPerceiveName() name = " + name + "; type = " + type);
        switch (type) {
            case 1:
                mObstacleNameTextView.setText(name);
                break;
            case 2:
                mTopObsName.setText(name);
                break;
            case 3:
                mBottomObsName.setText(name);
                break;
            default:
                break;
        }
    }

    /** 设置水平刹停距离提示*/
    public void setBrakeDistanceHint(String hint, int type) {
        MyLogUtils.i("setBrakeDistanceHint() hint = " + hint + "; type = " + type);
        switch (type) {
            case 1:
                mBrakeDistanceHintTextView.setText(hint);
                break;
            case 2:
                mBrakeDistanceHintTextViewTop.setText(hint);
                break;
            case 3:
                mBrakeDistanceHintTextViewBottom.setText(hint);
                break;
            default:
                break;
        }
    }

    /** 设置水平警告距离提示*/
    public void setWarnDistanceHint(String hint, int type) {
        MyLogUtils.i("setWarnDistanceHint() hint = " + hint + "; type = " + type);
        switch (type) {
            case 1:
                mWarnDistanceHintTextView.setText(hint);
                break;
            case 2:
                mWarnDistanceHintTextViewTop.setText(hint);
                break;
            case 3:
                mWarnDistanceHintTextViewBottom.setText(hint);
                break;
            default:
                break;
        }
    }

    /** 水平避障设置*/
    public interface OnPerceiveHorizontalListener {
        /**
         * 水平避障开关监听
         * @param isOpen
         */
        void onSwitch(boolean isOpen);

        /**
         * 水平避障刹停距离
         * @param progress
         */
        void onBrake(int progress);

        /**
         * 水平避障告警距离
         * @param progress
         */
        void onWarn(int progress);
    }
    /** 上视避障设置*/
    public interface OnPerceiveTopListener {
        /**
         * 上视避障开关监听
         * @param isOpen
         */
        void onSwitch(boolean isOpen);

        /**
         * 上视避障刹停距离
         * @param progress
         */
        void onBrake(int progress);

        /**
         * 上视避障告警距离
         * @param progress
         */
        void onWarn(int progress);
    }
    /** 下视避障设置*/
    public interface OnPerceiveBottomListener {
        /**
         * 下视避障开关监听
         * @param isOpen
         */
        void onSwitch(boolean isOpen);

        /**
         * 下视避障刹停距离
         * @param progress
         */
        void onBrake(int progress);

        /**
         * 下视避障告警距离
         * @param progress
         */
        void onWarn(int progress);
    }

    public void changeUnit() {
        String unit = UnitChnageUtils.getUnit();
        if (horSeekbarValue > 0) {
            mBrakeDistanceValueTextView.setText(UnitChnageUtils.getDecimalFormatUnit(
                    mBrakeDistanceSeekBar.getProgress()/100f));
        } else {
            mBrakeDistanceValueTextView.setText("N/A" + unit);
        }

        if (horizontalAlarmValue > 0) {
            mWarnDistanceValueTextView.setText(UnitChnageUtils.getDecimalFormatUnit(
                    mWarnDistanceSeekBar.getProgress()/100f));
        } else {
            mWarnDistanceValueTextView.setText("N/A" + unit);
        }

        if (topSeekbarValue > 0) {
            mBrakeDistanceValueTextViewTop.setText(UnitChnageUtils.getDecimalFormatUnit(
                    mBrakeDistanceSeekBarTop.getProgress()/100f));
        } else {
            mBrakeDistanceValueTextViewTop.setText("N/A" + unit);
        }

        if (topAlarmValue > 0) {
            mWarnDistanceValueTextViewTop.setText(UnitChnageUtils.getDecimalFormatUnit(
                    mWarnDistanceSeekBarTop.getProgress()/100f));
        } else {
            mWarnDistanceValueTextViewTop.setText("N/A" + unit);
        }

        if (bottomSeekbarValue > 0) {
            mBrakeDistanceValueTextViewBottom.setText(UnitChnageUtils.getDecimalFormatUnit(
                    mBrakeDistanceSeekBarBottom.getProgress()/100f));

        } else {
            mBrakeDistanceValueTextViewBottom.setText("N/A" + unit);
        }

        if (bottomAlarmValue > 0) {
            mWarnDistanceValueTextViewBottom.setText(UnitChnageUtils.getDecimalFormatUnit(
                    mWarnDistanceSeekBarBottom.getProgress()/100f));
        } else {
            mWarnDistanceValueTextViewBottom.setText("N/A" + unit);
        }
        tvTip.setText(getResources().getString(R.string.string_bottom_obstacle, UnitChnageUtils.getDecimalFormatUnit(3f),UnitChnageUtils.getDecimalFormatSpeedUnit(1.5f)));
        tvLandingProtectHint.setText(getResources().getString(R.string.precise_landing_protect_hint, UnitChnageUtils.getDecimalFormatUnit(1f)));
    }


}
