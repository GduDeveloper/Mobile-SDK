package com.gdu.demo.flight.pre.viewmodel;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.amap.api.maps.AMapUtils;
import com.gdu.api.GduRtkManager;
import com.gdu.api.Util.ConnectUtil;
import com.gdu.api.rtk.QxSdkManager;
import com.gdu.beans.WarnBean;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.flight.base.BaseFlightAssistantViewModel;
import com.gdu.demo.flight.base.BaseFlightViewModel;
import com.gdu.demo.flight.base.BaseRCViewModel;
import com.gdu.demo.flight.pre.bean.BaseFlightStatusBean;
import com.gdu.demo.flight.pre.bean.BaseSysStatusBean;
import com.gdu.demo.flight.pre.bean.ObstacleStatusBean;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.drone.ControlHand;
import com.gdu.drone.PlanType;
import com.gdu.drone.RTKNetConnectStatus;
import com.gdu.errreport.ErrCodeGrade;
import com.gdu.healthmanager.MessageBean;
import com.gdu.remotecontroller.AircraftMappingStyle;
import com.gdu.sdk.flightcontroller.bean.LimitDistanceInfo;
import com.gdu.sdk.flightcontroller.bean.LimitHeightInfo;
import com.gdu.sdk.flightcontroller.bean.LowBatteryWarnInfo;
import com.gdu.sdk.flightcontroller.flightassistant.FlightAssistant;
import com.gdu.sdk.remotecontroller.NetworkingHelper;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.DroneUtil;
import com.gdu.util.GimbalUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.StringUtils;
import com.gdu.util.logger.MyLogUtils;
import com.rxjava.rxlife.RxLife;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Observable;

/**
 * @author wuqb
 * @date 2025/1/10
 * @description TODO
 */
public class PreFlightInspectionViewModel extends ViewModel {

    private BaseFlightViewModel baseViewModel;

    private BaseRCViewModel baseRCViewModel;

    private BaseFlightAssistantViewModel baseFlightAssistantViewModel;

    private final MutableLiveData<Integer> toastLiveData;
    private final MutableLiveData<ArrayList<MessageBean>> mErrMsgLiveData;
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

    private HashMap<Long, WarnBean> warnTable;
    private boolean hadErr;
    private byte isHorSwitchSelected;
    private byte isTopSwitchSelected;
    private byte isBottomSwitchSelected;

