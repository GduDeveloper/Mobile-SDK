package com.gdu.demo.flight.base;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.gdu.common.error.Error;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.map.SpatialReference;
import com.gdu.demo.map.geometry.Point;
import com.gdu.demo.map.utils.JTSUtils;
import com.gdu.demo.utils.DroneUtils;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.flightcontroller.ConnectionFailSafeBehavior;
import com.gdu.msdk.config.DroneValueConstants;
import com.gdu.msdk.device.component.interfaces.IFlightController;
import com.gdu.msdk.device.interfaces.IGduDroneDevice;
import com.gdu.msdk.key.value.CycleFCInfo1;
import com.gdu.sdk.flightcontroller.FlightController;
import com.gdu.sdk.flightcontroller.bean.LimitDistanceInfo;
import com.gdu.sdk.flightcontroller.bean.LimitHeightInfo;
import com.gdu.sdk.flightcontroller.bean.LowBatteryWarnInfo;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description TODO
 */
public class BaseFlightViewModel extends BaseViewModel {

    private FlightController mGDUFlightController;
    private final MutableLiveData<LimitHeightInfo> limitHeightLiveData;

    private final MutableLiveData<LimitDistanceInfo> limitDistanceLiveData;
    private final MutableLiveData<GoHomeHeightBean> goHomeHeightBeanLiveData;
    private final MutableLiveData<ConnectionFailSafeBehaviorBean> connectionFailSafeBehaviorLiveData;

    private final MutableLiveData<LowBatteryWarnInfo> lowBatteryWarningLiveData;

    private final MutableLiveData<WarnTipBean> warnTipBeanLiveData;


    private final MutableLiveData<Boolean> homeLocationBeanLiveData;
    /**
     * 是否在编辑限高
     */
    private boolean isEditLimitHeight = false;


    /**
     * 是否在编辑限距
     */
    private boolean isEditLimitDistance = false;

    /**
     * 初始限制高度
     * */
    private int preLimitHeight;
    /**
     * 初始限制距离
     * */
    private int preDistanceLimit = -1;

    /** 记录最后一次获取到的失联行为控制 */
    private int preOutOfControlAction = 0;

    public BaseFlightViewModel() {
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        limitHeightLiveData = new MutableLiveData<>();
        warnTipBeanLiveData = new MutableLiveData<>();
        goHomeHeightBeanLiveData = new MutableLiveData<>();
        limitDistanceLiveData = new MutableLiveData<>();
        connectionFailSafeBehaviorLiveData = new MutableLiveData<>();
        homeLocationBeanLiveData = new MutableLiveData<>();
        lowBatteryWarningLiveData = new MutableLiveData<>();
    }


    /**
     * 获取默认高度
     * */
    public int getDefaultLimitHeight(Context context){
        return IFlightController.get().getFlightLimitHeight();
    }

    /**
     * 获取限制高度
     * */
    public void getLimitHeight() {
        if (isEditLimitHeight) {
            return;
        }
        if (mGDUFlightController != null) {
            mGDUFlightController.getDroneLimitHeight(new CommonCallbacks.CompletionCallbackWith<LimitHeightInfo>() {
                @Override
                public void onSuccess(LimitHeightInfo result) {
                    int heightLimit = checkAndSaveHeightData(result.isOpen(), result.getHeight());
                    result.setHeight(heightLimit);
                    result.setSet(false);
                    limitHeightLiveData.setValue(result);
                }

                @Override
                public void onFailure(Error var1) {
                    LimitHeightInfo bean = new LimitHeightInfo();
                    bean.setOpen(false);
                    bean.setHeight(0);
                    bean.setSet(false);
                    limitHeightLiveData.setValue(bean);
                }
            });
        }
    }

