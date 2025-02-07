package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingVisionBinding;
import com.gdu.drone.PlanType;
import com.gdu.drone.SwitchType;
import com.gdu.healthmanager.FlightHealthStatusDetailBean;
import com.gdu.sdk.util.CommonUtils;
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
    /**切换补光灯成功 */
    private final int SWITCH_FILL_IN_LIGHT_SUCCEED = 11;
    /**切换补光灯失败 */
    private final int SWITCH_FILL_IN_LIGHT_FAILED = 12;
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
        initSwitchBtn();
        pre_switch_vision_obstacle = AlgorithmMark.getSingleton().ObStacle;
        mVisionBinding.ivFillInLight.setSelected(GlobalVariable.sFillInLightOpen == 1);

        mVisionBinding.ivTopTof.setSelected(GlobalVariable.topTof == 1);

        if (UavStaticVar.isOpenTextEnvironment) {
            mVisionBinding.layoutVisualFusion.setVisibility(View.VISIBLE);
            mVisionBinding.rlObstacleTest.setVisibility(View.VISIBLE);
        } else {
            mVisionBinding.layoutVisualFusion.setVisibility(View.GONE);
            mVisionBinding.rlObstacleTest.setVisibility(View.GONE);
        }
        mVisionBinding.ivVisualFusionPosition.setSelected(GlobalVariable.visionHelpLocateState == 0);
        mVisionBinding.incFrontBinocularSwitch.tvLabel.setText(getString(R.string.Label_frontBinocularSavePicSwitch));
        mVisionBinding.incBackBinocularSwitch.tvLabel.setText(getString(R.string.Label_backBinocularSavePicSwitch));
        mVisionBinding.incLeftBinocularSwitch.tvLabel.setText(getString(R.string.Label_leftBinocularSavePicSwitch));
        mVisionBinding.incRightBinocularSwitch.tvLabel.setText(getString(R.string.Label_rightBinocularSavePicSwitch));
        mVisionBinding.incUpBinocularSwitch.tvLabel.setText(getString(R.string.Label_topBinocularSavePicSwitch));
        mVisionBinding.incDownBinocularSwitch.tvLabel.setText(getString(R.string.Label_downBinocularSavePicSwitch));

        mVisionBinding.tvLandProtectHeightTip.setText(getString(R.string.Msg_LandProtectHeightTip1));
        mVisionBinding.llTestFunctionLayout.setVisibility(UavStaticVar.isOpenTextEnvironment ? View.VISIBLE : View.GONE);
        mVisionBinding.tvLandProtectTip.setText(getString(R.string.Msg_LandProtectTip1));
        mVisionBinding.sbLandProtectHeight.setMax(100);

        setListener();

        if (GlobalVariable.planType == PlanType.S220
                || GlobalVariable.planType == PlanType.S280
                || GlobalVariable.planType == PlanType.S200
                || GlobalVariable.planType == PlanType.S220Pro
                || GlobalVariable.planType == PlanType.S220ProS
                || GlobalVariable.planType == PlanType.S220ProH
                || GlobalVariable.planType == PlanType.S220_SD
                || GlobalVariable.planType == PlanType.S200_SD
                || GlobalVariable.planType == PlanType.S220BDS
                || GlobalVariable.planType == PlanType.S280BDS
                || GlobalVariable.planType == PlanType.S200BDS
                || GlobalVariable.planType == PlanType.S220ProBDS
                || GlobalVariable.planType == PlanType.S220ProSBDS
                || GlobalVariable.planType == PlanType.S220ProHBDS
                || GlobalVariable.planType == PlanType.S220_SD_BDS
                || GlobalVariable.planType == PlanType.S200_SD_BDS) {
            mVisionBinding.clLandProtectSwitch.setVisibility(View.VISIBLE);
        }
    }

    private void initData() {
        preLoadData();
//        GduApplication.getSingleApp().gduCommunication.getObstacleSwitch((code, bean) -> {
//            MyLogUtils.i("getObstacleSwitch callBack() code = " + code);
//            boolean isEmptyData = code != GduConfig.OK || bean == null || bean.frameContent == null || bean.frameContent.length == 0;
//            if (isEmptyData) {
//                return;
//            }
//            String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
//            MyLogUtils.i("getGoHomeObstacleSwitch callback() hexStr = " + hexStr);
//            Message message = new Message();
//            message.what = GOT_OBSTACLE_SUCCEED;
//            message.arg1 = bean.frameContent[2];
//            message.arg2 = bean.frameContent[3];
//            MyLogUtils.i("getObstacleSwitch callBack() msgArg1 = " + message.arg1 + "; msgArg2 = " + message.arg2);
//            mHandler.sendMessage(message);
//        });
//
//        GduApplication.getSingleApp().gduCommunication.getGoHomeObstacleSwitch((code, bean) -> {
//            MyLogUtils.i("getGoHomeObstacleSwitch() code = " + code);
//            boolean isEmptyData = code != GduConfig.OK || bean == null || bean.frameContent == null || bean.frameContent.length < 3;
//            if (isEmptyData) {
//                return;
//            }
//            String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
//            MyLogUtils.i("getGoHomeObstacleSwitch callback() hexStr = " + hexStr);
//            if (!isAdded()) {
//                return;
//            }
//            mHandler.post(() -> {
//                final byte switchValue = bean.frameContent[2];
//                MyLogUtils.i("getGoHomeObstacleSwitch() switchValue = " + switchValue);
//                GlobalVariable.obstacleReturnIsOpen = switchValue == 0;
//                mVisionBinding.ivGoHomeObstacleSwitch.setSelected(switchValue == 0);
//            });
//        });
//
//        GduApplication.getSingleApp().gduCommunication.getLandingProtectStatus((code, bean) -> {
//            MyLogUtils.i("getLandingProtectStatus() code = " + code);
//            if (bean == null || bean.frameContent == null || bean.frameContent.length == 0) {
//                MyLogUtils.i("getLandingProtectStatus() empty data");
//                return;
//            }
//            String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
//            MyLogUtils.i("getLandingProtectStatus() hexStr = " + hexStr);
//            if (code == GduConfig.OK) {
//                // 开关状态：1 开 2 关
//                int switchStatus = bean.frameContent[2];
//                short heightValue = ByteUtilsLowBefore.byte2short(bean.frameContent, 3);
//                MyLogUtils.i("getLandingProtectStatus() switchStatus = " + switchStatus + "; heightValue = " + heightValue);
//                uiThreadHandle(() -> {
//                    changeLandProtectSwitchViewChange(switchStatus == 1);
//                    mVisionBinding.sbLandProtectHeight.setProgress(heightValue - 100);
//                    if (heightValue == 0) {
//                        mVisionBinding.etLandProtectHeightInput.setText("0");
//                    } else {
//                        mVisionBinding.etLandProtectHeightInput.setText(FormatConfig.format_4.format(heightValue / 100f));
//                    }
//                });
//            }
//        });
        getVisionBinocularSwitchStatus();
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
        mVisionBinding.ivSwitchShowRocker.setOnClickListener(mOnClickListener);
        mVisionBinding.ivFillInLight.setOnClickListener(mOnClickListener);
        mVisionBinding.ivGoHomeObstacleSwitch.setOnClickListener(mOnClickListener);
        mVisionBinding.ivInstruction.setOnClickListener(mOnClickListener);
        mVisionBinding.ivInstructionRocker.setOnClickListener(mOnClickListener);
        mVisionBinding.ivTopTof.setOnClickListener(mOnClickListener);
        mVisionBinding.tvOpen.setOnClickListener(mOnClickListener);
        mVisionBinding.tvClose.setOnClickListener(mOnClickListener);
        mVisionBinding.ivVisualFusionPosition.setOnClickListener(mOnClickListener);

        mVisionBinding.tvObstacleStop.setOnClickListener(mOnClickListener);
        mVisionBinding.tvObstacleDetour.setOnClickListener(mOnClickListener);
        mVisionBinding.tvObstacleClose.setOnClickListener(mOnClickListener);


        mVisionBinding.incFrontBinocularSwitch.ivSwitch.setOnClickListener(v -> {
            mVisionBinding.incFrontBinocularSwitch.ivSwitch.setSelected(
                    !mVisionBinding.incFrontBinocularSwitch.ivSwitch.isSelected());
            setVisionBinocularSwitchStatus();
        });
        mVisionBinding.incBackBinocularSwitch.ivSwitch.setOnClickListener(v -> {
            mVisionBinding.incBackBinocularSwitch.ivSwitch.setSelected(
                    !mVisionBinding.incBackBinocularSwitch.ivSwitch.isSelected());
            setVisionBinocularSwitchStatus();
        });
        mVisionBinding.incLeftBinocularSwitch.ivSwitch.setOnClickListener(v -> {
            mVisionBinding.incLeftBinocularSwitch.ivSwitch.setSelected(
                    !mVisionBinding.incLeftBinocularSwitch.ivSwitch.isSelected());
            setVisionBinocularSwitchStatus();
        });
        mVisionBinding.incRightBinocularSwitch.ivSwitch.setOnClickListener(v -> {
            mVisionBinding.incRightBinocularSwitch.ivSwitch.setSelected(
                    !mVisionBinding.incRightBinocularSwitch.ivSwitch.isSelected());
            setVisionBinocularSwitchStatus();
        });
        mVisionBinding.incUpBinocularSwitch.ivSwitch.setOnClickListener(v -> {
            mVisionBinding.incUpBinocularSwitch.ivSwitch.setSelected(
                    !mVisionBinding.incUpBinocularSwitch.ivSwitch.isSelected());
            setVisionBinocularSwitchStatus();
        });
        mVisionBinding.incDownBinocularSwitch.ivSwitch.setOnClickListener(v -> {
            mVisionBinding.incDownBinocularSwitch.ivSwitch.setSelected(
                    !mVisionBinding.incDownBinocularSwitch.ivSwitch.isSelected());
            setVisionBinocularSwitchStatus();
        });

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
            changeLandProtectSwitchViewChange(!mVisionBinding.ivLandProtectSwitch.isSelected());
            if (DroneUtil.isSmallFlight()) {
                float heightProgress = mVisionBinding.sbLandProtectHeight.getProgress();
                MyLogUtils.i("switchLandingProtectNew() heightProgress = " + heightProgress);
//                GduApplication.getSingleApp().gduCommunication.switchLandingProtectNew((byte)
//                                (mVisionBinding.ivLandProtectSwitch.isSelected() ? 1 : 2),
//                        (short) (heightProgress + 100), (code, bean) -> {
//                            MyLogUtils.i("switchLandingProtectNew() code = " + code);
//                            uiThreadHandle(() -> {
//                                if (code != 0) {
//                                    changeLandProtectSwitchViewChange(
//                                            !mVisionBinding.ivLandProtectSwitch.isSelected());
//                                }
//                                Toaster.show(code == GduConfig.OK ?
//                                        requireContext().getString(R.string.string_set_success) :
//                                        requireContext().getString(R.string.Label_SettingFail));
//                            });
//                        });
            } else {
//                GduApplication.getSingleApp().gduCommunication.switchLandingProtect((byte)
//                                (mVisionBinding.ivLandProtectSwitch.isSelected() ? 1 : 2),
//                        (code, bean) -> {
//                            MyLogUtils.i("switchLandingProtect() code = " + code);
//                            uiThreadHandle(() -> {
//                                if (code != 0) {
//                                    changeLandProtectSwitchViewChange(
//                                            !mVisionBinding.ivLandProtectSwitch.isSelected());
//                                }
//                                Toaster.show(code == GduConfig.OK ? requireContext().getString(R.string.string_set_success) :
//                                        requireContext().getString(R.string.Label_SettingFail));
//                            });
//                        });
            }
        });


        mVisionBinding.ivFrontSwitch.setOnClickListener(mOnClickListener);
        mVisionBinding.ivAfterSwitch.setOnClickListener(mOnClickListener);
        mVisionBinding.ivLeftSwitch.setOnClickListener(mOnClickListener);
        mVisionBinding.ivRightSwitch.setOnClickListener(mOnClickListener);
        mVisionBinding.ivTopSwitch.setOnClickListener(mOnClickListener);
        mVisionBinding.ivDownSwitch.setOnClickListener(mOnClickListener);



    }

    private void changeLandProtectSwitchViewChange(boolean isOpen) {
        mVisionBinding.ivLandProtectSwitch.setSelected(isOpen);
        mVisionBinding.tvLandProtectTip.setVisibility(isOpen ? View.VISIBLE : View.GONE);
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
//                GduApplication.getSingleApp().gduCommunication.switchLandingProtectNew((byte)
//                                (mVisionBinding.ivLandProtectSwitch.isSelected() ? 1 : 2),
//                        (short) height, (code, bean) -> {
//                            MyLogUtils.i("switchLandingProtectNew() code = " + code);
//                            uiThreadHandle(() -> {
//                                if (code != 0) {
//                                    changeLandProtectSwitchViewChange(
//                                            !mVisionBinding.ivLandProtectSwitch.isSelected());
//                                }
//                                Toaster.show(code == GduConfig.OK ? requireContext().getString(R.string.string_set_success) :
//                                        requireContext().getString(R.string.Label_SettingFail));
//                            });
//                        });
            } else {
//                GduApplication.getSingleApp().gduCommunication.switchLandingProtect((byte)
//                                (mVisionBinding.ivLandProtectSwitch.isSelected() ? 1 : 2),
//                        (code, bean) -> {
//                            MyLogUtils.i("switchLandingProtect() code = " + code);
//                            uiThreadHandle(() -> {
//                                if (code != 0) {
//                                    changeLandProtectSwitchViewChange(
//                                            !mVisionBinding.ivLandProtectSwitch.isSelected());
//                                }
//                                Toaster.show(code == GduConfig.OK ? R.string.string_set_success : R.string.Label_SettingFail);
//                            });
//                        });
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
//                        mDialogUtils.createDialogWithSingleBtn(getString(R.string.string_vision_error), errStr, getString(R.string.Label_Sure));
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
//                        mDialogUtils.createDialogWith2Btn(getString(R.string.close_vision_title),
//                                getString(R.string.close_vision_content), getString(R.string.Label_cancel),
//                                getString(R.string.Label_Sure), v -> {
//                                    switch (v.getId()) {
//                                        case R.id.dialog_btn_cancel:
//                                            mDialogUtils.cancelDialog();
//                                            break;
//
//                                        case R.id.dialog_btn_sure:
//                                            switchVisionObstacle(false, SwitchType.OBSTACLE_TYPE_MAIN);
//                                            curSwitch_vision_obstacle = false;
//                                            mVisionBinding.ivSwitchVisionObstacle.setSelected(false);
//                                            changeObserveTipVisibility(false);
//                                            mDialogUtils.cancelDialog();
//                                            break;
//
//                                        default:
//                                            break;
//                                    }
//                                });
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
//                        showConfirmDialog(false);
                    } else {
                        switchObstacleStrategy((byte) 0);
                        switch_vision_obstacle_strategy = true;
                        mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(true);
                    }
                    break;

                case R.id.iv_switch_show_rocker:
                    if (!connStateToast()) {// 显示雷达图
                        return;
                    }
                    //产品要求，如果视觉感知开关打开，不允许关闭显示雷达图
                    if (GlobalVariable.obstacleIsOpen && mVisionBinding.ivSwitchShowRocker.isSelected()) {
                        return;
                    }
                    //20230109 雷达图显示和避障无关，可单独开启
                    switchShowRadar(!mVisionBinding.ivSwitchShowRocker.isSelected());
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;

                case R.id.iv_instruction:
                    mVisionBinding.ivInstruction.setSelected(!mVisionBinding.ivInstruction.isSelected());
                    break;

                case R.id.iv_instruction_rocker:
                    if ( mVisionBinding.ivInstructionRocker.isSelected()) {
                        mVisionBinding.ivInstructionRocker.setSelected(false);
                        mVisionBinding.tvRockerInstrution.setVisibility(View.GONE);
                    } else {
                        mVisionBinding.ivInstructionRocker.setSelected(true);
                        mVisionBinding.tvRockerInstrution.setVisibility(View.VISIBLE);
                    }
                    break;

                case R.id.iv_fill_in_light:
                    // 补光灯
                    if (!connStateToast()) {
                        return;
                    }
                    if (mVisionBinding.ivFillInLight.isSelected()) {
                        mVisionBinding.ivFillInLight.setSelected(false);
                        switchFillInLight(false);
                    } else {
                        mVisionBinding.ivFillInLight.setSelected(true);
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
                case R.id.iv_top_tof:

                    if (!connStateToast()) {
                        return;
                    }
                    changeTopTof();

                    break;
                case R.id.iv_visual_fusion_position:
                    if (!connStateToast()) {
                        return;
                    }
                    // 开状态
                    changeVisualFusionPosition(!mVisionBinding.ivVisualFusionPosition.isSelected());
                    break;
                case R.id.tv_open:
                    changeVisualFusionPosition(true);
                    break;
                case R.id.tv_close:
                    changeVisualFusionPosition(false);
                    break;

                case R.id.tv_obstacle_stop:
                    switchObstacleStrategy((byte) 0);
                    break;
                case R.id.tv_obstacle_detour:
                    switchObstacleStrategy((byte) 2);
                    break;
                case R.id.tv_obstacle_close:
                    switchObstacleStrategy((byte) 1);
                    break;
                case R.id.iv_front_switch:
                    changObstacleLogSwitch(1, (byte) (mVisionBinding.ivFrontSwitch.isSelected() ? 0 : 1));
                    break;
                case R.id.iv_after_switch:
                    changObstacleLogSwitch(2,(byte) (mVisionBinding.ivAfterSwitch.isSelected() ? 0 : 1));
                    break;
                case R.id.iv_left_switch:_switch:
                    changObstacleLogSwitch(3,(byte) (mVisionBinding.ivLeftSwitch.isSelected() ? 0 : 1));
                    break;
                case R.id.iv_right_switch:
                    changObstacleLogSwitch(4,(byte) (mVisionBinding.ivRightSwitch.isSelected() ? 0 : 1));
                    break;
                case R.id.iv_top_switch:
                    changObstacleLogSwitch(5,(byte) (mVisionBinding.ivTopSwitch.isSelected() ? 0 : 1));
                    break;
                case R.id.iv_down_switch:
                    changObstacleLogSwitch(6,(byte) (mVisionBinding.ivDownSwitch.isSelected() ? 0 : 1));
                    break;
                default:
                    break;
            }
        }
    };

    private void changObstacleLogSwitch(int type, byte open) {
        byte front = (byte) (mVisionBinding.ivFrontSwitch.isSelected() ? 1 : 0);
        byte back = (byte) (mVisionBinding.ivAfterSwitch.isSelected() ? 1 : 0);
        byte left = (byte) (mVisionBinding.ivLeftSwitch.isSelected() ? 1 : 0);
        byte right = (byte) (mVisionBinding.ivRightSwitch.isSelected() ? 1 : 0);
        byte top = (byte) (mVisionBinding.ivTopSwitch.isSelected() ? 1 : 0);
        byte down = (byte) (mVisionBinding.ivDownSwitch.isSelected() ? 1 : 0);
        if (type == 1) {
            front = open;
        } else if (type == 2) {
            back = open;
        } else if (type == 3) {
            left = open;
        } else if (type == 4) {
            right = open;
        } else if (type == 5) {
            top = open;
        } else if (type == 6) {
            down = open;
        }
//        GduApplication.getSingleApp().gduCommunication.setVisionLogSwitch(front, back, left, right, top, down, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (isAdded() && mHandler != null) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (code == GduConfig.OK) {
//                                Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
//                                if (type == 1) {
//                                    mVisionBinding.ivFrontSwitch.setSelected(open == 1);
//                                } else if (type == 2) {
//                                    mVisionBinding.ivAfterSwitch.setSelected(open == 1);
//                                } else if (type == 3) {
//                                    mVisionBinding.ivLeftSwitch.setSelected(open == 1);
//                                } else if (type == 4) {
//                                    mVisionBinding.ivRightSwitch.setSelected(open == 1);
//                                } else if (type == 5) {
//                                    mVisionBinding.ivTopSwitch.setSelected(open == 1);
//                                } else if (type == 6) {
//                                    mVisionBinding.ivDownSwitch.setSelected(open == 1);
//                                }
//                            } else {
//                                Toast.makeText(requireContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//                }
//            }
//        });
    }

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




