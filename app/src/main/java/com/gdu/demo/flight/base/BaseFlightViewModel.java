package com.gdu.demo.flight.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.common.error.GDUError;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.map.SpatialReference;
import com.gdu.demo.map.geometry.Point;
import com.gdu.demo.map.utils.JTSUtils;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.flightcontroller.ConnectionFailSafeBehavior;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.logger.MyLogUtils;

import java.util.HashMap;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description TODO
 */
public class BaseFlightViewModel extends ViewModel {

    private GDUFlightController mGDUFlightController;
    private final MutableLiveData<LimitHeightBean> limitHeightLiveData;

    private final MutableLiveData<LimitDistanceBean> limitDistanceLiveData;
    private final MutableLiveData<GoHomeHeightBean> goHomeHeightBeanLiveData;
    private final MutableLiveData<ErrTipBean> errTipBeanLiveData;
    private final MutableLiveData<ConnectionFailSafeBehaviorBean> connectionFailSafeBehaviorLiveData;

    private final MutableLiveData<LowBatteryWarningBean> lowBatteryWarningBeanLiveData;

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


    private int preLimitHeightValue;

    /** 记录最后一次获取到的失联行为控制 */
    private int preOutOfControlAction = 0;

    public BaseFlightViewModel() {
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        limitHeightLiveData = new MutableLiveData<>();
        errTipBeanLiveData = new MutableLiveData<>();
        warnTipBeanLiveData = new MutableLiveData<>();
        goHomeHeightBeanLiveData = new MutableLiveData<>();
        limitDistanceLiveData = new MutableLiveData<>();
        connectionFailSafeBehaviorLiveData = new MutableLiveData<>();
        homeLocationBeanLiveData = new MutableLiveData<>();
        lowBatteryWarningBeanLiveData = new MutableLiveData<>();
    }

