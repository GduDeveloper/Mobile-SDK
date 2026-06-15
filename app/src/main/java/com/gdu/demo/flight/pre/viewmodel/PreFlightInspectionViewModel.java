package com.gdu.demo.flight.pre.viewmodel;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMapUtils;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.flight.base.BaseFlightAssistantViewModel;
import com.gdu.demo.flight.base.BaseFlightViewModel;
import com.gdu.demo.flight.base.BaseRCViewModel;
import com.gdu.demo.flight.msgbox.ErrCodeGetStringUtils;
import com.gdu.demo.flight.pre.bean.BaseFlightStatusBean;
import com.gdu.demo.flight.pre.bean.BaseSysStatusBean;
import com.gdu.demo.flight.pre.bean.ObstacleStatusBean;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.lib.util.StringUtils;
import com.gdu.lib.util.ThreadHelper;
import com.gdu.lib.util.core.ResourceUtils;
import com.gdu.lib.util.core.XLogger;
import com.gdu.msdk.device.component.interfaces.IHms;
import com.gdu.msdk.device.component.interfaces.IRTK;
import com.gdu.msdk.device.component.pod.utils.SDCardStatus;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.msdk.key.value.CycleBatteryInfo;
import com.gdu.msdk.key.value.CycleOnboardRTKInfo;
import com.gdu.msdk.key.value.bean.FlyMode;
import com.gdu.msdk.key.value.bean.PlanType;
import com.gdu.msdk.util.KVObserver;
import com.gdu.remotecontroller.AircraftMappingStyle;
import com.gdu.sdk.base.Diagnostics;
import com.gdu.sdk.flightcontroller.bean.LimitDistanceInfo;
import com.gdu.sdk.flightcontroller.bean.LimitHeightInfo;
import com.gdu.sdk.flightcontroller.bean.LowBatteryWarnInfo;
import com.gdu.sdk.flightcontroller.flightassistant.FlightAssistant;
import com.gdu.sdk.hms.WarningLevel;
import com.gdu.sdk.manager.SDKManager;
import com.rxjava.rxlife.RxLife;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

/**
 * @author wuqb
 * @date 2025/1/10
 * @description TODO
 */
public class PreFlightInspectionViewModel extends ViewModel implements Diagnostics.DiagnosticsInformationCallback {

    private BaseFlightViewModel baseViewModel;

    private BaseRCViewModel baseRCViewModel;

    private BaseFlightAssistantViewModel baseFlightAssistantViewModel;

    private final MutableLiveData<Integer> toastLiveData;
    private final MutableLiveData<List<Diagnostics>> mErrMsgLiveData;
    //飞行系统状态监听
    private final MutableLiveData<BaseSysStatusBean> sysStatusLiveData;
    //飞行状态数据检测
    private final ArrayList<BaseFlightStatusBean> mStatusData = new ArrayList<>();
    //飞机状态数据
    private final MutableLiveData<List<BaseFlightStatusBean>> flyInitStatusData;
    private final MutableLiveData<BaseFlightStatusBean> flyStatusData;
    //避障子方向开关和距离
    private final MutableLiveData<ObstacleStatusBean> obstacleHorLiveData;
    private final MutableLiveData<ObstacleStatusBean> obstacleTopLiveData;
    //飞行模式切换时视觉感知开关状态监听
    private final MutableLiveData<Boolean> visionSensingLiveData;
    //飞行模式切换时避障策略开关状态监听
    private final MutableLiveData<Boolean> obstacleAvoidanceStrategyLiveData;

    //返航高度
    private final MutableLiveData<String> goHomeHeightLiveData;
    private final String INF = "INF";

    //限制高度
    private final MutableLiveData<LimitHeightInfo> limitHeightLiveData;

    //限制距离
    private final MutableLiveData<LimitDistanceInfo> limitDistanceLiveData;


    //摇杆模式
    private final MutableLiveData<Integer> aircraftMappingStyleLiveData;

    /**
     * 低电量告警
     */
    private final MutableLiveData<LowBatteryWarnInfo> lowBatteryWarningLiveData;

