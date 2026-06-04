package com.gdu.demo.flight.setting.camera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.gdu.config.GduConfig;
import com.gdu.demo.FlightActivity;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.lib.util.NumberUtils;
import com.gdu.lib.util.StringUtils;
import com.gdu.lib.util.ViewUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.component.interfaces.IGimbal;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.msdk.key.callback.MSdkCallback;
import com.gdu.msdk.key.error.MError;
import com.gdu.msdk.key.value.bean.GimbalType;
import com.gdu.msdk.key.value.common.EmptyMsg;

import cc.taylorzhang.singleclick.SingleClickUtil;


/**
 * 可见光参数设置和获取
 */
public class VLCameraSetHelper extends CameraSetHelper implements View.OnClickListener {
    private static final String TAG = VLCameraSetHelper.class.getSimpleName();
    /**  设置失败通用提示 */
    protected final int SET_FAILE = 0x11;
    /** 设置云台俯仰转动速度成功 */
    protected final int SET_GIMBAL_PITCH_SPEED_SUCCEED = 0x12;
    /** 设置云台俯仰转动速度失败 */
    protected final int SET_GIMBAL_PITCH_FAILED = 0x13;

    /** 获取云台信息成功 */
    protected final int GET_GIMBAL_CURRENT_SETTING_SUCCEED = 0x14;
    /** 获取云台信息失败 */
    protected final int GET_GIMBAL_CURRENT_SETTING_FAILED = 0x15;
    /** 获取云台方位转动速度失败 */
    protected final int SET_GIMBAL_YAW_SPEED_FAILED = 0x16;
    /** 获取云台俯仰缓启停成功 */
    protected final int SET_GIMBAL_PITCH_SlOW_SETTING_FAILED = 0x17;
    /** 获取云台偏航缓启停失败 */
    protected final int SET_GIMBAL_YAW_SLOW_SETTING_FAILED = 0x18;
    /** 恢复云台默认设置成功 */
    protected final int RESET_GIMBAL_PARAMS_SUC = 0x19;
    /** 恢复云台默认设置失败 */
    protected final int RESET_GIMBAL_PARAMS_FAILED = 0x20;


    protected SeekBar sb_pitch_speed;
    protected EditText et_pitch_speed;
    protected SeekBar sb_ptz_yaw_speed;
    protected EditText et_ptz_yaw_speed;

    protected SeekBar sb_pitch_slow_setting;
    protected EditText et_pitch_slow_setting;
    protected SeekBar sb_yaw_slow_setting;
    protected EditText et_yaw_slow_setting;

    public SeekBar.OnSeekBarChangeListener mSeekBarFourLightListener = null;

    public SeekBar.OnSeekBarChangeListener mSeekBarPTZYawListener = null;

    public SeekBar.OnSeekBarChangeListener mSeekBarPitchSlowSettingListener = null;

    public SeekBar.OnSeekBarChangeListener mSeekBarYawSlowSettingListener = null;

    protected LinearLayout mCameraMainLayout;

    protected TextView tv_check_clound;
    protected View rl_check_clound;
    private TextView tv_reset_gimbal;

    /** 俯仰缓启停设置 */
    public static byte spitchSlowSetting = 15;
    /** 方位缓启停设置 */
    public static byte yawSlowSetting = 15;

    public VLCameraSetHelper() {
        super();
    }

    public VLCameraSetHelper(View view, FragmentActivity activity) {
        super(view, activity);
        initView();
        initCameraParams();
        initData();
        initListener();
    }

    /**
     * <P>shang</P>
     * <P>初始化一些配置</P>
     */

