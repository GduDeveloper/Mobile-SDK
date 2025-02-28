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
import androidx.fragment.app.FragmentActivity;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.FlightActivity;
import com.gdu.demo.R;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.drone.GimbalType;
import com.gdu.event.GimbalEvent;
import com.gdu.socket.GduFrame3;
import com.gdu.socket.GduSocketManager;
import com.gdu.socket.SocketCallBack3;
import com.gdu.util.DroneUtil;
import com.gdu.util.NumberUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.ViewUtils;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;
import com.lib.model.LiveType;

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
        MyLogUtils.d("initView()   ");
        mCameraMainLayout = mView.findViewById(R.id.camera_main_layout);

        tv_check_clound = mView.findViewById(R.id.tv_check_clound);
        rl_check_clound = mView.findViewById(R.id.rl_check_clound);
        //校漂
        ViewUtils.setViewShowOrHide(rl_check_clound, true);

        //恢复云台默认设置
        tv_reset_gimbal = mView.findViewById(R.id.tv_reset_gimbal);
        View mViewGimbalPositionGroup = mView.findViewById(R.id.viewGimbalPositionGroup);
        if (DroneUtil.unSupportGimbalYaw()) {
            ViewUtils.setViewShowOrHide(mViewGimbalPositionGroup, false);
        } else {
            LinearLayout llGimbalPitchStartAndStop = mView.findViewById(R.id.llGimbalPitchStartAndStop);
            ViewUtils.setViewShowOrHide(llGimbalPitchStartAndStop, !GlobalVariable.getMainGimbalSupportFun().disablePitchStartAndStop);

            LinearLayout llGimbalPositionStartAndStop = mView.findViewById(R.id.llGimbalPositionStartAndStop);
            ViewUtils.setViewShowOrHide(llGimbalPositionStartAndStop, !GlobalVariable.getMainGimbalSupportFun().disablePositionStartAndStop);
        }
    }

    public void initCameraParams() {
        MyLogUtils.d("initCameraParams()");
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
            setSlowSetting(valueInt, GlobalVariable.SlowSettingType.PITCH);
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
            setSlowSetting(valueInt, GlobalVariable.SlowSettingType.PTZ_YAW);
            return false;
        });

        mSeekBarFourLightListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MyLogUtils.d("mSeekBarListener onProgressChanged() progress = " + progress);
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
                MyLogUtils.d("mSeekBarPTZYawListener onProgressChanged() progress = " + progress);
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
                MyLogUtils.d("mSeekBarPitchSlowSettingListener onProgressChanged() progress = " + progress);
                et_pitch_slow_setting.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSlowSetting(seekBar.getProgress(), GlobalVariable.SlowSettingType.PITCH);
            }
        };

        mSeekBarYawSlowSettingListener = new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MyLogUtils.d("mSeekBarYawSlowSettingListener onProgressChanged() progress = " + progress);
                et_yaw_slow_setting.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSlowSetting(seekBar.getProgress(), GlobalVariable.SlowSettingType.PTZ_YAW);
            }
        };

        sb_pitch_speed.setEnabled(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess);
        sb_ptz_yaw_speed.setEnabled(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess);
        sb_pitch_slow_setting.setEnabled(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess);
        sb_yaw_slow_setting.setEnabled(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess);

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
        MyLogUtils.i("setPitchSpeed() thumbWheelSpeed = " + GlobalVariable.thumbWheelSpeed);
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
            AppLog.i(TAG, "getGimbalSetting callback() code = " + code);
            if (mHandler == null) {
                return;
            }
            boolean isHaveData = code == GduConfig.OK && bean != null && bean.frameContent != null && bean.frameContent.length > 3;
            MyLogUtils.d("getGimbalSetting callback() isHaveData = " + isHaveData);
            if (isHaveData) {
                GlobalVariable.thumbWheelSpeed = bean.frameContent[2];
                MyLogUtils.d("getGimbalSetting callback() thumbWheelSpeed = " + GlobalVariable.thumbWheelSpeed);
                if (bean.frameContent.length > 6) {
                    GlobalVariable.sGimbalYawMaxSpeed = bean.frameContent[3];
                    GlobalVariable.spitchSlowSetting = bean.frameContent[4];
                    GlobalVariable.yawSlowSetting = bean.frameContent[5];
                    MyLogUtils.d("getGimbalSetting callback() sGimbalYawMaxSpeed = " + GlobalVariable.sGimbalYawMaxSpeed
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
        MyLogUtils.i("setPitchSlowSetting() spitchSlowSetting = " + GlobalVariable.spitchSlowSetting);
        if (sb_pitch_slow_setting != null) {
            if (GlobalVariable.spitchSlowSetting >= 0 && GlobalVariable.spitchSlowSetting <= 30) {
                sb_pitch_slow_setting.setProgress(GlobalVariable.spitchSlowSetting);
            } else {
                sb_pitch_slow_setting.setProgress(0);
            }
        }
    }

    /**
     * 设置云台方位缓启停设置
     */
    private void setYawSlowSetting() {
        MyLogUtils.i("setYawSlowSetting() yawSlowSetting = " + GlobalVariable.yawSlowSetting);
        if (sb_yaw_slow_setting != null) {
            if (GlobalVariable.yawSlowSetting >= 0 && GlobalVariable.yawSlowSetting <= 30) {
                sb_yaw_slow_setting.setProgress(GlobalVariable.yawSlowSetting);
            } else {
                sb_yaw_slow_setting.setProgress(0);
            }
        }
    }

    /**
     * 设置云台方位最大偏航速度
     */
    private void setYawSpeed(){
        MyLogUtils.i("setYawSpeed() sGimbalYawMaxSpeed = " + GlobalVariable.sGimbalYawMaxSpeed);
        if (sb_ptz_yaw_speed != null) {
            if (GlobalVariable.sGimbalYawMaxSpeed >= 5) {
                sb_ptz_yaw_speed.setProgress(GlobalVariable.sGimbalYawMaxSpeed - 5);
            } else {
                sb_ptz_yaw_speed.setProgress(0);
            }
        }
    }

    private void setPTZYawSpeed(int speed) {
        MyLogUtils.d("setPTZYawSpeed() speed = " + speed);
//        GduApplication.getSingleApp().gduCommunication.setThumbWheelSpeed((byte) speed, GlobalVariable.ThumbWheelSpeedType.PTZ_YAW,
//                (code, bean) -> {
//                    MyLogUtils.d("setPTZYawSpeed callBack() code = " + code);
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



    private void setSlowSetting(int set, GlobalVariable.SlowSettingType type) {
        MyLogUtils.d("setSlowSetting() set = " + set + "; type = " + type);
        if (type == null) {
            return;
        }
        GduSocketManager.getInstance().getGduCommunication().setSlowSetting((byte) set, type,
                (code, bean) -> {
                    MyLogUtils.d("setSlowSetting callBack() code = " + code);
                    if (mHandler == null) {
                        return;
                    }
                    if (code == GduConfig.OK) {
                        if (type == GlobalVariable.SlowSettingType.PITCH) {
                            GlobalVariable.spitchSlowSetting = (byte) set;
                        } else  if (type == GlobalVariable.SlowSettingType.PTZ_YAW) {
                            GlobalVariable.yawSlowSetting = (byte) set;
                        }
                    } else {
                        mHandler.sendEmptyMessage(type == GlobalVariable.SlowSettingType.PITCH ? SET_GIMBAL_PITCH_SlOW_SETTING_FAILED : SET_GIMBAL_YAW_SLOW_SETTING_FAILED);
                    }
                });
    }

    public void initData() {
        MyLogUtils.d("initData()");
        initPTZSetting(mView);
    }

    public void initListener() {
        MyLogUtils.d("initListener()");
        if (tv_check_clound != null) {
            tv_check_clound.setOnClickListener(this);
        }
        SingleClickUtil.onSingleClick(tv_reset_gimbal, v1 -> {
            if (!checkDroneConnState()) {//恢复云台默认设置
                return;
            }
            if (GlobalVariable.gimbalType == GimbalType.ByrdT_None_Zoom) {
                showToast(R.string.Label_NoHolder);
                return;
            }
            resetGimbalParamsConfirmDialog();
        });

    }

    protected void setPitchSpeed(int speed) {
        MyLogUtils.d("setPitchSpeed() speed = " + speed);
        GduSocketManager.getInstance().getGduCommunication().setThumbWheelSpeed((byte) speed, GlobalVariable.ThumbWheelSpeedType.PITCH,
                (code, bean) -> {
                    MyLogUtils.d("setPitchSpeed callBack() code = " + code);
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
            if (GlobalVariable.gimbalType == GimbalType.ByrdT_None_Zoom) {
                showToast(R.string.Label_NoHolder);
                return;
            }
            if (GlobalVariable.droneFlyState == 1) {
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
        MyLogUtils.d("checkDroneConnState()");
        boolean result = false;
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                showToast(R.string.fly_no_conn);
                result = false;
            case Conn_MoreOne:
                showToast(R.string.Label_ConnMore);
                result = false;
            case Conn_Sucess:
                result = true;
            default:
                break;
        }
        MyLogUtils.d("checkDroneConnState() result = " + result);
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
        MyLogUtils.d("mHandler handleMessage() msgWhat = " + msg.what);
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
        MyLogUtils.d("checkWaypointTaskRunning()");
        boolean result;
        if (GlobalVariable.isOpenFlightRoutePlan) {
            showToast(R.string.please_exit_flight_route);
            result = true;
        } else {
            result = false;
        }
        MyLogUtils.d("checkWaypointTaskRunning() result = " + result);
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
                AppLog.i(TAG, "resetGimbalParams() code:" + code);
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
}