    private final FlightAssistant mFlightAssistant;

    
    /**
     * 返航高度
     */
    private int preGoHomeHeight = -1;

    private final KVObserver<SDCardStatus> sdCardObserver = new KVObserver<SDCardStatus>() {
        @Override
        public void update(SDCardStatus sdCardStatus) {
            getSDCardStatus();
        }
    };

    public PreFlightInspectionViewModel() {
        toastLiveData = new MutableLiveData<>();
        mErrMsgLiveData = new MutableLiveData<>();
        sysStatusLiveData = new MutableLiveData<>();
        flyInitStatusData = new MutableLiveData<>();
        flyStatusData = new MutableLiveData<>();
        obstacleHorLiveData = new MutableLiveData<>();
        obstacleTopLiveData = new MutableLiveData<>();
        visionSensingLiveData = new MutableLiveData<>();
        obstacleAvoidanceStrategyLiveData = new MutableLiveData<>();
        goHomeHeightLiveData = new MutableLiveData<>();
        limitHeightLiveData = new MutableLiveData<>();
        limitDistanceLiveData = new MutableLiveData<>();
        aircraftMappingStyleLiveData = new MutableLiveData<>();
        lowBatteryWarningLiveData = new MutableLiveData<>();

        mFlightAssistant = SdkDemoApplication.getAircraftInstance().getFlightController().getFlightAssistant();

        SDKManager.getInstance().getProduct().setDiagnosticsInformationCallback(this);

        IHms.get().getSdCardStatus().register(sdCardObserver);
    }

    public void init(FragmentActivity context){
        baseFlightAssistantViewModel = new ViewModelProvider(context).get(BaseFlightAssistantViewModel.class);
        baseViewModel = new ViewModelProvider(context).get(BaseFlightViewModel.class);
        baseRCViewModel = new ViewModelProvider(context).get(BaseRCViewModel.class);
    }

    public BaseFlightViewModel getBaseFlightViewModel(){
        return baseViewModel;
    }

    /**
     * 检查页面内容
     * */
    public void checkFlightStatus(FragmentActivity activity){
        initViewModelObserve(activity);
        //初始化飞行状态界面数据
        initFlightStatus();

        getHomeBackHeight();

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .to(RxLife.toMain(activity))
                .subscribe(l ->{
                            getFlyMode(activity);
                            getFlightBatteryAndTemp(); //获取飞行器电量
                            getRCBattery(); //获取遥控器电量
                            getRTKStatus(); //获取RTK状态
                            getCurrRC(); //获取当前遥控器控制
                            getSDRStatus(); //获取当前图传状态
                        }, throwable -> Log.e("更新界面状态出错", throwable.getMessage()));
        //获取SD卡状态
        getSDCardStatus();
    }

