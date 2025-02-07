package com.gdu.demo.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.utils.SettingDao;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.logger.MyLogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * 感知避障设置
 */
public class PerceivingObstacleAvoidanceSettingsView extends LinearLayout {

    /** 获取水平避障开关 */
    private final int GET_OBSTACLE_HORIZONTAL_OPEN = 5;
    /** 获取上视避障开关 */
    private final int GET_OBSTACLE_TOP_OPEN = 6;
    /** 获取下视避障开关 */
    private final int GET_OBSTACLE_BOTTOM_OPEN = 7;
    /** 设置避障成功 */
    private final int SET_OBSTACLE_SUCCESS = 8;
    /** 设置避障失败 */
    private final int SET_OBSTACLE_FAIL = 9;
    /** 获取水平避障刹停距离 */
    private final int GET_HOR_STOP_DISTANCE = 10;
    /** 获取水平避障告警距离 */
    private final int GET_HOR_WARN_DISTANCE = 11;
    /** 获取上视避障刹停距离 */
    private final int GET_TOP_STOP_DISTANCE = 12;
    /** 获取上视避障告警距离 */
    private final int GET_TOP_WARN_DISTANCE = 13;
    /** 获取下视避障刹停距离 */
    private final int GET_BOTTOM_STOP_DISTANCE = 14;
    /** 获取下视避障告警距离 */
    private final int GET_BOTTOM_WARN_DISTANCE = 15;

    /** 水平设置 */
    private final int HORIZONTALLY_SETTING = 1;
    /** 上方设置 */
    private final int ABOVE_SETTING = 2;
    /** 下方设置 */
    private final int BELOW_SETTING = 3;
    /** 高级设置 */
    private final int HIGH_SETTING = 4;

    /** 当前设置类型 */
    private int mCurrentSettingType;

    private final Context mContext;
    private PerceivingSettingsView mPerceivingSettingsView;
    private PerceivingHighSettingsView mPerceivingHighSettingsView;

    // 水平避障
    /** 水平避障开关 */
    private int isHorSwitchSelected;
    /** 水平避障刹停距离 */
    private int horBrakeDistance = 1000;
    /** 水平避障告警距离 */
    private int horWarnDistance = 2000;

    // 上视避障
    /** 上视避障开关 */
    private int isTopSwitchSelected;
    /** 上视避障刹停距离 */
    private int topBrakeDistance = 300;
    /** 上视避障告警距离 */
    private int topWarnDistance = 500;

    // 下视避障
    /** 下视避障开关 */
    private int isBottomSwitchSelected;
    /** 下视避障刹停距离 */
    private int bottomBrakeDistance = 100;
    /** 下视避障告警距离 */
    private int bottomWarnDistance = 500;
    private RadioGroup mRgVisionType;
    private RadioButton mRbBotVisionLabel;

    public PerceivingObstacleAvoidanceSettingsView(Context context) {
        this(context, null);
    }

    public PerceivingObstacleAvoidanceSettingsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PerceivingObstacleAvoidanceSettingsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        initHandler();
        initListener();
    }

    private void initView() {
//        EventBus.getDefault().register(this);
        LayoutInflater.from(mContext).inflate(R.layout.view_perceiv_obstacle_avoidance_setting, this);
        mRgVisionType = findViewById(R.id.rg_vision_type);
        mRbBotVisionLabel = findViewById(R.id.rb_vision_assisted);
        mPerceivingSettingsView = findViewById(R.id.perceive_setting_view);
        mPerceivingHighSettingsView = findViewById(R.id.perceive_high_setting_view);
        setHorizontallySetting();

        if (DroneUtil.isSmallFlight()) {
            mRbBotVisionLabel.setText(mContext.getString(R.string.below_settings));
        } else {
            mRbBotVisionLabel.setText(mContext.getString(R.string.assisted_landing));
        }
    }

    private void initHandler() {
        getObstacleCallback();
    }

    /** 获取避障子方向开关和距离*/
    private void getObstacleCallback() {
//        GduApplication.getSingleApp().gduCommunication.getObstacleDirectionDistance((code, bean) -> {
//            MyLogUtils.d("getObstacleCallback() code = " + code);
//            // 这里不去判断code的状态
//            if (bean != null && bean.frameContent != null) {
//                isHorSwitchSelected = bean.frameContent[0];
//                isTopSwitchSelected = bean.frameContent[5];
//                isBottomSwitchSelected = bean.frameContent[10];
//
//                GlobalVariable.isObsHorSwitchState = isHorSwitchSelected == 0;
//                GlobalVariable.isObsTopSwitchState = isTopSwitchSelected == 0;
//                GlobalVariable.isObsBottomSwitchState = isBottomSwitchSelected == 0;
//                mHandler.obtainMessage(GET_OBSTACLE_HORIZONTAL_OPEN,isHorSwitchSelected, 0).sendToTarget(); //水平避障开关
//                mHandler.obtainMessage(GET_OBSTACLE_TOP_OPEN,isTopSwitchSelected, 0).sendToTarget();        //上视避障开关
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    mHandler.obtainMessage(GET_OBSTACLE_BOTTOM_OPEN, isBottomSwitchSelected, 0).sendToTarget();    //下视避障开关
//                }
//
//                horBrakeDistance = ByteUtilsLowBefore.byte2short(bean.frameContent, 1);                       //水平避障刹停距离
//                horWarnDistance = ByteUtilsLowBefore.byte2short(bean.frameContent, 3);                        //水平避障告警距离
//                topBrakeDistance = ByteUtilsLowBefore.byte2short(bean.frameContent, 6);                       //上视避障刹停距离
//                topWarnDistance = ByteUtilsLowBefore.byte2short(bean.frameContent, 8);                        //上视觉避告警距离
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    //下视避障刹停距离
//                    bottomBrakeDistance = ByteUtilsLowBefore.byte2short(bean.frameContent, 11);
//                    //下视避障告警距离
//                    bottomWarnDistance = ByteUtilsLowBefore.byte2short(bean.frameContent, 13);
//                }
//
//                mHandler.obtainMessage(GET_HOR_STOP_DISTANCE, horBrakeDistance, 0).sendToTarget();
//                mHandler.obtainMessage(GET_HOR_WARN_DISTANCE, horWarnDistance, 0).sendToTarget();
//                mHandler.obtainMessage(GET_TOP_STOP_DISTANCE, topBrakeDistance, 0).sendToTarget();
//                mHandler.obtainMessage(GET_TOP_WARN_DISTANCE, topWarnDistance, 0).sendToTarget();
//                if (CommonUtils.curPlanIsSmallFlight()) {
//                    mHandler.obtainMessage(GET_BOTTOM_STOP_DISTANCE, bottomBrakeDistance, 0).sendToTarget();
//                    mHandler.obtainMessage(GET_BOTTOM_WARN_DISTANCE, bottomWarnDistance, 0).sendToTarget();
//                }
//
//                MyLogUtils.d("getObstacleCallback() horOpen = " + (isHorSwitchSelected));
//                MyLogUtils.d("getObstacleCallback() horStop = " + horBrakeDistance);
//                MyLogUtils.d("getObstacleCallback() horWarning = " + horWarnDistance);
//                MyLogUtils.d("getObstacleCallback() topOpen = " + (isTopSwitchSelected ));
//                MyLogUtils.d("getObstacleCallback() topStop = " + topBrakeDistance);
//                MyLogUtils.d("getObstacleCallback() topWarning = " + topWarnDistance);
//                MyLogUtils.d("getObstacleCallback() bottomOpen = " + (isBottomSwitchSelected));
//                MyLogUtils.d("getObstacleCallback() bottomStop = " + bottomBrakeDistance);
//                MyLogUtils.d("getObstacleCallback() bottomWarning = " + bottomWarnDistance);
//            }
//        });
    }

    /** 设置避障子方向开关和距离*/
    private void setObsSetting(byte horOpen, int horStop, int horWarning, byte topOpen, int topStop, int topWarning, byte bottomOpen, int bottomStop, int bottomWarning) {
//        GduApplication.getSingleApp().gduCommunication.setObstacleDirectionDistance(new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (code == GduConfig.OK) {
//                    mHandler.obtainMessage(SET_OBSTACLE_SUCCESS).sendToTarget();
//                } else {
//                    mHandler.obtainMessage(SET_OBSTACLE_FAIL).sendToTarget();
//                }
//            }
//        }, horOpen, (short) horStop, (short) horWarning, topOpen, (short) topStop, (short) topWarning, bottomOpen, (short) bottomStop, (short) bottomWarning);
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case GET_OBSTACLE_HORIZONTAL_OPEN:  //水平避障开关
                    mPerceivingSettingsView.setObstacleOpen(msg.arg1 == 0, 1);
                    break;
                case GET_OBSTACLE_TOP_OPEN:         //上视避障开关
                    mPerceivingSettingsView.setObstacleOpen(msg.arg1 == 0, 2);
                    break;
                case GET_OBSTACLE_BOTTOM_OPEN:      //下视避障开关
                    mPerceivingSettingsView.setObstacleOpen(msg.arg1 == 0, 3);
                    break;
                case SET_OBSTACLE_SUCCESS:          //设置避障成功
                    Toast.makeText(mContext, R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;
                case SET_OBSTACLE_FAIL:             //设置避障失败
                    Toast.makeText(mContext, R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                    break;
                case GET_HOR_STOP_DISTANCE:         //获取水平刹停距离
                    mPerceivingSettingsView.setBrakeSeekbarProgress(msg.arg1);
                    break;
                case GET_HOR_WARN_DISTANCE:         //获取水平告警距离
                    mPerceivingSettingsView.setWarnSeekbarProgress(msg.arg1);
                    break;
                case GET_TOP_STOP_DISTANCE:         //获取上视刹停距离
                    mPerceivingSettingsView.setBrakeSeekbarTop(msg.arg1);
                    break;
                case GET_TOP_WARN_DISTANCE:         //获取上视告警距离
                    mPerceivingSettingsView.setWarnSeekbarTop(msg.arg1);
                    break;
                case GET_BOTTOM_STOP_DISTANCE:      //获取下视刹停距离
                    mPerceivingSettingsView.setBrakeSeekbarBottom(msg.arg1);
                    break;
                case GET_BOTTOM_WARN_DISTANCE:      //获取下视告警距离
                    mPerceivingSettingsView.setWarnSeekbarBottom(msg.arg1);
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 水平避障设置
     */
    private void setHorizontallySetting(){
        mPerceivingHighSettingsView.setVisibility(GONE);
        mPerceivingSettingsView.setVisibility(VISIBLE);
        mCurrentSettingType = HORIZONTALLY_SETTING;
        mPerceivingSettingsView.setPerceiveName(mContext.getResources().getString(R.string.horizontally_disposed_name), 1);
        mPerceivingSettingsView.setBrakeDistanceHint(getResValue(1), 1);
        mPerceivingSettingsView.setWarnDistanceHint(getResValue(2), 1);
    }

    private void initListener() {

        mRgVisionType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_vision_horizontal){
                //水平设置
                setObstacleType(0);
            }else if (checkedId == R.id.rb_vision_above){
                //上视设置
                setObstacleType(1);
            }else if (checkedId == R.id.rb_vision_assisted){
                //辅助降落/下视设置（S200系列）
                setObstacleType(2);
            }

        });

        // 水平避障设置
        mPerceivingSettingsView.setOnPerceiveHorizontalListener(new PerceivingSettingsView.OnPerceiveHorizontalListener() {
            @Override
            public void onSwitch(boolean isOpen) {
                isHorSwitchSelected = isOpen ? 0 : 1;
                GlobalVariable.isObsHorSwitchState = isOpen;

                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }

            @Override
            public void onBrake(int progress) {
                MyLogUtils.d("setOnPerceiveHorizontalListener() onBrake -> progress = " + progress);
                if (isHorSwitchSelected != 0) {
                    return;
                }
                if(progress < 3){
                    tipBrake(R.string.warn_brake_distance);
                }
                horBrakeDistance = progress;
                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }

            @Override
            public void onWarn(int progress) {
                MyLogUtils.d("setOnPerceiveHorizontalListener() onWarn -> progress = " + progress);
                if (isHorSwitchSelected != 0) {
                    return;
                }
                horWarnDistance = progress;
                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }
        });
        // 上视避障设置
        mPerceivingSettingsView.setOnPerceiveTopListener(new PerceivingSettingsView.OnPerceiveTopListener() {
            @Override
            public void onSwitch(boolean isOpen) {
                isTopSwitchSelected = isOpen ? 0 : 1;
                GlobalVariable.isObsTopSwitchState = isOpen;

                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }

            @Override
            public void onBrake(int progress) {
                MyLogUtils.d("setOnPerceiveTopListener() onBrake -> progress = " + progress);
                if (isTopSwitchSelected != 0) {
                    return;
                }
                topBrakeDistance = progress;
                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }

            @Override
            public void onWarn(int progress) {
                MyLogUtils.d("setOnPerceiveTopListener() onWarn -> progress = " + progress);
                if (isTopSwitchSelected != 0) {
                    return;
                }
                topWarnDistance = progress;
                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }
        });
        // 下视避障设置
        mPerceivingSettingsView.setOnPerceiveBottomListener(new PerceivingSettingsView.OnPerceiveBottomListener() {
            @Override
            public void onSwitch(boolean isOpen) {
                isBottomSwitchSelected = isOpen ? 0 : 1;
                GlobalVariable.isObsBottomSwitchState = isOpen;

                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }

            @Override
            public void onBrake(int progress) {
                MyLogUtils.d("setOnPerceiveBottomListener() onBrake -> progress = " + progress);
                if (isBottomSwitchSelected != 0) {
                    return;
                }
                bottomBrakeDistance = progress;
                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }

            @Override
            public void onWarn(int progress) {
                MyLogUtils.d("setOnPerceiveBottomListener() onWarn -> progress = " + progress);
                if (isBottomSwitchSelected != 0) {
                    return;
                }
                bottomWarnDistance = progress;
                setObsSetting((byte) isHorSwitchSelected, horBrakeDistance,  horWarnDistance,
                        (byte) isTopSwitchSelected,  topBrakeDistance, topWarnDistance,
                        (byte) isBottomSwitchSelected,  bottomBrakeDistance,  bottomWarnDistance);
            }
        });

        // 高级避障设置
        mPerceivingHighSettingsView.setOnPerceiveListener(new PerceivingHighSettingsView.OnPerceiveHighListener() {
            @Override
            public void onVisualLocationSwitch(boolean isOpen) {

            }

            @Override
            public void onPreciseLandingSwitch(boolean isOpen) {

            }
        });
    }

    /**
     * @param position 0：水平设置 1：上视设置 3：辅助降落/下视设置（S200系列）
     */
    private void setObstacleType(int position) {
        MyLogUtils.i("setObstacleType() position = " + position);
        if (position == 0) {
            setHorizontallySetting();
            mPerceivingSettingsView.showHideSettingView(1);
        } else if (position == 1) {
            mPerceivingSettingsView.showHideSettingView(2);
            mPerceivingHighSettingsView.setVisibility(GONE);
            mPerceivingSettingsView.setVisibility(VISIBLE);
            mCurrentSettingType = ABOVE_SETTING;
            mPerceivingSettingsView.setPerceiveName(mContext.getResources().getString(R.string.above_disposed_name), 2);
            mPerceivingSettingsView.setBrakeDistanceHint(getResValue(3), 2);
            mPerceivingSettingsView.setWarnDistanceHint(getResValue(4), 2);
        } else if (position == 2) {
            mPerceivingSettingsView.showHideSettingView(3);
            mPerceivingHighSettingsView.setVisibility(GONE);
            mPerceivingSettingsView.setVisibility(VISIBLE);
            mCurrentSettingType = BELOW_SETTING;
            String botTipLabel = CommonUtils.curPlanIsSmallFlight() ?
                    mContext.getResources().getString(R.string.below_disposed_name_new) :
                    mContext.getResources().getString(R.string.below_disposed_name);
            mPerceivingSettingsView.setPerceiveName(botTipLabel, 3);
            mPerceivingSettingsView.setBrakeDistanceHint(getResValue(5), 3);
            mPerceivingSettingsView.setWarnDistanceHint(getResValue(6), 3);
        }
    }

    /**
     * 当避障安全距离较小时，请注意障碍物距离，谨慎飞行。
     * @param str
     */
    private void tipBrake(int str) {
        MyLogUtils.i("tipSpeedObstealOn() str = " + str);
//        if (mDialogUtils == null) {
//            mDialogUtils = new DialogUtils(mContext);
//        }
//        mDialogUtils.createDialogWithSingleBtn(mContext.getString(R.string.tip), mContext.getString(str), mContext.getString(R.string.Label_Sure), new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mDialogUtils.cancelDialog();
//            }
//        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        unRegisterEventBus();
    }

    private String getResValue(int resType) {
        boolean isMetric = UnitChnageUtils.getUnitType() == SettingDao.Unit_Merch;
        switch (resType) {
            case 1:
                return mContext.getString(isMetric ? R.string.Msg_ObstacleDistanceTip1
                        : R.string.Msg_ObstacleDistanceTip1_ft);
            case 2:
                return mContext.getString(isMetric ? R.string.Msg_ObstacleAlarmDistanceTip1
                        : R.string.Msg_ObstacleAlarmDistanceTip1_ft);
            case 3:
                return mContext.getString(isMetric ? R.string.Msg_ObstacleDistanceTip2
                        : R.string.Msg_ObstacleDistanceTip2_ft);
            case 4:
                return mContext.getString(isMetric ? R.string.Msg_ObstacleAlarmDistanceTip2
                        : R.string.Msg_ObstacleAlarmDistanceTip2_ft);
            case 5:
                return CommonUtils.curPlanIsSmallFlight() ?
                        mContext.getString(isMetric ? R.string.Msg_ObstacleDistanceTip3New
                                : R.string.Msg_ObstacleDistanceTip3New_ft) :
                        mContext.getString(isMetric ? R.string.Msg_ObstacleDistanceTip3
                                : R.string.Msg_ObstacleDistanceTip3_ft);
            case 6:
                return CommonUtils.curPlanIsSmallFlight() ?
                        mContext.getString(isMetric ? R.string.Msg_ObstacleAlarmDistanceTip3New
                                : R.string.Msg_ObstacleAlarmDistanceTip3New_ft) :
                        mContext.getString(isMetric ? R.string.Msg_ObstacleAlarmDistanceTip3
                                : R.string.Msg_ObstacleAlarmDistanceTip3_ft);
            default:
                return "";
        }
    }

//    @Subscribe
//    public void onEventMainThread(ChangeUnitEvent event) {
//        //动态变化参数单位需要
//        if (mPerceivingSettingsView != null) {
//            mPerceivingSettingsView.changeUnit();
//        }
//        changeUnit();
//    }

    private void changeUnit() {
        mPerceivingSettingsView.setBrakeDistanceHint(getResValue(1), 1);
        mPerceivingSettingsView.setWarnDistanceHint(getResValue(2), 1);
        mPerceivingSettingsView.setBrakeDistanceHint(getResValue(3), 2);
        mPerceivingSettingsView.setWarnDistanceHint(getResValue(4), 2);
        mPerceivingSettingsView.setBrakeDistanceHint(getResValue(5), 3);
        mPerceivingSettingsView.setWarnDistanceHint(getResValue(6), 3);
    }

    public void unRegisterEventBus() {
        EventBus.getDefault().unregister(this);
    }


}