//    LogCancelOrBackConfirmDialog confirmDialog;
//    private void showConfirmDialog(boolean isOn) {
//        confirmDialog = new LogCancelOrBackConfirmDialog(requireContext());
//        confirmDialog.setTitleText("", -1, -1);
//        confirmDialog.setContentText(requireContext().getString(R.string.string_close_obstacle_strategy_tips), -1, -1);
//        confirmDialog.setCancelable(false);
//        confirmDialog.setOnBottomClickListener(new LogCancelOrBackConfirmDialog.OnBottomClickListener() {
//            @Override
//            public void onLeftClick() {
//                if(confirmDialog != null){
//                    confirmDialog.dismiss();
//                    confirmDialog = null;
//                }
//            }
//
//            @Override
//            public void onRightClick(String content) {
//                if (confirmDialog != null) {
//                    confirmDialog.dismiss();
//                    confirmDialog = null;
//                }
//                switchObstacleStrategy((byte) 1);
//                getActivity().runOnUiThread(()->{
//                    switch_vision_obstacle_strategy = false;
//                    mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(false);
//                });
//            }
//        });
//        confirmDialog.show();
//    }

    private void changeVisualFusionPosition(boolean isOpen) {
//        GduApplication.getSingleApp().gduCommunication.changeVisualFusionPosition(isOpen, (code, bean) -> {
//            if (isAdded() && mHandler != null) {
//                mHandler.post(() -> {
//                    if (code == GduConfig.OK) {
//                        mVisionBinding.ivVisualFusionPosition.setSelected(isOpen);
//                        Toaster.show(getString(R.string.Label_SettingSuccess));
//                    } else {
//                        Toaster.show(getString(R.string.Label_SettingFail));
//                    }
//                });
//            }
//        });
    }

    private void changeTopTof() {
        byte open = (byte) (mVisionBinding.ivTopTof.isSelected() ? 0 : 1);
//        GduApplication.getSingleApp().gduCommunication.changeTopTof(open, (code, bean) -> {
//            if (!isAdded() || mHandler == null) {
//                return;
//            }
//            mHandler.post(() -> {
//                if (code == GduConfig.OK) {
//                    mVisionBinding.ivTopTof.setSelected(open == 1);
//                } else {
//                    Toaster.show(getString(R.string.Label_SettingFail));
//                }
//            });
//        });

    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SWITCH_SUCCESS:
                    if (setFlyType == SWITCH_VISION_ON) {
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_MAIN);
                        switchShowRadar(true);
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
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY, 0);
                        changeVisibilityObstacleView();
                    } else if (setFlyType == SWITCH_OBSTACLE_STRATEGY_OFF) {
                        GlobalVariable.obstacleStrategyIsOpen = false;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY,1);
                        changeVisibilityObstacleView();
                    } else if (setFlyType == SWITCH_OBSTACLE_STRATEGY_AROUND) {
                        GlobalVariable.obstacleStrategyIsOpen = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY,2);
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
                        //经和产品沟通，开启视觉感知，默认打开显示雷达图
                        switchShowRadar(true);
                    } else {
                        curSwitch_vision_obstacle = false;
                    }

                    mVisionBinding.ivSwitchVisionObstacle.setSelected(curSwitch_vision_obstacle);
                    changeObserveTipVisibility(curSwitch_vision_obstacle);
                    int arg2 = msg.arg2;
                    // 避障策略开启
                    if (arg2 == 0) {
                        switch_vision_obstacle_strategy = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY, 0);
                    } else if (arg2 == 1) {
                        switch_vision_obstacle_strategy = false;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY, 1);
                    } else if (arg2 == 2) {
                        switch_vision_obstacle_strategy = true;
                        changeSwitchStateSuccess(SwitchType.OBSTACLE_TYPE_STRATEGY, 2);
                    }
                    changeVisibilityObstacleView();

                    AlgorithmMark.getSingleton().ObStacle = switch_vision_obstacle_strategy;
                    mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(switch_vision_obstacle_strategy);

                    break;
                case SWITCH_FILL_IN_LIGHT_SUCCEED:
                    Toast.makeText(requireContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
                    break;
                case SWITCH_FILL_IN_LIGHT_FAILED:
                    boolean isOpen = (boolean) msg.obj;
                    mVisionBinding.ivFillInLight.setSelected(!isOpen);
                    Toast.makeText(requireContext(), R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
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

    private void changeSwitchStateSuccess(SwitchType type, int param) {
        MyLogUtils.i("changeSwitchStateSuccess() type = " + type + ",param = " + param);
        switch (type) {
            case OBSTACLE_TYPE_MAIN:
                pre_switch_vision_obstacle = curSwitch_vision_obstacle;
                mVisionBinding.ivSwitchVisionObstacle.setSelected(curSwitch_vision_obstacle);
                changeObserveTipVisibility(curSwitch_vision_obstacle);
                //视觉避障功能关闭后再次开启，雷达图选项默认开启; 视觉避障功能处于关闭状态时，显示雷达图选项置灰；
                GlobalVariable.obstacleIsOpen = curSwitch_vision_obstacle;
                break;
            case OBSTACLE_TYPE_STRATEGY:
                switch_vision_obstacle_strategy = param == 0 || param == 2;
                pre_switch_vision_obstacle_strategy = switch_vision_obstacle_strategy;
                AlgorithmMark.getSingleton().ObStacle = param == 0 || param == 2;
                mVisionBinding.ivSwitchVisionObstacleStrategy.setSelected(switch_vision_obstacle_strategy);
                switch (param) {
                    // 避障策略(刹停)开启
                    case 0:
                        mVisionBinding.tvObstacleStop.setSelected(true);
                        mVisionBinding.tvObstacleClose.setSelected(false);
                        mVisionBinding.tvObstacleDetour.setSelected(false);
                        break;
                    // 避障策略关门
                    case 1:
                        mVisionBinding.tvObstacleClose.setSelected(true);
                        mVisionBinding.tvObstacleStop.setSelected(false);
                        mVisionBinding.tvObstacleDetour.setSelected(false);
                        break;
                    // 避障策略(绕障)开启
                    case 2:
                        mVisionBinding.tvObstacleDetour.setSelected(true);
                        mVisionBinding.tvObstacleClose.setSelected(false);
                        mVisionBinding.tvObstacleStop.setSelected(false);
                        break;

                    default:
                        break;
                }
                break;

            default:
                break;
        }
    }

    private void changeSwitchStateFailed(SwitchType type) {
        MyLogUtils.i("changeSwitchStateFailed() type = " + type);
        switch (type) {
            case OBSTACLE_TYPE_MAIN:
//                switchShowRadar(false);
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
    public void switchObstacleStrategy(byte type) {
        MyLogUtils.i("switchObstacleStrategy() isOn = " + type);
        switch (type) {
            case 0:
                setFlyType = SWITCH_OBSTACLE_STRATEGY_ON;
                break;
            case 1:
                setFlyType = SWITCH_OBSTACLE_STRATEGY_OFF;
                break;
            case 2:
                setFlyType = SWITCH_OBSTACLE_STRATEGY_AROUND;
                break;
            default:
                break;
        }
//        GduApplication.getSingleApp().gduCommunication.switchObstacleStrategy(type, visionCallback);
    }

    private void switchGoHomeObstacle(boolean isOpen) {
        MyLogUtils.i("switchGoHomeObstacle() isOpen = " + isOpen);
//        GduApplication.getSingleApp().gduCommunication.switchGoHomeObstacle(isOpen ? (byte) 0 : (byte) 1, (code, bean) -> {
//            MyLogUtils.i("switchGoHomeObstacle() code = " + code);
//            if (code == GduConfig.OK) {
//                if (mHandler != null && isAdded()) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastUtil.show(R.string.Label_SettingSuccess);
//                            GlobalVariable.obstacleReturnIsOpen = !mVisionBinding.ivGoHomeObstacleSwitch.isSelected();
//                            mVisionBinding.ivGoHomeObstacleSwitch.setSelected(!mVisionBinding.ivGoHomeObstacleSwitch.isSelected());
//                        }
//                    });
//                }
//            }
//        });
    }

    public void switchFillInLight(boolean open) {
        byte isOn;
        if (open) {
            isOn = 1;
        } else {
            isOn = 0;
        }
//        GduApplication.getSingleApp().gduCommunication.switchFillInLight(isOn, (code, bean) -> {
//            MyLogUtils.i("switchFillInLight() code = " + code);
//            if (mHandler == null) {
//                return;
//            }
//            if (code == GduConfig.OK) {
//                mHandler.obtainMessage(SWITCH_FILL_IN_LIGHT_SUCCEED, open).sendToTarget();
//            } else {
//                mHandler.obtainMessage(SWITCH_FILL_IN_LIGHT_FAILED, open).sendToTarget();
//            }
//        });
    }

    /**
     * 切换是否显示雷达图
     * @param isShow
     */
    private void switchShowRadar(boolean isShow) {
        MyLogUtils.d("switchRadar() isShow = " + isShow);
        mVisionBinding.ivSwitchShowRocker.setSelected(isShow);
        GlobalVariable.hadShowObstacle = isShow;
        SPUtils.put(requireContext(), GduConfig.ISSHOWROCKER, isShow);
    }

    private final SocketCallBack3 visionCallback = (code, bean) -> {
        MyLogUtils.i("visionCallback callBack() code = " + code);
        if (mHandler != null) {
            switch (code) {
                case 0x00:
                    mHandler.sendEmptyMessage(SWITCH_SUCCESS);
                    break;
                case 0x01:
                case 0x02:
                case 0x03:
                case 0x04:
                case 0x05:
                case 0x06:
                case 0x07:
                case 0x08:
                default:
                    mHandler.sendEmptyMessage(SWITCH_FAILED);
                    break;
            }
        }
    };

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
//        GduApplication.getSingleApp().gduCommunication.obstacleALG(isOn, type, visionCallback);
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
        final boolean isShowRadar = SPUtils.getBoolean(requireContext(), GduConfig.ISSHOWROCKER);
        mVisionBinding.ivSwitchShowRocker.setSelected(isShowRadar);
    }


    public static SettingVisionFragment newInstance() {
        Bundle args = new Bundle();
        SettingVisionFragment fragment = new SettingVisionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void getVisionBinocularSwitchStatus() {
        MyLogUtils.i("getVisionBinocularSwitchStatus()");
//        GduApplication.getSingleApp().gduCommunication.getVisionBinocularSwitch((code, bean) -> {
//            MyLogUtils.i("getVisionBinocularSwitch callback() code = " + code);
//            if (bean == null || bean.frameContent == null || bean.frameContent.length == 0) {
//                return;
//            }
//            String hexStr = DataUtil.bytes2HexAddPlaceHolder(bean.frameContent);
//            MyLogUtils.i("getVisionBinocularSwitch callback() hexStr = " + hexStr);
//            uiThreadHandle(() -> {
//                mVisionBinding.incFrontBinocularSwitch.ivSwitch.setSelected(bean.frameContent[0] == 1);
//                mVisionBinding.incBackBinocularSwitch.ivSwitch.setSelected(bean.frameContent[1] == 1);
//                mVisionBinding.incLeftBinocularSwitch.ivSwitch.setSelected(bean.frameContent[2] == 1);
//                mVisionBinding.incRightBinocularSwitch.ivSwitch.setSelected(bean.frameContent[3] == 1);
//                mVisionBinding.incUpBinocularSwitch.ivSwitch.setSelected(bean.frameContent[4] == 1);
//                mVisionBinding.incDownBinocularSwitch.ivSwitch.setSelected(bean.frameContent[5] == 1);
//            });
//        });
    }

    private void setVisionBinocularSwitchStatus() {
        byte front = (byte) (mVisionBinding.incFrontBinocularSwitch.ivSwitch.isSelected() ? 1 : 0);
        byte back = (byte) (mVisionBinding.incBackBinocularSwitch.ivSwitch.isSelected() ? 1 : 0);
        byte left = (byte) (mVisionBinding.incLeftBinocularSwitch.ivSwitch.isSelected() ? 1 : 0);
        byte right = (byte) (mVisionBinding.incRightBinocularSwitch.ivSwitch.isSelected() ? 1 : 0);
        byte top = (byte) (mVisionBinding.incUpBinocularSwitch.ivSwitch.isSelected() ? 1 : 0);
        byte down = (byte) (mVisionBinding.incDownBinocularSwitch.ivSwitch.isSelected() ? 1 : 0);
//        GduApplication.getSingleApp().gduCommunication.setVisionBinocularSwitch(front, back, left,
//                right, top, down, (code, bean) -> {
//                    MyLogUtils.i("setVisionBinocularSwitch callback() code = " + code);
//
//                });
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