    public void setConnectionFailSafeBehavior(int position){
        ConnectionFailSafeBehavior behavior;
        if (position == 1) {
            behavior = ConnectionFailSafeBehavior.HOVER;
        } else {
            behavior = ConnectionFailSafeBehavior.GO_HOME;
        }
        mGDUFlightController.setConnectionFailSafeBehavior(behavior, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(Error error) {
                if (error != null) {
                    ConnectionFailSafeBehaviorBean behaviorBean = new ConnectionFailSafeBehaviorBean();
                    behaviorBean.setSet(true);
                    behaviorBean.setSuccess(false);
                    behaviorBean.setPosition(preOutOfControlAction);
                   connectionFailSafeBehaviorLiveData.postValue(behaviorBean);
                }
                getOutOfControlAction();
            }
        });
    }

    /**
     * 获取失控行为
     */
    public void getOutOfControlAction() {
        mGDUFlightController.getConnectionFailSafeBehavior(new CommonCallbacks.CompletionCallbackWith<ConnectionFailSafeBehavior>() {
            @Override
            public void onSuccess(ConnectionFailSafeBehavior behavior) {
                if (behavior == ConnectionFailSafeBehavior.GO_HOME) {
                    preOutOfControlAction = 0;
                } else {
                    preOutOfControlAction = 1;
                }
                ConnectionFailSafeBehaviorBean behaviorBean = new ConnectionFailSafeBehaviorBean();
                behaviorBean.setPosition(preOutOfControlAction);
                behaviorBean.setSuccess(true);
                connectionFailSafeBehaviorLiveData.postValue(behaviorBean);
            }

            @Override
            public void onFailure(Error var1) {

            }
        });
    }

    public void setGoHomeHeight(int height) {
        if (!connStateToast()) {
            WarnTipBean warnTipBean = new WarnTipBean();
            warnTipBean.setType(3);
            warnTipBean.setWarnType(1);
            warnTipBean.setIntValue(height);
            warnTipBeanLiveData.postValue(warnTipBean);
            return;
        }
        if (GlobalVariable.droneFlyState == 3 || GlobalVariable.backState == 2) {
            WarnTipBean warnTipBean = new WarnTipBean();
            warnTipBean.setType(3);
            warnTipBean.setWarnType(3);
            warnTipBean.setIntValue(height);
            warnTipBeanLiveData.postValue(warnTipBean);
            return;
        }

        if (height < MyConstants.GO_HOME_HEIGHT_MIN || height > MyConstants.GO_HOME_HEIGHT_MAX) {
            WarnTipBean warnTipBean = new WarnTipBean();
            warnTipBean.setType(3);
            warnTipBean.setWarnType(4);
            warnTipBean.setIntValue(height);
            warnTipBeanLiveData.postValue(warnTipBean);
            return;
        }
        mGDUFlightController.setGoHomeHeightInMeters((short) height, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(Error error) {
                GoHomeHeightBean homeHeightBean = new GoHomeHeightBean();
                if (error == null) {
                    int value = checkAndSaveGoHomeHeightData(height);
                    homeHeightBean.setGoHomeHeight(value);
                    homeHeightBean.setSet(true);
                    homeHeightBean.setSetSuccess(true);
                } else {
                    homeHeightBean.setGoHomeHeight(-1);
                    homeHeightBean.setSet(true);
                    homeHeightBean.setSetSuccess(false);
                }
                goHomeHeightBeanLiveData.postValue(homeHeightBean);
            }
        });
    }

    public void setEditLimitHeight(boolean editLimitHeight) {
        isEditLimitHeight = editLimitHeight;
    }

    public void setLimitHeight(boolean isOpen, int limitHeight) {
        if (limitHeight > DroneValueConstants.LIMIT_HEIGHT_DEFAULT && preLimitHeight <= DroneValueConstants.LIMIT_HEIGHT_DEFAULT && isEditLimitHeight) {
            WarnTipBean warnTipBean = new WarnTipBean();
            warnTipBean.setType(1);
            warnTipBean.setWarnType(1);
            warnTipBean.setIntValue(limitHeight);
            warnTipBeanLiveData.postValue(warnTipBean);
        } else {
            if (DroneUtils.isNewHeightLimitStrategy) {

                if (isOpen) {
                    if (limitHeight < DroneValueConstants.LIMIT_HEIGHT_MIN || limitHeight > DroneValueConstants.LIMIT_HEIGHT_MAX) {
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(4);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                    // 转化飞机高度单位(cm -> m)
                    CycleFCInfo1 fcInfo1 = IFlightController.get().getFcInfo1().getValue();
                    int droneHeight = 0;
                    if (fcInfo1 != null) {
                        droneHeight = fcInfo1.getDroneHeight() / 100;
                    }
                    if (limitHeight <= droneHeight + 10) {
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(5);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                }
                int value = checkAndSaveHeightData(isOpen, limitHeight);
                LimitHeightInfo bean = new LimitHeightInfo();
                bean.setOpen(isOpen);
                bean.setHeight(value);
                bean.setSet(true);
                limitHeightLiveData.setValue(bean);
                mGDUFlightController.setFlightLimitHeight(isOpen, limitHeight, var1 -> {

                });
            } else {
                if (!isConnect()) {
                    ErrTipBean tipBean = new ErrTipBean();
                    tipBean.setSetType(1);
                    tipBean.setType(1);
                    errTipBeanLiveData.postValue(tipBean);
                    return;
                }

                if (isOpen) {
                    if (limitHeight < DroneValueConstants.LIMIT_HEIGHT_MIN || limitHeight > DroneValueConstants.LIMIT_HEIGHT_MAX) {
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(4);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                    // 转化飞机高度单位(cm -> m)
                    CycleFCInfo1 fcInfo1 = IFlightController.get().getFcInfo1().getValue();
                    int droneHeight = 0;
                    if (fcInfo1 != null) {
                        droneHeight = fcInfo1.getDroneHeight() / 100;
                    }
                    if (limitHeight <= droneHeight + 10) {
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(5);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                }

                mGDUFlightController.setFlightLimitHeight(isOpen,limitHeight, error -> {
                    if (error == null) {
                        if (isOpen){
                            toastLiveData.postValue(R.string.string_set_success);
                        }
                        int value = checkAndSaveHeightData(isOpen, limitHeight);
                        LimitHeightInfo bean = new LimitHeightInfo();
                        bean.setOpen(isOpen);
                        bean.setHeight(value);
                        bean.setSet(true);
                        limitHeightLiveData.setValue(bean);
                    } else {
                        CycleFCInfo1 fcInfo1 = IFlightController.get().getFcInfo1().getValue();
                        boolean isActive = true;
                        if (fcInfo1 != null) {
                            isActive = fcInfo1.isDroneActive();
                        }
                        if (!isActive) {
                            toastLiveData.postValue(R.string.Err_DevUnActiveRetryTip);
                        } else {
                            toastLiveData.postValue(R.string.Label_SettingFail);
                        }
                        LimitHeightInfo bean = new LimitHeightInfo();
                        bean.setOpen(isOpen);
                        bean.setHeight(preLimitHeight);
                        bean.setSet(false);
                        limitHeightLiveData.postValue(bean);
                    }
                });
            }
        }
    }

    /**
     * 获取默认限制距离
     * */
    public int getDefaultLimitDistance(Context context) {
        return IFlightController.get().getDefaultLimitDistance();
    }

    /**
     * 获取限距
     * */
    public void getLimitDistance(){
        mGDUFlightController.getLimitDistance(new CommonCallbacks.CompletionCallbackWith<LimitDistanceInfo>() {
            @Override
            public void onSuccess(LimitDistanceInfo result) {
                int distance = IFlightController.get().checkAndSaveDistanceData(result.isOpen(), result.getDistance());
                result.setDistance(distance);
                result.setSet(false);
                result.setSuccess(true);
                limitDistanceLiveData.postValue(result);
            }

            @Override
            public void onFailure(Error var1) {

            }
        });
    }

    public void setEditLimitDistance(boolean editLimitDistance) {
        isEditLimitDistance = editLimitDistance;
    }

    public void setLimitDistance(boolean isOpen, int distance) {
        if (!IGduDroneDevice.get().isConnected()) {
            ErrTipBean tipBean = new ErrTipBean();
            tipBean.setSetType(2);
            tipBean.setType(1);
            errTipBeanLiveData.postValue(tipBean);
            return;
        }

        if (isOpen) {
            if (distance < DroneValueConstants.LIMIT_DISTANCE_MIN || distance > DroneValueConstants.LIMIT_DISTANCE_MAX) {
                ErrTipBean tipBean = new ErrTipBean();
                tipBean.setSetType(2);
                tipBean.setType(4);
                errTipBeanLiveData.postValue(tipBean);
                return;
            }

            CycleFCInfo1 fcInfo1 = IFlightController.get().getFcInfo1().getValue();
            int flyDistance = 0;
            if (fcInfo1 != null) {
                flyDistance = fcInfo1.getFlyDistance();
            }
            if (flyDistance > DroneValueConstants.LIMIT_DISTANCE_MIN && distance < flyDistance) {
                ErrTipBean tipBean = new ErrTipBean();
                tipBean.setSetType(2);
                tipBean.setType(6);
                errTipBeanLiveData.postValue(tipBean);
                return;
            }
            if (GlobalVariable.droneFlyState != 1) {
                Point returnPoint = new Point(GlobalVariable.backHomeLan, GlobalVariable.backHomeLon, SpatialReference.WGS84);
                Point drone = new Point(GlobalVariable.GPS_Lat, GlobalVariable.GPS_Lon, SpatialReference.WGS84);
                final double distanceValue = JTSUtils.INSTANCE.calPointsDistance(returnPoint, drone);
                if (distanceValue > distance) {
                    ErrTipBean tipBean = new ErrTipBean();
                    tipBean.setSetType(2);
                    tipBean.setType(7);
                    errTipBeanLiveData.postValue(tipBean);
                    return;
                }
            }

        }
        mGDUFlightController.setLimitDistance(isOpen, (short) distance, error -> {
            LimitDistanceInfo limitDistanceBean = new LimitDistanceInfo();
            if (error == null) {
                if (isOpen) {
                    toastLiveData.postValue(R.string.string_set_success);
                }
                int value = distance;
                value = IFlightController.get().checkAndSaveDistanceData(isOpen, value);
                limitDistanceBean.setSet(true);
                limitDistanceBean.setDistance(value);
                limitDistanceBean.setOpen(isOpen);
                limitDistanceBean.setSuccess(true);
                preDistanceLimit = value;
            } else {
                CycleFCInfo1 fcInfo1 = IFlightController.get().getFcInfo1().getValue();
                boolean isActive = true;
                if (fcInfo1 != null) {
                    isActive = fcInfo1.isDroneActive();
                }
                if (!isActive) {
                    toastLiveData.postValue(R.string.Err_DevUnActiveRetryTip);
                } else {
                    toastLiveData.postValue(R.string.Label_SettingFail);
                }
                limitDistanceBean.setSet(true);
                limitDistanceBean.setDistance(preDistanceLimit);
                limitDistanceBean.setOpen(isOpen);
                limitDistanceBean.setSuccess(false);
            }
            limitDistanceLiveData.postValue(limitDistanceBean);
        });
    }

    private int checkAndSaveGoHomeHeightData(int value) {
        if (value < MyConstants.GO_HOME_HEIGHT_MIN) {
            value = MyConstants.GO_HOME_HEIGHT_MIN;
        } else if (value > MyConstants.GO_HOME_HEIGHT_MAX) {
            value = MyConstants.GO_HOME_HEIGHT_MAX;
        }
        return value;
    }

    private int checkAndSaveHeightData(boolean isOpen, int value) {
        if (!isOpen) {
            return value;
        }
        if (value < DroneValueConstants.LIMIT_HEIGHT_MIN) {
            value = DroneValueConstants.LIMIT_HEIGHT_MIN;
        } else if (value > DroneValueConstants.LIMIT_HEIGHT_MAX) {
            value = DroneValueConstants.LIMIT_HEIGHT_MAX;
        }
        return value;
    }

    public MutableLiveData<LimitHeightInfo> getLimitHeightLiveData() {
        return limitHeightLiveData;
    }

    public MutableLiveData<GoHomeHeightBean> getGoHomeHeightBeanLiveData() {
        return goHomeHeightBeanLiveData;
    }

    public MutableLiveData<WarnTipBean> getWarnTipBeanLiveData() {
        return warnTipBeanLiveData;
    }

    public MutableLiveData<Boolean> getHomeLocationBeanLiveData() {
        return homeLocationBeanLiveData;
    }

    public MutableLiveData<LimitDistanceInfo> getLimitDistanceLiveData() {
        return limitDistanceLiveData;
    }

    public MutableLiveData<ConnectionFailSafeBehaviorBean> getConnectionFailSafeBehaviorLiveData() {
        return connectionFailSafeBehaviorLiveData;
    }

    public MutableLiveData<LowBatteryWarnInfo> getLowBatteryWarningLiveData() {
        return lowBatteryWarningLiveData;
    }

    public boolean isConnect() {
        return SdkDemoApplication.getAircraftInstance().isConnected();
    }

    public void setHomePoint(double lat, double lng, byte type) {
        LocationCoordinate2D homeLocation = new LocationCoordinate2D(lat, lng);
        mGDUFlightController.setHomeLocation(homeLocation, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(Error error) {
                if (error == null) {
//                    CommonUtils.stopAllHomePointMusic();
                    GlobalVariable.returnHomeSettingType = type;
//                    CommonUtils.playHomePointByReturnType();
                    homeLocationBeanLiveData.postValue(true);
//                    showToast(getString(R.string.home_point_set_successfully));
                } else {
//                    showToast(getString(R.string.home_point_set_failed));
                    homeLocationBeanLiveData.postValue(false);
                }
            }
        });
    }

    public void setLowBatteryWarningThreshold(int lowBatteryWarning, int seriousLowBatteryWarning){
        mGDUFlightController.setLowBatteryWarningThreshold((byte) seriousLowBatteryWarning, (byte) lowBatteryWarning, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(Error error) {
                getLowBatteryWarningThreshold();
            }
        });
    }

    public void getLowBatteryWarningThreshold(){
        mGDUFlightController.getLowBatteryWarningThreshold(new CommonCallbacks.CompletionCallbackWith<LowBatteryWarnInfo>() {
            @Override
            public void onSuccess(LowBatteryWarnInfo info) {
                int twolevel = info.getTwoLevelWarn();
                int onelevel = info.getOneLevelWarn();
                if (onelevel < MyConstants.DRONE_LOW_BATTERY_ONE_LEVEL_MIN) {
                    onelevel = MyConstants.DRONE_LOW_BATTERY_ONE_LEVEL_MIN;
                }
                info.setOneLevelWarn(onelevel);
                if (twolevel < MyConstants.DRONE_LOW_BATTERY_TWO_LEVEL_MIN) {
                    twolevel = MyConstants.DRONE_LOW_BATTERY_TWO_LEVEL_MIN;
                }
                info.setTwoLevelWarn(twolevel);
                info.setSuccess(true);
                GlobalVariable.twoLevelLowBattery = twolevel;
                GlobalVariable.oneLevelLowBattery = onelevel;
                lowBatteryWarningLiveData.postValue(info);
            }

            @Override
            public void onFailure(Error var1) {
                LowBatteryWarnInfo warningBean = new LowBatteryWarnInfo();
                warningBean.setSuccess(false);
                warningBean.setOneLevelWarn(0);
                warningBean.setTwoLevelWarn(0);
                lowBatteryWarningLiveData.postValue(warningBean);
            }
        });
    }
}
