package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gdu.AlgorithmMark;
import com.gdu.common.error.GDUError;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.databinding.FragmentSettingVisionBinding;
import com.gdu.demo.flight.base.VisionSensingBean;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.drone.PlanType;
import com.gdu.drone.SwitchType;
import com.gdu.healthmanager.FlightHealthStatusDetailBean;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.flightcontroller.flightassistant.FillLightMode;
import com.gdu.sdk.flightcontroller.flightassistant.FlightAssistant;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.socket.GduSocketManager;
import com.gdu.socket.GduUDPSocket3;
import com.gdu.socket.SocketCallBack3;
import com.gdu.util.DroneUtil;
import com.gdu.util.FormatConfig;
import com.gdu.util.NumberUtils;
import com.gdu.util.SPUtils;
import com.gdu.util.StringUtils;
import com.gdu.util.ViewUtils;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Action;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingVisionFragment extends Fragment {

    private FragmentSettingVisionBinding mVisionBinding;
    private FlightAssistant mFlightAssistant;

    private int setFlyType;
    private boolean curSwitch_vision_obstacle;
    private boolean pre_switch_vision_obstacle;

    private boolean switch_vision_obstacle_return;
    private boolean switch_vision_obstacle_back;
    private boolean switch_vision_obstacle_strategy;
    private boolean pre_switch_vision_obstacle_return;
    private boolean pre_switch_vision_obstacle_back;
    private boolean pre_switch_vision_obstacle_strategy;
    private final int SWITCH_SUCCESS = 0;
    private final int SWITCH_FAILED = 1;
    /** 视觉避障开 */
    private final int SWITCH_VISION_ON = 1;
    /** 视觉避障关 */
    private final int SWITCH_VISION_OFF = 2;
    /** 返航避障开 */
    private final int SWITCH_VISION_RETURN_ON = 3;
    /** 返航避障关 */
    private final int SWITCH_VISION_RETURN_OFF = 4;
    /** 后退避障开 */
    private final int SWITCH_VISION_BACK_ON = 5;
    /** 后退避障关 */
    private final int SWITCH_VISION_BACK_OFF = 6;
    /** 运动模式是否已经发生变化 */
    private final int SPORTMODEHADCHANGDE = 7;
    /** 避障策略开 */
    private final int SWITCH_OBSTACLE_STRATEGY_ON = 8;
    /** 避障策略关 */
    private final int SWITCH_OBSTACLE_STRATEGY_OFF = 9;
    /** 获取避障状态成功 */
    private final int GOT_OBSTACLE_SUCCEED = 10;
    /** 预加载视觉感知 */
    private final int PRE_LOAD_OBSTACLE = 16;

    /** 避障策略(绕障)开 */
    private final int SWITCH_OBSTACLE_STRATEGY_AROUND = 17;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mVisionBinding = FragmentSettingVisionBinding.inflate(inflater, container, false);
        return mVisionBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initData();
    }

    private void initView() {

        mFlightAssistant = SdkDemoApplication.getAircraftInstance().getFlightController().getFlightAssistant();
        initSwitchBtn();
        pre_switch_vision_obstacle = AlgorithmMark.getSingleton().ObStacle;
        mVisionBinding.ivFillInLight.setSelected(GlobalVariable.sFillInLightOpen == 1);


        mVisionBinding.tvLandProtectHeightTip.setText(getString(R.string.Msg_LandProtectHeightTip1));
        mVisionBinding.tvLandProtectTip.setText(getString(R.string.Msg_LandProtectTip1));
        mVisionBinding.sbLandProtectHeight.setMax(100);

        setListener();

        if (CommonUtils.isSmallFlight(GlobalVariable.planType)) {
            mVisionBinding.clLandProtectSwitch.setVisibility(View.VISIBLE);
        }
    }

    private void initData() {
        preLoadData();
        mFlightAssistant.getLandingProtectionEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean open) {
                uiThreadHandle(() -> {
                    Log.d("Vision", "getLanding   open = " + open);
                    mVisionBinding.ivLandProtectSwitch.setSelected(open);
                    mVisionBinding.tvLandProtectTip.setVisibility(open ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onFailure(GDUError gduError) {

                Log.d("Vision", "getLanding   gduError = " + gduError.getDescription());

            }
        });

        mFlightAssistant.getRTHObstacleAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean open) {
                uiThreadHandle(() -> {
                    mVisionBinding.ivGoHomeObstacleSwitch.setSelected(open);
                });
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    private void preLoadData(){
        Message preMessage = new Message();
        preMessage.what = PRE_LOAD_OBSTACLE;
        preMessage.arg1 = GlobalVariable.obstacleIsOpen ? 0 : 1;
        preMessage.arg2 = GlobalVariable.obstacleStrategyIsOpen ? 0 : 1;
        mHandler.sendMessage(preMessage);
    }

    private void setListener() {
        mVisionBinding.ivSwitchVisionObstacle.setOnClickListener(mOnClickListener);
        mVisionBinding.ivSwitchVisionObstacleStrategy.setOnClickListener(mOnClickListener);
        mVisionBinding.ivFillInLight.setOnClickListener(mOnClickListener);
        mVisionBinding.ivGoHomeObstacleSwitch.setOnClickListener(mOnClickListener);

        mVisionBinding.sbLandProtectHeight.setOnSeekBarChangeListener(landProtectSbChangeListener);
        mVisionBinding.etLandProtectHeightInput.setOnFocusChangeListener(landProtectFocusChagneListener);
        mVisionBinding.etLandProtectHeightInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            mVisionBinding.etLandProtectHeightInput.clearFocus();
            return false;
        });
        mVisionBinding.ivLandProtectSwitch.setOnClickListener(v -> {
            boolean open = !mVisionBinding.ivLandProtectSwitch.isSelected();
            mFlightAssistant.setLandingProtectionEnabled(open, error ->
                    uiThreadHandle(() -> {
                        if (error == null) {
                            mVisionBinding.ivLandProtectSwitch.setSelected(open);
                            mVisionBinding.tvLandProtectTip.setVisibility(open ? View.VISIBLE : View.GONE);
                            Toast.makeText(requireContext(), "设置成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "设置失败", Toast.LENGTH_SHORT).show();
                        }
            }));
        });
    }

    private View.OnFocusChangeListener landProtectFocusChagneListener = (v, hasFocus) -> {
        if (!hasFocus) {
            String inputStr = mVisionBinding.etLandProtectHeightInput.getText().toString().trim();
            boolean isErrInput = StringUtils.isEmptyString(inputStr) || !NumberUtils.isNumeric(inputStr);
            if (isErrInput) {
                Toast.makeText(requireContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                return;
            }
            float height = Float.parseFloat(inputStr);
            if (height < 1 || height > 2) {
                Toast.makeText(requireContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                return;
            }
            int progress = (int) (height * 100 - 100);
            mVisionBinding.sbLandProtectHeight.setProgress(progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener landProtectSbChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            mVisionBinding.etLandProtectHeightInput.setText(FormatConfig.format_4.format((progress + 100) / 100f));
            if (DroneUtil.isSmallFlight()) {
                String inputStr = mVisionBinding.etLandProtectHeightInput.getText().toString().trim();
                boolean isErrInput = StringUtils.isEmptyString(inputStr) || !NumberUtils.isNumeric(inputStr);
                if (isErrInput) {
                    Toast.makeText(requireContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    return;
                }
                float height = Float.parseFloat(inputStr) * 100;
                MyLogUtils.i("switchLandingProtectNew() height = " + height);
            } else {
            }
        }
    };

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_switch_vision_obstacle:
                    if (!connStateToast()) {//视觉避障  打开避障时
                        return;
                    }

                    // 视觉避障异常时无法开启
                    if (!mVisionBinding.ivSwitchVisionObstacle.isSelected()
                            && !CommonUtils.isEmptyList(CommonUtils.allowOpenObstacle(requireContext()))) {
                        String errStr = getVisionObstacleErrContent(CommonUtils.allowOpenObstacle(requireContext()));
                        Toast.makeText(requireContext(), "视觉避障异常 无法开启", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (GlobalVariable.flyMode == 0 && !mVisionBinding.ivSwitchVisionObstacle.isSelected()) {
                        Toast.makeText(requireContext(), R.string.Label_AttitudeModel_obstaticIsOff, Toast.LENGTH_SHORT).show();
                        return;
                    } else if (GlobalVariable.flyMode == 1 && GlobalVariable.DroneFlyMode == 0
                            && !mVisionBinding.ivSwitchVisionObstacle.isSelected()) {
                        Toast.makeText(requireContext(), R.string.Label_SportModel_obstaticIsOff, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mVisionBinding.ivSwitchVisionObstacle.isSelected()) {
                        new CommonDialog.Builder(getChildFragmentManager())
                                .setTitle(getString(R.string.close_vision_title))
                                .setContent( getString(R.string.close_vision_content))
                                .setCancel(getString(R.string.Label_cancel))
                                .setSure(getString(R.string.Label_Sure))
                                .setCancelableOutside(false)
                                .setPositiveListener((dialogInterface, i) -> {
                                    switchVisionObstacle(false, SwitchType.OBSTACLE_TYPE_MAIN);
                                    curSwitch_vision_obstacle = false;
                                    mVisionBinding.ivSwitchVisionObstacle.setSelected(false);
                                    changeObserveTipVisibility(false);
                                }).build().show();
                    } else {
                        switchVisionObstacle(true, SwitchType.OBSTACLE_TYPE_MAIN);
                        curSwitch_vision_obstacle = true;
                        mVisionBinding.ivSwitchVisionObstacle.setSelected(true);
                        changeObserveTipVisibility(true);
                    }
                    break;
                case R.id.iv_switch_vision_obstacle_strategy:
                    if (!connStateToast()) {//避障策略
                        return;
                    }

                    if (mVisionBinding.ivSwitchVisionObstacleStrategy.isSelected()) {
                        new CommonDialog.Builder(getChildFragmentManager())
                                .setContent( getString(R.string.string_close_obstacle_strategy_tips))
                                .setCancel(getString(R.string.Label_cancel))
                                .setSure(getString(R.string.Label_Sure))
                                .setCancelableOutside(false)
                                .setPositiveListener((dialogInterface, i) -> {
                                    switchObstacleStrategy(false);
                                }).build().show();
                    } else {
                        switchObstacleStrategy(true);
                    }
                    break;
                case R.id.iv_fill_in_light:
                    // 补光灯
                    if (!connStateToast()) {
                        return;
                    }
                    if (mVisionBinding.ivFillInLight.isSelected()) {

                        switchFillInLight(false);
                    } else {
                        switchFillInLight(true);
                    }
                    break;

                case R.id.iv_goHomeObstacleSwitch:
                    // 返航避障
                    if (!connStateToast()) {
                        return;
                    }
                    switchGoHomeObstacle(!mVisionBinding.ivGoHomeObstacleSwitch.isSelected());
                    break;
                default:
                    break;
            }
        }
    };

    private String getVisionObstacleErrContent(List<FlightHealthStatusDetailBean> errList) {
        StringBuilder error = new StringBuilder();
        for (int i = 0; i < errList.size(); i++) {
            error.append(errList.get(i).getWarStr());
            if (i != errList.size() - 1) {
                error.append("; ");
            }
        }
        return error.toString();
    }


    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SWITCH_SUCCESS:
                    if (setFlyType == SWITCH_VISION_ON) {
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_MAIN);
                        changeVisibilityObstacleView();
                        changeObserveTipVisibility(true);
                    } else if (setFlyType == SWITCH_VISION_OFF) {
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_MAIN);
                        changeVisibilityObstacleView();
                        changeObserveTipVisibility(false);
                    } else if (setFlyType == SWITCH_VISION_RETURN_ON) {
                        GlobalVariable.obstacleReturnIsOpen = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_RETURN);
                    } else if (setFlyType == SWITCH_VISION_RETURN_OFF) {
                        GlobalVariable.obstacleReturnIsOpen = false;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_RETURN);
                    } else if (setFlyType == SWITCH_VISION_BACK_ON) {
                        GlobalVariable.obstacleBackIsOpen = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_BACK);
                    } else if (setFlyType == SWITCH_VISION_BACK_OFF) {
                        GlobalVariable.obstacleBackIsOpen = false;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_BACK);
                    } else if (setFlyType == SWITCH_OBSTACLE_STRATEGY_ON) {
                        GlobalVariable.obstacleStrategyIsOpen = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY);
                        changeVisibilityObstacleView();
                    } else if (setFlyType == SWITCH_OBSTACLE_STRATEGY_OFF) {
                        GlobalVariable.obstacleStrategyIsOpen = false;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY);
                        changeVisibilityObstacleView();
                    } else if (setFlyType == SWITCH_OBSTACLE_STRATEGY_AROUND) {
                        GlobalVariable.obstacleStrategyIsOpen = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY);
                        changeVisibilityObstacleView();
                    }
                    break;

                case SWITCH_FAILED:
                    if (setFlyType == SWITCH_VISION_ON) {
                        GlobalVariable.algorithmType = AlgorithmMark.AlgorithmType.NONE;
                        changeSwitchStateFailed(SwitchType.OBSTACLE_TYPE_MAIN);
                    } else if (setFlyType == SWITCH_VISION_OFF) {
                        changeSwitchStateFailed(SwitchType.OBSTACLE_TYPE_MAIN);
                    } else if (setFlyType == SWITCH_VISION_RETURN_ON || setFlyType == SWITCH_VISION_RETURN_OFF) {
                        changeSwitchStateFailed(SwitchType.OBSTACLE_TYPE_RETURN);
                    } else if (setFlyType == SWITCH_VISION_BACK_ON || setFlyType == SWITCH_VISION_BACK_OFF) {
                        changeSwitchStateFailed(SwitchType.OBSTACLE_TYPE_BACK);
                    } else if (setFlyType == SWITCH_OBSTACLE_STRATEGY_ON || setFlyType == SWITCH_OBSTACLE_STRATEGY_OFF) {
                        changeSwitchStateFailed(SwitchType.OBSTACLE_TYPE_STRATEGY);
                    }
                    break;
                case PRE_LOAD_OBSTACLE:
                case GOT_OBSTACLE_SUCCEED:
                    int arg1 = msg.arg1;
                    // 避障开启
                    if (arg1 == 0) {
                        curSwitch_vision_obstacle = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_MAIN);
                    } else {
                        curSwitch_vision_obstacle = false;
                    }

                    mVisionBinding.ivSwitchVisionObstacle.setSelected(curSwitch_vision_obstacle);
                    changeObserveTipVisibility(curSwitch_vision_obstacle);
                    int arg2 = msg.arg2;
                    // 避障策略开启
                    if (arg2 == 0) {
                        switch_vision_obstacle_strategy = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY);
                    } else if (arg2 == 1) {
                        switch_vision_obstacle_strategy = false;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY);
                    } else if (arg2 == 2) {
                        switch_vision_obstacle_strategy = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY);
                    }
                    changeVisibilityObstacleView();

                    AlgorithmMark.getSingleton().ObStacle = switch_vision_obstacle_strategy;
                    mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(switch_vision_obstacle_strategy);

                    break;

                default:
                    break;
            }
        }
    };

    private void changeObserveTipVisibility(boolean visible) {
        ViewUtils.setViewShowOrHide(mVisionBinding.llObserveHint, visible);
    }

    /**
     * 避障策略显示隐藏
     * isShowObstacle 避障是否打开
     * isShowObstacleStrategy 避障策略是否打开
     */
    private void changeVisibilityObstacleView() {
        if (curSwitch_vision_obstacle) {
            mVisionBinding.rlObstacle.setVisibility(View.VISIBLE);
            if (switch_vision_obstacle_strategy) {
                mVisionBinding.viewObstacleSetting.setVisibility(View.VISIBLE);
                mVisionBinding.divObstacleSetting.setVisibility(View.VISIBLE);
                mVisionBinding.divObstacleStrategy.setVisibility(View.VISIBLE);
            } else {
                mVisionBinding.viewObstacleSetting.setVisibility(View.GONE);
                mVisionBinding.divObstacleSetting.setVisibility(View.GONE);
                mVisionBinding.divObstacleStrategy.setVisibility(View.GONE);
            }
        } else {
            mVisionBinding.rlObstacle.setVisibility(View.GONE);
            mVisionBinding.viewObstacleSetting.setVisibility(View.GONE);
            mVisionBinding.divObstacleSetting.setVisibility(View.GONE);
            mVisionBinding.divObstacleStrategy.setVisibility(View.GONE);
        }

    }

    private void changeSwitchStateSuccess(SwitchType type) {
        MyLogUtils.i("changeSwitchStateSuccess() type = " + type);
        switch (type) {
            case OBSTACLE_TYPE_MAIN:
                pre_switch_vision_obstacle = curSwitch_vision_obstacle;
                mVisionBinding.ivSwitchVisionObstacle.setSelected(curSwitch_vision_obstacle);
                changeObserveTipVisibility(curSwitch_vision_obstacle);
                //视觉避障功能关闭后再次开启，雷达图选项默认开启; 视觉避障功能处于关闭状态时，显示雷达图选项置灰；
                GlobalVariable.obstacleIsOpen = curSwitch_vision_obstacle;
                break;
            case OBSTACLE_TYPE_STRATEGY:
                pre_switch_vision_obstacle_strategy = switch_vision_obstacle_strategy;
                AlgorithmMark.getSingleton().ObStacle = switch_vision_obstacle_strategy;
                mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(switch_vision_obstacle_strategy);
                break;

            default:
                break;
        }
    }

    private void changeSwitchStateFailed(SwitchType type) {
        MyLogUtils.i("changeSwitchStateFailed() type = " + type);
        switch (type) {
            case OBSTACLE_TYPE_MAIN:
                mVisionBinding.ivSwitchVisionObstacle.setSelected(pre_switch_vision_obstacle);
                changeObserveTipVisibility(pre_switch_vision_obstacle);
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                }
                break;
            case OBSTACLE_TYPE_STRATEGY:
                AlgorithmMark.getSingleton().ObStacle = pre_switch_vision_obstacle_strategy;
                mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(pre_switch_vision_obstacle_strategy);
                if (isAdded()) {
                    Toast.makeText(requireContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    /**
     * 避障策略开启关闭
     */
    public void switchObstacleStrategy(boolean open) {
        mFlightAssistant.setObstacleAvoidanceStrategyEnabled(open, error ->
                uiThreadHandle(() -> {
                    if (error == null) {
                        switch_vision_obstacle_strategy = open;
                        mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(open);
                        changeVisibilityObstacleView();
                        Toast.makeText(requireContext(), "设置成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "设置失败", Toast.LENGTH_SHORT).show();
                    }
        }));

    }

    private void switchGoHomeObstacle(boolean isOpen) {
        MyLogUtils.i("switchGoHomeObstacle() isOpen = " + isOpen);

        mFlightAssistant.setRTHObstacleAvoidanceEnabled(isOpen, error -> {
            uiThreadHandle(() -> {
                if (error == null) {
                    GlobalVariable.obstacleReturnIsOpen = !mVisionBinding.ivGoHomeObstacleSwitch.isSelected();
                    mVisionBinding.ivGoHomeObstacleSwitch.setSelected(isOpen);
                    Toast.makeText(requireContext(), "设置成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "设置失败", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public void switchFillInLight(boolean open) {
        mFlightAssistant.setDownwardFillLightMode(open ? FillLightMode.ON : FillLightMode.OFF, error -> {
            uiThreadHandle(() ->{
                if (error == null) {
                    mVisionBinding.ivFillInLight.setSelected(open);
                    Toast.makeText(requireContext(), "设置成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "设置失败", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    public void switchVisionObstacle(boolean isOn, SwitchType type) {
        MyLogUtils.i("switchVisionObstacle() isOn = " + isOn + "; type = " + type);
        switch (type) {
            case OBSTACLE_TYPE_MAIN:
                setFlyType = isOn ? SWITCH_VISION_ON : SWITCH_VISION_OFF;
                break;

            case OBSTACLE_TYPE_RETURN:
                setFlyType = isOn ? SWITCH_VISION_RETURN_ON : SWITCH_VISION_RETURN_OFF;
                break;

            case OBSTACLE_TYPE_BACK:
                setFlyType = isOn ? SWITCH_VISION_BACK_ON : SWITCH_VISION_BACK_OFF;
                break;

            default:
                break;
        }

        mFlightAssistant.setVisionSensingEnabled(isOn, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    mHandler.sendEmptyMessage(SWITCH_SUCCESS);
                } else {
                    mHandler.sendEmptyMessage(SWITCH_FAILED);
                }
            }
        });
    }

    private boolean connStateToast() {
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                Toast.makeText(requireContext(), R.string.fly_no_conn, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_MoreOne:
                Toast.makeText(requireContext(), R.string.Label_ConnMore, Toast.LENGTH_SHORT).show();
                return false;
            case Conn_Sucess:
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initSwitchBtn() {
        //当视觉避障关闭时进入该界面雷达图关闭    余浩
        mVisionBinding.ivSwitchVisionObstacle.setSelected(AlgorithmMark.getSingleton().ObStacle && GlobalVariable.DroneFlyMode != 0);
        changeObserveTipVisibility(mVisionBinding.ivSwitchVisionObstacle.isSelected());
    }


    public static SettingVisionFragment newInstance() {
        Bundle args = new Bundle();
        SettingVisionFragment fragment = new SettingVisionFragment();
        fragment.setArguments(args);
        return fragment;
    }



    public void uiThreadHandle(Action action) {
        MyLogUtils.i("uiThreadHandle() isAdded = " + isAdded());
        if (!isAdded()) {
            return;
        }
        Observable.empty().to(RxLife.toMain(requireActivity())).subscribe(o -> {},
                throwable -> MyLogUtils.e("UI线程处理失败", throwable), action);
    }
}