    public void getLimitHeight() {
        if (isEditLimitHeight) {
            return;
        }
        if (GlobalVariable.isNewHeightLimitStrategy) {
            int localHeightLimit = SPUtils.getCustomInt(GduAppEnv.application,
                    SPUtils.KEY_LOCAL_HEIGHT_LIMIT, MyConstants.LIMIT_HEIGHT_DEFAULT);
            LimitHeightBean bean = new LimitHeightBean();
            bean.setOpen(localHeightLimit != MyConstants.LIMIT_HEIGHT_CLOSE);
            bean.setHeightLimit(localHeightLimit);
            bean.setSet(false);
            limitHeightLiveData.setValue(bean);
            return;
        }
        if (mGDUFlightController != null) {
            mGDUFlightController.getDroneLimitHeight(new CommonCallbacks.CompletionCallbackWith<HashMap<String, Object>>() {
                @Override
                public void onSuccess(HashMap<String, Object> result) {
                    boolean isOpen = (boolean) result.get("open");
                    int heightLimit = ((int) result.get("height") & 0xFFFF);
                    heightLimit = checkAndSaveHeightData(isOpen, heightLimit);
                    LimitHeightBean bean = new LimitHeightBean();
                    bean.setOpen(isOpen);
                    bean.setHeightLimit(heightLimit);
                    bean.setSet(false);
                    limitHeightLiveData.setValue(bean);
                }

                @Override
                public void onFailure(GDUError var1) {
                    LimitHeightBean bean = new LimitHeightBean();
                    bean.setOpen(false);
                    bean.setHeightLimit(0);
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
            public void onResult(GDUError error) {
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
                    System.out.println("=============1===========");
                    preOutOfControlAction = 0;
                } else {
                    System.out.println("=============2===========");
                    preOutOfControlAction = 1;
                }
                ConnectionFailSafeBehaviorBean behaviorBean = new ConnectionFailSafeBehaviorBean();
                behaviorBean.setPosition(preOutOfControlAction);
                behaviorBean.setSuccess(true);
                connectionFailSafeBehaviorLiveData.postValue(behaviorBean);
            }

            @Override
            public void onFailure(GDUError var1) {

            }
        });
    }

    public void setGoHomeHeight(int height) {
        mGDUFlightController.setGoHomeHeightInMeters((short) height, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
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
        if (limitHeight > MyConstants.LIMIT_HEIGHT_DEFAULT && preLimitHeightValue <= MyConstants.LIMIT_HEIGHT_DEFAULT && isEditLimitHeight) {
            WarnTipBean warnTipBean = new WarnTipBean();
            warnTipBean.setType(1);
            warnTipBean.setWarnType(1);
            warnTipBean.setIntValue(limitHeight);
            warnTipBeanLiveData.postValue(warnTipBean);
//            showLimitHeightDialog(limitHeight);
        } else {
            if (GlobalVariable.isNewHeightLimitStrategy) {
                if (GlobalVariable.isTetherModel) {
                    ErrTipBean tipBean = new ErrTipBean();
                    tipBean.setSetType(1);
                    tipBean.setType(2);
                    errTipBeanLiveData.postValue(tipBean);
                    return;
                }


                if (isOpen) {
                    if (limitHeight < MyConstants.LIMIT_HEIGHT_MIN || limitHeight > MyConstants.LIMIT_HEIGHT_MAX) {
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(4);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                    // 转化飞机高度单位(cm -> m)
                    int droneHeight = GlobalVariable.height_drone / 100;
                    if (limitHeight <= droneHeight + 10) {
//                        mViewCallback.setHeightBelowCurValueErr();
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(5);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                }
                SPUtils.put(GduAppEnv.application, SPUtils.KEY_LOCAL_HEIGHT_LIMIT, isOpen ? limitHeight : MyConstants.LIMIT_HEIGHT_CLOSE);
                int value = checkAndSaveHeightData(isOpen, limitHeight);
                LimitHeightBean bean = new LimitHeightBean();
                bean.setOpen(isOpen);
                bean.setHeightLimit(value);
                bean.setSet(true);
                limitHeightLiveData.setValue(bean);
                mGDUFlightController.setMaxFlightHeight(limitHeight, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(GDUError var1) {

                    }
                });
            } else {
                if (!isConnect()) {
                    ErrTipBean tipBean = new ErrTipBean();
                    tipBean.setSetType(1);
                    tipBean.setType(1);
                    errTipBeanLiveData.postValue(tipBean);
                    return;
                }

                if (GlobalVariable.isTetherModel) {
                    ErrTipBean tipBean = new ErrTipBean();
                    tipBean.setSetType(1);
                    tipBean.setType(2);
                    errTipBeanLiveData.postValue(tipBean);
                    return;
                }

                if (isOpen) {
                    if (limitHeight < MyConstants.LIMIT_HEIGHT_MIN || limitHeight > MyConstants.LIMIT_HEIGHT_MAX) {
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(4);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                    // 转化飞机高度单位(cm -> m)
                    int droneHeight = GlobalVariable.height_drone / 100;
                    if (limitHeight <= droneHeight + 10) {
//                            mViewCallback.setHeightBelowCurValueErr();
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(5);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                }

                mGDUFlightController.setMaxFlightHeight(limitHeight, error -> {
                    if (error == null) {
                        int value = checkAndSaveHeightData(isOpen, limitHeight);
                        LimitHeightBean bean = new LimitHeightBean();
                        bean.setOpen(isOpen);
                        bean.setHeightLimit(value);
                        bean.setSet(true);
                        limitHeightLiveData.setValue(bean);
                    } else {
                        LimitHeightBean bean = new LimitHeightBean();
                        bean.setOpen(isOpen);
                        bean.setHeightLimit(limitHeight);
                        bean.setSet(false);
                        limitHeightLiveData.setValue(bean);
                    }
                });
            }
        }
    }

    public void setEditLimitDistance(boolean editLimitDistance) {
        isEditLimitDistance = editLimitDistance;
    }

    public void setLimitDistance(boolean isOpen, int distance) {
//            if (!connStateToast()) {
//                mViewCallback.showErrTip(2,1);
//                return;
//            }

        if (GlobalVariable.isTetherModel) {
            ErrTipBean tipBean = new ErrTipBean();
            tipBean.setSetType(2);
            tipBean.setType(2);
            errTipBeanLiveData.postValue(tipBean);
            return;
        }

        if (isOpen) {
            if (distance < MyConstants.LIMIT_DISTANCE_MIN || distance > MyConstants.LIMIT_DISTANCE_MAX) {
                ErrTipBean tipBean = new ErrTipBean();
                tipBean.setSetType(2);
                tipBean.setType(4);
                errTipBeanLiveData.postValue(tipBean);
                return;
            }

            if (GlobalVariable.flyDistance > MyConstants.LIMIT_DISTANCE_MIN && distance < GlobalVariable.flyDistance) {
//                    mViewCallback.setDistanceBelowCurValueErr();
                ErrTipBean tipBean = new ErrTipBean();
                tipBean.setSetType(2);
                tipBean.setType(6);
                errTipBeanLiveData.postValue(tipBean);
                return;
            }
            if (GlobalVariable.droneFlyState != 1) {
                Point returnPoint = new Point(GlobalVariable.backHomeLan,
                        GlobalVariable.backHomeLon, SpatialReference.WGS84);
                Point drone = new Point(GlobalVariable.GPS_Lat,
                        GlobalVariable.GPS_Lon, SpatialReference.WGS84);
                final double distanceValue = JTSUtils.INSTANCE.calPointsDistance(returnPoint, drone);
                if (distanceValue > distance) {
                    ErrTipBean tipBean = new ErrTipBean();
                    tipBean.setSetType(2);
                    tipBean.setType(7);
                    errTipBeanLiveData.postValue(tipBean);
                    return;
                }
            }
            mGDUFlightController.setMaxFlightRadius((short) distance, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    LimitDistanceBean limitDistanceBean = new LimitDistanceBean();
                    if (error == null) {
                        int value = distance;
                        value = checkAndSaveDistanceData(isOpen, value);
                        limitDistanceBean.setSet(true);
                        limitDistanceBean.setDistanceLimit(value);
                        limitDistanceBean.setOpen(true);
                        limitDistanceBean.setSuccess(true);
                    } else {
                        limitDistanceBean.setSet(true);
                        limitDistanceBean.setDistanceLimit(distance);
                        limitDistanceBean.setOpen(true);
                        limitDistanceBean.setSuccess(false);
                    }
                    limitDistanceLiveData.postValue(limitDistanceBean);
                }
            });
        } else {
            mGDUFlightController.setMaxFlightRadiusLimitationEnabled(false, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    LimitDistanceBean limitDistanceBean = new LimitDistanceBean();
                    if (error == null) {
                        int value = distance;
                        value = checkAndSaveDistanceData(isOpen, value);
                        limitDistanceBean.setSet(true);
                        limitDistanceBean.setDistanceLimit(value);
                        limitDistanceBean.setOpen(false);
                        limitDistanceBean.setSuccess(true);
                    } else {
                        limitDistanceBean.setSet(true);
                        limitDistanceBean.setDistanceLimit(distance);
                        limitDistanceBean.setOpen(false);
                        limitDistanceBean.setSuccess(false);
                    }
                    limitDistanceLiveData.postValue(limitDistanceBean);
                }
            });
        }
    }

    private int checkAndSaveDistanceData(boolean isOpen, int distance) {
        if (!isOpen) {
            return distance;
        }
        if (distance > MyConstants.LIMIT_DISTANCE_MAX) {
            distance = MyConstants.LIMIT_DISTANCE_MAX;
        } else if (distance < MyConstants.LIMIT_DISTANCE_MIN) {
            distance = MyConstants.LIMIT_DISTANCE_MIN;
        }
        GlobalVariable.limitDiatsnce = (short) distance;
        SPUtils.put(GduAppEnv.application, SPUtils.LAST_LIMIT_DISTANCE, distance);
        return distance;
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
        if (value < MyConstants.LIMIT_HEIGHT_MIN) {
            value = MyConstants.LIMIT_HEIGHT_MIN;
        } else if (value > MyConstants.LIMIT_HEIGHT_MAX) {
            value = MyConstants.LIMIT_HEIGHT_MAX;
        }
        GlobalVariable.limitHeight = (short) value;
        SPUtils.put(GduAppEnv.application, SPUtils.LAST_LIMIT_HEIGHT, value);
        return value;
    }

    public MutableLiveData<LimitHeightBean> getLimitHeightLiveData() {
        return limitHeightLiveData;
    }

    public MutableLiveData<GoHomeHeightBean> getGoHomeHeightBeanLiveData() {
        return goHomeHeightBeanLiveData;
    }

    public MutableLiveData<ErrTipBean> getErrTipBeanLiveData() {
        return errTipBeanLiveData;
    }

    public MutableLiveData<WarnTipBean> getWarnTipBeanLiveData() {
        return warnTipBeanLiveData;
    }

    public MutableLiveData<Boolean> getHomeLocationBeanLiveData() {
        return homeLocationBeanLiveData;
    }

    public MutableLiveData<LimitDistanceBean> getLimitDistanceLiveData() {
        return limitDistanceLiveData;
    }

    public MutableLiveData<ConnectionFailSafeBehaviorBean> getConnectionFailSafeBehaviorLiveData() {
        return connectionFailSafeBehaviorLiveData;
    }

    public MutableLiveData<LowBatteryWarningBean> getLowBatteryWarningBeanLiveData() {
        return lowBatteryWarningBeanLiveData;
    }

    public boolean isConnect() {
        MyLogUtils.i("connStateToast()");
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
                return false;
            case Conn_MoreOne:
                return false;
            case Conn_Sucess:
                return true;
            default:
                break;
        }
        return false;
    }