    private int horBrakeDistance;
    private int horWarnDistance;
    private int topBrakeDistance;
    private int topWarnDistance;
    private int bottomBrakeDistance;
    private int bottomWarnDistance;

    
    /**
     * 返航高度
     */
    private int preGoHomeHeight = -1;

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
     * 健康检测
     * */
    public void checkAlarmData(FragmentActivity activity){
        //设置初始状态
        ArrayList<MessageBean> messageBeans = new ArrayList<>();
        final MessageBean mBean = new MessageBean();
        mBean.setMsg(activity.getString(R.string.Msg_PreCheckDefaultAlarm));
        mBean.setAlarmLevel(2);
        messageBeans.add(mBean);
        mErrMsgLiveData.setValue(messageBeans);
        warnTable = CommonUtils.initWarnTable(activity);
        //定时器检测错误码
        Observable.interval(0, 3, TimeUnit.SECONDS)
                .to(RxLife.toMain(activity))
                .subscribe(l -> judgeHaveAlarm(activity), throwable -> throwable.printStackTrace());
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
                            getFlightStatus(); //飞机状态信息
                            getFlyMode(activity);
                            getFlightBatteryAndTemp(); //获取飞行器电量
                            getRCBattery(); //获取遥控器电量
                            getRTKStatus(); //获取RTK状态
                            getSDCardStatus(activity); //获取SD卡状态
                            getCurrRC(); //获取当前遥控器控制
                            getSDRStatus(); //获取当前图传状态
                        }, throwable -> Log.e("更新界面状态出错", throwable.getMessage()));
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
    private void getFlightStatus(){
        BaseSysStatusBean bean = new BaseSysStatusBean();
        if(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            ArrayList<MessageBean> warnErrorList = getWarnErrorList();
            boolean isHaveAbnormal = !warnErrorList.isEmpty();

            bean.setStatusTitleColor(R.color.white);
            bean.setFlightStatusColor(R.color.white);
            if (isHaveAbnormal) {
                bean.setFlightStatusStr(R.string.Label_AircraftStatusAbnormal);
                bean.setStatusBg(R.drawable.shape_gradient_ff6c00_ffa96b);
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
        if(!ConnectUtil.isConnect()){
            modeStr = "---";
        }else {
            if (GlobalVariable.flyMode == 0) {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_AGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            } else if (GlobalVariable.flyMode == 4) {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_VGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            } else if (GlobalVariable.flyMode == 5) {
                modeStr = context.getResources().getString(R.string.Label_FlyMode_TGear);
                judgeIsGetVisionInfo(bean.getContent(), modeStr);
            } else if (GlobalVariable.DroneFlyMode == 0) {
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
        if (GlobalVariable.sBattery1InfoZ4C == null || !ConnectUtil.isConnect()) {
            bean.setContent("--");
            bean.setContentEnable(false);
            if (!TextUtils.equals(bean.getContent(), batteryStr)) {
                flyStatusData.setValue(bean);
            }
            return;
        }
        final int renameBattery = GlobalVariable.sBattery1InfoZ4C.getPower();
        final int batteryTemp = GlobalVariable.sBattery1InfoZ4C.getTemp();
        float realTemp = batteryTemp / 10f;
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
            realTemp = (batteryTemp - 2731) / 10f;
        }

        bean.setContent(renameBattery + "% " + realTemp + "℃");
        if (renameBattery > GlobalVariable.twoLevelLowBattery) {
            bean.setContentSelect(true);
        } else if (renameBattery > GlobalVariable.oneLevelLowBattery && GlobalVariable.power_rc <= GlobalVariable.twoLevelLowBattery) {
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
        bean.setContent(GlobalVariable.power_rc >= 0 ? GlobalVariable.power_rc + "%" : "--");
        if (GlobalVariable.power_rc > 20) {
            bean.setContentSelect(true);
        } else if (GlobalVariable.power_rc > 0) {
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
        final boolean connectStatus = GduRtkManager.getInstance().getConnectStatus() == RTKNetConnectStatus.SERVER_COMMUNICATE;

        final boolean rtkConnected = (GlobalVariable.sRTKType == 1 && connectStatus)
                || (GlobalVariable.sRTKType == 2 && GlobalVariable.sBSRTKStatus == 1)
                || (GlobalVariable.sRTKType == 3 && GlobalVariable.onDroneRtkState == 2)
                || (GlobalVariable.sRTKType == 5 && QxSdkManager.getInstance().isConnect());
        if (DroneUtil.isSmallFlight() && GlobalVariable.RTKOnline == 1) {
            bean.setContentStrId(R.string.string_not_insert);
            bean.setContentTextColor(R.color.color_FF5800);
            bean.setContentEnable(false);
        } else {
            if (GlobalVariable.connStateEnum != ConnStateEnum.Conn_None && rtkConnected) {
                bean.setContentStrId(R.string.flight_connect);
                bean.setContentTextColor(R.color.color_5B5B5B);
                bean.setContentEnable(true);
            } else {
                bean.setContentStrId(com.gdu.api.R.string.flight_loast_connect);
                bean.setContentTextColor(R.color.color_FF5800);
                bean.setContentEnable(false);
            }
        }
        if (bean.getContentStrId()!=rtkStatus) {
            flyStatusData.setValue(bean);
        }
    }

    private void getSDCardStatus(Context context) {
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_SDCARD);
        if (null == bean) return;
        String sdCardStatusStr = bean.getContent();
        final String lightSdCardName = context.getResources().getString(com.gdu.api.R.string.Label_VisibleLightSDCard);
        final String irSdCardName = context.getResources().getString(com.gdu.api.R.string.Label_IRSDCard);
        String sdCardTip = "";
        if (GimbalUtil.isMultiCardStatusGimbal()) {
            if (GlobalVariable.lightSDCardStatus == 3 && GlobalVariable.IRSDCardStatus == 3) {
                sdCardTip = context.getResources().getString(R.string.Label_NoCardInserted);
            } else {
                String lightErrStr = CommonUtils.getSDCardDetailErrTip(context, 2);
                if (!CommonUtils.isEmptyString(lightErrStr)) {
                    sdCardTip = lightErrStr;
                }
                String irErrStr = CommonUtils.getSDCardDetailErrTip(context, 1);
                if (CommonUtils.isEmptyString(sdCardTip) && !CommonUtils.isEmptyString(irErrStr)) {
                    sdCardTip = irErrStr;
                }

                if (CommonUtils.isEmptyString(sdCardTip)) {
                    sdCardTip = context.getString(com.gdu.api.R.string.Label_CardInserted);
                }
            }
        } else if (GlobalVariable.getMainGimbalSupportFun().enableMultiSDCard) {
            String lightStorageFull = String.format(context.getResources().getString(com.gdu.api.R.string.Label_SdISFULL_Compatible), lightSdCardName);
            String irStorageFull = String.format(context.getResources().getString(com.gdu.api.R.string.Label_SdISFULL_Compatible), irSdCardName);
            if (GimbalUtil.isLightMemoryFull()) {
                sdCardTip = lightStorageFull;
            }

            if (CommonUtils.isEmptyString(sdCardTip) && GimbalUtil.isIRMemoryIsFull()) {
                sdCardTip = irStorageFull;
            }

            if (CommonUtils.isEmptyString(sdCardTip)) {
                int sdInsertStatus = GimbalUtil.checkTMSSDCard();
                switch (sdInsertStatus) {
                    // 2张卡都已插入
                    case 0:
                        sdCardTip = context.getResources().getString(com.gdu.api.R.string.Label_CardInserted);
                        break;
                    // sd1(红外未插卡)
                    case 1:
                        sdCardTip = context.getResources().getString(com.gdu.api.R.string.Label_IRNoCardInserted);
                        break;
                    // sd2(可见光未插卡)
                    case 2:
                        sdCardTip = context.getResources().getString(com.gdu.api.R.string.Label_VisibleLightNoCardInserted);
                        break;
                    // 3 两张卡均未插
                    case 3:
                        sdCardTip = context.getResources().getString(R.string.Label_NoCardInserted);
                        break;

                    default:
                        break;
                }
            }
        } else if (GimbalUtil.isSingleSDReportGimbal()) {
            sdCardTip = CommonUtils.getSDCardDetailErrTip(context, 3);
            if (CommonUtils.isEmptyString(sdCardTip)) {
                sdCardTip = context.getResources().getString(com.gdu.api.R.string.Label_CardInserted);
            }
        } else {
            if (GimbalUtil.isMemoryFull()) {
                sdCardTip = String.format(context.getResources().getString(com.gdu.api.R.string.Label_SdISFULL_Compatible), "");
            }

            if (CommonUtils.isEmptyString(sdCardTip)) {
                if (GimbalUtil.isInsertSDCard()) {
                    sdCardTip = context.getResources().getString(com.gdu.api.R.string.Label_CardInserted);
                } else {
                    sdCardTip = context.getResources().getString(R.string.Label_NoCardInserted);
                }
            }
        }
        bean.setContent(sdCardTip);
        bean.setContentEnable(context.getResources().getString(com.gdu.api.R.string.Label_CardInserted).equals(sdCardTip));
        if (!TextUtils.equals(bean.getContent(), sdCardStatusStr)) {
            flyStatusData.setValue(bean);
        }
    }

    /**
     * 获取当前遥控器
     * */
    private void getCurrRC(){
        BaseFlightStatusBean bean =  getFlightStatusBean(BaseFlightStatusBean.STATUS_TYPE_RC_CONTROL);
        if (null == bean) return;
        int preRCId = bean.getContentStrId();
        if (NetworkingHelper.isRCHasControlPower()) {
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
        if (GlobalVariable.isUseBackupsAirlink) {
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



    private void judgeHaveAlarm(Context context) {
        //已连接的才需处理
        if(GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess) {
            getAlarmData(context);
            sendCmdHandle();
        }
    }

    private void getAlarmData(Context context) {
        CommonUtils.updateWarnList(context, warnTable);
        ArrayList<MessageBean> warnErrorList = getWarnErrorList();
        hadErr = !warnErrorList.isEmpty();
        if (hadErr) {
            mErrMsgLiveData.setValue(null);
            mErrMsgLiveData.setValue(warnErrorList);
        }
    }

    private ArrayList<MessageBean> getWarnErrorList() {
        ArrayList<MessageBean> msgBeanList = new ArrayList<>();
        for (Map.Entry<Long, WarnBean> mEntry : warnTable.entrySet()) {
            if (mEntry.getValue().isErr) {
                MessageBean msgBean = new MessageBean();
                msgBean.setMsg(mEntry.getValue().warnStr);
                if (ErrCodeGrade.ErrCodeGrade_1 == mEntry.getValue().getWarnLevel()) {
                    msgBean.setAlarmLevel(2);
                } else if (ErrCodeGrade.ErrCodeGrade_2 == mEntry.getValue().getWarnLevel()) {
                    msgBean.setAlarmLevel(1);
                } else {
                    msgBean.setAlarmLevel(3);
                }

                msgBeanList.add(msgBean);
            }
        }
        return msgBeanList;
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
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_Sucess){
            if (GlobalVariable.backHeight > 0) {
                preGoHomeHeight = GlobalVariable.backHeight / 10;
                goHomeHeightLiveData.postValue(String.valueOf(UnitChnageUtils.getUnitValue(preGoHomeHeight)));
            } else {
                preGoHomeHeight = -1;
                goHomeHeightLiveData.postValue(INF);
            }
        }else {
            if (GlobalVariable.isNewHeightLimitStrategy){
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

    public MutableLiveData<ArrayList<MessageBean>> getErrMsgLiveData() {
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


        baseViewModel.getWarnTipBeanLiveData().observe(activity, data->{
            if(data.getWarnType() == 1){

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
            GlobalVariable.controlHand = ControlHand.HAND_JAPAN;
        } else if(style == AircraftMappingStyle.STYLE_2){
            index = 0;
            GlobalVariable.controlHand = ControlHand.HAND_AMERICA;
        } else if(style == AircraftMappingStyle.STYLE_3){
            index = 1;
            GlobalVariable.controlHand = ControlHand.HAND_CHINA;
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
//        if (preGoHomeHeight <= value) {
//            return;
//        }
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
        com.amap.api.maps.model.LatLng currentLatLng = new com.amap.api.maps.model.LatLng(GlobalVariable.GPS_Lat, GlobalVariable.GPS_Lon);
        float distance = AMapUtils.calculateLineDistance(homeLatLng, currentLatLng);
        MyLogUtils.i("setHomePoint() distance = " + BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP));
        if (distance > 2500) {
            // 返航点设置不能大于2500米
            toastLiveData.setValue(R.string.Msg_GoHomePointDistanceOut);
            return;
        }
        baseViewModel.setHomePoint(lat, lng, (byte) 0);
    }
}
