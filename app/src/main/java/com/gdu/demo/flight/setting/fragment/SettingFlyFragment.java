package com.gdu.demo.flight.setting.fragment;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.flyco.tablayout.listener.OnTabSelectListener;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSetingFlyBinding;
import com.gdu.demo.flight.base.BaseFlightViewModel;
import com.gdu.demo.flight.event.ChangeUnitEvent;
import com.gdu.demo.flight.event.EventConnState;
import com.gdu.demo.flight.setting.viewmodel.SettingFlyViewModel;
import com.gdu.demo.utils.AnimationUtils;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.sdk.vision.GDUVision;
import com.gdu.util.ChannelUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.ViewUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/23
 */
public class SettingFlyFragment extends Fragment implements View.OnClickListener {

    private FragmentSetingFlyBinding mViewBinding;
    private UnitChnageUtils mUnitChnageUtils;

    private int preBackHeight = 20;
    private int preHeightLimit = -1;
    private int preDistanceLimit = -1;
    private int preOutOfControlAction = 0;

    /**
     * 是否开启限高
     */
    private boolean isOpenLimitHeight;

    private int currentSecondLevelType = 0;

    private FragmentActivity mActivity;
    private BaseFlightViewModel baseViewModel;
    private SettingFlyViewModel flyViewModel;

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
        if (null != mActivity) {
            baseViewModel = new ViewModelProvider(mActivity).get(BaseFlightViewModel.class);
            flyViewModel = new ViewModelProvider(mActivity).get(SettingFlyViewModel.class);
        }
        initView();
        initData();
    }

    private void initView() {
        EventBus.getDefault().register(this);
        mUnitChnageUtils = new UnitChnageUtils();
        setListener();


        mViewBinding.ivNoFlyBackSwitch.setSelected(GlobalVariable.noFlyAreBackAction == 1);


        if (GlobalVariable.isTetherModel) {
            mViewBinding.sbLimitDistance.setEnabled(false);
            mViewBinding.sbLimitHeight.setEnabled(false);
            mViewBinding.etDistanceLimit.setEnabled(false);
            mViewBinding.etHeightLimit.setEnabled(false);
        }
    }

    private void initData() {
        initLimitHeight();
        initLimitDistance();
        initBackHomeHeight();
        initBackHomeSpeed();
        initOutOfControlAction();
        initBackHomeAction();
        initGps();
        initChangeNoFlyAction();
        initFlyModeState();
    }

    /**
     * 设置限高相关内容
     */
    private void initLimitHeight() {
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
        } else {
            if (!GlobalVariable.isNewHeightLimitStrategy) {
                mViewBinding.etHeightLimit.setEnabled(false);
                mViewBinding.sbLimitHeight.setEnabled(false);
            }
            mViewBinding.sbLimitHeight.setProgress(0);
            mViewBinding.ivSwitchLimitHeight.setSelected(false);
            updateLimitHeightView(false);
            mViewBinding.etHeightLimit.setText("INF");
        }
        if (null == mActivity) return;
        baseViewModel.getLimitHeightLiveData().observe(mActivity, data -> {
            mViewBinding.ivSwitchLimitHeight.setSelected(data.isOpen());
            updateLimitHeightView(data.isOpen());
            isOpenLimitHeight = data.isOpen();
            if (GlobalVariable.isNewHeightLimitStrategy) {
                mViewBinding.sbLimitHeight.setEnabled(data.isOpen());
                mViewBinding.etHeightLimit.setEnabled(data.isOpen());
            }
            if (data.isOpen() || (data.getHeight() >= MyConstants.LIMIT_HEIGHT_MIN && data.getHeight() <= MyConstants.LIMIT_HEIGHT_MAX)) {
                mViewBinding.sbLimitHeight.setProgress(data.getHeight());
                String limitHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(data.getHeight()));
                mViewBinding.etHeightLimit.setText(limitHeightStr);
                preHeightLimit = data.getHeight();
            }
            if (data.isOpen()) {
                // 返航高度相关判断
                if (preHeightLimit > MyConstants.GO_HOME_HEIGHT_MAX || preHeightLimit <= 0) {
                    mViewBinding.sbBackHeight.setMax(MyConstants.GO_HOME_HEIGHT_MAX);
                    mUnitChnageUtils.showUnit(MyConstants.GO_HOME_HEIGHT_MAX, mViewBinding.tvMaxBackHeight);
                } else if (preBackHeight > data.getHeight()) {
                    preBackHeight = data.getHeight();
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
        mViewBinding.etHeightLimit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {//执行设置流程
                if (TextUtils.isEmpty(mViewBinding.etHeightLimit.getText())) {
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
                if (mViewBinding.ivSwitchLimitHeight.isSelected() && valueInt > MyConstants.LIMIT_HEIGHT_DEFAULT && preHeightLimit <= MyConstants.LIMIT_HEIGHT_DEFAULT) {
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
     */
    private void initLimitDistance() {
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
        } else {
            mViewBinding.etDistanceLimit.setText("INF");
            mViewBinding.etDistanceLimit.setEnabled(false);
            mViewBinding.sbLimitDistance.setEnabled(false);
            mViewBinding.sbLimitDistance.setProgress(0);
            mViewBinding.ivSwitchLimitDistance.setSelected(false);
            updateLimitDistanceView(false);
        }
        mViewBinding.etDistanceLimit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {//执行设置流程
                if (TextUtils.isEmpty(mViewBinding.etDistanceLimit.getText())) {
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
        baseViewModel.getLimitDistanceLiveData().observe(mActivity, data -> {
            mViewBinding.ivSwitchLimitDistance.setSelected(data.isOpen());
            updateLimitDistanceView(data.isOpen());
            mViewBinding.sbLimitDistance.setEnabled(data.isOpen());
            mViewBinding.etDistanceLimit.setEnabled(data.isOpen());
            if (data.isOpen() || (data.getDistance() >= MyConstants.LIMIT_DISTANCE_MIN && data.getDistance() <= MyConstants.LIMIT_DISTANCE_MAX)) {
                mViewBinding.sbLimitDistance.setProgress(data.getDistance());
                String limitDisStr = String.valueOf(UnitChnageUtils.getUnitValue(data.getDistance()));
                mViewBinding.etDistanceLimit.setText(limitDisStr);
                preDistanceLimit = data.getDistance();
            }
        });
        baseViewModel.getLimitDistance();
    }

    /**
     * 返航高度
     */
    private void initBackHomeHeight() {
        mUnitChnageUtils.showUnit(MyConstants.GO_HOME_HEIGHT_MIN, mViewBinding.tvMinBackHeight);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mViewBinding.sbBackHeight.setMin(MyConstants.GO_HOME_HEIGHT_MIN);
        }
        mViewBinding.sbBackHeight.setMax(isOpenLimitHeight ? preHeightLimit : MyConstants.GO_HOME_HEIGHT_MAX);
        mUnitChnageUtils.showUnit(isOpenLimitHeight ? preHeightLimit : MyConstants.GO_HOME_HEIGHT_MAX, mViewBinding.tvMaxBackHeight);

        if (GlobalVariable.backHeight > 0) {
            preBackHeight = GlobalVariable.backHeight / 10;
            mViewBinding.sbBackHeight.setProgress(preBackHeight);
            String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preBackHeight));
            mViewBinding.etHeight.setText(goHomeHeightStr);
        }

        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            mViewBinding.etHeight.setText(String.valueOf(UnitChnageUtils.getUnitValue(mViewBinding.sbBackHeight.getProgress())));
            mViewBinding.etHeight.setEnabled(true);
            mViewBinding.sbBackHeight.setEnabled(true);
        } else {
            mViewBinding.etHeight.setEnabled(false);
            mViewBinding.sbBackHeight.setEnabled(false);
        }

        mViewBinding.etHeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {//执行设置流程
                String value = mViewBinding.etHeight.getText().toString();
                flyViewModel.setBackHomeHeight(value);
            }
        });
        mViewBinding.etHeight.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mViewBinding.etHeight.clearFocus();
            }
            return false;
        });
        flyViewModel.getBackHomeHeightLiveData().observe(mActivity, data -> {
            preBackHeight = data;
            mViewBinding.sbBackHeight.setProgress(data);
            String goHomeHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(data));
            mViewBinding.etHeight.setText(goHomeHeightStr);
        });
    }

    /**
     * 返航速度
     */
    private void initBackHomeSpeed() {
        //设置返航速度默认值
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mViewBinding.sbBackSpeed.setMin(MyConstants.GO_HOME_SPEED_MIN);
        }
        mViewBinding.sbBackSpeed.setMax(MyConstants.GO_HOME_SPEED_MAX);
        mViewBinding.tvMinSpeedLabel.setText(mUnitChnageUtils.getUnitSpeedString(MyConstants.GO_HOME_SPEED_MIN));
        mViewBinding.tvMaxSpeedLabel.setText(mUnitChnageUtils.getUnitSpeedString(MyConstants.GO_HOME_SPEED_MAX));
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            mViewBinding.etBackSpeed.setText(String.valueOf(UnitChnageUtils.getUnitValue(mViewBinding.sbBackSpeed.getProgress())));
            mViewBinding.etBackSpeed.setEnabled(true);
            mViewBinding.sbBackSpeed.setEnabled(true);
        } else {
            mViewBinding.etBackSpeed.setEnabled(false);
            mViewBinding.sbBackSpeed.setEnabled(false);
        }

        flyViewModel.getBackHomeSpeedLiveData().observe(mActivity, data -> {
            setBackSpeedData(data.shortValue());
        });
        flyViewModel.getBackHomeSpeed();


        mViewBinding.etBackSpeed.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {//执行设置流程
                flyViewModel.setBackHomeSpeed(mViewBinding.etBackSpeed.getText().toString());
            }
        });
        mViewBinding.etBackSpeed.setOnEditorActionListener((textView, actionId, keyEvent) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mViewBinding.etBackSpeed.clearFocus();
            }
            return false;
        });
    }

    /**
     * 失联行为
     */
    private void initOutOfControlAction() {
        flyViewModel.getOutOfControlAction();
        flyViewModel.getOutOfControlActionLiveData().observe(mActivity, data -> {
            setOutOfControlActionData(data);
        });
        mViewBinding.ovOutOfControl.setOnOptionClickListener((parentId, view, position) ->
                flyViewModel.setOutOfControlAction(position));
    }

    /**
     * 返航行为
     */
    private void initBackHomeAction() {
        flyViewModel.getBackHomeActionLiveData().observe(mActivity, data -> {
            mViewBinding.ovReturnHome.setIndex(data);
        });
        flyViewModel.getBackHomeAction();

        mViewBinding.ovReturnHome.setOnOptionClickListener((parentId, view, position) -> {
            flyViewModel.setBackHomeAction(position);
        });
    }

    /**
     * 卫星定位系统
     */
    private void initGps() {
        if (CommonUtils.curPlanIsSmallFlight()) {
            mViewBinding.groupGnss.setVisibility(View.GONE);
        } else {
            mViewBinding.groupGnss.setVisibility(View.VISIBLE);
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

        // dh大华的先默认BDS
        if (ChannelUtils.isDahua(getContext()) || ChannelUtils.isDahuaBDS(getContext())) {
            GlobalVariable.sGNSSType = (byte) SPUtils.getCustomInt(GduAppEnv.application, "sGNSSType", 6);
        }
        if (GlobalVariable.sGNSSType == 6) {
            mViewBinding.ovGnss.setIndex(1);
        } else {
            mViewBinding.ovGnss.setIndex(0);
        }

        mViewBinding.ovGnss.setOnOptionClickListener((parentId, view, position) -> {
            if (!flyViewModel.getCanSetGps()) return;
            new CommonDialog.Builder(getChildFragmentManager())
                    .setContent(getResources().getString(R.string.gnss_switch_hint))
                    .setCancel(getResources().getString(R.string.Label_cancel))
                    .setSure(getResources().getString(R.string.Label_Sure))
                    .setCancelableOutside(false)
                    .setPositiveListener((dialogInterface, i) -> flyViewModel.setGps(requireContext(), position)).build().show();
        });
        flyViewModel.getGnssLiveData().observe(mActivity, data -> {
            mViewBinding.ovGnss.setIndex(data);
        });
    }

    /**
     * 是否允许切换飞行模式
     */
    private void initFlyModeState() {
        setSwitchFlyModeView(GlobalVariable.enableSwitchFlyMode == GlobalVariable.FlyModeSwitchModeStatus.ON);
        String[] array = getResources().getStringArray(R.array.array_fly_model);
        mViewBinding.tabFlyModel.setTabData(array);

        flyViewModel.getSwitchFlyModeLiveData().observe(mActivity, data -> {
            if (data != null) {
                setSwitchFlyModeView(data);
            } else {
                Toast.makeText(mActivity, R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
            }
        });
        flyViewModel.getSwitchFlyModeState();
        flyViewModel.getTripoModeLiveData().observe(mActivity, this::switchFlightModeView);

        mViewBinding.tabFlyModel.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                if (flyViewModel.setCanTripodMode(position)) {
                    new CommonDialog.Builder(getChildFragmentManager())
                            .setTitle(getResources().getString(position != 0 ? R.string.string_change_to_tfa : R.string.string_change_to_pfa))
                            .setContent(getResources().getString(position != 0 ? R.string.Label_tfa_hint : R.string.Label_pfa_hint))
                            .setCancel(getResources().getString(R.string.Label_cancel))
                            .setSure(getResources().getString(R.string.Label_Sure))
                            .setCancelableOutside(false)
                            .setPositiveListener((dialogInterface, i) -> flyViewModel.setTripodMode(position != 0)).build().show();

                }
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
    }


    private void setHeightFailHandle() {
        mViewBinding.sbLimitHeight.setProgress(preHeightLimit);
        String limitHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preHeightLimit));
        mViewBinding.etHeightLimit.setText(limitHeightStr);
    }

    private void setOutOfControlActionData(int action) {
        if (action == 0) {
            preOutOfControlAction = action;
        } else if (action == 1 || action == 2) {
            preOutOfControlAction = 1;
            mViewBinding.tvReturnHomeTitle.setVisibility(View.VISIBLE);
            mViewBinding.ovReturnHome.setVisibility(View.VISIBLE);
        }
        mViewBinding.ovOutOfControl.setIndex(preOutOfControlAction);
    }

    private void setBackSpeedData(short backSpeed) {
        backSpeed /= 100;
        mViewBinding.sbBackSpeed.setProgress(backSpeed);
        mViewBinding.etBackSpeed.setText(String.valueOf(backSpeed));
    }

    private void setListener() {
        mViewBinding.ivBack.setOnClickListener(this);
        mViewBinding.sbBackSpeed.setOnSeekBarChangeListener(backSpeedListener);
        mViewBinding.sbBackHeight.setOnSeekBarChangeListener(backHeightListener);
        mViewBinding.sbLimitHeight.setOnSeekBarChangeListener(limitHeightListener);
        mViewBinding.sbLimitDistance.setOnSeekBarChangeListener(limitDistanceListener);
        mViewBinding.ivSwitchLimitDistance.setOnClickListener(this);
        mViewBinding.ivSwitchLimitHeight.setOnClickListener(this);
        mViewBinding.tpsApsSwitch.setOnClickListener(this);
        mViewBinding.tvSensorStatus.setOnClickListener(this);
        mViewBinding.ivNoFlyBackSwitch.setOnClickListener(this);
        mViewBinding.ivModeTip.setOnClickListener(this);

        flyViewModel.getToastLiveData().observe(mActivity, data -> {
            if (data != 0) {
                Toast.makeText(mActivity, data, Toast.LENGTH_SHORT).show();
            }
        });
        baseViewModel.getToastLiveData().observe(mActivity, data -> {
            if (data != 0) {
                Toast.makeText(mActivity, data, Toast.LENGTH_SHORT).show();
            }
        });
        baseViewModel.getErrTipBeanLiveData().observe(mActivity, data -> {
            int setType = data.getSetType();
            int type = data.getType();
            if (setType == 1) {
                setHeightFailHandle();
            } else if (setType == 2) {
                setDistanceFailHandle();
            } else if (setType == 3) {
                setBackHeightFailHandle();
            }
            switch (type) {
                case 1:
                    mViewBinding.sbLimitHeight.setEnabled(false);
                    mViewBinding.sbLimitDistance.setEnabled(false);
                    mViewBinding.etHeightLimit.setEnabled(false);
                    mViewBinding.etDistanceLimit.setEnabled(false);
                    if (null!=getContext())
                        Toast.makeText(getContext(), R.string.DeviceNoConn, Toast.LENGTH_SHORT).show();
                    break;

                case 2:
                    if (null!=getContext())
                        Toast.makeText(getContext(), R.string.string_tether_can_not_set, Toast.LENGTH_SHORT).show();
                    break;

                case 3:
                    if (null!=getContext())
                        Toast.makeText(getContext(), R.string.Msg_GoHomingUnSet, Toast.LENGTH_SHORT).show();
                    break;

                case 4:
                    if (null!=getContext())
                        Toast.makeText(getContext(), R.string.input_error, Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    if (null!=getContext())
                        Toast.makeText(getContext(), R.string.Label_set_invalid_distance, Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    if (null!=getContext())
                        Toast.makeText(getContext(), R.string.return_more_than_limit, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                updateBackView();
                break;
            case R.id.iv_switch_limit_height:
                // 限高
                if (!GlobalVariable.isNewHeightLimitStrategy) {
                    if (!flyViewModel.connStateToast()) {
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
                if (!flyViewModel.connStateToast()) {
                    return;
                }
                if (GlobalVariable.isTetherModel) {
                    Toast.makeText(getContext(), R.string.string_tether_can_not_set, Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean isSelected = !mViewBinding.tpsApsSwitch.isSelected();
                flyViewModel.setSwitchFlyModeState(isSelected);

                break;
            case R.id.tv_sensor_status:
                setSecondLevelView(mViewBinding.vSensorStatus, true, getString(R.string.sensor_status));
                currentSecondLevelType = 2;
                break;
            case R.id.iv_mode_tip:
                showModeTipsDialog();
                break;
            case R.id.iv_no_fly_back_switch:
                flyViewModel.changeNoFlyAreAction(!mViewBinding.ivNoFlyBackSwitch.isSelected());
                break;
            default:
                break;
        }
    }


    private void initChangeNoFlyAction() {
        flyViewModel.getChangeNoFlyAreActionLiveData().observe(mActivity, data -> {
            if (data) {
                mViewBinding.ivNoFlyBackSwitch.setSelected(!mViewBinding.ivNoFlyBackSwitch.isSelected());
                Toast.makeText(mActivity, R.string.string_set_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(mActivity, R.string.Label_SettingFail, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showLimitHeightDialog(int limit) {
        new CommonDialog.Builder(getChildFragmentManager())
                .setTitle(getResources().getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                .setContent(getResources().getString(R.string.limit_height_statement_content))
                .setCancel(getResources().getString(R.string.think_moment))
                .setSure(getResources().getString(R.string.think_agree))
                .setCancelableOutside(false)
                .setPositiveListener((dialogInterface, i) -> baseViewModel.setLimitHeight(true, limit))
                .setNegativeListener((dialogInterface, i) -> setHeightFailHandle()).build().show();
    }

    private void switchLimitHeight() {
        if (mViewBinding.ivSwitchLimitHeight.isSelected() || preHeightLimit > MyConstants.LIMIT_HEIGHT_DEFAULT) {
            new CommonDialog.Builder(getChildFragmentManager())
                    .setTitle(getResources().getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                    .setContent(getResources().getString(R.string.limit_height_statement_content))
                    .setCancel(getResources().getString(R.string.think_moment))
                    .setSure(getResources().getString(R.string.think_agree))
                    .setCancelableOutside(false)
                    .setPositiveListener((dialogInterface, i) -> {
                        if (null != baseViewModel)
                            baseViewModel.setLimitHeight(!mViewBinding.ivSwitchLimitHeight.isSelected(), preHeightLimit);
                    }).build().show();
        } else {
            if (null != baseViewModel)
                baseViewModel.setLimitHeight(true, preHeightLimit);
        }
    }

    private void updateBackView() {
        if (currentSecondLevelType == 2) {
            setSecondLevelView(mViewBinding.vSensorStatus, false, "");
        }
        currentSecondLevelType = 0;
    }

    private void setSecondLevelView(View view, boolean show, String title) {
        AnimationUtils.animatorRightInOut(view, show);
        if (show) {
            mViewBinding.ivBack.setVisibility(View.VISIBLE);
            mViewBinding.tvTitle.setText(title);
        } else {
            mViewBinding.ivBack.setVisibility(View.GONE);
            mViewBinding.tvTitle.setText(R.string.title_fly);
        }
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
            //显示当前调节的高度
            mViewBinding.etBackSpeed.setText(String.valueOf(UnitChnageUtils.getUnitValue(progress)));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            flyViewModel.setBackHomeSpeed(String.valueOf(seekBar.getProgress()));
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
            if (!flyViewModel.connStateToast()) {
                mViewBinding.sbBackHeight.setProgress(preBackHeight);
                return;
            }
            flyViewModel.setBackHomeHeight(seekBar.getProgress());
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
            if (!GlobalVariable.isNewHeightLimitStrategy) {
                if (!flyViewModel.connStateToast()) {
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
                        .setTitle(getResources().getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                        .setContent(getResources().getString(R.string.limit_height_statement_content))
                        .setCancel(getResources().getString(R.string.think_moment))
                        .setSure(getResources().getString(R.string.think_agree))
                        .setCancelableOutside(false)
                        .setPositiveListener((dialogInterface, i) -> baseViewModel.setLimitHeight(mViewBinding.ivSwitchLimitHeight.isSelected(), limitHeight))
                        .setNegativeListener((dialogInterface, i) -> mViewBinding.sbLimitHeight.setProgress(preHeightLimit)).build().show();
            } else {
                baseViewModel.setLimitHeight(mViewBinding.ivSwitchLimitHeight.isSelected(), limitHeight);
            }
        }
    };

    private void switchFlightModeView(boolean isTfaMode) {
        if (getContext() == null) return;
        mViewBinding.tvPfaTfaModeContent.setText(isTfaMode ? getResources().getString(R.string.Msg_tfa_mode) : getResources().getString(R.string.Msg_pfa_mode));
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
            if (!flyViewModel.connStateToast()) {
                //distance
                mViewBinding.sbLimitDistance.setProgress(preDistanceLimit);
                mViewBinding.ivSwitchLimitDistance.setSelected(false);
                mViewBinding.sbLimitDistance.setEnabled(false);
                updateLimitDistanceView(false);
                return;
            }
            baseViewModel.setLimitDistance(mViewBinding.ivSwitchLimitDistance.isSelected(), seekBar.getProgress());
        }
    };


    @Subscribe
    public void onEventMainThread(ChangeUnitEvent event) {
        initData();//动态变化参数单位需要
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static SettingFlyFragment newInstance() {
        Bundle args = new Bundle();
        SettingFlyFragment fragment = new SettingFlyFragment();
        fragment.setArguments(args);
        return fragment;
    }

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

    private void setSwitchFlyModeView(boolean isOpen) {
        mViewBinding.tpsApsSwitch.setSelected(isOpen);
        if (isOpen) {
            mViewBinding.layoutSetModel.setVisibility(View.VISIBLE);
        } else {
            mViewBinding.layoutSetModel.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connDrone(EventConnState connEvent) {
        if (connEvent.connStateEnum != ConnStateEnum.Conn_Sucess) {
            initData();
        }
    }

    private void showModeTipsDialog(){
        new CommonDialog.Builder(getChildFragmentManager())
                .setLayoutResId(R.layout.dialog_set_fly_model_tip)
                .setWidth((int) getResources().getDimension(R.dimen.dp_370))
                .setLayoutBindViewListener((dialogInterface, itemView) -> {
                    TextView tv_confirm = itemView.findViewById(R.id.tv_confirm);
                    TextView tv_a_model_text = itemView.findViewById(R.id.tv_a_model_text);
                    TextView tv_p_model_text = itemView.findViewById(R.id.tv_p_model_text);
                    TextView tv_s_model_text = itemView.findViewById(R.id.tv_s_model_text);
                    tv_a_model_text.setText(getAModelContentString(getContext()));
                    tv_p_model_text.setText(getPModelContentString(getContext()));
                    tv_s_model_text.setText(getSModelContentString(getContext()));
                    tv_confirm.setOnClickListener(view -> dialogInterface.dismiss());
                }).build().show();

    }

    public static String getAModelContentString(Context context) {
        if (DroneUtil.showBDSOrGNSS()) {
            return context.getString(R.string.string_a_model_content_bds);
        } else {
            return context.getString(R.string.string_a_model_content);
        }
    }

    public static String getPModelContentString(Context context) {
        if (DroneUtil.showBDSOrGNSS()) {
            return context.getString(R.string.string_p_mode_content_bds);
        } else {
            return context.getString(R.string.string_p_mode_content);
        }
    }

    public static String getSModelContentString(Context context) {
        if (DroneUtil.showBDSOrGNSS()) {
            return context.getString(R.string.string_s_model_content_bds);
        } else {
            return context.getString(R.string.string_s_model_content);
        }
    }
}

