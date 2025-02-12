package com.gdu.demo.flight.setting.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gdu.GlobalVariableTest;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.databinding.FragmentSettingBatteryBinding;
import com.gdu.demo.flight.base.BaseFlightViewModel;
import com.gdu.demo.flight.setting.viewmodel.SettingBatteryViewModel;
import com.gdu.demo.utils.BatteryUtil;
import com.gdu.drone.BatteryInfoZ4C;
import com.gdu.drone.PlanType;
import com.gdu.util.FormatConfig;
import com.gdu.util.TimeUtil;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * @Author: lixiqiang
 * @Date: 2022/6/27
 */
public class SettingBatteryFragment extends Fragment {

    private FragmentSettingBatteryBinding mViewBinding;
    private BaseFlightViewModel baseViewModel;
    private SettingBatteryViewModel batteryViewModel;
    private FragmentActivity mActivity;

    private BatteryInfoZ4C mBatteryNo1;

    private CellAdapter upCellAdapter, downCellAdapter;
    private List<Integer> upCellDatas = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0,0));
    private final List<Integer> downCellDatas = new ArrayList<Integer>(Arrays.asList(0,0,0,0,0,0));


    private final DecimalFormat df = FormatConfig.format_4;
    private final DecimalFormat df2 = FormatConfig.format_6;
    private final DecimalFormat df3 = FormatConfig.format_9;

    /** 电池更新超时时间 */
    private static final int BATTERY_UPDATE_TIME_OUT = 30;

    private final static int SET_SUCCESS = 100;
    private final static int SET_FAILED = 101;

    /**
     * 是否因断联重置了电池信息
     */
    private boolean isResetBatteryInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewBinding = FragmentSettingBatteryBinding.inflate(inflater, container, false);
        return mViewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActivity = getActivity();
        if (null != mActivity) {
            baseViewModel = new ViewModelProvider(mActivity).get(BaseFlightViewModel.class);
            batteryViewModel = new ViewModelProvider(mActivity).get(SettingBatteryViewModel.class);
        }
        initView();
        initData();
    }

    private void initView() {
        setListener();

        GlobalVariable.twoLevelLowBattery = mViewBinding.lowPowerWarnSb.getSeekBarMin();
        GlobalVariable.oneLevelLowBattery = mViewBinding.lowestPowerWarnSb.getSeekBarMin();

        mViewBinding.lowPowerWarnSb.setEnabled(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess);
        mViewBinding.lowestPowerWarnSb.setEnabled(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess);
        mViewBinding.incBatteryLayout2.getRoot().setVisibility(View.GONE);
        LinearLayoutManager upLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager downLayoutManager = new LinearLayoutManager(getContext());
        upLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        downLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mViewBinding.incBatteryLayout2.rvCellUpContent.setLayoutManager(downLayoutManager);
        mViewBinding.incBatteryLayout1.rvCellUpContent.setLayoutManager(upLayoutManager);

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
            upCellDatas = new ArrayList<>(Arrays.asList(0, 0, 0, 0));
        }

        upCellAdapter = new CellAdapter(upCellDatas);
        downCellAdapter = new CellAdapter(downCellDatas);
        mViewBinding.incBatteryLayout2.rvCellUpContent.setAdapter(downCellAdapter);
        mViewBinding.incBatteryLayout1.rvCellUpContent.setAdapter(upCellAdapter);

        if (GlobalVariable.planType == PlanType.MGP12
                || GlobalVariable.planType == PlanType.S480
                || GlobalVariable.planType == PlanType.S450
                || GlobalVariable.planType == PlanType.S220
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
            mViewBinding.incBatteryLayout1.tvBatteryName.setText( getResources().getString(R.string.Label_Battery));
        } else {
            mViewBinding.incBatteryLayout1.tvBatteryName.setText(getResources().getString(R.string.Label_Battery_Upper));
        }
        mViewBinding.incBatteryLayout2.tvBatteryName.setText(getResources().getString(R.string.Label_Battery_Lower));

        mViewBinding.flightBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTable = 1;
                // 从遥控器切换到飞行器电池，需要重新判断是否刷新电池信息（因为共用一个布局显示的还是遥控器电池信息）
                // 若未连接需要重新刷新一次，且防止后面重复刷新
                isResetBatteryInfo = false;
                switchSelectTab(selectTable);
            }
        });
        mViewBinding.rcBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTable = 2;
                switchSelectTab(selectTable);
            }
        });

    }

    public void switchSelectTab(int selectTable) {

        if (selectTable == 1){
            updateSelectTab(mViewBinding.flightBattery);
            updateFlBatteryView();
            if (GlobalVariable.sBattery1InfoZ4C != null ){
                setFlBatteryData();
            }else {
                resetBatteryInfoOnce();
            }
        } else {
            updateSelectTab(mViewBinding.rcBattery);
            updateRcBatteryView();
            resetBatteryStateView(mViewBinding.incBatteryLayout1.tvBatteryState);
            if (GlobalVariable.subLevel > -1){
                resetBatteryStateView(mViewBinding.incBatteryLayout2.tvBatteryState);
            }
            setRCBatteryData();
        }
    }

    //1：tab切换，显示电池1，隐藏电池2，（显示时长，显示sn、显示控制组件，隐藏内外置标题）
    private void updateFlBatteryView() {
        mViewBinding.incBatteryLayout2.z4bBatterySub.setVisibility(View.GONE);
        mViewBinding.incBatteryLayout1.llFlightDuration.setVisibility(View.VISIBLE);
        mViewBinding.lvSnNumber.setVisibility(View.VISIBLE);
        mViewBinding.settingBar.setVisibility(View.VISIBLE);
        mViewBinding.battery1.setVisibility(View.GONE);
        mViewBinding.battery2.setVisibility(View.GONE);
        mViewBinding.layoutBms.setVisibility(View.VISIBLE);
    }

    //2：显示电池1，显示电池2（如果有），（隐藏时长，隐藏sn、隐藏控制组件，显示内外置标题）
    private void updateRcBatteryView() {
        if (GlobalVariable.subLevel > -1){
            mViewBinding.incBatteryLayout2.z4bBatterySub.setVisibility(View.VISIBLE);
            mViewBinding.battery2.setVisibility(View.VISIBLE);
        }else {
            mViewBinding.incBatteryLayout2.z4bBatterySub.setVisibility(View.GONE);
            mViewBinding.battery2.setVisibility(View.GONE);
        }
        mViewBinding.incBatteryLayout1.llFlightDuration.setVisibility(View.GONE);
        mViewBinding.incBatteryLayout2.llFlightDuration.setVisibility(View.GONE);
        mViewBinding.lvSnNumber.setVisibility(View.GONE);
        mViewBinding.settingBar.setVisibility(View.GONE);
        mViewBinding.battery1.setVisibility(View.VISIBLE);
        mViewBinding.layoutBms.setVisibility(View.GONE);
    }

    private void updateSelectTab(View view) {
        if (null == getContext()) return;
        if (view == mViewBinding.flightBattery) {
            mViewBinding.flightBattery.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.shape_bg_ff4e00));
            mViewBinding.flightBattery.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            mViewBinding.rcBattery.setBackground(null);
            mViewBinding.rcBattery.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        } else {
            mViewBinding.rcBattery.setBackground(ContextCompat.getDrawable(getContext(),R.drawable.shape_bg_ff4e00));
            mViewBinding.rcBattery.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            mViewBinding.flightBattery.setBackground(null);
            mViewBinding.flightBattery.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        }
    }

    private void resetBatteryStateView(TextView view) {
        view.setText(getString(R.string.state_normal));
        view.setBackgroundResource(R.drawable.shape_bg_battery_state);
        view.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    private void resetBattery1(){
        mViewBinding.incBatteryLayout1.tvBatteryState.setText(getString(R.string.state_none));
        mViewBinding.incBatteryLayout2.tvBatteryState.setText(getString(R.string.state_none));
        mViewBinding.incBatteryLayout1.tvBatteryState.setBackground(null);
        mViewBinding.incBatteryLayout1.tvBatteryState.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        mViewBinding.incBatteryLayout1.tvTempContent.setText("--");
        mViewBinding.incBatteryLayout1.tvBatteryPercent.setText("--");
        mViewBinding.incBatteryLayout1.tvBatteryPercent.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        mViewBinding.incBatteryLayout1.tvBatteryVoltage.setText("--");
        mViewBinding.incBatteryLayout1.tvBatteryCapacity.setText("--");
        mViewBinding.incBatteryLayout1.tvChargeNumContent.setText("--");
        mViewBinding.incBatteryLayout1.tvCurElectricContent.setText("--");
        mViewBinding.incBatteryLayout1.tvVoltageDifferential.setText("--");
        mViewBinding.valueBmsTemp.setText(getString(R.string.Label_N_A));
        upCellDatas.clear();
        upCellDatas.addAll(Arrays.asList(-1,-1,-1,-1,-1,-1));
        upCellAdapter.notifyDataSetChanged();
    }

    private void initData() {
        getBatteryWaringSet();
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(RxLife.to(this))
                .subscribe(aLong -> {
                    switchSelectTab(selectTable);
                });
    }


    private void setRCBatteryData() {
        if (GlobalVariable.mainLevel != -1) {
            updateRcBattery1UI();
        } else {
            resetBattery1();
        }
        if (GlobalVariable.subLevel != -1) {
            updateRcBattery2UI();
        } else {
            downCellDatas.clear();
        }
    }

    //飞行器电池UI显示
    private void setFlBatteryData() {
        //1、更新飞行时长，飞机电池界面可见，遥控器电池界面隐藏
        updateFlightDuration();
        //2、更新UI可见组件：飞机电池界面，第二个电池隐藏；遥控器电池界面，根据电池数量显示
        mBatteryNo1 = GlobalVariable.sBattery1InfoZ4C;
        if (mBatteryNo1 == null) {
            resetBatteryInfoOnce();
            return;
        }

        if(GlobalVariable.connStateEnum == ConnStateEnum.Conn_None){
            resetBatteryInfoOnce();
            return;
        } else {
            isResetBatteryInfo = false;
        }
        resetBatteryStateView(mViewBinding.incBatteryLayout1.tvBatteryState);
        long currentTime = System.currentTimeMillis();
        if (currentTime - mBatteryNo1.getLastUpdateTime()> BATTERY_UPDATE_TIME_OUT) {
            updateFlBattery1UI();
        } else {
            upCellDatas.clear();
            upCellAdapter.notifyDataSetChanged();
        }
    }

    // 未连接飞机 只刷新一次电池信息
    private void resetBatteryInfoOnce(){
        if (!isResetBatteryInfo) {
            resetBattery1();
            isResetBatteryInfo = true;
        }
    }

    //刷新飞行器电池1界面
    private void updateFlBattery1UI() {
        short batteryTemp;
        int upBatteryProgress = mBatteryNo1.getPower();
        mViewBinding.incBatteryLayout1.batteryUpPb.setProgress(upBatteryProgress);
        if(upBatteryProgress >= 40){
            mViewBinding.incBatteryLayout1.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(),R.drawable.main_progress_vertical_green));
        } else if(upBatteryProgress >= 20 && upBatteryProgress < 40){
            mViewBinding.incBatteryLayout1.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(),R.drawable.main_progress_vertical_yellow));
        }else {
            mViewBinding.incBatteryLayout1.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(),R.drawable.main_progress_vertical_red));
        }
        batteryTemp = mBatteryNo1.getTemp();
        float realTemp = batteryTemp / 10;
        if (GlobalVariable.planType == PlanType.MGP12
                || GlobalVariable.planType == PlanType.S480
                || GlobalVariable.planType == PlanType.S450
                || GlobalVariable.planType == PlanType.S220
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
            realTemp = (batteryTemp - 2731)/10;
        }
        String vlotage = df.format(mBatteryNo1.getTotalVoltage()/1000.0f);
        if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_Sucess) {
            realTemp = 0;
            vlotage = "0";
        }
        mViewBinding.incBatteryLayout1.tvTempContent.setText(realTemp + "℃");
        mViewBinding.incBatteryLayout1.tvBatteryPercent.setText(mBatteryNo1.getPower() + "%");
        mViewBinding.incBatteryLayout1.tvBatteryPercent.setTextColor(ContextCompat.getColor(getContext(), R.color.color_05C336));
        mViewBinding.incBatteryLayout1.tvBatteryVoltage.setText(vlotage + "V");
        mViewBinding.incBatteryLayout1.tvBatteryCapacity.setText(mBatteryNo1.getBattery_capacity_left() + "mAH");
        mViewBinding.incBatteryLayout1.tvChargeNumContent.setText(String.valueOf(mBatteryNo1.getInflationNumber()));
        final int electricValue = Math.abs(mBatteryNo1.getCurrentElectricity()) * 2;
        mViewBinding.incBatteryLayout1.tvCurElectricContent.setText(electricValue + "mA");
        mViewBinding.incBatteryLayout1.tvVoltageDifferential.setText("0" + df3.format(mBatteryNo1.getBattery_max_dropout_voltage() /1000.0f) + "V");
        if(mBatteryNo1.getAfe_temp() != BatteryInfoZ4C.LOWEST_TEMP) {
            mViewBinding.valueBmsTemp.setText((mBatteryNo1.getAfe_temp() - 2731) / 10 + "℃");
        }
        upCellDatas.clear();
        upCellDatas.addAll(mBatteryNo1.getBatteryCellList());
        upCellAdapter.notifyDataSetChanged();
    }


    //刷新并显示当前遥控器电池2信息界面
    private void updateRcBattery1UI() {
        MyLogUtils.i("updateRcBattery1UI()");
        mViewBinding.incBatteryLayout2.tvBatteryState.setText(getString(R.string.state_normal));
        mViewBinding.battery1.setVisibility(View.VISIBLE);
        int upBatteryProgress = GlobalVariable.mainLevel;
        mViewBinding.incBatteryLayout1.batteryUpPb.setProgress(upBatteryProgress);
        if (upBatteryProgress >= 40) {
            mViewBinding.incBatteryLayout1.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.main_progress_vertical_green));
        } else if (upBatteryProgress >= 20 && upBatteryProgress < 40) {
            mViewBinding.incBatteryLayout1.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.main_progress_vertical_yellow));
        } else {
            mViewBinding.incBatteryLayout1.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.main_progress_vertical_red));
        }
        String vlotage = df.format(GlobalVariable.mainVolt / 1000.0f);
        mViewBinding.incBatteryLayout1.tvTempContent.setText(GlobalVariable.mainTemp + "℃");
        mViewBinding.incBatteryLayout1.tvBatteryPercent.setText(GlobalVariable.mainLevel + "%");
        mViewBinding.incBatteryLayout1.tvBatteryPercent.setTextColor(ContextCompat.getColor(getContext(), R.color.color_05C336));
        mViewBinding.incBatteryLayout1.tvBatteryVoltage.setText(vlotage + "V");
        mViewBinding.incBatteryLayout1.tvBatteryCapacity.setText(GlobalVariable.mainCapacity + "mAH");
        mViewBinding.incBatteryLayout1.tvChargeNumContent.setText(String.valueOf(GlobalVariable.mainCycleCount));

        mViewBinding.incBatteryLayout1.tvCurElectricContent.setText(Math.abs(GlobalVariable.mainCurrent) + "mA");
        String voltDif = df3.format(Math.abs(GlobalVariable.mainCell1 - GlobalVariable.mainCell2) / 1000.0f);

        mViewBinding.incBatteryLayout1.tvVoltageDifferential.setText("0" + voltDif + "V");
        upCellDatas.clear();
        upCellDatas.addAll(Arrays.asList(GlobalVariable.mainCell1, GlobalVariable.mainCell1, GlobalVariable.mainCell2, GlobalVariable.mainCell2));
        upCellAdapter.notifyDataSetChanged();
    }

    //刷新并显示当前遥控器电池2信息界面
    private void updateRcBattery2UI() {
        MyLogUtils.i("updateRcBattery2UI()");
        mViewBinding.incBatteryLayout2.tvBatteryState.setText(getString(R.string.state_normal));
        mViewBinding.battery2.setVisibility(View.VISIBLE);
        mViewBinding.incBatteryLayout2.z4bBatterySub.setVisibility(View.VISIBLE);
        int downBatteryProgress = GlobalVariable.subLevel;
        mViewBinding.incBatteryLayout2.batteryUpPb.setProgress(downBatteryProgress);
        if (downBatteryProgress >= 40) {
            mViewBinding.incBatteryLayout2.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.main_progress_vertical_green));
        } else if (downBatteryProgress >= 20 && downBatteryProgress < 40) {
            mViewBinding.incBatteryLayout2.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.main_progress_vertical_yellow));
        } else {
            mViewBinding.incBatteryLayout2.batteryUpPb.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.main_progress_vertical_red));
        }
        mViewBinding.incBatteryLayout2.tvTempContent.setText(GlobalVariable.subTemp + "℃");
        mViewBinding.incBatteryLayout2.tvBatteryPercent.setText(GlobalVariable.subLevel + "%");
        mViewBinding.incBatteryLayout2.tvBatteryPercent.setTextColor(ContextCompat.getColor(getContext(), R.color.color_05C336));
        mViewBinding.incBatteryLayout2.tvBatteryVoltage.setText(df.format(GlobalVariable.subVolt / 1000.0f) + "V");
        mViewBinding.incBatteryLayout2.tvBatteryCapacity.setText(GlobalVariable.subCapacity + "mAH");
        mViewBinding.incBatteryLayout2.tvChargeNumContent.setText(String.valueOf(GlobalVariable.subCycleCount));
        mViewBinding.incBatteryLayout2.tvCurElectricContent.setText(Math.abs(GlobalVariable.subCurrent) + "mA");
        String voltDif1 = df3.format(Math.abs(GlobalVariable.subCell1 - GlobalVariable.subCell2) / 1000.0f);
        mViewBinding.incBatteryLayout2.tvVoltageDifferential.setText("0" + voltDif1 + "V");
        downCellDatas.clear();
        downCellDatas.addAll(Arrays.asList(GlobalVariable.subCell1, GlobalVariable.subCell1, GlobalVariable.subCell2, GlobalVariable.subCell2));
        downCellAdapter.notifyDataSetChanged();
    }

    int selectTable = 1; //1:飞机电池 2：遥控器电池
    /**
     * 更新飞行时间
     */
    private void updateFlightDuration() {
        long flyTime = GlobalVariableTest.FlyTimeOnSky;
        if (flyTime < 0) {
            flyTime += 65535;
        }
        if (GlobalVariable.droneFlyState == 1 || GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            flyTime = 0;
        }
        String time = TimeUtil.getTime(flyTime * 1000 - 8 * 3600 * 1000, "HH:mm:ss");
        mViewBinding.incBatteryLayout1.tvFlightDuration.setText(time);

    }

    private void getBatteryWaringSet() {
        baseViewModel.getLowBatteryWarningLiveData().observe(mActivity, data->{
            mViewBinding.lowestPowerWarnSb.setProgress(data.getOneLevelWarn());
            mViewBinding.lowPowerWarnSb.setProgress(data.getTwoLevelWarn());
        });
        baseViewModel.getLowBatteryWarningThreshold();
        batteryViewModel.getBatterySNLiveData().observe(mActivity, data->{
            mViewBinding.tvBatterySn.setText(data);
        });
        batteryViewModel.getBatterFactoryInfo();

    }


    private void setListener() {
        mViewBinding.lowPowerWarnSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mViewBinding.lowPowerWarnSb.setTextProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlobalVariable.twoLevelLowBattery = seekBar.getProgress();
                mViewBinding.lowPowerWarnSb.setTextProgress(GlobalVariable.twoLevelLowBattery );
                if (GlobalVariable.twoLevelLowBattery < GlobalVariable.oneLevelLowBattery + GlobalVariable.BATTERY_MIN_INTERVAL) {
                    GlobalVariable.oneLevelLowBattery = GlobalVariable.twoLevelLowBattery - GlobalVariable.BATTERY_MIN_INTERVAL;
                    mViewBinding.lowestPowerWarnSb.setProgress(GlobalVariable.oneLevelLowBattery);
                }
                sendSetBatteryWaring();
            }
        });

        mViewBinding.lowPowerWarnSb.setOnEditChangeListener(progress -> {
            GlobalVariable.twoLevelLowBattery = progress;
            if (GlobalVariable.twoLevelLowBattery < GlobalVariable.oneLevelLowBattery + GlobalVariable.BATTERY_MIN_INTERVAL) {
                GlobalVariable.oneLevelLowBattery = GlobalVariable.twoLevelLowBattery - GlobalVariable.BATTERY_MIN_INTERVAL;
                mViewBinding.lowestPowerWarnSb.setProgress(GlobalVariable.oneLevelLowBattery);
            }
            sendSetBatteryWaring();
        });

        mViewBinding.lowestPowerWarnSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mViewBinding.lowestPowerWarnSb.setTextProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                GlobalVariable.oneLevelLowBattery = seekBar.getProgress();
                mViewBinding.lowestPowerWarnSb.setTextProgress(GlobalVariable.oneLevelLowBattery);
                if (GlobalVariable.oneLevelLowBattery > GlobalVariable.twoLevelLowBattery - GlobalVariable.BATTERY_MIN_INTERVAL) {
                    GlobalVariable.twoLevelLowBattery = GlobalVariable.oneLevelLowBattery + GlobalVariable.BATTERY_MIN_INTERVAL;
                    mViewBinding.lowPowerWarnSb.setProgress(GlobalVariable.twoLevelLowBattery);
                }
                sendSetBatteryWaring();
            }
        });

        mViewBinding.lowestPowerWarnSb.setOnEditChangeListener(progress -> {
            GlobalVariable.oneLevelLowBattery = progress;

            if (GlobalVariable.oneLevelLowBattery > GlobalVariable.twoLevelLowBattery - GlobalVariable.BATTERY_MIN_INTERVAL) {
                GlobalVariable.twoLevelLowBattery = GlobalVariable.oneLevelLowBattery + GlobalVariable.BATTERY_MIN_INTERVAL;
                mViewBinding.lowPowerWarnSb.setProgress(GlobalVariable.twoLevelLowBattery);
            }
            sendSetBatteryWaring();
        });
    }

    private void sendSetBatteryWaring() {
        baseViewModel.setLowBatteryWarningThreshold((byte) GlobalVariable.twoLevelLowBattery, (byte) GlobalVariable.oneLevelLowBattery);
    }

    public class CellAdapter extends RecyclerView.Adapter<CellAdapter.VH>{
        //② 创建ViewHolder
        public class VH extends RecyclerView.ViewHolder{
            public final ProgressBar cellProgress;
            public final TextView cellVoltage;
            public VH(View v) {
                super(v);
                cellProgress = v.findViewById(R.id.cell_progressBar);
                cellVoltage = v.findViewById(R.id.cell_voltage);
            }
        }

        private final List<Integer> mDatas;
        public CellAdapter(List<Integer> data) {
            this.mDatas = data;
        }

        //③ 在Adapter中实现3个方法
        @Override
        public void onBindViewHolder(VH holder, int position) {
            int voltage = mDatas.get(position);
            if(voltage == 0){
                holder.cellVoltage.setText("0V");
            }else if(voltage == -1){
                holder.cellVoltage.setText("--");
            }else {
                holder.cellVoltage.setText(df2.format(voltage / 1000.0f) + "V");
            }

            int progress = BatteryUtil.getSingleRemainingPower(GlobalVariable.planType,voltage);

            MyLogUtils.d("cellVoltage  value =   " + mDatas.get(position) + " progress = " + progress);
            if(progress >= 40){
                holder.cellProgress.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cell_progress_vertical_green));
                holder.cellProgress.setBackgroundResource(R.drawable.battery_bg_normal);
            } else if(progress >= 20 && progress < 40){
                holder.cellProgress.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.cell_progress_vertical_yellow));
                holder.cellProgress.setBackgroundResource(R.drawable.battery_bg_low);
            }else {
                holder.cellProgress.setProgressDrawable(ContextCompat.getDrawable(getContext(),R.drawable.cell_progress_vertical_red));
                holder.cellProgress.setBackgroundResource(0);
            }
            holder.cellProgress.setProgress(progress);
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            //LayoutInflater.from指定写法
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_z4b_cell_info, parent, false);
            return new VH(v);
        }
    }




    public static SettingBatteryFragment newInstance() {
        Bundle args = new Bundle();
        SettingBatteryFragment fragment = new SettingBatteryFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