    private void initFlightStatus(){
        mStatusData.clear();
        mStatusData.add(new BaseFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_MODE, R.string.fly_mode));
        mStatusData.add(new BaseFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_FLY_BATTERY, R.string.string_aircraft_battery));
        mStatusData.add(new BaseFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_RC_BATTERY, R.string.string_rc_battery));
        mStatusData.add(new BaseFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_RTK, R.string.flight_plane_rtk_status));
        mStatusData.add(new BaseFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_SDCARD, R.string.string_states_sd));
        mStatusData.add(new BaseFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_RC_CONTROL, R.string.string_rc_control));
        mStatusData.add(new BaseFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_SDR, R.string.string_states_translate));
        flyInitStatusData.setValue(mStatusData);
    }

    /**
     * 获取飞机状态信息
     * */
    private void getFlightStatus(List<Diagnostics> list){
        BaseSysStatusBean bean = new BaseSysStatusBean();
        if(IGduDroneDevice.get().isConnected()) {
            boolean isHaveAbnormal = !list.isEmpty();
            bean.setStatusTitleColor(R.color.white);
            bean.setFlightStatusColor(R.color.white);
            if (isHaveAbnormal) {
                bean.setFlightStatusStr(R.string.Label_AircraftStatusAbnormal);
                bean.setStatusBg(R.drawable.shape_gradient_ff6c00_ffa96b);

                for (Diagnostics diagnostics : list) {
                    int warnResId = ErrCodeGetStringUtils.getErrCodeStringResId(diagnostics.getHealthInformation().getComponentId(), diagnostics.getHealthInformation().getFunctionId(), diagnostics.getCode());
                    diagnostics.setReason(ResourceUtils.getString(warnResId));
                }
                mErrMsgLiveData.postValue(list);
            } else {
                bean.setFlightStatusStr(R.string.Label_AircraftStatusNormal);
                bean.setStatusBg(R.drawable.shape_gradient_11cf42_6ce377);
            }
            bean.setMoreRes(R.drawable.icon_right_enter_white);
        } else {
            bean.setStatusTitleColor(R.color.color_535658);
            bean.setFlightStatusColor(R.color.color_5B5B5B);
            bean.setFlightStatusStr(R.string.DeviceNoConn);
            bean.setStatusBg(R.drawable.shape_gradient_c6c6c6_e8ebed);
            bean.setMoreRes(R.drawable.icon_right_enter_gray);
        }
        sysStatusLiveData.setValue(bean);
    }

    /**
     * 获取当前状态内容
     * */
    private BaseFlightStatusBean getFlightStatusBean(int type){
        for (int i=0;i<mStatusData.size();i++){
            BaseFlightStatusBean bean = mStatusData.get(i);
            if (null!=bean && bean.getType() == type){
                bean.setPosition(i);
                return bean;
            }
        }
        return null;
    }

    /**
     * 获取飞行模式
     * */
    private void getFlyMode(Context context) {
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_MODE);
        if (null == bean) return;
        String modeStr;
        if(!IGduDroneDevice.get().isConnected()){
            modeStr = "---";
        }else {
            if (DroneUtils.getFlyModel() == FlyMode.ATTITUDE) {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_AGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            } else if (DroneUtils.getFlyModel() == FlyMode.VISION) {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_VGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            } else if (DroneUtils.getFlyModel() == FlyMode.TRIPOD) {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_TGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            } else if (DroneUtils.getFlyModel() == FlyMode.GPS_SPORT) {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_FGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            } else {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_PGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            }
        }
        //如果挡位一致则不再更新UI
        if (!TextUtils.equals(modeStr, bean.getContent())) {
            bean.setContent(modeStr);
            flyStatusData.setValue(bean);
        }
    }

    private void getFlightBatteryAndTemp() {
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_FLY_BATTERY);
        if (null == bean) return;
        String batteryStr = bean.getContent();
        CycleBatteryInfo battery1Info = DroneUtils.getBattery1InfoZ4C();
        if (battery1Info == null || !IGduDroneDevice.get().isConnected()) {
            bean.setContent("--");
            bean.setContentEnable(false);
            if (!TextUtils.equals(bean.getContent(), batteryStr)) {
                flyStatusData.setValue(bean);
            }
            return;
        }
        final int renameBattery = battery1Info.getPower();
        final int batteryTemp = battery1Info.getTemp();
        float realTemp = batteryTemp / 10f;
        PlanType planType = IGduDroneDevice.get().getPlanType().getValue();
        if (planType == PlanType.MGP12
                || planType == PlanType.S480
                || planType == PlanType.S450
                || planType == PlanType.S220
                || planType == PlanType.S280
                || planType == PlanType.S200
                || planType == PlanType.S220Pro
                || planType == PlanType.S220ProS
                || planType == PlanType.S220ProH
                || planType == PlanType.S220_SD
                || planType == PlanType.S200_SD
                || planType == PlanType.S220BDS
                || planType == PlanType.S280BDS
                || planType == PlanType.S200BDS
                || planType == PlanType.S220ProBDS
                || planType == PlanType.S220ProSBDS
                || planType == PlanType.S220ProHBDS
                || planType == PlanType.S220_SD_BDS
                || planType == PlanType.S200_SD_BDS) {
            realTemp = (batteryTemp - 2731) / 10f;
        }

        int powerRc = 0;
        try {
            powerRc = DroneUtils.getRcInfo().getRcPower();
        } catch (Exception ignore) {
        }
        bean.setContent(renameBattery + "% " + realTemp + "℃");
        if (renameBattery > DroneUtils.getTwoLevelLowBattery()) {
            bean.setContentSelect(true);
        } else if (renameBattery > DroneUtils.getOneLevelLowBattery() && powerRc <= DroneUtils.getTwoLevelLowBattery()) {
            bean.setContentTextColor(R.color.color_FFCC00);
        } else {
            bean.setContentEnable(false);
        }
        if (!TextUtils.equals(bean.getContent(), batteryStr)) {
            flyStatusData.setValue(bean);
        }
    }

    /**
     * 获取遥控器电池电量
     * */
    private void getRCBattery(){
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_RC_BATTERY);
        if (null == bean) return;
        String batteryStr = bean.getContent();
        int powerRc = 0;
        try {
            powerRc = DroneUtils.getRcInfo().getRcPower();
        } catch (Exception ignore) {
        }
        bean.setContent(powerRc >= 0 ? powerRc + "%" : "--");
        if (powerRc > 20) {
            bean.setContentSelect(true);
        } else if (powerRc > 0) {
            bean.setContentTextColor(R.color.color_FFCC00);
        } else {
            bean.setContentEnable(false);
        }
        if (!TextUtils.equals(bean.getContent(), batteryStr)) {
            flyStatusData.setValue(bean);
        }
    }

    /**
     * 获取RTK状态
     * */
    private void getRTKStatus(){
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_RTK);
        if (null == bean) return;
        int rtkStatus = bean.getContentStrId();