    public void setHomePoint(double lat, double lng, byte type) {
        LocationCoordinate2D homeLocation = new LocationCoordinate2D(lat, lng);
        mGDUFlightController.setHomeLocation(homeLocation, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
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
        GlobalVariable.oneLevelLowBattery = seriousLowBatteryWarning;
        mGDUFlightController.setLowBatteryWarningThreshold(lowBatteryWarning, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                getLowBatteryWarningThreshold();
            }
        });
    }

    public void setSeriousLowBatteryWarningThreshold(int percent){
        mGDUFlightController.setSeriousLowBatteryWarningThreshold(percent, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                getLowBatteryWarningThreshold();
            }
        });
    }

    public void getLowBatteryWarningThreshold(){
        mGDUFlightController.getLowBatteryWarningThreshold(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer lowBatteryWarning) {

            }

            @Override
            public void onFailure(GDUError var1) {

            }
        });
    }

    public void getSeriousLowBatteryWarningThreshold(){
        mGDUFlightController.getSeriousLowBatteryWarningThreshold(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer seriousLowBatteryWarning) {
                int twolevel = GlobalVariable.twoLevelLowBattery;
                int onelevel = GlobalVariable.oneLevelLowBattery;
                if (onelevel < MyConstants.DRONE_LOW_BATTERY_ONE_LEVEL_MIN) {
                    onelevel = MyConstants.DRONE_LOW_BATTERY_ONE_LEVEL_MIN;
                }
                if (twolevel < MyConstants.DRONE_LOW_BATTERY_TWO_LEVEL_MIN) {
                    twolevel = MyConstants.DRONE_LOW_BATTERY_TWO_LEVEL_MIN;
                }
                GlobalVariable.twoLevelLowBattery = twolevel;
                GlobalVariable.oneLevelLowBattery = onelevel;
                LowBatteryWarningBean warningBean = new LowBatteryWarningBean();
                warningBean.setSuccess(true);
                warningBean.setLowBatteryWarningValue(twolevel);
                warningBean.setSeriousLowBatteryWarningValue(onelevel);
                lowBatteryWarningBeanLiveData.postValue(warningBean);
            }

            @Override
            public void onFailure(GDUError var1) {
                LowBatteryWarningBean warningBean = new LowBatteryWarningBean();
                warningBean.setSuccess(false);
                warningBean.setLowBatteryWarningValue(0);
                warningBean.setLowBatteryWarningValue(0);
                lowBatteryWarningBeanLiveData.postValue(warningBean);
            }
        });
    }
}
