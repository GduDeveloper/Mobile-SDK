package com.gdu.demo.flight.base;

import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.common.error.GDUError;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.map.SpatialReference;
import com.gdu.demo.map.geometry.Point;
import com.gdu.demo.map.utils.JTSUtils;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.drone.LocationCoordinate2D;
import com.gdu.flightcontroller.ConnectionFailSafeBehavior;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.flightcontroller.bean.DroneBackInfo;
import com.gdu.sdk.flightcontroller.bean.LimitDistanceInfo;
import com.gdu.sdk.flightcontroller.bean.LimitHeightInfo;
import com.gdu.sdk.flightcontroller.bean.LowBatteryWarnInfo;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.ConnectUtil;
import com.gdu.util.DroneUtil;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;
import com.gdu.util.logger.MyLogUtils;

import java.util.HashMap;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description TODO
 */
public class BaseFlightViewModel extends BaseViewModel {

    private GDUFlightController mGDUFlightController;
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
        if (null == context) return 0;
        int mLimitHeightValue = SPUtils.getInt(context, SPUtils.LAST_LIMIT_HEIGHT);
        if (mLimitHeightValue == 0) {
            preLimitHeight = MyConstants.LIMIT_HEIGHT_DEFAULT;
        } else if (mLimitHeightValue < MyConstants.LIMIT_HEIGHT_MIN) {
            preLimitHeight = MyConstants.LIMIT_HEIGHT_MIN;
        } else if (mLimitHeightValue > MyConstants.LIMIT_HEIGHT_MAX) {
            preLimitHeight = MyConstants.LIMIT_HEIGHT_MAX;
        } else {
            preLimitHeight = mLimitHeightValue;
        }
        return preLimitHeight;
    }

    /**
     * 获取限制高度
     * */
    public void getLimitHeight() {
        if (isEditLimitHeight) {
            return;
        }
        if (GlobalVariable.isNewHeightLimitStrategy) {
            int localHeightLimit = SPUtils.getCustomInt(GduAppEnv.application,
                    SPUtils.KEY_LOCAL_HEIGHT_LIMIT, MyConstants.LIMIT_HEIGHT_DEFAULT);
            LimitHeightInfo bean = new LimitHeightInfo();
            bean.setOpen(localHeightLimit != MyConstants.LIMIT_HEIGHT_CLOSE);
            bean.setHeight(localHeightLimit);
            bean.setSet(false);
            limitHeightLiveData.setValue(bean);
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
                public void onFailure(GDUError var1) {
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
        if (limitHeight > MyConstants.LIMIT_HEIGHT_DEFAULT && preLimitHeight <= MyConstants.LIMIT_HEIGHT_DEFAULT && isEditLimitHeight) {
            WarnTipBean warnTipBean = new WarnTipBean();
            warnTipBean.setType(1);
            warnTipBean.setWarnType(1);
            warnTipBean.setIntValue(limitHeight);
            warnTipBeanLiveData.postValue(warnTipBean);
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
                        ErrTipBean tipBean = new ErrTipBean();
                        tipBean.setSetType(1);
                        tipBean.setType(5);
                        errTipBeanLiveData.postValue(tipBean);
                        return;
                    }
                }
                SPUtils.put(GduAppEnv.application, SPUtils.KEY_LOCAL_HEIGHT_LIMIT, isOpen ? limitHeight : MyConstants.LIMIT_HEIGHT_CLOSE);
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
                        if (!GlobalVariable.isActive) {
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
    public int getDefaultLimitDistance(Context context){
        int mLimitDistanceValue = SPUtils.getInt(context, SPUtils.LAST_LIMIT_DISTANCE);

        if (mLimitDistanceValue == 0) {
            preDistanceLimit = MyConstants.LIMIT_DISTANCE_DEFAULT;
        } else if (mLimitDistanceValue < MyConstants.LIMIT_DISTANCE_MIN) {
            preDistanceLimit = MyConstants.LIMIT_DISTANCE_MIN;
        } else if (mLimitDistanceValue > MyConstants.LIMIT_DISTANCE_MAX) {
            preDistanceLimit = MyConstants.LIMIT_DISTANCE_MAX;
        } else {
            preDistanceLimit = mLimitDistanceValue;
        }
        return preDistanceLimit;
    }

    /**
     * 获取限距
     * */
    public void getLimitDistance(){
        mGDUFlightController.getLimitDistance(new CommonCallbacks.CompletionCallbackWith<LimitDistanceInfo>() {
            @Override
            public void onSuccess(LimitDistanceInfo result) {
                int distance = checkAndSaveDistanceData(result.isOpen(), result.getDistance());
                result.setDistance(distance);
                result.setSet(false);
                result.setSuccess(true);
                limitDistanceLiveData.postValue(result);
            }

            @Override
            public void onFailure(GDUError var1) {

            }
        });
    }

    public void setEditLimitDistance(boolean editLimitDistance) {
        isEditLimitDistance = editLimitDistance;
    }

    public void setLimitDistance(boolean isOpen, int distance) {
            if (!ConnectUtil.isConnect()) {
                ErrTipBean tipBean = new ErrTipBean();
                tipBean.setSetType(2);
                tipBean.setType(1);
                errTipBeanLiveData.postValue(tipBean);
                return;
            }

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
                value = checkAndSaveDistanceData(isOpen, value);
                limitDistanceBean.setSet(true);
                limitDistanceBean.setDistance(value);
                limitDistanceBean.setOpen(isOpen);
                limitDistanceBean.setSuccess(true);
                preDistanceLimit = value;
            } else {
                if (!GlobalVariable.isActive) {
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
        switch (GlobalVariable.connStateEnum) {
            case Conn_None:
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
        mGDUFlightController.setLowBatteryWarningThreshold((byte) seriousLowBatteryWarning, (byte) lowBatteryWarning, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
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
            public void onFailure(GDUError var1) {
                LowBatteryWarnInfo warningBean = new LowBatteryWarnInfo();
                warningBean.setSuccess(false);
                warningBean.setOneLevelWarn(0);
                warningBean.setTwoLevelWarn(0);
                lowBatteryWarningLiveData.postValue(warningBean);
            }
        });
    }
}