    public void initView() {
        XLogger.INSTANCE.getAPP().i("initView()   ");
        mCameraMainLayout = mView.findViewById(R.id.camera_main_layout);

        tv_check_clound = mView.findViewById(R.id.tv_check_clound);
        rl_check_clound = mView.findViewById(R.id.rl_check_clound);
        //校漂
        ViewUtils.setViewShowOrHide(rl_check_clound, true);

        //恢复云台默认设置
        tv_reset_gimbal = mView.findViewById(R.id.tv_reset_gimbal);
        View mViewGimbalPositionGroup = mView.findViewById(R.id.viewGimbalPositionGroup);
        if (IGduDroneDevice.get().getPlanType().getValue().isS200Type()) {
            ViewUtils.setViewShowOrHide(mViewGimbalPositionGroup, false);
        } else {
            LinearLayout llGimbalPitchStartAndStop = mView.findViewById(R.id.llGimbalPitchStartAndStop);
            ViewUtils.setViewShowOrHide(llGimbalPitchStartAndStop, !IGimbal.get().getSupportFun().getDisablePitchStartAndStop());

            LinearLayout llGimbalPositionStartAndStop = mView.findViewById(R.id.llGimbalPositionStartAndStop);
            ViewUtils.setViewShowOrHide(llGimbalPositionStartAndStop, !IGimbal.get().getSupportFun().getDisablePositionStartAndStop());
        }
    }

    public void initCameraParams() {
        XLogger.INSTANCE.getAPP().i("initCameraParams()");
    }


