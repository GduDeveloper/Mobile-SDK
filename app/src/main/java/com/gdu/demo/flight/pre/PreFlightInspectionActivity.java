package com.gdu.demo.flight.pre;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduConfig;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.FlightActivity;
import com.gdu.demo.R;
import com.gdu.demo.databinding.ActivityPreFligthInspectionBinding;
import com.gdu.demo.databinding.DialogLayoutBackHomePointBinding;
import com.gdu.demo.flight.pre.adapter.PreFlightStatusAdapter;
import com.gdu.demo.flight.pre.adapter.TextAdapter;
import com.gdu.demo.flight.pre.viewmodel.PreFlightInspectionViewModel;
import com.gdu.demo.utils.CommonDialog;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.demo.widget.DoubleDragThumbSeekBar2;
import com.gdu.drone.ControlHand;
import com.gdu.healthmanager.FlightHealthStatusDetailBean;
import com.gdu.healthmanager.MessageBean;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.CollectionUtils;
import com.gdu.util.ConnectUtil;
import com.gdu.util.DroneUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.ThreadHelper;
import com.gdu.util.ViewUtils;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.util.ArrayList;
import java.util.List;

import cc.taylorzhang.singleclick.SingleClickUtil;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Action;

/**
 * @author wuqb
 * @date 2025/1/9
 * @description 飞行前检查页
 */
public class PreFlightInspectionActivity extends FragmentActivity {

    private ActivityPreFligthInspectionBinding mViewBinding;
    private PreFlightInspectionViewModel viewModel;
    private int preLimitHeightValue;
    private int preLimitDistanceValue;
    private TextAdapter mTextAdapter;
    private final List<MessageBean> bannerData = new ArrayList<>();
    private PreFlightStatusAdapter mStatusAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityPreFligthInspectionBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        viewModel = new ViewModelProvider(this).get(PreFlightInspectionViewModel.class);
        viewModel.init(this);

        final int mLimitHeightValue = SPUtils.getInt(this, SPUtils.LAST_LIMIT_HEIGHT);
        final int mLimitDistanceValue = SPUtils.getInt(this, SPUtils.LAST_LIMIT_DISTANCE);
        if (mLimitHeightValue == 0) {
            preLimitHeightValue = MyConstants.LIMIT_HEIGHT_DEFAULT;
        } else if (mLimitHeightValue < MyConstants.LIMIT_HEIGHT_MIN) {
            preLimitHeightValue = MyConstants.LIMIT_HEIGHT_MIN;
        } else if (mLimitHeightValue > MyConstants.LIMIT_HEIGHT_MAX) {
            preLimitHeightValue = MyConstants.LIMIT_HEIGHT_MAX;
        } else {
            preLimitHeightValue = mLimitHeightValue;
        }

        if (mLimitDistanceValue == 0) {
            preLimitDistanceValue = MyConstants.LIMIT_DISTANCE_DEFAULT;
        } else if (mLimitDistanceValue < MyConstants.LIMIT_DISTANCE_MIN) {
            preLimitDistanceValue = MyConstants.LIMIT_DISTANCE_MIN;
        } else if (mLimitDistanceValue > MyConstants.LIMIT_DISTANCE_MAX) {
            preLimitDistanceValue = MyConstants.LIMIT_DISTANCE_MAX;
        } else {
            preLimitDistanceValue = mLimitDistanceValue;
        }

