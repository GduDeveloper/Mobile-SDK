package com.gdu.demo.flight.setting.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.gdu.GlobalVariableTest;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GlobalVariable;
import com.gdu.config.UavStaticVar;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSetingFlyBinding;
import com.gdu.demo.flight.base.BaseFlightViewModel;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.ChannelUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.logger.MyLogUtils;
import com.gdu.util.logs.AppLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import cc.taylorzhang.singleclick.SingleClickUtil;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/23
 */
public class SettingFlyFragment extends Fragment implements View.OnClickListener {

    private FragmentSetingFlyBinding mViewBinding;
    private UnitChnageUtils mUnitChnageUtils;

    private int curBackSpeed;

    private int preBackSpeed = 8;
    private int preBackHeight = 20;
    private int preHeightLimit = -1;
    private int preDistanceLimit = -1;
    private int preOutOfControlAction = 0;

    /**
     * 是否开启限高
     */
    private boolean pre_switch_limit_height;

    private boolean isTfaMode = false;

    /**
     * 是否开启限高
     */
    private boolean isOpenLimitHeight;

    private int currentSecondLevelType = 0;

    private FragmentActivity mActivity;
    private BaseFlightViewModel baseViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSetingFlyBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = getActivity();
        if (null!=mActivity)
            baseViewModel = new ViewModelProvider(mActivity).get(BaseFlightViewModel.class);
        initView();
        initData();
    }

    private void initView() {
//        EventBus.getDefault().register(this);
        mUnitChnageUtils = new UnitChnageUtils();
        setListener();

        if (CommonUtils.curPlanIsSmallFlight()) {
            mViewBinding.groupGnss.setVisibility(View.GONE);
            mViewBinding.groupAdvancedSetting.setVisibility(View.GONE);
        } else {
            mViewBinding.groupGnss.setVisibility(View.VISIBLE);
            mViewBinding.groupAdvancedSetting.setVisibility(View.VISIBLE);
        }
        // 大华需支持S200软件单北斗模式切换+GPS等GNSS功能(S400已经支持切换)
        if (ChannelUtils.isDahua(getContext()) && CommonUtils.curPlanIsSmallFlight() && !DroneUtil.isBDSOnlyDrone()) {
            mViewBinding.groupGnss.setVisibility(View.VISIBLE);
        }
        // dhBDS不允许切换GNSS，直接显示BDS
        else if (ChannelUtils.isDahuaBDS(getContext())) {
            mViewBinding.groupGnss.setVisibility(View.GONE);
            mViewBinding.groupBds.setVisibility(View.VISIBLE);
        }

//        setSwitchFlyModeView(GlobalVariable.enableSwitchFlyMode == GlobalVariable.FlyModeSwitchModeStatus.ON);

        mViewBinding.ivNoFlyBackSwitch.setSelected(GlobalVariable.noFlyAreBackAction == 1);

        String[] array = getResources().getStringArray(R.array.array_fly_model);
//        mViewBinding.tabFlyModel.setTabData(array);

        if (GlobalVariable.isTetherModel) {
            mViewBinding.sbLimitDistance.setEnabled(false);
            mViewBinding.sbLimitHeight.setEnabled(false);
            mViewBinding.etDistanceLimit.setEnabled(false);
            mViewBinding.etHeightLimit.setEnabled(false);
        }

        // dh大华的先默认BDS
        if (ChannelUtils.isDahua(getContext()) || ChannelUtils.isDahuaBDS(getContext())) {
            GlobalVariable.sGNSSType = (byte) SPUtils.getCustomInt(GduAppEnv.application, "sGNSSType", 6);
        }
        if (GlobalVariable.sGNSSType == 6) {
            mViewBinding.ovGnss.setIndex(1);
        } else {
            mViewBinding.ovGnss.setIndex(0);
        }

//        mViewBinding.ivLocationSwitch.setSelected(GlobalVariable.vioLocationSwitchState == 1);
//
//        mViewBinding.ivLocationSwitch.setOnClickListener(v -> {
//            mViewBinding.ivLocationSwitch.setSelected(!mViewBinding.ivLocationSwitch.isSelected());
//            GduApplication.getSingleApp().gduCommunication.visionLocationSwitch(
//                    (byte) (mViewBinding.ivLocationSwitch.isSelected() ? 1 : 0),
//                    (code, bean) -> uiThreadHandle(() -> GduApplication.getSingleApp().show(code == GduConfig.OK ?
//                            getContext().getString(R.string.Label_SettingSuccess)
//                            : getContext().getString(R.string.Label_SettingFail))));
//        });
    }

    private void initData() {
        initLimitHeight();
        initLimitDistance();
        setLimitData();
        getTripodMode();
        // 高度距离等限制参数
//        if (mLimitHeightOrDistanceSetPresenter != null) {
//            mLimitHeightOrDistanceSetPresenter.getLimitDistance();
//        }
        getBackInfo();
        getOutOfControlAction();
        getBatterySwitch();
        getReturnHomeAction();
//        getFlyModeState();
        setStateListener();
    }

    /**
     * 设置限高相关内容
     * */
    private void initLimitHeight(){
        preHeightLimit = baseViewModel.getDefaultLimitHeight(getContext());
        mViewBinding.sbLimitHeight.setProgress(preHeightLimit);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mViewBinding.sbLimitHeight.setMin(MyConstants.LIMIT_HEIGHT_MIN);
        }
        mViewBinding.sbLimitHeight.setMax(MyConstants.LIMIT_HEIGHT_MAX);
        mViewBinding.etHeightLimit.setText(String.valueOf(preHeightLimit));
        mUnitChnageUtils.showUnit(MyConstants.LIMIT_HEIGHT_MIN, mViewBinding.tvLimitHeightMin);
        mUnitChnageUtils.showUnit(MyConstants.LIMIT_HEIGHT_MAX, mViewBinding.tvLimitHeightMax);
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            mViewBinding.etHeightLimit.setText(String.valueOf(UnitChnageUtils.getUnitValue(mViewBinding.sbLimitHeight.getProgress())));
        }else {
            if (!GlobalVariable.isNewHeightLimitStrategy){
                mViewBinding.etHeightLimit.setEnabled(false);
                mViewBinding.sbLimitHeight.setEnabled(false);
            }
            mViewBinding.sbLimitHeight.setProgress(0);
            mViewBinding.ivSwitchLimitHeight.setSelected(false);
            updateLimitHeightView(false);
            mViewBinding.etHeightLimit.setText("INF");
        }
        if (null == mActivity) return;
        baseViewModel.getLimitHeightLiveData().observe(mActivity, data->{
            if (data.isSet() && data.isOpen()) {
                Toast.makeText(getContext(), R.string.string_set_success, Toast.LENGTH_SHORT).show();
            }
            mViewBinding.ivSwitchLimitHeight.setSelected(data.isOpen());
            updateLimitHeightView(data.isOpen());
            isOpenLimitHeight = data.isOpen();
            if (GlobalVariable.isNewHeightLimitStrategy){
                mViewBinding.sbLimitHeight.setEnabled(data.isOpen());
                mViewBinding.etHeightLimit.setEnabled(data.isOpen());
            }
            if (data.isOpen() || (data.getHeightLimit() >= MyConstants.LIMIT_HEIGHT_MIN && data.getHeightLimit() <= MyConstants.LIMIT_HEIGHT_MAX)) {
                mViewBinding.sbLimitHeight.setProgress(data.getHeightLimit());
                String limitHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(data.getHeightLimit()));
                mViewBinding.etHeightLimit.setText(limitHeightStr);
                preHeightLimit = data.getHeightLimit();
            }
            if (data.isOpen()) {
                // 返航高度相关判断
                if (preHeightLimit > MyConstants.GO_HOME_HEIGHT_MAX || preHeightLimit <= 0) {
                    mViewBinding.sbBackHeight.setMax(MyConstants.GO_HOME_HEIGHT_MAX);
                    mUnitChnageUtils.showUnit(MyConstants.GO_HOME_HEIGHT_MAX, mViewBinding.tvMaxBackHeight);
                } else if (preBackHeight > data.getHeightLimit()) {
                    preBackHeight = data.getHeightLimit();
                    mViewBinding.sbBackHeight.setMax(preBackHeight);
                    mUnitChnageUtils.showUnit(preBackHeight, mViewBinding.tvMaxBackHeight);
                    mViewBinding.sbBackHeight.setProgress(preBackHeight);
                    baseViewModel.setGoHomeHeight(preBackHeight);
                } else {
                    mViewBinding.sbBackHeight.setMax(preHeightLimit);
                    mUnitChnageUtils.showUnit(preHeightLimit, mViewBinding.tvMaxBackHeight);
                }
            } else {
                mViewBinding.sbBackHeight.setMax(MyConstants.GO_HOME_HEIGHT_MAX);
                mUnitChnageUtils.showUnit(MyConstants.GO_HOME_HEIGHT_MAX, mViewBinding.tvMaxBackHeight);
            }
        });
        baseViewModel.getErrTipBeanLiveData().observe(mActivity, data->{
            int setType = data.getSetType();
            int type = data.getType();
            if (setType == 1) {
                setHeightFailHandle();
            } else if(setType == 2) {
                setDistanceFailHandle();
            }else if(setType == 3){
                setBackHeightFailHandle();
            }
            switch (type) {
                case 1:
                    mViewBinding.sbLimitHeight.setEnabled(false);
                    mViewBinding.sbLimitDistance.setEnabled(false);
                    mViewBinding.etHeightLimit.setEnabled(false);
                    mViewBinding.etDistanceLimit.setEnabled(false);

                    Toast.makeText(getContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    Toast.makeText(getContext(), R.string.string_tether_can_not_set, Toast.LENGTH_SHORT).show();
                    break;

                case 3:
                    Toast.makeText(getContext(), R.string.Msg_GoHomingUnSet, Toast.LENGTH_SHORT).show();
                    break;

                case 4:
                    Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        });
        mViewBinding.etHeightLimit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {//执行设置流程
                if(TextUtils.isEmpty(mViewBinding.etHeightLimit.getText())){
                    Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    setHeightFailHandle();
                    return;
                }
                String value = mViewBinding.etHeightLimit.getText().toString();
                if (CommonUtils.isEmptyString(value) || !CommonUtils.isNumber(value)) {
                    Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    setHeightFailHandle();
                    return;
                }
                int valueInt = UnitChnageUtils.inch2m(Integer.parseInt(value));
                if (valueInt < MyConstants.LIMIT_HEIGHT_MIN || valueInt > MyConstants.LIMIT_HEIGHT_MAX) {
                    Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    setHeightFailHandle();
                    return;
                }
                if (mViewBinding.ivSwitchLimitHeight.isSelected() && valueInt > MyConstants.LIMIT_HEIGHT_DEFAULT && preHeightLimit <= MyConstants.LIMIT_HEIGHT_DEFAULT){
                    showLimitHeightDialog(valueInt);
                } else {
                    baseViewModel.setLimitHeight(mViewBinding.ivSwitchLimitHeight.isSelected(), valueInt);
                }
            }
        });

        mViewBinding.etHeightLimit.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                return false;
            }
            mViewBinding.etHeightLimit.clearFocus();
            return false;
        });
        baseViewModel.getLimitHeight();
    }

    /**
     * 限制距离开关
     * */
    private void initLimitDistance(){
        preDistanceLimit = baseViewModel.getDefaultLimitDistance(getContext());
        mViewBinding.sbLimitDistance.setProgress(preDistanceLimit);
        mViewBinding.etDistanceLimit.setText(String.valueOf(preDistanceLimit));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mViewBinding.sbLimitDistance.setMin(MyConstants.LIMIT_DISTANCE_MIN);
        }
        mViewBinding.sbLimitDistance.setMax(MyConstants.LIMIT_DISTANCE_MAX);
        mUnitChnageUtils.showUnit(MyConstants.LIMIT_DISTANCE_MIN, mViewBinding.tvLimitDistanceMin);
        mUnitChnageUtils.showUnit(MyConstants.LIMIT_DISTANCE_MAX, mViewBinding.tvLimitDistanceMax);

        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            mViewBinding.etDistanceLimit.setText(String.valueOf(UnitChnageUtils.getUnitValue(mViewBinding.sbLimitDistance.getProgress())));
        }else {
            mViewBinding.etDistanceLimit.setText("INF");
            mViewBinding.etDistanceLimit.setEnabled(false);
            mViewBinding.sbLimitDistance.setEnabled(false);
            mViewBinding.sbLimitDistance.setProgress(0);
            mViewBinding.ivSwitchLimitDistance.setSelected(false);
            updateLimitDistanceView(false);
        }
        mViewBinding.etDistanceLimit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {//执行设置流程
                if(TextUtils.isEmpty(mViewBinding.etDistanceLimit.getText())){
                    Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    setDistanceFailHandle();
                    return;
                }
                String value = mViewBinding.etDistanceLimit.getText().toString();
                if (CommonUtils.isEmptyString(value) || !CommonUtils.isNumber(value)) {
                    Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    setDistanceFailHandle();
                    return;
                }
                int valueInt = UnitChnageUtils.inch2m(Integer.parseInt(value));
                if (valueInt < MyConstants.LIMIT_DISTANCE_MIN || valueInt > MyConstants.LIMIT_DISTANCE_MAX) {
                    Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    setDistanceFailHandle();
                    return;
                }
                baseViewModel.setLimitDistance(mViewBinding.ivSwitchLimitDistance.isSelected(), valueInt);
            }
        });
        mViewBinding.etDistanceLimit.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mViewBinding.etDistanceLimit.clearFocus();
            }
            return false;
        });
        baseViewModel.getLimitDistance();
    }

    private void setHeightFailHandle() {
        mViewBinding.sbLimitHeight.setProgress(preHeightLimit);
        String limitHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preHeightLimit));
        mViewBinding.etHeightLimit.setText(limitHeightStr);
    }

    private void setStateListener() {
//        GduApplication.getSingleApp().gduCommunication.addCycleACKCB(GduSocketConfig3.EXACT_BACK_STATE, (b, gduFrame3) -> {
//            if (gduFrame3 == null || gduFrame3.frameContent == null) {
//                return;
//            }
//            int exactBackState = gduFrame3.frameContent[0];
//            uiThreadHandle(() -> {
//                if (!isAdded()) {
//                    return;
//                }
//                updateExactBackState(exactBackState);
//            });
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        GduApplication.getSingleApp().gduCommunication.removeCycleACKCB(GduSocketConfig3.EXACT_BACK_STATE);
    }

    private void updateExactBackState(int exactBackState) {
        if (!isAdded() || getContext() == null) {
            return;
        }
        String state = "";
        if (exactBackState == 1) {
            state = "飞向移动home点上空中";
        } else if (exactBackState == 2) {
            state = "降落中";
        } else if (exactBackState == 3) {
            state = "差分位置失效,退出";
        } else if (exactBackState == 4) {
            state = "退出";
        } else if (exactBackState == 5) {
            state = "完成";
        } else if (exactBackState == 6) {
            state = "差分速度失效";
        } else if (exactBackState == 7) {
            state = "切姿态退出";
        } else if (exactBackState == 8) {
            state = "差分航向失效";
        } else if (exactBackState == 9) {
            state = "打杆退出";
        } else {
            state = String.valueOf(exactBackState);
        }
//        mViewBinding.tvExactBackState.setText("状态: " + state);
    }

    /**
     * 获取返航行为
     */
    private void getReturnHomeAction() {
//        GduApplication.getSingleApp().gduCommunication.getBackHomeAction((code, bean) -> {
//            MyLogUtils.i("getBackHomeAction callback() code = " + code);
//            if (!isAdded()) {
//                return;
//            }
//            if (code == GduConfig.OK && bean != null && bean.frameContent != null) {
//                MyLogUtils.i("getBackHomeAction callback() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
//                int position = bean.frameContent[2];
//                uiThreadHandle(() -> mViewBinding.ovReturnHome.setIndex(position));
//            }
//        });
    }

    private void getBatterySwitch() {
//        GduApplication.getSingleApp().gduCommunication.getSmartBattery((code, bean) -> {
//            MyLogUtils.i("getSmartBattery callBack() code = " + code + "; isAdded = " + isAdded());
//            if (!isAdded()) {
//                return;
//            }
//            if (code != GduConfig.OK || bean == null || bean.frameContent == null) {
//                return;
//            }
//            byte status = bean.frameContent[2];
//            GlobalVariable.isOpenSmartBattery = status != 0;
//            uiThreadHandle(() -> mViewBinding.smartBatterySwitch.setSelected(status != 0));
//        });
    }

    private void getOutOfControlAction() {
//        GduApplication.getSingleApp().gduCommunication.getOutOfControlAction((code, bean) -> {
//            if (!isAdded()) {
//                return;
//            }
//            if (code != GduConfig.OK || bean == null || bean.frameContent == null) {
//                return;
//            }
//            byte action = bean.frameContent[2];
//            if (bean.frameContent.length < 5) {
//                return;
//            }
//            short actionTime = ByteUtilsLowBefore.byte2short(bean.frameContent, 3);
//            setOutOfControlActionData(action, actionTime);
//        });
    }

    private void getBackInfo() {
//        GduApplication.getSingleApp().gduCommunication.getDroneBackInfo((code, bean) -> {
//            if (!isAdded()) {
//                return;
//            }
//            if (code != GduConfig.OK || bean == null || bean.frameContent == null) {
//                return;
//            }
//            short height = (ByteUtilsLowBefore.byte2short(bean.frameContent, 2));
//            short speed = (ByteUtilsLowBefore.byte2short(bean.frameContent, 4));
//            int goHomeSpeedMin = MyConstants.GO_HOME_SPEED_MIN * 100;
//            if (speed < goHomeSpeedMin) {
//                speed = (short) goHomeSpeedMin;
//            }
//            GlobalVariable.sBackSpeed = speed;
//            short finalSpeed = speed;
//            uiThreadHandle(() -> setBackSpeedData(finalSpeed));
//        });
    }

    private void getTripodMode() {
//        GduApplication.getSingleApp().gduCommunication.getTripodMode((code, bean) -> {
//            if (!isAdded()) {
//                return;
//            }
//            if (code == GduConfig.OK && bean != null && bean.frameContent != null && bean.frameContent.length > 2) {
//                isTfaMode = bean.frameContent[2] == 1;
//            } else {
//                isTfaMode = false;
//            }
//            uiThreadHandle(() -> switchFlightModeView(isTfaMode));
//        });
    }

    private void setOutOfControlActionData(byte action, short actionTime) {
//        if (action == 0) {
//            preOutOfControlAction = action;
//        } else if (action == 1 || action == 2) {
//            preOutOfControlAction = 1;
//            uiThreadHandle(() -> mViewBinding.tvReturnHomeTitle.setVisibility(View.VISIBLE));
//            uiThreadHandle(() -> mViewBinding.ovReturnHome.setVisibility(View.VISIBLE));
//        }
//        uiThreadHandle(() -> mViewBinding.ovOutOfControl.setIndex(preOutOfControlAction));
    }

    private void setBackSpeedData(short backSpeed) {
        MyLogUtils.i("hasGetBackSpeed() backSpeed = " + backSpeed);
        backSpeed /= 100;
        mViewBinding.sbBackSpeed.setProgress(backSpeed);
        preBackSpeed = backSpeed;
    }

    private void setListener() {
        mViewBinding.ivBack.setOnClickListener(this);
        mViewBinding.sbBackSpeed.setOnSeekBarChangeListener(backSpeedListener);
        mViewBinding.sbBackHeight.setOnSeekBarChangeListener(backHeightListener);
        mViewBinding.sbLimitHeight.setOnSeekBarChangeListener(limitHeightListener);
        mViewBinding.sbLimitDistance.setOnSeekBarChangeListener(limitDistanceListener);
        mViewBinding.ivSwitchLimitDistance.setOnClickListener(this);
        mViewBinding.ivSwitchLimitHeight.setOnClickListener(this);
//        mViewBinding.ivOnkeyDestroy.setOnClickListener(listener);
//        mViewBinding.ivSwitchSimulate.setOnClickListener(listener);
//        mViewBinding.ivSwitchRcShield.setOnClickListener(listener);
//        mViewBinding.tpsApsSwitch.setOnClickListener(listener);
//        mViewBinding.smartBatterySwitch.setOnClickListener(listener);
//        mViewBinding.tvAdvancedSetting.setOnClickListener(listener);
//        mViewBinding.tvSensorStatus.setOnClickListener(listener);
//        mViewBinding.ivModeTip.setOnClickListener(listener);
//        mViewBinding.tvExactBackOpen.setOnClickListener(listener);
//        mViewBinding.tvExactBackClose.setOnClickListener(listener);
//        mViewBinding.setHomeBtn.setOnClickListener(listener);
        mViewBinding.ivNoFlyBackSwitch.setOnClickListener(this);

//        mViewBinding.ovOutOfControl.setOnOptionClickListener((parentId, view, position) ->
//                GduApplication.getSingleApp().gduCommunication.setOutOfControlAction((byte) position, (code, bean) -> {
//                    if (!isAdded()) {
//                        return;
//                    }
//                    uiThreadHandle(() -> {
//                        if (code == GduConfig.OK) {
//                            mViewBinding.ovOutOfControl.setIndex(position);
//                            int visible;
//                            if (position == 1) {
//                                visible = View.VISIBLE;
//                            } else {
//                                visible = View.VISIBLE;
//                            }
//                            mViewBinding.tvReturnHomeTitle.setVisibility(visible);
//                            mViewBinding.ovReturnHome.setVisibility(visible);
//                            mDialogUtils.Toast(requireContext().getString(R.string.Label_SettingSuccess));
//                        } else {
//                            mDialogUtils.Toast(R.string.Label_SettingFail);
//                        }
//                    });
//                }));
//        mViewBinding.cbOpenSaveLocalCache.setOnCheckedChangeListener((buttonView, isChecked) -> RcStatusCheckManager.getInstance().cycleSendRockerInfo(isChecked));
//
//        mViewBinding.ovReturnHome.setOnOptionClickListener((parentId, view, position) -> {
//            MyLogUtils.i("onOptionClick() position = " + position);
//            GduApplication.getSingleApp().gduCommunication.setBackHomeAction((byte)position, (code, bean) -> {
//                MyLogUtils.i("setBackHomeAction callback() code = " + code);
//                if (!isAdded()) {
//                    return;
//                }
////                MyLogUtils.i("setBackHomeAction callback() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
//                if (code == GduConfig.OK) {
//                    uiThreadHandle(() -> {
//                        mViewBinding.ovReturnHome.setIndex(position);
//                        mDialogUtils.Toast(requireContext().getString(R.string.Label_SettingSuccess));
//                    });
//                } else {
//                    uiThreadHandle(() -> mDialogUtils.Toast(R.string.Label_SettingFail));
//                }
//            });
//        });
//
//        /**
//         * 设置卫星定位系统 模式
//         */
//        mViewBinding.ovGnss.setOnOptionClickListener((parentId, view, position) -> {
//            if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
//                mDialogUtils.Toast(R.string.DeviceNoConn);
//                return;
//            }
//            if (GlobalVariable.droneFlyState != 1) {
//                mDialogUtils.Toast(R.string.string_flying_forbid);
//                return;
//            }
//            String rtkStatus = GlobalVariable.rtk_model.getRtk1_status();
//            if (rtkStatus.equals("Fixed") || rtkStatus.equals("Float")) {
//                mDialogUtils.Toast(R.string.gnss_exit_rtk);
//                return;
//            }
//            new CommonDialog2Btn.Builder<>(getContext()).setTitle(null).setAutoDismiss(true)
//                    .setMsg(R.string.gnss_switch_hint).setCancel(R.string.Label_cancel).setConfirm(R.string.Label_Sure)
//                    .setListener(new CommonDialog2Btn.OnListener() {
//                        @Override
//                        public void onConfirm(BaseDialog dialog) {
//                            byte isOpen;
//                            if (position == 0) {
//                                isOpen = 7;
//                            } else {
//                                isOpen = 6;
//                            }
//                            // 大华渠道S200系列软件支持单北斗模式(目前会切换失败 但是需要返回成功，做个假的支持)
//                            if ((ChannelUtils.isDahua(getContext()) || ChannelUtils.isDahuaBDS(getContext())) && CommonUtils.curPlanIsSmallFlight()) {
//                                GlobalVariable.sGNSSType = isOpen;
//                                SPUtils.put(GduAppEnv.application, "sGNSSType", (int) isOpen);
//                                uiThreadHandle(() -> {
//                                    mDialogUtils.Toast(requireContext().getString(R.string.Label_SettingSuccess));
//                                    mViewBinding.ovGnss.setIndex(position);
//                                });
//                            } else {
//                                GduApplication.getSingleApp().gduCommunication.change482RtkStates(isOpen, (code, bean) -> {
//                                    if (!isAdded()) {
//                                        return;
//                                    }
//                                    if (code == GduConfig.OK) {
//                                        uiThreadHandle(() -> {
//                                            mDialogUtils.Toast(requireContext().getString(R.string.Label_SettingSuccess));
//                                            mViewBinding.ovGnss.setIndex(position);
//                                        });
//                                    } else {
//                                        uiThreadHandle(() -> mDialogUtils.Toast(R.string.Label_SettingFail));
//                                    }
//                                });
//                            }
//                        }
//                    })
//                    .show();
//        });
//
//        mViewBinding.btnRefreshNoFlyZone.setOnClickListener(v -> refreshNoFlyZone());
//
//        mViewBinding.tabFlyModel.setOnTabSelectListener(new OnTabSelectListener() {
//            @Override
//            public void onTabSelect(int position) {
//
//                mViewBinding.tabFlyModel.setCurrentTab(isTfaMode ? 1 : 0);
//                if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
//                    mDialogUtils.Toast(R.string.DeviceNoConn);
//                    return;
//                }
//                if (GlobalVariable.droneFlyState == 1 || GlobalVariable.droneFlyState == 4) {
//                } else {
//                    mDialogUtils.Toast(getString(R.string.string_flying_forbid));
//                    return;
//                }
//                if (position == 0) {
//                    if (!isTfaMode) {
//                        return;
//                    }
//                    mDialogUtils.createDialogWith2Btn(requireActivity().getString(R.string.string_change_to_pfa),
//                            getResources().getString(R.string.Label_pfa_hint),
//                            getString(R.string.Label_cancel),
//                            getString(R.string.Label_Sure), new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//
//                                    if (v.getId() == R.id.dialog_btn_sure) {
//                                        setTripodMode(false);
//                                    }
//                                    mDialogUtils.cancelDialog();
//                                }
//                            });
//
//                } else {
//                    if (isTfaMode) {
//                        return;
//                    }
//                    mDialogUtils.createDialogWith2Btn(getString(R.string.string_change_to_tfa),
//                            getResources().getString(R.string.Label_tfa_hint),
//                            getString(R.string.Label_cancel),
//                            getString(R.string.Label_Sure), new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    if (v.getId() == R.id.dialog_btn_sure) {
//                                        setTripodMode(true);
//                                    }
//                                    mDialogUtils.cancelDialog();
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onTabReselect(int position) {
//
//            }
//        });
//
//        mViewBinding.etHeight.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {//执行设置流程
//                String value = mViewBinding.etHeight.getText().toString();
//                if (CommonUtils.isEmptyString(value) || !CommonUtils.isNumber(value)) {
//                    Toaster.show(getString(R.string.input_error));
//                    String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preBackHeight));
//                    mViewBinding.etHeight.setText(goHomeHeightStr);
//                    return;
//                }
//                int valueInt = UnitChnageUtils.inch2m(Integer.parseInt(value));
//                if (isOpenLimitHeight && valueInt > preHeightLimit) {
//                    Toaster.show(getString(R.string.input_error));
//                    String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preBackHeight));
//                    mViewBinding.etHeight.setText(goHomeHeightStr);
//                    return;
//                }
//                if (mLimitHeightOrDistanceSetPresenter != null) {
//                    mLimitHeightOrDistanceSetPresenter.setGoHomeHeight(valueInt);
//                }
//            }
//        });
//        mViewBinding.etHeight.setOnEditorActionListener((textView, actionId, keyEvent) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                mViewBinding.etHeight.clearFocus();
//            }
//            return false;
//        });
//
//        mViewBinding.etBackSpeed.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus) {//执行设置流程
//                if (!connStateToast()) {
//                    mViewBinding.sbBackSpeed.setProgress(preBackSpeed);
//                    mViewBinding.etBackSpeed.setText(String.valueOf(preBackSpeed));
//                    return;
//                }
//
//                if (GlobalVariable.backState == 2) {//返航中不允许设置返航速度
//                    mViewBinding.sbBackSpeed.setProgress(preBackSpeed);
//                    mViewBinding.etBackSpeed.setText(String.valueOf(preBackSpeed));
//                    Toaster.show(getString(R.string.string_returning_cannot_set_back_speed));
//                    return;
//                }
//
//                String value = mViewBinding.etBackSpeed.getText().toString();
//                if (CommonUtils.isEmptyString(value) || !CommonUtils.isNumber(value)) {
//                    mViewBinding.sbBackSpeed.setProgress(preBackSpeed);
//                    mViewBinding.etBackSpeed.setText(String.valueOf(preBackSpeed));
//                    Toaster.show(getString(R.string.input_error));
//                    return;
//                }
//                int valueInt = UnitChnageUtils.inch2m(Integer.parseInt(value));
//                if (valueInt < MyConstants.GO_HOME_SPEED_MIN || valueInt > MyConstants.GO_HOME_SPEED_MAX) {
//                    mViewBinding.sbBackSpeed.setProgress(preBackSpeed);
//                    mViewBinding.etBackSpeed.setText(String.valueOf(preBackSpeed));
//                    Toaster.show(getString(R.string.input_error));
//                    return;
//                }
//                curBackSpeed = valueInt;
//                setFlyBackSpeed(valueInt);
//            }
//        });
//        mViewBinding.etBackSpeed.setOnEditorActionListener((textView, actionId, keyEvent) -> {
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                mViewBinding.etBackSpeed.clearFocus();
//            }
//            return false;
//        });
//
//        KeyboardUtils.setListener(getActivity(), new KeyboardUtils.OnSoftKeyBoardChangeListener() {
//            @Override
//            public void keyBoardShow(int height) {
//            }
//
//            @Override
//            public void keyBoardHide(int height) {
//            }
//        });

//        mViewBinding.btSetFlyTime.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String time = mViewBinding.tvSetFlyTime.getText().toString().trim();
//                if (!TextUtils.isEmpty(time)) {
//                    GlobalVariableTest.sFlyTime = Integer.parseInt(time);
//                }
//            }
//        });
    }

    private void refreshNoFlyZone() {
//        String lngStr = mViewBinding.lngEdittext.getText().toString().trim();
//        String latStr = mViewBinding.latEdittext.getText().toString().trim();
//        if (TextUtils.isEmpty(lngStr) || TextUtils.isEmpty(latStr)) {
//            return;
//        }
//        double lon = Double.parseDouble(lngStr);
//        double lat = Double.parseDouble(latStr);
//        NoFlyZoneTestEvent event = new NoFlyZoneTestEvent(lat, lon);
//        EventBus.getDefault().post(event);
    }

    @Override
    public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_back:
                    updateBackView();
                    break;
                case R.id.iv_switch_limit_height:
                    // 限高
                    if (!GlobalVariable.isNewHeightLimitStrategy){
                        if (!connStateToast()) {
                            return;
                        }
                    }
                    if (GlobalVariable.isTetherModel) {
                        Toast.makeText(getContext(), R.string.string_tether_can_not_set, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    switchLimitHeight();
                    break;
                case R.id.iv_switch_limit_distance:
                    // 限距
                    baseViewModel.setLimitDistance(!mViewBinding.ivSwitchLimitDistance.isSelected(), preDistanceLimit);
                    break;
                case R.id.tps_aps_switch:
                    if (!connStateToast()) {
                        return;
                    }
                    if (GlobalVariable.isTetherModel) {
//                        Toaster.show(getString(R.string.string_tether_can_not_set));
                        return;
                    }
                    SingleClickUtil.onSingleClick(view, v -> {
                        boolean isSelected = !mViewBinding.tpsApsSwitch.isSelected();
//                        setSwitchFlyModeState(isSelected);
                    });
                    break;
                case R.id.tv_advanced_setting:
//                    setSecondLevelView(mViewBinding.viewAdjustScenario, true, getString(R.string.advanced_settings));
                    currentSecondLevelType = 1;
                    break;
                case R.id.tv_sensor_status:
//                    setSecondLevelView(mViewBinding.vSensorStatus, true, getString(R.string.sensor_status));
                    currentSecondLevelType = 2;
//                    break;
//                case R.id.iv_switch_rc_shield:
//                    boolean isRCOpen = !mViewBinding.ivSwitchRcShield.isSelected();
//                    mViewBinding.ivSwitchRcShield.setSelected(isRCOpen);
//                    GduApplication.getSingleApp().gduCommunication.rcShield(isRCOpen, (code, bean) -> {
//                        MyLogUtils.i("rcShield callBack() code = " + code);
//                        if (!isAdded()) {
//                            return;
//                        }
//                        short cmd = 0;
//                        if (bean != null && bean.frameContent != null) {
//                            cmd = ByteUtilsLowBefore.byte2short(bean.frameContent, 0);
//                        }
//                        if (cmd == GduConfig.RECEIVE_MESSAGE) {
//                            uiThreadHandle(() -> {
//                                mViewBinding.ivSwitchRcShield.setSelected(isRCOpen);
//                                mDialogUtils.Toast(R.string.Label_SettingSuccess);
//                            });
//                        } else {
//                            uiThreadHandle(() -> {
//                                mViewBinding.ivSwitchRcShield.setSelected(!isRCOpen);
//                                mDialogUtils.Toast(R.string.Label_SettingFail);
//                            });
//                        }
//                    });
//                    break;
//                case R.id.iv_onkey_destroy:
//                    new CommonDialog2Btn.Builder<>(getActivity()).setTitle(null).setAutoDismiss(true)
//                            .setMsg(R.string.Label_confirm_lock)
//                            .setListener(new CommonDialog2Btn.OnListener() {
//                                @Override
//                                public void onConfirm(BaseDialog dialog) {
//                                    GduApplication.getSingleApp().gduCommunication.oneKeyLockDrone((code, bean) -> {
//                                        MyLogUtils.i("destroyPlane callBack() code = " + code);
//                                        if (!isAdded()) {
//                                            return;
//                                        }
//                                        uiThreadHandle(() -> {
//                                            if (bean != null && bean.frameContent[0] == GduConfig.OK) {
//                                                mDialogUtils.Toast(R.string.string_the_aerocraft_boom);
//                                            } else {
//                                                mDialogUtils.Toast(R.string.Label_SettingFail);
//                                            }
//                                        });
//                                    });
//                                }
//                            }).show();
//                    break;
//                case R.id.smart_battery_switch:
                    // 智能电池
//                    if (!connStateToast()) {
//                        return;
//                    }
//                    boolean isOn = !mViewBinding.smartBatterySwitch.isSelected();
//                    GduApplication.getSingleApp().gduCommunication.switchSmartBattery(isOn, (code, bean) -> {
//                        if (!isAdded()) {
//                            return;
//                        }
//                        uiThreadHandle(() -> {
//                            if (code == GduConfig.OK) {
//                                GlobalVariable.isOpenSmartBattery = isOn;
//                                mViewBinding.smartBatterySwitch.setSelected(isOn);
//                            } else {
//                                GlobalVariable.isOpenSmartBattery = !isOn;
//                                mViewBinding.smartBatterySwitch.setSelected(!isOn);
//                                mDialogUtils.Toast(getString(R.string.Label_SettingFail));
//                            }
//                        });
//                    });
                    break;
                case R.id.iv_mode_tip:
//                    mDialogUtils.createFlyModelTipDialog(getContext(), true);
                    break;
//                case R.id.tv_exact_back_open:
//                    startExactBack();
//                    break;
//                case R.id.tv_exact_back_close:
//                    GduApplication.getSingleApp().gduCommunication.exactBack(false, (short) 0, (short) 0,
//                            (short) 0, (code, bean) -> uiThreadHandle(() -> {
//                                if (!isAdded()) {
//                                    return;
//                                }
//                                if (code == GduConfig.OK) {
//                                    Toaster.show(getString(R.string.Label_SettingSuccess));
//                                } else {
//                                    Toaster.show(getString(R.string.Label_SettingFail));
//                                }
//                            }));
//                    break;
//                case R.id.set_home_btn:
//                    setHomeInfo();
//                    break;
                case R.id.iv_no_fly_back_switch:
                    changeNoFlyAction();
                    break;
                default:
                    break;
            }
    }

    private void changeNoFlyAction( ) {
        boolean change = !mViewBinding.ivNoFlyBackSwitch.isSelected();
//        GduApplication.getSingleApp().gduCommunication.changeNoFlyAreAction(change, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                if (isAdded()) {
//                    uiThreadHandle(() -> {
//                        if (code == GduConfig.OK) {
//                            mViewBinding.ivNoFlyBackSwitch.setSelected(change);
//                            Toaster.show(R.string.Label_SettingSuccess);
//                        } else {
//                            Toaster.show(R.string.Label_SettingFail);
//                        }
//                    });
//                }
//            }
//        });

    }

    private void startExactBack() {
//        String front = mViewBinding.etFront.getText().toString().trim();
//        if (!CommonUtils.isNumber(front)) {
//            mDialogUtils.Toast(R.string.input_error);
            return;
//        }
//        String right = mViewBinding.etRight.getText().toString().trim();
//        if (!CommonUtils.isNumber(right)) {
//            mDialogUtils.Toast(R.string.input_error);
//            return;
//        }
//        String top = mViewBinding.etTop.getText().toString().trim();
//        if (!CommonUtils.isNumber(top)) {
//            mDialogUtils.Toast(R.string.input_error);
//            return;
//        }
//
//        int frontDis = Integer.parseInt(front);
//        int rightDis = Integer.parseInt(right);
//        int topDis = Integer.parseInt(top);

//        SPUtils.put(GduApplication.context, "exact_back_front", String.valueOf(frontDis));
//        SPUtils.put(GduApplication.context, "exact_back_right", String.valueOf(rightDis));
//        SPUtils.put(GduApplication.context, "exact_back_top", String.valueOf(topDis));
//
//        GduApplication.getSingleApp().gduCommunication.exactBack(true, (short) frontDis, (short) rightDis, (short) topDis, new SocketCallBack3() {
//            @Override
//            public void callBack(int code, GduFrame3 bean) {
//                uiThreadHandle(() -> {
//                    if (isAdded()) {
//                        if (code == GduConfig.OK) {
//                            Toaster.show(getString(R.string.Label_SettingSuccess));
//                        } else {
//                            Toaster.show(getString(R.string.Label_SettingFail));
//                        }
//                    }
//                });
//            }
//        });
    }

    /**
     * 设置home点信息，包括坐标和绝对高度
     */
    private void setHomeInfo(){
//         String lats = mViewBinding.homeLatEdittext.getText().toString().trim();
//         String lngs = mViewBinding.homeLngEdittext.getText().toString().trim();
//        if (CommonUtils.isNumber(lats) && CommonUtils.isNumber(lngs)) {
//            double lat = Double.parseDouble(lats);
//            double lng = Double.parseDouble(lngs);
//            GduApplication.getSingleApp().gduCommunication.setHomePoint(lat, lng, (byte) 0, new SocketCallBack3() {
//                @Override
//                public void callBack(int code, GduFrame3 bean) {
//                    uiThreadHandle(() -> {
//                        if (code == GduConfig.OK) {
//                            mDialogUtils.Toast(R.string.Label_SettingSuccess);
//                        } else {
//                            mDialogUtils.Toast(R.string.Label_SettingFail);
//                        }
//                    });
//                }
//            });
//        }
//        String elevations = mViewBinding.homeElevationEdittext.getText().toString().trim();
//        if (CommonUtils.isNumber(elevations)) {
//            int elevation = Integer.parseInt(elevations);
//            GduApplication.getSingleApp().gduCommunication.setHomeHeight((byte) 0, elevation, new SocketCallBack3() {
//                @Override
//                public void callBack(int code, GduFrame3 bean) {
//                    uiThreadHandle(() -> {
//                        if (code == GduConfig.OK) {
//                            mDialogUtils.Toast(R.string.Label_SettingSuccess);
//                        } else {
//                            mDialogUtils.Toast(R.string.Label_SettingFail);
//                        }
//                    });
//                }
//            });
//        }
    }

    private void showLimitHeightDialog(int limit){
        new CommonDialog.Builder(getChildFragmentManager())
                .setTitle(getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                .setContent(getString(R.string.limit_height_statement_content))
                .setCancel(getString(R.string.think_moment))
                .setSure(getString(R.string.think_agree))
                .setCancelableOutside(false)
                .setPositiveListener((dialogInterface, i) -> baseViewModel.setLimitHeight(true, limit))
                .setNegativeListener((dialogInterface, i) -> setHeightFailHandle()).build().show();
    }

    private void switchLimitHeight() {
        if (mViewBinding.ivSwitchLimitHeight.isSelected() || preHeightLimit > MyConstants.LIMIT_HEIGHT_DEFAULT) {
            new CommonDialog.Builder(getChildFragmentManager())
                    .setTitle(getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                    .setContent(getString(R.string.limit_height_statement_content))
                    .setCancel(getString(R.string.think_moment))
                    .setSure(getString(R.string.think_agree))
                    .setCancelableOutside(false)
                    .setPositiveListener((dialogInterface, i) -> {
                        pre_switch_limit_height = false;
                        if (null!=baseViewModel)
                            baseViewModel.setLimitHeight(!mViewBinding.ivSwitchLimitHeight.isSelected(), preHeightLimit);
                    }).build().show();
        } else {
            pre_switch_limit_height = true;
            if (null!=baseViewModel)
                baseViewModel.setLimitHeight(true, preHeightLimit);
        }
    }

    private void updateBackView() {
//        if (currentSecondLevelType == 1) {
//            setSecondLevelView(mViewBinding.viewAdjustScenario, false, "");
//        } else if (currentSecondLevelType == 2) {
//            setSecondLevelView(mViewBinding.vSensorStatus, false, "");
//        }
        currentSecondLevelType = 0;
    }

    private void setSecondLevelView(View view, boolean show, String title) {

//        MyAnimationUtils.animatorRightInOut(view, show);
        if (show) {
            mViewBinding.ivBack.setVisibility(View.VISIBLE);
            mViewBinding.tvTitle.setText(title);
        } else {
            mViewBinding.ivBack.setVisibility(View.GONE);
            mViewBinding.tvTitle.setText(R.string.title_fly);
        }
    }

    /**
     * 开启或关闭三脚架模式
     *
     * @param isOpen
     */
    private void setTripodMode(boolean isOpen) {
//        GduApplication.getSingleApp().gduCommunication.setTripodMode(isOpen, (code, bean) -> {
//
//            uiThreadHandle(() -> {
//                if (!isAdded()) {
//                    return;
//                }
//                if (code == GduConfig.OK) {
//                    mDialogUtils.Toast(getString(R.string.Label_SettingSuccess));
//                    isTfaMode = isOpen;
//                } else {
//                    if (GlobalVariable.droneFlyState == 1 || GlobalVariable.droneFlyState == 4) {
//                        mDialogUtils.Toast(getString(R.string.Label_SettingFail));
//                    } else {
//                        mDialogUtils.Toast(getString(R.string.string_flying_forbid));
//                    }
////                    mDialogUtils.Toast(getString(R.string.Label_SettingFail));
//                }
//                switchFlightModeView(isTfaMode);
//            });
//        });
    }

    /**
     * 返航速度的拖拽进度条进度监听
     */
    private final SeekBar.OnSeekBarChangeListener backSpeedListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress < MyConstants.GO_HOME_SPEED_MIN) {
                mViewBinding.sbBackSpeed.setProgress(MyConstants.GO_HOME_SPEED_MIN);
                progress = MyConstants.GO_HOME_SPEED_MIN;
            }
            //显示当前调节的高度---ron
//            binding.tvBackSpeed.setText(mUnitChnageUtils.getUnitSpeedString(progress));
            mViewBinding.etBackSpeed.setText(String.valueOf(UnitChnageUtils.getUnitValue(progress)));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
//            if (!connStateToast()) {
//                mViewBinding.sbBackSpeed.setProgress(preBackSpeed);
//                mViewBinding.sbBackSpeed.setEnabled(false);
//                return;
//            }
            if (GlobalVariable.backState == 2) {//返航中不允许设置返航速度
                mViewBinding.sbBackSpeed.setProgress(preBackSpeed);
                mViewBinding.etBackSpeed.setText(String.valueOf(preBackSpeed));
//                Toaster.show(getString(R.string.string_returning_cannot_set_back_speed));
                return;
            }
            curBackSpeed = seekBar.getProgress();
            setFlyBackSpeed(curBackSpeed);
        }
    };


    /**
     * 返航高度的拖拽进度条进度监听
     */
    private final SeekBar.OnSeekBarChangeListener backHeightListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress < MyConstants.GO_HOME_HEIGHT_MIN) {
                mViewBinding.sbBackHeight.setProgress(MyConstants.GO_HOME_HEIGHT_MIN);
                progress = MyConstants.GO_HOME_HEIGHT_MIN;
            }
            //显示当前调节的高度---ron
            String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(progress));
            mViewBinding.etHeight.setText(goHomeHeightStr);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