    protected void initPTZSetting(View view){
        if (view == null) {
            return;
        }
        sb_pitch_speed = view.findViewById(R.id.seekBar_pitch_speed);
        sb_ptz_yaw_speed = view.findViewById(R.id.seekBar_ptz_yaw_speed);

        sb_pitch_slow_setting = view.findViewById(R.id.seekBar_pitch_slow_setting);
        sb_yaw_slow_setting = view.findViewById(R.id.seekBar_yaw_slow_setting);

        et_pitch_speed = view.findViewById(R.id.et_pitch_speed);
        et_pitch_speed.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            String value = textView.getText().toString();
            if (StringUtils.isEmptyString(value) || !NumberUtils.isNumeric(value)) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            et_pitch_speed.clearFocus();
            int valueInt = Integer.parseInt(value);
            if (valueInt < 5 || valueInt > 100) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            sb_pitch_speed.setProgress(valueInt - 5);
            setPitchSpeed(valueInt);
            return false;
        });

        et_ptz_yaw_speed = view.findViewById(R.id.et_ptz_yaw_speed);
        et_ptz_yaw_speed.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            String value = textView.getText().toString();
            if (StringUtils.isEmptyString(value) || !NumberUtils.isNumeric(value)) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            et_ptz_yaw_speed.clearFocus();
            int valueInt = Integer.parseInt(value);
            if (valueInt < 5 || valueInt > 100) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            sb_ptz_yaw_speed.setProgress(valueInt - 5);
            setPTZYawSpeed(valueInt);
            return false;
        });

        et_pitch_slow_setting = view.findViewById(R.id.et_pitch_slow_setting);
        et_pitch_slow_setting.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            String value = textView.getText().toString();
            if (StringUtils.isEmptyString(value) || !NumberUtils.isNumeric(value)) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            et_pitch_slow_setting.clearFocus();
            int valueInt = Integer.parseInt(value);
            if (valueInt < 0 || valueInt > 30) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            sb_pitch_slow_setting.setProgress(valueInt);
            setSlowSetting(valueInt, SlowSettingType.PITCH);
            return false;
        });

        et_yaw_slow_setting = view.findViewById(R.id.et_yaw_slow_setting);
        et_yaw_slow_setting.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            String value = textView.getText().toString();
            if (StringUtils.isEmptyString(value) || !NumberUtils.isNumeric(value)) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            et_yaw_slow_setting.clearFocus();
            int valueInt = Integer.parseInt(value);
            if (valueInt < 0 || valueInt > 30) {
                showToast(mView.getContext().getString(R.string.input_error));
                return true;
            }
            sb_yaw_slow_setting.setProgress(valueInt);
            setSlowSetting(valueInt, SlowSettingType.PTZ_YAW);
            return false;
        });

        mSeekBarFourLightListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                XLogger.INSTANCE.getAPP().i("mSeekBarListener onProgressChanged() progress = " + progress);
                et_pitch_speed.setText(String.valueOf(progress + 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setPitchSpeed(seekBar.getProgress() + 5);
            }
        };

        mSeekBarPTZYawListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                XLogger.INSTANCE.getAPP().i("mSeekBarPTZYawListener onProgressChanged() progress = " + progress);
                et_ptz_yaw_speed.setText(String.valueOf(progress + 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setPTZYawSpeed(seekBar.getProgress() + 5);
            }
        };

        mSeekBarPitchSlowSettingListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                XLogger.INSTANCE.getAPP().i("mSeekBarPitchSlowSettingListener onProgressChanged() progress = " + progress);
                et_pitch_slow_setting.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSlowSetting(seekBar.getProgress(), SlowSettingType.PITCH);
            }
        };

        mSeekBarYawSlowSettingListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                XLogger.INSTANCE.getAPP().i("mSeekBarYawSlowSettingListener onProgressChanged() progress = " + progress);
                et_yaw_slow_setting.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSlowSetting(seekBar.getProgress(), SlowSettingType.PTZ_YAW);
            }
        };

        boolean isConnect = SdkDemoApplication.getAircraftInstance().isConnected();
        sb_pitch_speed.setEnabled(isConnect);
        sb_ptz_yaw_speed.setEnabled(isConnect);
        sb_pitch_slow_setting.setEnabled(isConnect);
        sb_yaw_slow_setting.setEnabled(isConnect);

        sb_pitch_speed.setOnSeekBarChangeListener(mSeekBarFourLightListener);
        sb_ptz_yaw_speed.setOnSeekBarChangeListener(mSeekBarPTZYawListener);
        sb_pitch_slow_setting.setOnSeekBarChangeListener(mSeekBarPitchSlowSettingListener);
        sb_yaw_slow_setting.setOnSeekBarChangeListener(mSeekBarYawSlowSettingListener);

        getGimbalCurrentSetting(false);
    }

    /**
     * 根据周期反馈，更新进度值
     */
    protected void setPitchSpeed() {
        XLogger.INSTANCE.getAPP().i("setPitchSpeed() thumbWheelSpeed = " + GlobalVariable.thumbWheelSpeed);
        if (sb_pitch_speed != null) {
            if (GlobalVariable.thumbWheelSpeed >= 5) {
                sb_pitch_speed.setProgress(GlobalVariable.thumbWheelSpeed - 5);
            } else {
                sb_pitch_speed.setProgress(0);
            }
        }
    }

    /**
     * 获取云台信息
     * @param isRetry 是否手动刷新
     */
    protected void getGimbalCurrentSetting(boolean isRetry) {
        GduSocketManager.getInstance().getGduCommunication().getGimbalSetting((code, bean) -> {
            XLogger.INSTANCE.getAPP().i(TAG, "getGimbalSetting callback() code = " + code);
            if (mHandler == null) {
                return;
            }
            boolean isHaveData = code == GduConfig.OK && bean != null && bean.frameContent != null && bean.frameContent.length > 3;
            XLogger.INSTANCE.getAPP().i("getGimbalSetting callback() isHaveData = " + isHaveData);
            if (isHaveData) {
                GlobalVariable.thumbWheelSpeed = bean.frameContent[2];
                XLogger.INSTANCE.getAPP().i("getGimbalSetting callback() thumbWheelSpeed = " + GlobalVariable.thumbWheelSpeed);
                if (bean.frameContent.length > 6) {
                    GlobalVariable.sGimbalYawMaxSpeed = bean.frameContent[3];
                    spitchSlowSetting = bean.frameContent[4];
                    yawSlowSetting = bean.frameContent[5];
                    XLogger.INSTANCE.getAPP().i("getGimbalSetting callback() sGimbalYawMaxSpeed = " + GlobalVariable.sGimbalYawMaxSpeed
                            + "; spitchSlowSetting = " + bean.frameContent[4] + "; yawSlowSetting = " + bean.frameContent[5]);
                }
                mHandler.sendEmptyMessageDelayed(GET_GIMBAL_CURRENT_SETTING_SUCCEED, 500);
            } else if (isRetry) {//手动刷新，不管成功失败，都展示
                mHandler.sendEmptyMessageDelayed(GET_GIMBAL_CURRENT_SETTING_SUCCEED, 500);
            } else {
                mHandler.sendEmptyMessageDelayed(GET_GIMBAL_CURRENT_SETTING_FAILED, 500);
            }
        });
    }

    /**
     * 设置云台俯仰缓启停设置
     */
    private void setPitchSlowSetting() {
        XLogger.INSTANCE.getAPP().i("setPitchSlowSetting() spitchSlowSetting = " + spitchSlowSetting);
        if (sb_pitch_slow_setting != null) {
            if (spitchSlowSetting >= 0 && spitchSlowSetting <= 30) {
                sb_pitch_slow_setting.setProgress(spitchSlowSetting);
            } else {
                sb_pitch_slow_setting.setProgress(0);
            }
        }
    }

    /**
     * 设置云台方位缓启停设置
     */
    private void setYawSlowSetting() {
        XLogger.INSTANCE.getAPP().i("setYawSlowSetting() yawSlowSetting = " + yawSlowSetting);
        if (sb_yaw_slow_setting != null) {
            if (yawSlowSetting >= 0 && yawSlowSetting <= 30) {
                sb_yaw_slow_setting.setProgress(yawSlowSetting);
            } else {
                sb_yaw_slow_setting.setProgress(0);
            }
        }
    }

    /**
     * 设置云台方位最大偏航速度
     */
    private void setYawSpeed(){
        XLogger.INSTANCE.getAPP().i("setYawSpeed() sGimbalYawMaxSpeed = " + GlobalVariable.sGimbalYawMaxSpeed);
        if (sb_ptz_yaw_speed != null) {
            if (GlobalVariable.sGimbalYawMaxSpeed >= 5) {
                sb_ptz_yaw_speed.setProgress(GlobalVariable.sGimbalYawMaxSpeed - 5);
            } else {
                sb_ptz_yaw_speed.setProgress(0);
            }
        }
    }

    private void setPTZYawSpeed(int speed) {
        XLogger.INSTANCE.getAPP().i("setPTZYawSpeed() speed = " + speed);
//        GduApplication.getSingleApp().gduCommunication.setThumbWheelSpeed((byte) speed, GlobalVariable.ThumbWheelSpeedType.PTZ_YAW,
//                (code, bean) -> {
//                    XLogger.INSTANCE.getAPP().i("setPTZYawSpeed callBack() code = " + code);
//                    if (mHandler == null) {
//                        return;
//                    }
//                    if (code == GduConfig.OK) {
//                        GlobalVariable.sGimbalYawMaxSpeed = (byte) speed;
//                        //                    handler.obtainMessage(SET_OK).sendToTarget();
//                    } else {
//                        mHandler.sendEmptyMessage(SET_GIMBAL_YAW_SPEED_FAILED);
//                    }
//                });
    }


    private void setSlowSetting(int set, SlowSettingType type) {
        XLogger.INSTANCE.getAPP().i("setSlowSetting() set = " + set + "; type = " + type);
        if (type == null) {
            return;
        }
        byte pitchOffset = (byte) 255;
        byte courseOffset = (byte) 255;
        if (type == SlowSettingType.PITCH) {
            pitchOffset = (byte) set;
        } else if (type == SlowSettingType.PTZ_YAW) {
            courseOffset = (byte) set;
        }

        IGimbal.get().slowOffset(pitchOffset, courseOffset, new MSdkCallback.ActionCallback<EmptyMsg>() {
            @Override
            public void onSuccess(@Nullable EmptyMsg emptyMsg) {
                if (type == SlowSettingType.PITCH) {
                    spitchSlowSetting = (byte) set;
                } else  if (type == SlowSettingType.PTZ_YAW) {
                    yawSlowSetting = (byte) set;
                }
            }
            @Override
            public void onFailure(@NonNull MError mError) {
                mHandler.sendEmptyMessage(type == SlowSettingType.PITCH ? SET_GIMBAL_PITCH_SlOW_SETTING_FAILED : SET_GIMBAL_YAW_SLOW_SETTING_FAILED);
            }
        });
    }

    public void initData() {
        XLogger.INSTANCE.getAPP().i("initData()");
        initPTZSetting(mView);
    }

    public void initListener() {
        XLogger.INSTANCE.getAPP().i("initListener()");
        if (tv_check_clound != null) {
            tv_check_clound.setOnClickListener(this);
        }
        SingleClickUtil.onSingleClick(tv_reset_gimbal, v1 -> {
            if (!checkDroneConnState()) {//恢复云台默认设置
                return;
            }
            GimbalType gimbalType = IGimbal.get().getGimbalType();
            if (gimbalType == GimbalType.ByrdT_None_Zoom) {
                showToast(R.string.Label_NoHolder);
                return;
            }
            resetGimbalParamsConfirmDialog();
        });

    }

    protected void setPitchSpeed(int speed) {
        XLogger.INSTANCE.getAPP().i("setPitchSpeed() speed = " + speed);
        GduSocketManager.getInstance().getGduCommunication().setThumbWheelSpeed((byte) speed, GlobalVariable.ThumbWheelSpeedType.PITCH,
                (code, bean) -> {
                    XLogger.INSTANCE.getAPP().i("setPitchSpeed callBack() code = " + code);
                    if (mHandler == null) {
                        return;
                    }
                    if (code == GduConfig.OK) {
                        GlobalVariable.thumbWheelSpeed = (byte) speed;
                    } else {
                        mHandler.sendEmptyMessage(SET_GIMBAL_PITCH_FAILED);
                    }
                });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_check_clound) {
            if (!checkDroneConnState()) {
                return;
            }
            GimbalType gimbalType = IGimbal.get().getGimbalType();
            if (gimbalType == GimbalType.ByrdT_None_Zoom) {
                showToast(R.string.Label_NoHolder);
                return;
            }
            if (DroneUtils.isGround()) {
                new CommonDialog.Builder(mActivity.getSupportFragmentManager())
                        .setContent(mActivity.getString(R.string.dialog_clound_check_content))
                        .setCancel(mActivity.getString(R.string.Label_cancel))
                        .setSure(mActivity.getString(R.string.start_clound_check))
                        .setCancelableOutside(true)
                        .setPositiveListener((dialog, which) -> {
                            checkGimbal();
                        })
                        .build().show();
            } else {
                showToast(R.string.dialog_clound_check_content);
            }
        }
    }

    private void checkGimbal() {
        if (mActivity != null && mActivity instanceof FlightActivity) {
            ((FlightActivity) mActivity).beginCheckCloud();
            if (closeListener != null) {
                closeListener.onClose();
            }
        }
    }

    /**
     * <P>shang</P>
     * <P>检查无人机连接状态</P>
     */
    public boolean checkDroneConnState() {
        XLogger.INSTANCE.getAPP().i("checkDroneConnState()");
        boolean result = false;
        if (!SdkDemoApplication.getAircraftInstance().isConnected()) {
            showToast(R.string.fly_no_conn);
            result = false;
        } else {
            result = true;
        }
        XLogger.INSTANCE.getAPP().i("checkDroneConnState() result = " + result);
        return result;
    }


    /**
     * @author yuhao
     * <p>
     * 获取录像分辨率和预览流分辨率后显示
     */
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            handleSetMessage(msg);
        }
    };

    public void handleSetMessage(Message msg) {
        XLogger.INSTANCE.getAPP().i("mHandler handleMessage() msgWhat = " + msg.what);
        switch (msg.what) {

            case SET_FAILE:
                showToast(R.string.Label_SettingFail);
                break;
            case GET_GIMBAL_CURRENT_SETTING_SUCCEED:
            case GET_GIMBAL_CURRENT_SETTING_FAILED:
                setPitchSpeed();
                setYawSpeed();
                setPitchSlowSetting();
                setYawSlowSetting();
                break;
            case SET_GIMBAL_PITCH_FAILED:
                showToast(R.string.Label_SettingFail);
                setPitchSpeed();
                break;
            case SET_GIMBAL_YAW_SPEED_FAILED:
                showToast(R.string.Label_SettingFail);
                setYawSpeed();
                break;
            case SET_GIMBAL_PITCH_SlOW_SETTING_FAILED:
                showToast(R.string.Label_SettingFail);
                setPitchSlowSetting();
                break;
            case SET_GIMBAL_YAW_SLOW_SETTING_FAILED:
                showToast(R.string.Label_SettingFail);
                setYawSlowSetting();
                break;
            case RESET_GIMBAL_PARAMS_SUC:
                getGimbalCurrentSetting(false);
//                LoadingDialogUtils.cancelLoadingDialog();
                showToast(R.string.string_set_success);
                break;
            case RESET_GIMBAL_PARAMS_FAILED:
//                LoadingDialogUtils.cancelLoadingDialog();
                showToast(R.string.Label_SettingFail);
                break;

            default:
                break;
        }
    }


    /**
     * 检查航迹是否执行,航迹执行中禁止
     */
    public boolean checkWaypointTaskRunning() {
        XLogger.INSTANCE.getAPP().i("checkWaypointTaskRunning()");
        boolean result;
        if (DroneUtils.isOpenFlightRoutePlan()) {
            showToast(R.string.please_exit_flight_route);
            result = true;
        } else {
            result = false;
        }
        XLogger.INSTANCE.getAPP().i("checkWaypointTaskRunning() result = " + result);
        return result;
    }



    @Override
    public void onDestory() {
        super.onDestory();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        et_pitch_speed.setOnEditorActionListener(null);
        et_ptz_yaw_speed.setOnEditorActionListener(null);
        et_pitch_slow_setting.setOnEditorActionListener(null);
        et_yaw_slow_setting.setOnEditorActionListener(null);

        sb_pitch_speed.setOnSeekBarChangeListener(null);
        sb_ptz_yaw_speed.setOnSeekBarChangeListener(null);
        sb_pitch_slow_setting.setOnSeekBarChangeListener(null);
        sb_yaw_slow_setting.setOnSeekBarChangeListener(null);

        closeListener = null;
        txVideoLiveListener = null;
        mSeekBarPitchSlowSettingListener = null;
        mSeekBarYawSlowSettingListener = null;
        mSeekBarFourLightListener = null;
        mSeekBarPTZYawListener = null;


        sb_pitch_speed = null;
        et_pitch_speed = null;
        et_ptz_yaw_speed = null;

        sb_pitch_slow_setting = null;
        et_pitch_slow_setting = null;
        sb_yaw_slow_setting = null;
        et_yaw_slow_setting = null;
        sb_ptz_yaw_speed = null;
        mCameraMainLayout = null;
        tv_check_clound = null;
        rl_check_clound = null;
        tv_reset_gimbal = null;
    }




    public CloseListener closeListener;

    public interface  CloseListener{
        void onClose();
    }

    public void setCloseListener(CloseListener listener) {
        this.closeListener = listener;
    }

    @Override
    public void connGimbalListener(GimbalEvent event) {
        if (event.gimbalType != GimbalType.ByrdT_None_Zoom || GlobalVariable.sPSDKCompId != 0) {//有云台
            initPTZSetting(mView);
        }else{//无云台
            ViewUtils.setViewShowOrHide(mCameraMainLayout, false);
        }
    }


    //恢复云台默认设置
    private void resetGimbalParams() {
        GduSocketManager.getInstance().getGduCommunication().resetGimbalParams(new SocketCallBack3() {
            @Override
            public void callBack(int code, GduFrame3 bean) {
                if (mHandler == null) {
                    return;
                }
                XLogger.INSTANCE.getAPP().i(TAG, "resetGimbalParams() code:" + code);
                if (code == GduConfig.OK) {
                    mHandler.sendEmptyMessage(RESET_GIMBAL_PARAMS_SUC);
                } else {
                    mHandler.sendEmptyMessage(RESET_GIMBAL_PARAMS_FAILED);
                }
            }
        });
    }

    private void resetGimbalParamsConfirmDialog() {
        new CommonDialog.Builder(mActivity.getSupportFragmentManager())
                .setTitle(mActivity.getString(R.string.string_gimbal_reset_params_label))
                .setContent(mActivity.getString(R.string.string_gimbal_reset_params_dialog_content))
                .setCancel(mActivity.getString(R.string.Label_cancel))
                .setSure(mActivity.getString(R.string.start_clound_check))
                .setCancelableOutside(true)
                .setPositiveListener((dialog, which) -> {
                    resetGimbalParams();
                })
                .build().show();

    }

    /************ron******************
     * 缓启停类型
     * PITCH 俯仰
     * PTZ_YAW   偏航速度
     *********************************/
    public enum SlowSettingType {
        /**
         * 缓启停类型_俯仰
         */
        PITCH(0),
        /**
         * 缓启停类型_方向
         */
        PTZ_YAW(1);

        public int index;

        SlowSettingType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }
}