//        final boolean connectStatus = RtkManager.getInstance().getConnectStatus() == RTKNetConnectStatus.SERVER_COMMUNICATE;

        CycleOnboardRTKInfo onboardRTKInfo = IRTK.get().getOnboardRTKInfo().getValue();
        boolean bsRTKStatus = false;
        if (onboardRTKInfo != null) {
            bsRTKStatus = onboardRTKInfo.getBsRtkStatus();
        }
//        final boolean rtkConnected = (DroneUtils.getRtkType() == 1 && connectStatus)
//                || (DroneUtils.getRtkType() == 2 && bsRTKStatus)
//                || (DroneUtils.getRtkType() == 3 && DroneUtils.getOnboardRTKConnectState() == 2)
//                || (DroneUtils.getRtkType() == 5 && QXRTKManager.Companion.getInstance().isConnect());
//        if (IGduDroneDevice.get().getPlanType().getValue().isS200Type() && !DroneUtils.getRtkOnline()) {
//            bean.setContentStrId(R.string.string_not_insert);
//            bean.setContentTextColor(R.color.color_FF5800);
//            bean.setContentEnable(false);
//        } else {
//            if (SdkDemoApplication.getAircraftInstance().isConnected() && rtkConnected) {
//                bean.setContentStrId(R.string.flight_connect);
//                bean.setContentTextColor(R.color.color_5B5B5B);
//                bean.setContentEnable(true);
//            } else {
//                bean.setContentStrId(R.string.flight_loast_connect);
//                bean.setContentTextColor(R.color.color_FF5800);
//                bean.setContentEnable(false);
//            }
//        }
        if (bean.getContentStrId()!=rtkStatus) {
            flyStatusData.setValue(bean);
        }
    }

    private void getSDCardStatus() {
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_SDCARD);
        if (null == bean) return;
        String sdCardStatusStr = bean.getContent();
        String sdCardTip = "";
        SDCardStatus status = IHms.get().getSdCardStatus().getValue();
        if (status == SDCardStatus.NORMAL) {
            sdCardTip = ResourceUtils.getString(R.string.Label_CardInserted);
        } else if (status == SDCardStatus.CARD_ERROR) {
            sdCardTip = ResourceUtils.getString(R.string.Label_AbnormalSDCard);
        } else if (status == SDCardStatus.CARD_FULL) {
            sdCardTip = ResourceUtils.getString(R.string.Label_SdISFULL);
        } else if (status == SDCardStatus.NO_CARD) {
            sdCardTip = ResourceUtils.getString(R.string.Label_NoCardInserted);
        } else if (status == SDCardStatus.LOW_SPEED_CARD) {
            sdCardTip = ResourceUtils.getString(R.string.Label_LowSpeedSDCard);
        } else if (status == SDCardStatus.FORMAT_ERROR) {
            sdCardTip = ResourceUtils.getString(R.string.Label_SdFormatErr);
        } else if (status == SDCardStatus.EXTERNAL_CARD_FORMAT_ERROR) {
            sdCardTip = ResourceUtils.getString(R.string.Label_SdFormatErr);
        }

        bean.setContent(sdCardTip);
        bean.setContentEnable(ResourceUtils.getString(R.string.Label_CardInserted).equals(sdCardTip));
        XLogger.APP.i("SDCardStatus:"+status+",sdCardTip:"+sdCardTip);
        if (!TextUtils.equals(bean.getContent(), sdCardStatusStr)) {
            ThreadHelper.runOnUiThread(() -> {
                flyStatusData.setValue(bean);
            });
        }
    }

    /**
     * 获取当前遥控器
     * */
    private void getCurrRC(){
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_RC_CONTROL);
        if (null == bean) return;
        int preRCId = bean.getContentStrId();
        if (DroneUtils.isRCHasControlPower()) {
            bean.setContentStrId(R.string.Label_MasterRemoteControl);
        } else {
            bean.setContentStrId(R.string.Label_SubRemoteControl);
        }
        if (bean.getContentStrId()!=preRCId) {
            flyStatusData.setValue(bean);
        }
    }

    /**
     * 获取当前图传状态
     * */
    private void getSDRStatus(){
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_SDR);
        if (null == bean) return;
        int preSDRId = bean.getContentStrId();
        if (DroneUtils.isUseBackupsAirlink()) {
            bean.setContentStrId(R.string.string_link_type_let);
        } else {
            bean.setContentStrId(R.string.string_link_type_image_transmission);
        }
        if (bean.getContentStrId()!=preSDRId) {
            flyStatusData.setValue(bean);
        }
    }

    private void judgeIsGetVisionInfo(String preMode, String curMode) {
        if (!StringUtils.isEmptyString(preMode) && !preMode.equals(curMode)) {
            baseFlightAssistantViewModel.getVisionObstacleSwitch();
        }
    }



    private void judgeHaveAlarm() {
        //已连接的才需处理
        if(IGduDroneDevice.get().isConnected()) {
            sendCmdHandle();
        }
    }

    private void sendCmdHandle() {
        baseViewModel.getLowBatteryWarningThreshold();
        //获取限高
        baseViewModel.getLimitHeight();
        //获取失联行为
        baseViewModel.getOutOfControlAction();
        //获取摇杆模式
        baseRCViewModel.getAircraftMappingStyle();
        //获取视觉感知相关开关
        baseFlightAssistantViewModel.getVisionObstacleSwitch();
    }

    /**
     * 返航高度获取
     * */
    private void getHomeBackHeight(){
        if (IGduDroneDevice.get().isConnected()){
            if (DroneUtils.getBackHeight() > 0) {
                preGoHomeHeight = DroneUtils.getBackHeight() / 10;
                goHomeHeightLiveData.postValue(String.valueOf(UnitChnageUtils.getUnitValue(preGoHomeHeight)));
            } else {
                preGoHomeHeight = -1;
                goHomeHeightLiveData.postValue(INF);
            }
        }else {
            if (DroneUtils.isNewHeightLimitStrategy){
                baseViewModel.getLimitHeight();
            }
        }
    }

    public BaseFlightAssistantViewModel getBaseFlightAssistantViewModel(){
        return baseFlightAssistantViewModel;
    }

    public MutableLiveData<Integer> getToastLiveData() {
        return toastLiveData;
    }

    public MutableLiveData<List<Diagnostics>> getErrMsgLiveData() {
        return mErrMsgLiveData;
    }

    public MutableLiveData<BaseSysStatusBean> getSysStatusLiveData() {
        return sysStatusLiveData;
    }

    public MutableLiveData<List<BaseFlightStatusBean>> getFlyInitStatusData() {
        return flyInitStatusData;
    }

    public MutableLiveData<BaseFlightStatusBean> getFlyStatusData() {
        return flyStatusData;
    }


    public MutableLiveData<ObstacleStatusBean> getObstacleHorLiveData() {
        return obstacleHorLiveData;
    }

    public MutableLiveData<Boolean> getVisionSensingLiveData() {
        return visionSensingLiveData;
    }

    public MutableLiveData<String> getGoHomeHeightLiveData() {
        return goHomeHeightLiveData;
    }

    public MutableLiveData<LimitHeightInfo> getLimitHeightLiveData() {
        return limitHeightLiveData;
    }


    public MutableLiveData<LimitDistanceInfo> getLimitDistanceLiveData() {
        return limitDistanceLiveData;
    }


    public MutableLiveData<Integer> getAircraftMappingStyleLiveData() {
        return aircraftMappingStyleLiveData;
    }

    public MutableLiveData<LowBatteryWarnInfo> getLowBatteryWarningLiveData() {
        return lowBatteryWarningLiveData;
    }


    public int getPreGoHomeHeight(){
        return preGoHomeHeight;
    }

    private void initViewModelObserve(FragmentActivity activity){
        baseViewModel.getLimitHeightLiveData().observe(activity, data->{
            limitHeightLiveData.postValue(data);
            if (data.isSet()) {
                toastLiveData.setValue(R.string.string_set_success);
            }
        });
        baseViewModel.getGoHomeHeightBeanLiveData().observe(activity, data->{
            if (data.isSet()) {
                int height = preGoHomeHeight;
                if (data.isSetSuccess()) {
                    height = data.getGoHomeHeight();
                    preGoHomeHeight = height;
                    toastLiveData.setValue(R.string.string_set_success);
                } else {
                    toastLiveData.setValue(R.string.Label_SettingFail);
                }
                if (height != -1){
                    goHomeHeightLiveData.postValue(String.valueOf(UnitChnageUtils.getUnitValue(height)));
                }else {
                    goHomeHeightLiveData.postValue(INF);
                }
            }
        });
        baseViewModel.getLimitDistanceLiveData().observe(activity, data->{
            limitDistanceLiveData.postValue(data);
            if (data.isSet()) {
                if (data.isSuccess()) {
                    toastLiveData.setValue(R.string.string_set_success);
                } else {
                    toastLiveData.setValue(R.string.Label_SettingFail);
                }
            }
        });

        baseViewModel.getHomeLocationBeanLiveData().observe(activity, data->{
            if (data) {
                toastLiveData.setValue(R.string.home_point_set_successfully);
            } else {
                toastLiveData.setValue(R.string.home_point_set_failed);
            }
        });

        baseRCViewModel.getAircraftMappingStyleLiveData().observe(activity, data->{
            if (data.isSet()) {
                if (data.isSuccess()) {
                    AircraftMappingStyle style = data.getAircraftMappingStyle();
                    int index = getIndexFromAircraftMappingStyle(style);
                    aircraftMappingStyleLiveData.setValue(index);
                    toastLiveData.setValue(R.string.string_set_success);
                } else {
                    toastLiveData.setValue(R.string.Label_SettingFail);
                }
            } else {
                AircraftMappingStyle style = data.getAircraftMappingStyle();
                int index = getIndexFromAircraftMappingStyle(style);
                aircraftMappingStyleLiveData.setValue(index);
            }
        });

        baseViewModel.getErrTipBeanLiveData().observe(activity, data->{
            int setType = data.getSetType();
            int type = data.getType();
            if (setType == 1) {
//                setHeightFailHandle();
            } else if(setType == 2) {
//                setDistanceFailHandle();
            } else if(setType == 3){
//                setGoHomeHeightFailHandle();
            }
            switch (type) {
                case 1:
//                    mViewBinding.etHeightLimitSet.setEnabled(false);
//                    mViewBinding.etDistanceLimitSet.setEnabled(false);
//
//                    mViewBinding.ivHeightLimitSwitch.setSelected(false);
//                    mViewBinding.ivDistanceLimitSwitch.setSelected(false);
                    toastLiveData.setValue(R.string.DeviceNoConn);
                    break;

                case 2:
                    toastLiveData.setValue(R.string.string_tether_can_not_set);
                    break;

                case 3:
                    toastLiveData.setValue(R.string.Msg_GoHomingUnSet);
                    break;

                case 4:
                    toastLiveData.setValue(R.string.input_error);
                    break;

                default:
                    break;
            }
        });

        baseViewModel.getLowBatteryWarningLiveData().observe(activity, lowBatteryWarningLiveData::setValue);

//        baseFlightAssistantViewModel.getVisionSensingLiveData().observe(activity, data->{
//            if (data.isSuccess()) {
//                visionSensingLiveData.postValue(data.isVisionSensingEnable());
//                obstacleAvoidanceStrategyLiveData.postValue(data.isObstacleAvoidanceStrategyEnable());
//            }
//
//        });
    }

    public void setAircraftMappingStyle(int position){
        baseRCViewModel.setAircraftMappingStyle(getAircraftMappingStyleFromIndex(position));
    }

    private AircraftMappingStyle getAircraftMappingStyleFromIndex(int index){
        AircraftMappingStyle style = AircraftMappingStyle.STYLE_1;
        switch (index){
            case 0:
                style = AircraftMappingStyle.STYLE_2;
                break;
            case 1:
                style = AircraftMappingStyle.STYLE_3;
                break;
            case 2:
                style = AircraftMappingStyle.STYLE_1;
                break;
        }
        return style;
    }

    private int getIndexFromAircraftMappingStyle(AircraftMappingStyle style){
        int index = 0;
        if (style == AircraftMappingStyle.STYLE_1) {
            index = 2;
        } else if(style == AircraftMappingStyle.STYLE_2){
            index = 0;
        } else if(style == AircraftMappingStyle.STYLE_3){
            index = 1;
        }
       return index;
    }

    public void setEditLimitHeight(boolean editLimitHeight) {
        baseViewModel.setEditLimitHeight(editLimitHeight);
    }

    /**
     * 设置限高
     * @param isOpen 是否是开启限高
     * @param limitHeight 限高值(单位 m)
     */
    public void setLimitHeight(boolean isOpen, int limitHeight) {
        baseViewModel.setLimitHeight(isOpen, limitHeight);
    }

    public void setHeightLimitSwitch(boolean isSelect){

    }

    public void setEditLimitDistance(boolean editLimitDistance) {
        baseViewModel.setEditLimitDistance(editLimitDistance);
    }


    /**
     * 设置限高
     * @param isOpen 是否是开启限高
     * @param limitDistance 限距值(单位 m)
     */
    public void setLimitDistance(boolean isOpen, int limitDistance){
        baseViewModel.setLimitDistance(isOpen, limitDistance);
    }

    public void setGoHomeHeight(int value){
        baseViewModel.setGoHomeHeight(value);
    }

    public void setHomePoint(double lat, double lng, byte type) {
        if (lat == 0 || lng == 0) {
            if (type == 1) {
                toastLiveData.setValue(R.string.Msg_UnCoordinatesTip);
            } else if (type == 2) {
                toastLiveData.setValue(R.string.string_no_rc_location);
            }
            return;
        }
//        boolean isVerify = lat < 90 && lat > -90 && lng < 180 && lng > -180;
        com.amap.api.maps.model.LatLng homeLatLng = new com.amap.api.maps.model.LatLng(lat, lng);
        com.amap.api.maps.model.LatLng currentLatLng = new com.amap.api.maps.model.LatLng(DroneUtils.getDroneGpsLat(), DroneUtils.getDroneGpsLon());
        float distance = AMapUtils.calculateLineDistance(homeLatLng, currentLatLng);
        XLogger.INSTANCE.getAPP().i("setHomePoint() distance = " + BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
        if (distance > 2500) {
            // 返航点设置不能大于2500米
            toastLiveData.setValue(R.string.Msg_GoHomePointDistanceOut);
            return;
        }
        baseViewModel.setHomePoint(lat, lng, (byte) 0);
    }

    @Override
    public void onUpdate(List<Diagnostics> list) {
        ThreadHelper.runOnUiThread(() -> {
            getFlightStatus(list); //飞机状态信息
            judgeHaveAlarm();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        IHms.get().getSdCardStatus().unregister(sdCardObserver);
    }
}