//            if (!connStateToast()) {
//                mViewBinding.sbBackHeight.setProgress(preBackHeight);
//                return;
//            }
//            if (mLimitHeightOrDistanceSetPresenter != null) {
//                mLimitHeightOrDistanceSetPresenter.setGoHomeHeight(seekBar.getProgress());
//            }
        }
    };


    /**
     * 高度限制的拖拽进度条进度监听
     */
    private final SeekBar.OnSeekBarChangeListener limitHeightListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress < MyConstants.LIMIT_HEIGHT_MIN) {
                mViewBinding.sbLimitHeight.setProgress(MyConstants.LIMIT_HEIGHT_MIN);
                progress = MyConstants.LIMIT_HEIGHT_MIN;
            }
            if (mViewBinding.sbLimitHeight.isEnabled()) {
                mViewBinding.etHeightLimit.setText(String.valueOf(UnitChnageUtils.getUnitValue(progress)));
            } else {
                mViewBinding.etHeightLimit.setText("INF");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            if (!GlobalVariable.isNewHeightLimitStrategy){
                if (!connStateToast()) {
                    mViewBinding.sbLimitHeight.setProgress(preHeightLimit);
                    mViewBinding.ivSwitchLimitHeight.setSelected(false);
                    mViewBinding.sbLimitHeight.setEnabled(false);
                    updateLimitHeightView(false);
                    return;
                }
            }
            int limitHeight = seekBar.getProgress();
            if (limitHeight < preBackHeight) {
                mViewBinding.sbLimitHeight.setProgress(preHeightLimit);
                mViewBinding.etHeightLimit.setText(String.valueOf(preHeightLimit));
                Toast.makeText(getContext(), R.string.Msg_LimitHeight_LessThan_BackHeight, Toast.LENGTH_SHORT).show();
                return;
            }
            //设置高度限制大于120需要免责声明  余浩
            if (mViewBinding.ivSwitchLimitHeight.isSelected() && limitHeight > MyConstants.LIMIT_HEIGHT_DEFAULT && preHeightLimit <= MyConstants.LIMIT_HEIGHT_DEFAULT) {
                new CommonDialog.Builder(getChildFragmentManager())
                        .setTitle(getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                        .setContent(getString(R.string.limit_height_statement_content))
                        .setCancel(getString(R.string.think_moment))
                        .setSure(getString(R.string.think_agree))
                        .setCancelableOutside(false)
                        .setPositiveListener((dialogInterface, i) -> baseViewModel.setLimitHeight(mViewBinding.ivSwitchLimitHeight.isSelected(), limitHeight))
                        .setNegativeListener((dialogInterface, i) -> mViewBinding.sbLimitHeight.setProgress(preHeightLimit)).build().show();
            } else {
                baseViewModel.setLimitHeight(mViewBinding.ivSwitchLimitHeight.isSelected(), limitHeight);
            }
        }
    };

    private void switchFlightModeView(boolean isTfaMode) {
        if (!isAdded()) {
            return;
        }
        mViewBinding.tvPfaTfaModeContent.setText(isTfaMode ? getString(R.string.Msg_tfa_mode) : getString(R.string.Msg_pfa_mode));
        mViewBinding.ivPfaTfaImageview.setImageResource(isTfaMode ? R.drawable.icon_pfa_1 : R.drawable.icon_pfa_2);
        mViewBinding.tabFlyModel.setCurrentTab(isTfaMode ? 1 : 0);
    }

    /**
     * 距离限制的拖拽进度条的进度监听
     */
    private final SeekBar.OnSeekBarChangeListener limitDistanceListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress < MyConstants.LIMIT_DISTANCE_MIN) {
                mViewBinding.sbLimitDistance.setProgress(MyConstants.LIMIT_DISTANCE_MIN);
                progress = MyConstants.LIMIT_DISTANCE_MIN;
            }
            if (mViewBinding.sbLimitDistance.isEnabled()) {
                mViewBinding.etDistanceLimit.setText(String.valueOf(UnitChnageUtils.getUnitValue(progress)));
            } else {
                mViewBinding.etDistanceLimit.setText("INF");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!connStateToast()) {
                //distance
                mViewBinding.sbLimitDistance.setProgress(preDistanceLimit);
                mViewBinding.ivSwitchLimitDistance.setSelected(false);
                mViewBinding.sbLimitDistance.setEnabled(false);
                // ViewUtils.setViewShowOrHide(mViewBinding.layoutDistanceLimit, false);
                updateLimitDistanceView(false);
                return;
            }
            baseViewModel.setLimitDistance(mViewBinding.ivSwitchLimitDistance.isSelected(), seekBar.getProgress());
        }
    };

    private void setFlyBackSpeed(int backSpeed) {
//        GduApplication.getSingleApp().gduCommunication.setBackSpeed((short) backSpeed, (code, bean) -> {
//            if (!isAdded()) {
//                return;
//            }
//            uiThreadHandle(() -> {
//                if (code == GduConfig.OK) {
//                    mDialogUtils.Toast(getString(R.string.Label_SettingSuccess));
//                    preBackSpeed = curBackSpeed;
//                    mViewBinding.sbBackSpeed.setProgress(curBackSpeed);
//                    mViewBinding.etBackSpeed.setText(String.valueOf(curBackSpeed));
//                } else {
//                    mDialogUtils.Toast(getString(R.string.Label_SettingFail));
//                    mViewBinding.sbBackSpeed.setProgress(preBackSpeed);
//                    mViewBinding.etBackSpeed.setText(String.valueOf(preBackSpeed));
//                }
//            });
//        });
    }

    private void setLimitData() {
////
////
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            mViewBinding.sbBackSpeed.setMin(MyConstants.GO_HOME_SPEED_MIN);
////        }
////        mViewBinding.sbBackSpeed.setMax(MyConstants.GO_HOME_SPEED_MAX);
////        mViewBinding.tvMinSpeedLabel.setText(mUnitChnageUtils.getUnitSpeedString(MyConstants.GO_HOME_SPEED_MIN));
////        mViewBinding.tvMaxSpeedLabel.setText(mUnitChnageUtils.getUnitSpeedString(MyConstants.GO_HOME_SPEED_MAX));
////
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////            mViewBinding.sbBackHeight.setMin(MyConstants.GO_HOME_HEIGHT_MIN);
////        }
////        AppLog.i("wsd","setLimitData-sbBackHeight setMax:"+MyConstants.GO_HOME_HEIGHT_MAX);
////        mViewBinding.sbBackHeight.setMax(isOpenLimitHeight ? preHeightLimit : MyConstants.GO_HOME_HEIGHT_MAX);
////        mUnitChnageUtils.showUnit(MyConstants.GO_HOME_HEIGHT_MIN, mViewBinding.tvMinBackHeight);
////        mUnitChnageUtils.showUnit(isOpenLimitHeight ? preHeightLimit : MyConstants.GO_HOME_HEIGHT_MAX, mViewBinding.tvMaxBackHeight);
//
////        if (GlobalVariable.backHeight > 0) {
////            preBackHeight = GlobalVariable.backHeight / 10;
////            mViewBinding.sbBackHeight.setProgress(preBackHeight);
////            String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preBackHeight));
////            mViewBinding.etHeight.setText(goHomeHeightStr);
////        }
//
//        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
////            mViewBinding.etHeight.setText(String.valueOf(UnitChnageUtils.getUnitValue(mViewBinding.sbBackHeight.getProgress())));
////            mViewBinding.etBackSpeed.setText(String.valueOf(UnitChnageUtils.getUnitValue(mViewBinding.sbBackSpeed.getProgress())));
////
////            mViewBinding.etHeight.setEnabled(true);
////            mViewBinding.etBackSpeed.setEnabled(true);
////            mViewBinding.sbBackHeight.setEnabled(true);
////            mViewBinding.sbBackSpeed.setEnabled(true);
//        } else {
////            mViewBinding.etHeight.setEnabled(false);
////            mViewBinding.sbBackHeight.setEnabled(false);
////            mViewBinding.etBackSpeed.setEnabled(false);
////            mViewBinding.sbBackSpeed.setEnabled(false);
//
//        }
    }

    private boolean connStateToast() {
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                Toast.makeText(getContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
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

//    @Subscribe
//    public void onEventMainThread(ChangeUnitEvent event) {
//        setLimitData();//动态变化参数单位需要
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        EventBus.getDefault().unregister(this);
//    }

    public static SettingFlyFragment newInstance() {
        Bundle args = new Bundle();
        SettingFlyFragment fragment = new SettingFlyFragment();
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void setGoHomeHeightSuc(int value) {
//        MyLogUtils.i("setGoHomeHeightSuc() value = " + value);
//        uiThreadHandle(() -> {
//            if (mDialogUtils != null) {
//                mDialogUtils.Toast(getString(R.string.Label_SettingSuccess));
//            }
//            preBackHeight = value;
//            mViewBinding.sbBackHeight.setProgress(value);
//            String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(value));
//            mViewBinding.etHeight.setText(goHomeHeightStr);
//        });
//    }
//
//    @Override
//    public void setGoHomeHeightFail() {
//        MyLogUtils.i("setGoHomeHeightFail()");
//        uiThreadHandle(() -> {
//            if (mDialogUtils != null) {
//                mDialogUtils.Toast(getString(R.string.Label_SettingFail));
//            }
//            mViewBinding.sbBackHeight.setProgress(preBackHeight);
//            String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preBackHeight));
//            mViewBinding.etHeight.setText(goHomeHeightStr);
//        });
//    }
//
//
//    @Override
//    public void changeLimitHeightFail(boolean isOpen, int value) {
//        MyLogUtils.i("changeLimitHeightFail() isOpen = " + isOpen + "; value = " + value);
//        uiThreadHandle(() -> {
//            showErrTipMsg();
//            setHeightFailHandle();
//        });
//    }
//
//    @Override
//    public void updateLimitDistanceValue(boolean isOpen, int value, boolean isSet) {
//        MyLogUtils.i("updateLimitDistanceValue() isOpen = " + isOpen + "; value = " + value + "; isSet = " + isSet);
//        uiThreadHandle(() -> {
//            if (isSet && mDialogUtils != null && isOpen) {
//                mDialogUtils.Toast(getString(R.string.Label_SettingSuccess));
//            }
//            mViewBinding.ivSwitchLimitDistance.setSelected(isOpen);
//            updateLimitDistanceView(isOpen);
//            if (isOpen || (value >= MyConstants.LIMIT_DISTANCE_MIN && value <= MyConstants.LIMIT_DISTANCE_MAX)) {
//                mViewBinding.sbLimitDistance.setProgress(value);
//                String limitDisStr = String.valueOf(UnitChnageUtils.getUnitValue(value));
//                mViewBinding.etDistanceLimit.setText(limitDisStr);
//                preDistanceLimit = value;
//            }
//            AppLog.e("SettingFlyFragment", "距离限制 = " + GlobalVariable.limitDiatsnce);
//        });
//    }

    private void updateLimitHeightView(boolean isOpen) {
        if (isOpen) {
            mViewBinding.etHeightLimit.setTextColor(GduAppEnv.application.getResources().getColor(R.color.color_EF4E22, null));
        } else {
            mViewBinding.etHeightLimit.setTextColor(GduAppEnv.application.getResources().getColor(R.color.color_9D9D9D, null));
        }
    }

    private void updateLimitDistanceView(boolean isOpen) {
        if (isOpen) {
            mViewBinding.etDistanceLimit.setTextColor(GduAppEnv.application.getResources().getColor(R.color.color_EF4E22, null));
        } else {
            mViewBinding.etDistanceLimit.setTextColor(GduAppEnv.application.getResources().getColor(R.color.color_9D9D9D, null));
        }
    }
//
//    @Override
//    public void changeLimitDistanceFail(boolean isOpen, int value) {
//        MyLogUtils.i("changeLimitDistanceFail() isOpen = " + isOpen + "; value = " + value);
//        uiThreadHandle(() -> {
//            showErrTipMsg();
//            setDistanceFailHandle();
//        });
//    }
//
    private void setDistanceFailHandle() {
        mViewBinding.sbLimitDistance.setProgress(preDistanceLimit);
        String limitDisStr = String.valueOf(UnitChnageUtils.getUnitValue(preDistanceLimit));
        mViewBinding.etDistanceLimit.setText(limitDisStr);
    }

    private void setBackHeightFailHandle() {
        mViewBinding.sbBackHeight.setProgress(preBackHeight);
        String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preBackHeight));
        mViewBinding.etHeight.setText(goHomeHeightStr);
    }
//
//    @Override
//    public void setHeightBelowCurValueErr() {
//        MyLogUtils.i("setHeightBelowCurValueErr()");
//        if (mDialogUtils == null) {
//            return;
//        }
//        uiThreadHandle(() -> {
//            setHeightFailHandle();
//            String invalidHeight = UnitChnageUtils.getUnitValue(10) + UnitChnageUtils.getUnit();
//            String result = String.format(ResourceUtil.getStringById(R.string.Label_set_invalid_height), invalidHeight);
//            mDialogUtils.Toast(result);
//        });
//    }
//
//    @Override
//    public void setDistanceBelowCurValueErr() {
//        MyLogUtils.i("setDistanceBelowCurValueErr");
//        if (mDialogUtils == null) {
//            return;
//        }
//        uiThreadHandle(() -> {
//            setDistanceFailHandle();
//            mDialogUtils.Toast(R.string.Label_set_invalid_distance);
//        });
//    }
//
//    private void showErrTipMsg() {
//        if (mDialogUtils == null) {
//            return;
//        }
//        if (!GlobalVariable.isActive) {
//            mDialogUtils.Toast(getString(R.string.Err_DevUnActiveRetryTip));
//        } else {
//            mDialogUtils.Toast(getString(R.string.Label_SettingFail));
//        }
//    }
//
//    /**
//     * 获取模式切换开关状态
//     */
//    private void getFlyModeState() {
//        GduApplication.getSingleApp().gduCommunication.getFlyModeState((code, bean) -> {
//            MyLogUtils.i("getFlyModeState callback() code = " + code);
//            if (!isAdded()) {
//                return;
//            }
//            if (code == GduConfig.OK && bean != null && bean.frameContent != null && bean.frameContent.length >= 3) {
//                MyLogUtils.i("getFlyModeState callback() hexStr = " + DataUtil.bytes2HexAddPlaceHolder(bean.frameContent));
//                GlobalVariable.enableSwitchFlyMode = bean.frameContent[2];
//                uiThreadHandle(() -> {
//                    setSwitchFlyModeView(GlobalVariable.enableSwitchFlyMode== GlobalVariable.FlyModeSwitchModeStatus.ON);
//                });
//            }
//        });
//    }
//
//    /**
//     * 设置模式切换开关状态
//     */
//    private void setSwitchFlyModeState(boolean isOpen) {
//        mViewBinding.tpsApsSwitch.setEnabled(false);
//        GduApplication.getSingleApp().gduCommunication.setFlyModeState(isOpen, (code, bean) -> {
//            if (!isAdded()) {
//                return;
//            }
//            uiThreadHandle(() -> {
//                mViewBinding.tpsApsSwitch.setEnabled(true);
//                if (code == GduConfig.OK) {
//                    GlobalVariable.enableSwitchFlyMode = (byte) (isOpen ? GlobalVariable.FlyModeSwitchModeStatus.ON : GlobalVariable.FlyModeSwitchModeStatus.OFF);
//                    setSwitchFlyModeView(isOpen);
//                } else {
//                    mDialogUtils.Toast(getString(R.string.Label_SettingFail));
//                }
//            });
//        });
//    }
//
//    private void setSwitchFlyModeView(boolean isOpen) {
//        mViewBinding.tpsApsSwitch.setSelected(isOpen);
//        if (isOpen) {
//            mViewBinding.layoutSetModel.setVisibility(View.VISIBLE);
//        } else {
//            mViewBinding.layoutSetModel.setVisibility(View.GONE);
//        }
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void connDrone(EventConnState connEvent) {
//        if (connEvent.connStateEnum == ConnStateEnum.Conn_Sucess) {
//
//        }else{
//            setLimitData();
//        }
    }