        initViews();
        initListener();
    }

        private void initViews() {
        mTextAdapter = new TextAdapter(this, bannerData);
        mViewBinding.viewFlightStatusTip.setAdapter(mTextAdapter).addBannerLifecycleObserver(this);
        mViewBinding.btnNextStep.setVisibility(View.GONE);

        mViewBinding.sbpLowPowerAlarmPb.setMaxPb(50);
        mViewBinding.sbpLowPowerAlarmPb.setLowMinMaxValue(10, 45);
        mViewBinding.sbpLowPowerAlarmPb.setHeightMinMaxValue(20, 50);
        mViewBinding.sbpLowPowerAlarmPb.setMinMarginPercent(GlobalVariable.BATTERY_MIN_INTERVAL);
        mViewBinding.sbpLowPowerAlarmPb.setShowScale(true);
        ViewUtils.setViewShowOrInVisible(mViewBinding.sbpLowPowerAlarmPb, ConnectUtil.isConnect());

        mViewBinding.sbpAroundObstacleAvoidPb.setMaxPb(4000);
        mViewBinding.sbpAroundObstacleAvoidPb.setObstacleSet(true);
        mViewBinding.sbpAroundObstacleAvoidPb.setLowMinMaxValue(100, 1000);
        mViewBinding.sbpAroundObstacleAvoidPb.setHeightMinMaxValue(110, 4000);
        mViewBinding.sbpAroundObstacleAvoidPb.setMinMarginPercent(100);
        mViewBinding.sbpAroundObstacleAvoidPb.setShowScale(false);

        mViewBinding.sbpTopObstacleAvoidPb.setMaxPb(2000);
        mViewBinding.sbpTopObstacleAvoidPb.setObstacleSet(true);
        mViewBinding.sbpTopObstacleAvoidPb.setLowMinMaxValue(100, 1000);
        mViewBinding.sbpTopObstacleAvoidPb.setHeightMinMaxValue(110, 2000);
        mViewBinding.sbpTopObstacleAvoidPb.setMinMarginPercent(100);
        mViewBinding.sbpAroundObstacleAvoidPb.setShowScale(false);

        mViewBinding.sbpBottomObstacleAvoidPb.setMaxPb(2000);
        mViewBinding.sbpBottomObstacleAvoidPb.setObstacleSet(true);
        mViewBinding.sbpBottomObstacleAvoidPb.setLowMinMaxValue(50, 300);
        mViewBinding.sbpBottomObstacleAvoidPb.setHeightMinMaxValue(60, 2000);
        mViewBinding.sbpBottomObstacleAvoidPb.setMinMarginPercent(100);
        mViewBinding.sbpAroundObstacleAvoidPb.setShowScale(false);

    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    public void initData() {
        //初始化默认状态
        initViewStatus();
        //状态信息
        initFlightStatus();
        //返航高度
        initBackHomeHeight();
        //限高
        initLimitHeight();
        //失联行为
        initLostContactBehavior();
        //返航点
        initBackHomePoint();
        //限距
        initLimitDistance();
        //摇杆模式
        initRockerMode();

        //Toast弹窗提示
        viewModel.getToastLiveData().observe(this, data -> {
            if (data != 0){
                showToast(getResources().getString(data));
            }
        });
        viewModel.getBaseFlightAssistantViewModel().getToastLiveData().observe(this, data -> {
            if (data != 0){
                showToast(getResources().getString(data));
            }
        });
        viewModel.getSysStatusLiveData().observe(this, data -> {
            mViewBinding.tvFlightStatus.setText(getString(data.getFlightStatusStr()));
            mViewBinding.viewStatusBg.setBackgroundResource(data.getStatusBg());
            mViewBinding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, data.getStatusTitleColor()));
            mViewBinding.tvFlightStatus.setTextColor(ContextCompat.getColor(this, data.getFlightStatusColor()));
            mViewBinding.ivMore.setImageResource(data.getMoreRes());
        });
        viewModel.getErrMsgLiveData().observe(this, bean -> {
            if (null == bean) {
                bannerData.clear();
            } else {
                CollectionUtils.listAddAllAvoidNPE(bannerData, bean);
            }
            mTextAdapter.notifyDataSetChanged();
        });
        viewModel.getBaseFlightViewModel().getWarnTipBeanLiveData().observe(this, data->{
            showErrTip(data.getType(), data.getWarnType());
        });

        viewModel.getBaseFlightAssistantViewModel().getObstacleHorLiveData().observe(this, data -> {
            mViewBinding.ivAroundObstacleAvoidSwitch.setSelected(data.isSelect());
            if (data.isObsOpen()) {
                setObstacleItemValue(data.getStopValue(), data.getAlarmValue(),
                        mViewBinding.sbpAroundObstacleAvoidPb, mViewBinding.viewAroundObstacleAvoidGroup, mViewBinding.tvAroundObstacleClose);
            } else {
                closeItem(mViewBinding.tvAroundObstacleClose, mViewBinding.viewAroundObstacleAvoidGroup);
            }
        });
        viewModel.getBaseFlightAssistantViewModel().getObstacleTopLiveData().observe(this, data -> {
            mViewBinding.ivTopObstacleAvoidSwitch.setSelected(data.isSelect());
            if (data.isObsOpen()) {
                setObstacleItemValue(data.getStopValue(), data.getAlarmValue(),
                        mViewBinding.sbpTopObstacleAvoidPb, mViewBinding.viewTopObstacleAvoidGroup, mViewBinding.tvTopObstacleClose);
            } else {
                closeItem(mViewBinding.tvTopObstacleClose, mViewBinding.viewTopObstacleAvoidGroup);
            }
        });
        viewModel.getBaseFlightAssistantViewModel().getVisionSensingLiveData().observe(this, data -> {
            mViewBinding.ivVisionObstacleSwitch.setSelected(data);
            mViewBinding.viewObstacleStrategyGroup.setVisibility(data ? View.VISIBLE : View.GONE);
        });
        viewModel.getBaseFlightAssistantViewModel().getObstacleAvoidanceStrategyLiveData().observe(this, data -> {
            mViewBinding.clVisionObstacleLayout.setVisibility(data ? View.VISIBLE : View.GONE);
            mViewBinding.ivObstacleStrategySwitch.setSelected(data);
        });

        viewModel.getLowBatteryWarningLiveData().observe(this, data->{
            setLowBatteryValue(data.getOneLevelWarn(), data.getTwoLevelWarn());
        });

        //检查飞行错误状态
        viewModel.checkAlarmData(this);
        //检查飞行参数状态
        viewModel.checkFlightStatus(this);
    }

    private void initListener() {
        SingleClickUtil.onSingleClick(mViewBinding.preFlightClose, 3000, false,
                v -> backHandle());

        mViewBinding.sbpLowPowerAlarmPb.setOnSeekBarChangeListener(
                new DoubleDragThumbSeekBar2.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressBefore() {

                    }

                    @Override
                    public void onProgressChanged(DoubleDragThumbSeekBar2 seekBar, double progressLow,
                                                  double progressHigh) {
                    }

                    @Override
                    public void onValueUpdate(String lowStr, String heightStr) {
                        if (CommonUtils.isEmptyString(lowStr) || CommonUtils.isEmptyString(heightStr)) {
                            return;
                        }
                        mViewBinding.tvLostLowPowerWarn.setText(lowStr + "%");
                        mViewBinding.tvLowPowerWarn.setText(heightStr + "%");
                    }

                    @Override
                    public void onProgressAfter() {
                        final byte lowValue =
                                Double.valueOf(mViewBinding.sbpLowPowerAlarmPb.getCurLowProgress()).byteValue();
                        final byte heightValue =
                                Double.valueOf(mViewBinding.sbpLowPowerAlarmPb.getCurHeightProgress()).byteValue();
                        viewModel.getBaseFlightViewModel().setLowBatteryWarningThreshold(heightValue, lowValue);
                    }
                });

        /*********************避障******************************/
        SingleClickUtil.onSingleClick(mViewBinding.ivVisionObstacleSwitch,  false, v -> switchVisionObstacleClickHandle());
        SingleClickUtil.onSingleClick(mViewBinding.ivObstacleStrategySwitch,  false, v -> {
            if (uavUnConnect()) return;
            switchObstacleStrategy(!mViewBinding.ivObstacleStrategySwitch.isSelected());
        });
        SingleClickUtil.onSingleClick(mViewBinding.ivAroundObstacleAvoidSwitch,  false, v -> {
            if (uavUnConnect()) return;
            mViewBinding.ivAroundObstacleAvoidSwitch.setSelected(!mViewBinding.ivAroundObstacleAvoidSwitch.isSelected());
            viewModel.getBaseFlightAssistantViewModel().setHorizontalVisionObstacleAvoidanceEnabled(mViewBinding.ivAroundObstacleAvoidSwitch.isSelected());
        });
        SingleClickUtil.onSingleClick(mViewBinding.ivTopObstacleAvoidSwitch,  false, v -> {
            if (uavUnConnect()) return;
            viewModel.getBaseFlightAssistantViewModel().setTopVisionObstacleAvoidanceEnabled(!mViewBinding.ivTopObstacleAvoidSwitch.isSelected());
            mViewBinding.ivTopObstacleAvoidSwitch.setSelected(!mViewBinding.ivTopObstacleAvoidSwitch.isSelected());
        });
        SingleClickUtil.onSingleClick(mViewBinding.ivBottomObstacleAvoidSwitch,  false, v -> {
            if (uavUnConnect()) return;
            viewModel.getBaseFlightAssistantViewModel().setBottomVisionObstacleAvoidanceEnabled(mViewBinding.ivBottomObstacleAvoidSwitch.isSelected());
            mViewBinding.ivBottomObstacleAvoidSwitch.setSelected(!mViewBinding.ivBottomObstacleAvoidSwitch.isSelected());
        });

        mViewBinding.sbpAroundObstacleAvoidPb.setOnSeekBarChangeListener(
                new DoubleDragThumbSeekBar2.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressBefore() {

                    }

                    @Override
                    public void onProgressChanged(DoubleDragThumbSeekBar2 seekBar, double progressLow,
                                                  double progressHigh) {
                    }

                    @Override
                    public void onValueUpdate(String lowStr, String heightStr) {
                        if (CommonUtils.isEmptyString(lowStr) || CommonUtils.isEmptyString(heightStr)) {
                            return;
                        }
                        mViewBinding.tvAroundObstacleStop.setText(lowStr);
                        mViewBinding.tvAroundObstacleWarn.setText(heightStr);
                    }

                    @Override
                    public void onProgressAfter() {
                        final int lowValue =
                                Double.valueOf(mViewBinding.sbpAroundObstacleAvoidPb.getCurLowProgress()).intValue();
                        final int heightValue =
                                Double.valueOf(mViewBinding.sbpAroundObstacleAvoidPb.getCurHeightProgress()).intValue();
                        viewModel.getBaseFlightAssistantViewModel().setHorVisionObstacleAvoidanceDistance(lowValue, heightValue);
                    }
                });
        mViewBinding.sbpTopObstacleAvoidPb.setOnSeekBarChangeListener(
                new DoubleDragThumbSeekBar2.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressBefore() {

                    }

                    @Override
                    public void onProgressChanged(DoubleDragThumbSeekBar2 seekBar, double progressLow,
                                                  double progressHigh) {
                    }

                    @Override
                    public void onValueUpdate(String lowStr, String heightStr) {
                        if (CommonUtils.isEmptyString(lowStr) || CommonUtils.isEmptyString(heightStr)) {
                            return;
                        }
                        mViewBinding.tvTopObstacleStop.setText(lowStr);
                        mViewBinding.tvTopObstacleWarn.setText(heightStr);
                    }

                    @Override
                    public void onProgressAfter() {
                        final int lowValue = Double.valueOf(mViewBinding.sbpTopObstacleAvoidPb.getCurLowProgress()).intValue();
                        final int heightValue =
                                Double.valueOf(mViewBinding.sbpTopObstacleAvoidPb.getCurHeightProgress()).intValue();
                        viewModel.getBaseFlightAssistantViewModel().setTopVisionObstacleAvoidanceDistance(lowValue, heightValue);
                    }
                });
        mViewBinding.sbpBottomObstacleAvoidPb.setOnSeekBarChangeListener(
                new DoubleDragThumbSeekBar2.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressBefore() {

                    }

                    @Override
                    public void onProgressChanged(DoubleDragThumbSeekBar2 seekBar, double progressLow,
                                                  double progressHigh) {
                    }

                    @Override
                    public void onValueUpdate(String lowStr, String heightStr) {
                        if (CommonUtils.isEmptyString(lowStr) || CommonUtils.isEmptyString(heightStr)) {
                            return;
                        }
                        mViewBinding.tvBotObstacleStop.setText(lowStr);
                        mViewBinding.tvBotObstacleWarn.setText(heightStr);
                    }

                    @Override
                    public void onProgressAfter() {
                        final int lowValue =
                                Double.valueOf(mViewBinding.sbpBottomObstacleAvoidPb.getCurLowProgress()).intValue();
                        final int heightValue =
                                Double.valueOf(mViewBinding.sbpBottomObstacleAvoidPb.getCurHeightProgress()).intValue();
                        viewModel.getBaseFlightAssistantViewModel().setBottomVisionObstacleAvoidanceDistance(lowValue, heightValue);
                    }
                });
    }

    private void initViewStatus() {

        mViewBinding.ivVisionObstacleSwitch.setSelected(false);
        mViewBinding.viewObstacleStrategyGroup.setVisibility(View.GONE);
        mViewBinding.ivObstacleStrategySwitch.setSelected(false);
        mViewBinding.ivAroundObstacleAvoidSwitch.setSelected(false);
        mViewBinding.ivTopObstacleAvoidSwitch.setSelected(false);
        mViewBinding.ivBottomObstacleAvoidSwitch.setSelected(false);
        mViewBinding.clVisionObstacleLayout.setVisibility(View.GONE);

        closeItem(mViewBinding.tvAroundObstacleClose, mViewBinding.viewAroundObstacleAvoidGroup);
        closeItem(mViewBinding.tvTopObstacleClose, mViewBinding.viewTopObstacleAvoidGroup);
    }

    /**
     * 初始化飞行状态适配器内容
     *      包含：飞行模式、飞行器电池、遥控器电池、RTK、存储卡、遥控控制、图传状态等
     * */
    private void initFlightStatus(){
        mStatusAdapter = new PreFlightStatusAdapter();
        mViewBinding.preFlightStatus.setAdapter(mStatusAdapter);
        viewModel.getFlyInitStatusData().observe(this, data -> {
            mStatusAdapter.setNewInstance(data);
        });
        viewModel.getFlyStatusData().observe(this, data -> {
            mStatusAdapter.notifyItemChanged(data.getPosition());
        });
    }

    /** 返航高度 */
    private void initBackHomeHeight(){
        String goHomeTipStr = UnitChnageUtils.getUnitValue(MyConstants.GO_HOME_HEIGHT_MIN) + "-"
                + UnitChnageUtils.getUnitValue(MyConstants.GO_HOME_HEIGHT_MAX)
                + UnitChnageUtils.getUnit();
        mViewBinding.preFlightHomeHeightValue.setText(goHomeTipStr);
        //根据英寸单位换算的最大数值长度，设置输入框的最大可输入范围
        mViewBinding.preFlightHomeHeightEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(String.valueOf(UnitChnageUtils.getUnitValue(MyConstants.GO_HOME_HEIGHT_MAX)).length())});
        mViewBinding.preFlightHomeHeightEdit.setText("INF");
        mViewBinding.preFlightLimitHeightSwitch.setSelected(false);

        viewModel.getGoHomeHeightLiveData().observe(this, data -> {
            mViewBinding.preFlightHomeHeightEdit.setText(data);
        });
        mViewBinding.preFlightHomeHeightEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) {
                showToast(getResources().getString(R.string.Label_SettingFail));
                return false;
            }
            // 取出返航高度值
            final String inputStr = v.getText().toString();
            // 取出限高值
            final String preHeightStr = mViewBinding.preFlightLimitHeightEdit.getText().toString().trim();
            boolean isErrData;
            // 是否开启限高
            if (mViewBinding.preFlightLimitHeightSwitch.isSelected()) {
                isErrData = CommonUtils.isEmptyString(inputStr)
                        || !CommonUtils.isNumberInt(inputStr)
                        || CommonUtils.isEmptyString(preHeightStr)
                        || !CommonUtils.isNumberInt(preHeightStr);
            } else {
                isErrData = CommonUtils.isEmptyString(inputStr) || !CommonUtils.isNumberInt(inputStr);
            }
            // 是否未输入数据或数据数据格式错误
            if (isErrData) {
                showToast(getResources().getString(R.string.Label_SettingFail));
                return false;
            }
            // 把取出的值单位转换为m
            int curHeightBack = UnitChnageUtils.inch2m(Integer.parseInt(inputStr));

            if (mViewBinding.preFlightLimitHeightSwitch.isSelected()) {
                final int preHeightLimit = UnitChnageUtils.inch2m(Integer.parseInt(preHeightStr));
                final boolean isCanSet = preHeightLimit >= curHeightBack;
                mViewBinding.preFlightHomeHeightEdit.clearFocus();
                if (isCanSet) {
                    viewModel.setGoHomeHeight(curHeightBack);
                } else {
                    showErrTip(3,4);
                }
            } else {
                viewModel.setGoHomeHeight(curHeightBack);
            }
            return false;
        });
    }

    /** 限高 */
    private void initLimitHeight(){
        mViewBinding.preFlightLimitHeightEdit.setText("--");
        mViewBinding.preFlightLimitHeightEdit.setEnabled(false);

        String heightLimitTipStr = UnitChnageUtils.getUnitValue(com.gdu.util.MyConstants.LIMIT_HEIGHT_MIN) + "-"
                + UnitChnageUtils.getUnitValue(com.gdu.util.MyConstants.LIMIT_HEIGHT_MAX) + UnitChnageUtils.getUnit();
        mViewBinding.preFlightLimitHeightValue.setText(heightLimitTipStr);
        //根据英寸单位换算的最大数值长度，设置输入框的最大可输入范围
        mViewBinding.preFlightLimitHeightEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(String.valueOf(UnitChnageUtils.getUnitValue(com.gdu.util.MyConstants.LIMIT_HEIGHT_MAX)).length())});
        viewModel.getLimitHeightLiveData().observe(this, data -> {
            if (data.getHeight() == -1) {
                mViewBinding.preFlightLimitHeightEdit.setText("INF");
            } else {
                String limitHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(data.getHeight()));
                mViewBinding.preFlightLimitHeightEdit.setText(limitHeightStr);
            }
            preLimitHeightValue = data.isOpen() ? data.getHeight() : preLimitHeightValue;
            mViewBinding.preFlightLimitHeightSwitch.setSelected(data.isOpen());
            mViewBinding.preFlightLimitHeightEdit.setEnabled(data.isOpen());
        });
        SingleClickUtil.onSingleClick(mViewBinding.preFlightLimitHeightSwitch, false, v -> setHeightLimitSwitch());
        mViewBinding.preFlightLimitHeightEdit.setOnFocusChangeListener((v, hasFocus) -> viewModel.setEditLimitHeight(hasFocus));
        mViewBinding.preFlightLimitHeightEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE || !mViewBinding.preFlightLimitHeightSwitch.isSelected()) {
                showToast(getResources().getString(R.string.Label_SettingFail));
                return false;
            }
            final String inputStr = v.getText().toString();
            if (CommonUtils.isEmptyString(inputStr) || !CommonUtils.isNumberInt(inputStr)) {
                showToast(getResources().getString(R.string.input_error));
                return false;
            }
            int mLimitHeight = UnitChnageUtils.inch2m(Integer.parseInt(inputStr));
            if (mLimitHeight < viewModel.getPreGoHomeHeight()) {
                showToast(getResources().getString(R.string.Msg_LimitHeight_LessThan_BackHeight));
                return false;
            }
            mViewBinding.preFlightLimitHeightEdit.clearFocus();
            if (mLimitHeight > MyConstants.LIMIT_HEIGHT_DEFAULT && preLimitHeightValue <= MyConstants.LIMIT_HEIGHT_DEFAULT) {
                showLimitHeightDialog(mLimitHeight);
            } else {
                viewModel.setLimitHeight(true, mLimitHeight);
            }
            return false;
        });
    }

    /**
     * 失联行为
     * */
    private void initLostContactBehavior(){
        viewModel.getBaseFlightViewModel().getConnectionFailSafeBehaviorLiveData().observe(this, data->{
            mViewBinding.preFlightLostContactEdit.setIndex(data.getPosition());
        });
        mViewBinding.preFlightLostContactEdit.setOnOptionClickListener((parentId, view, position) ->
                SingleClickUtil.determineTriggerSingleClick(view, v -> {
                    if (uavUnConnect()) return;
                    viewModel.getBaseFlightViewModel().setConnectionFailSafeBehavior(position);
                }));
    }

    /**
     * 返航点设置
     * */
    private void initBackHomePoint(){
        SingleClickUtil.onSingleClick(mViewBinding.preFlightHomePointUav,  false,
                v -> {
                    if (uavUnConnect()) return;
                    showBackHomePointConfirmDialog(0, GlobalVariable.GPS_Lat, GlobalVariable.GPS_Lon,
                            GlobalVariable.ReturnHomeSettingType.PLANET);
                });

        SingleClickUtil.onSingleClick(mViewBinding.preFlightHomePointRc, false,
                v -> {
                    if (uavUnConnect()) return;

                    double rcLat = 0;
                    double rcLng = 0;
                    showBackHomePointConfirmDialog(1, rcLat, rcLng, GlobalVariable.ReturnHomeSettingType.CONTROL);
                });
    }

    /**
     * 限距
     * */
    private void initLimitDistance(){
        mViewBinding.preFlightLimitDistanceSwitch.setSelected(false);
        mViewBinding.preFlightLimitDistanceEdit.setText("--");
        mViewBinding.preFlightLimitDistanceEdit.setEnabled(false);
        String distanceLimitTipStr = UnitChnageUtils.getUnitValue(com.gdu.util.MyConstants.LIMIT_DISTANCE_MIN)
                + "-" + UnitChnageUtils.getUnitValue(com.gdu.util.MyConstants.LIMIT_DISTANCE_MAX)
                + UnitChnageUtils.getUnit();
        mViewBinding.preFlightLimitDistanceValue.setText(distanceLimitTipStr);
        //根据英寸单位换算的最大数值长度，设置输入框的最大可输入范围
        mViewBinding.preFlightLimitDistanceEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(String.valueOf(UnitChnageUtils.getUnitValue(com.gdu.util.MyConstants.LIMIT_DISTANCE_MAX)).length())});

        viewModel.getLimitDistanceLiveData().observe(this, data->{
            if (data.getDistance() == -1) {
                mViewBinding.preFlightLimitDistanceEdit.setText("INF");
            } else {
                String limitDistanceStr = String.valueOf(UnitChnageUtils.getUnitValue(data.getDistance()));
                mViewBinding.preFlightLimitDistanceEdit.setText(limitDistanceStr);
            }
            preLimitDistanceValue  = data.isOpen() ? data.getDistance() : preLimitDistanceValue;
            mViewBinding.preFlightLimitDistanceSwitch.setSelected(data.isOpen());
            mViewBinding.preFlightLimitDistanceEdit.setSelected(data.isOpen());
            mViewBinding.preFlightLimitDistanceEdit.setEnabled(data.isOpen());
        });
        SingleClickUtil.onSingleClick(mViewBinding.preFlightLimitDistanceSwitch, false, v -> {
            if (!ConnectUtil.isConnect()) {
                return;
            }
            final int value = preLimitDistanceValue == 0 ? MyConstants.LIMIT_DISTANCE_DEFAULT : preLimitDistanceValue;
            viewModel.setLimitDistance(!mViewBinding.preFlightLimitDistanceSwitch.isSelected(), mViewBinding.preFlightLimitDistanceSwitch.isSelected() ? 0 : value);
            mViewBinding.preFlightLimitDistanceEdit.clearFocus();
        });

        mViewBinding.preFlightLimitDistanceEdit.setOnFocusChangeListener((v, hasFocus) -> viewModel.setEditLimitDistance(hasFocus));
        mViewBinding.preFlightLimitDistanceEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE || !mViewBinding.preFlightLimitDistanceSwitch.isSelected()) {
                showToast(getResources().getString(R.string.Label_SettingFail));
                return false;
            }
            final String inputStr = v.getText().toString();
            if (CommonUtils.isEmptyString(inputStr) || !CommonUtils.isNumberInt(inputStr)) {
                showToast(getResources().getString(R.string.Label_SettingFail));
                return false;
            }
            int value = UnitChnageUtils.inch2m(Integer.parseInt(inputStr));

            mViewBinding.preFlightLimitDistanceEdit.clearFocus();
            viewModel.setLimitDistance(true, value);
            return false;
        });
        viewModel.getBaseFlightViewModel().getLimitDistance();
    }

    /**
     * 摇杆模式设置
     * */
    private void initRockerMode(){
        viewModel.getAircraftMappingStyleLiveData().observe(this, data->{
            mViewBinding.preFlightRockerModeEdit.setIndex(data);
        });
        mViewBinding.preFlightRockerModeEdit.setOnOptionClickListener((parentId, view, position) ->
                SingleClickUtil.determineTriggerSingleClick(view, v -> {
                    if (uavUnConnect()) return;
                    if ((GlobalVariable.controlHand == ControlHand.HAND_AMERICA && position == 0) || (GlobalVariable.controlHand == ControlHand.HAND_CHINA && position == 1)
                            || (GlobalVariable.controlHand == ControlHand.HAND_JAPAN && position == 2)) {
                        return;
                    }
                    new CommonDialog.Builder(getSupportFragmentManager())
                            .setTitle(getString(R.string.sure_switch_hand_title))
                            .setContent(getString(R.string.sure_switch_hand_content))
                            .setCancel(getString(R.string.Label_cancel))
                            .setSure(getString(R.string.Label_Sure))
                            .setCancelableOutside(false)
                            .setPositiveListener((dialog, which) -> {
                                viewModel.setAircraftMappingStyle(position);
                            }).build().show();
                }));
    }

    /**
     * 返航点设置二次确认弹框
     */
    private void showBackHomePointConfirmDialog(int showType, double lat, double lng, byte type){
        DialogLayoutBackHomePointBinding binding = DialogLayoutBackHomePointBinding.inflate(LayoutInflater.from(this));
        binding.mBackHomePointSetBgIv.setImageResource(showType == 0 ? R.drawable.icon_backpoint_airf :
                (showType == 1 ? R.drawable.icon_backpoint_rc : R.drawable.icon_backpoint_target));
        binding.rcTv.setVisibility(showType == 0 ? View.GONE : View.VISIBLE);
        binding.rcTv.setText(getResources().getString(showType == 1 ? R.string.Label_RemoteControl :
                R.string.string_target_point));
        binding.rcTv.setTextColor(ContextCompat.getColor(this, showType == 1 ? R.color.color_E6BD00 : R.color.color_00C586));
        new CommonDialog.Builder(getSupportFragmentManager()).setCustomContentView(binding.getRoot())
                .setCancelableOutside(false)
                .setCancel(getString(R.string.Label_cancel))
                .setSure(getString(R.string.Label_Sure))
                .setContent(showType == 0 ? getString(R.string.string_back_home_point_to_now_airf) :
                        (showType == 1 ? getString(R.string.string_back_home_point_to_rc) : getString(R.string.string_back_home_point_to_target_point)))
                .setPositiveListener((dialog, which) -> {
                    viewModel.setHomePoint(lat, lng, type);
                }).build().show();
    }

    private void closeItem(TextView statusTv, Group itemGroup) {
        statusTv.setVisibility(View.VISIBLE);
        itemGroup.setVisibility(View.GONE);
    }

    private void setObstacleItemValue(int stopValue, int alarmValue,
                                      DoubleDragThumbSeekBar2 doubleDragThumbSeekBar, Group group, TextView closeView) {
        if (stopValue <= 0 || alarmValue <= 0) {
            return;
        }
        doubleDragThumbSeekBar.setLowAndHeightProgress(stopValue, alarmValue);
        group.setVisibility(View.VISIBLE);
        closeView.setVisibility(View.GONE);
    }

    private void setHeightLimitSwitch() {
        MyLogUtils.i("setHeightLimitSwitch()");
        if (!GlobalVariable.isNewHeightLimitStrategy) {
            if (!ConnectUtil.isConnect()) {
                return;
            }
        }
        if (mViewBinding.preFlightLimitHeightSwitch.isSelected()) {
            mViewBinding.preFlightLimitHeightEdit.clearFocus();
            new CommonDialog.Builder(getSupportFragmentManager()).setTitle(getResources().getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                    .setContent(getResources().getString(R.string.limit_height_statement_content))
                    .setCancel(getResources().getString(R.string.think_moment))
                    .setSure(getResources().getString(R.string.think_agree))
                    .setCancelableOutside(false)
                    .setPositiveListener((dialog, which) -> {
                        viewModel.setLimitHeight(false, 0);
                    }).build().show();
        } else {
            final int value = preLimitHeightValue == 0 ? com.gdu.util.MyConstants.LIMIT_HEIGHT_DEFAULT : preLimitHeightValue;
            viewModel.setLimitHeight(true, value);
            viewModel.setGoHomeHeight(value);

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backHandle();
    }

    private void backHandle() {
        final Intent intent = new Intent(this, FlightActivity.class);
        startActivity(intent);
        finish();
    }

    public void showToast(String str) {
        ThreadHelper.runOnUiThread(() -> Toast.makeText(this, str, Toast.LENGTH_SHORT).show());
    }

    private void showLimitHeightDialog(int limit) {
        new CommonDialog.Builder(getSupportFragmentManager()).setTitle(getResources().getString(R.string.Toast_planset_fly_pager_limit_height_statement))
                .setContent(getResources().getString(R.string.limit_height_statement_content))
                .setCancel(getResources().getString(R.string.think_moment))
                .setSure(getResources().getString(R.string.think_agree))
                .setCancelableOutside(false)
                .setPositiveListener((dialog, which) -> {
                    viewModel.setLimitHeight(true, limit);
                }).build().show();
    }

    public void showErrTip(int setType, int type) {
        ThreadHelper.runOnUiThread(() -> {
            if (setType == 1) {
                setHeightFailHandle();
            } else if(setType == 2) {
                setDistanceFailHandle();
            } else if(setType == 3){
                setGoHomeHeightFailHandle();
            }
            switch (type) {
                case 1:
                    mViewBinding.preFlightLimitHeightEdit.setEnabled(false);
                    mViewBinding.preFlightLimitDistanceEdit.setEnabled(false);

                    mViewBinding.preFlightLimitHeightSwitch.setSelected(false);
                    mViewBinding.preFlightLimitDistanceSwitch.setSelected(false);

                    showToast(getString(R.string.DeviceNoConn));
                    break;

                case 2:
                    showToast(getString(R.string.string_tether_can_not_set));
                    break;

                case 3:
                    showToast(getString(R.string.Msg_GoHomingUnSet));
                    break;

                case 4:
                    showToast(getString(R.string.input_error));
                    break;

                default:
                    break;
            }
        });
    }

    private void setHeightFailHandle() {
        String limitHeightStr = String.valueOf(UnitChnageUtils.getUnitValue(preLimitHeightValue));
        mViewBinding.preFlightLimitHeightEdit.setText(limitHeightStr);
    }

    private void setDistanceFailHandle() {
        String limitDistanceStr = getLimitValue(preLimitDistanceValue);
        mViewBinding.preFlightLimitDistanceEdit.setText(limitDistanceStr);
    }

    private void setGoHomeHeightFailHandle(){
        mViewBinding.preFlightHomeHeightEdit.setText(getLimitValue(viewModel.getPreGoHomeHeight()));
    }
    private String getLimitValue(int val) {
        final String var = val == -1 ? "INF" : String.valueOf(UnitChnageUtils.getUnitValue(val));
        return var;
    }

    private void setLowBatteryValue(int lowValue, int heightValue) {
        mViewBinding.tvLostLowPowerWarn.setText(lowValue + "%");
        mViewBinding.tvLowPowerWarn.setText(heightValue + "%");
        mViewBinding.sbpLowPowerAlarmPb.setLowAndHeightProgress(lowValue, heightValue);
    }

    /**
     * 避障策略开启关闭
     */
    public void switchObstacleStrategy(boolean isOn) {
        MyLogUtils.i("switchObstacleStrategy() isOn = " + isOn);
        if(isOn){
            sendObstacleStrategy(true);
        }else{
            showConfirmDialog(false);
        }
    }

    private void showConfirmDialog(boolean isOn) {
        new CommonDialog.Builder(getSupportFragmentManager()).setContent(getString(R.string.string_close_obstacle_strategy_tips))
                .setCancelableOutside(false)
                .setCancelable(false)
                .setPositiveListener((dialog, which) -> sendObstacleStrategy(isOn)).build().show();
    }

    /**
     * 发送主动刹停指令
     */
    private void sendObstacleStrategy(boolean isOpen){
        viewModel.getBaseFlightAssistantViewModel().setObstacleAvoidanceStrategyEnabled(isOpen);
    }

    private void switchVisionObstacleClickHandle() {
        if (uavUnConnect()) return;
        // 视觉避障异常时无法开启
        if (!mViewBinding.ivVisionObstacleSwitch.isSelected()
                && !CommonUtils.isEmptyList(CommonUtils.allowOpenObstacle(this))) {
            String errStr = getVisionObstacleErrContent(CommonUtils.allowOpenObstacle(this));
            new CommonDialog.Builder(getSupportFragmentManager()).setTitle(getString(R.string.string_vision_error))
                    .setContent(errStr)
                    .setCancelVisible(false)
                    .setCancelableOutside(false)
                    .setSure(getString(R.string.Label_Sure)).build().show();
            return;
        }


        if (GlobalVariable.flyMode == 0 && !mViewBinding.ivVisionObstacleSwitch.isSelected()) {
            showToast(getString(R.string.Label_AttitudeModel_obstaticIsOff));
            return;
        } else if (GlobalVariable.flyMode == 1 && GlobalVariable.DroneFlyMode == 0
                && !mViewBinding.ivVisionObstacleSwitch.isSelected()) {
            showToast(getString(R.string.Label_SportModel_obstaticIsOff));
            return;
        }

        if (mViewBinding.ivVisionObstacleSwitch.isSelected()) {
            new CommonDialog.Builder(getSupportFragmentManager()).setTitle(getString(R.string.close_vision_title))
                    .setContent(getString(R.string.close_vision_content))
                    .setCancel(getString(R.string.Label_cancel))
                    .setSure(getString(R.string.Label_Sure))
                    .setCancelableOutside(false)
                    .setPositiveListener((dialog, which) -> {
                        switchVisionObstacle(false);
                        mViewBinding.ivVisionObstacleSwitch.setSelected(!mViewBinding.ivVisionObstacleSwitch.isSelected());
                        mViewBinding.viewObstacleStrategyGroup.setVisibility(!mViewBinding.ivVisionObstacleSwitch.isSelected() ? View.VISIBLE : View.GONE);

                    }).build().show();
        } else {
            switchVisionObstacle(true);
            closeOrShowRadar(true);
            mViewBinding.ivVisionObstacleSwitch.setSelected(!mViewBinding.ivVisionObstacleSwitch.isSelected());
            mViewBinding.viewObstacleStrategyGroup.setVisibility(!mViewBinding.ivVisionObstacleSwitch.isSelected() ? View.VISIBLE : View.GONE);
        }
    }

    private void closeOrShowRadar(boolean isShow) {
        GlobalVariable.hadShowObstacle = isShow;
        SPUtils.put(this, GduConfig.ISSHOWROCKER, isShow);
    }

    private void switchVisionObstacle(boolean isOn) {
        MyLogUtils.i("switchVisionObstacle() isOn = " + isOn);
        viewModel.getBaseFlightAssistantViewModel().setVisionSensingEnabled(isOn);
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

    /**
     * 无人机未连接判断
     * */
    private boolean uavUnConnect(){
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            showToast(getString(R.string.DeviceNoConn));
            return true;
        }
        return false;
    }
}
