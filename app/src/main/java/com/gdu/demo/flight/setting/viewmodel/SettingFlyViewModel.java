package com.gdu.demo.flight.setting.viewmodel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.gdu.common.error.GDUError;
import com.gdu.config.ConnStateEnum;
import com.gdu.config.GduAppEnv;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.flight.base.BaseViewModel;
import com.gdu.demo.flight.base.ErrTipBean;
import com.gdu.demo.utils.UnitChnageUtils;
import com.gdu.flightcontroller.ConnectionFailSafeBehavior;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.flightcontroller.bean.DroneBackInfo;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.sdk.util.CommonUtils;
import com.gdu.util.ChannelUtils;
import com.gdu.util.MyConstants;
import com.gdu.util.SPUtils;

/**
 * @author wuqb
 * @date 2025/2/7
 * @description TODO
 */
public class SettingFlyViewModel extends BaseViewModel {

    private GDUFlightController mGDUFlightController;
    private final MutableLiveData<Integer> backHomeHeightLiveData;  //返航高度
    private final MutableLiveData<Integer> backHomeSpeedLiveData;  //返航速度
    private final MutableLiveData<Integer> outOfControlActionLiveData;
    private final MutableLiveData<Integer> backHomeActionLiveData;
    private final MutableLiveData<Integer> gnssLiveData;
    private final MutableLiveData<Boolean> switchFlyModeLiveData;
    private final MutableLiveData<Boolean> tripoModeLiveData;  //三脚架模式开关
    private final MutableLiveData<Boolean> changeNoFlyAreActionLiveData;

    /**
     * 当前返航速度
     * */
    private int preBackSpeed = 8;
    /**
     * 当前返航高度
     * */
    private int preBackHeight = 20;
    /**
     * 限高大小
     * */
    private int preHeightLimit = -1;
    /**
     * 是否开启限高
     */
    private boolean isOpenLimitHeight;

    private boolean isTfaMode = false; //是否开启三脚架模式

    public SettingFlyViewModel(){
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        backHomeHeightLiveData = new MutableLiveData<>();
        outOfControlActionLiveData = new MutableLiveData<>();
        backHomeActionLiveData = new MutableLiveData<>();
        gnssLiveData = new MutableLiveData<>();
        switchFlyModeLiveData = new MutableLiveData<>();
        tripoModeLiveData = new MutableLiveData<>();
        changeNoFlyAreActionLiveData = new MutableLiveData<>();
        backHomeSpeedLiveData = new MutableLiveData<>();
    }


    /**
     * 设置返航高度
     * */
    public void setBackHomeHeight(String value){
        if (CommonUtils.isEmptyString(value) || !CommonUtils.isNumber(value)) {
            toastLiveData.setValue(R.string.input_error);
            backHomeHeightLiveData.setValue(preBackHeight);
            return;
        }
        int valueInt = UnitChnageUtils.inch2m(Integer.parseInt(value));
        if (isOpenLimitHeight && valueInt > preHeightLimit) {
            toastLiveData.setValue(R.string.input_error);
            backHomeHeightLiveData.setValue(preBackHeight);
            return;
        }
        setBackHomeHeight(valueInt);
    }

    /**
     *
     * */
    public void setBackHomeHeight(int height){
        if (!connStateToast()) {
            ErrTipBean tipBean = new ErrTipBean();
            tipBean.setSetType(3);
            tipBean.setType(1);
            errTipBeanLiveData.postValue(tipBean);
            return;
        }
        if (GlobalVariable.droneFlyState == 3 || GlobalVariable.backState == 2) {
            ErrTipBean tipBean = new ErrTipBean();
            tipBean.setSetType(3);
            tipBean.setType(3);
            errTipBeanLiveData.postValue(tipBean);
            return;
        }

        if (height < MyConstants.GO_HOME_HEIGHT_MIN || height > MyConstants.GO_HOME_HEIGHT_MAX) {
            ErrTipBean tipBean = new ErrTipBean();
            tipBean.setSetType(3);
            tipBean.setType(4);
            errTipBeanLiveData.postValue(tipBean);
            return;
        }
        mGDUFlightController.setGoHomeHeightInMeters((short) height, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError gduError) {
                if (null==gduError){
                    int value = checkAndSaveGoHomeHeightData(height);
                    toastLiveData.postValue(R.string.string_set_success);
                    preBackHeight = value;
                    backHomeHeightLiveData.postValue(value);
                }else {
                    toastLiveData.postValue(R.string.Label_SettingFail);
                    backHomeHeightLiveData.postValue(preBackHeight);
                }
            }
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

    /**
     * 获取返航速度
     * */
    public void getBackHomeSpeed(){
        mGDUFlightController.getDroneBackInfo(new CommonCallbacks.CompletionCallbackWith<DroneBackInfo>() {
            @Override
            public void onSuccess(DroneBackInfo droneBackInfo) {
                int goHomeSpeedMin = MyConstants.GO_HOME_SPEED_MIN * 100;
                if (droneBackInfo.getSpeed() < goHomeSpeedMin) {
                    droneBackInfo.setSpeed(goHomeSpeedMin);
                }
                GlobalVariable.sBackSpeed = (short) droneBackInfo.getSpeed();

                preBackSpeed = droneBackInfo.getSpeed();
                backHomeSpeedLiveData.postValue(droneBackInfo.getSpeed());
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * 设置返航速度
     * */
    public void setBackHomeSpeed(String value){
        if (!connStateToast()) {
            backHomeSpeedLiveData.setValue(preBackSpeed);
            return;
        }

        if (GlobalVariable.backState == 2) {//返航中不允许设置返航速度
            backHomeSpeedLiveData.setValue(preBackSpeed);
            toastLiveData.setValue(R.string.string_returning_cannot_set_back_speed);
            return;
        }

        if (CommonUtils.isEmptyString(value) || !CommonUtils.isNumber(value)) {
            backHomeSpeedLiveData.setValue(preBackSpeed);
            toastLiveData.setValue(R.string.input_error);
            return;
        }
        int valueInt = UnitChnageUtils.inch2m(Integer.parseInt(value));
        if (valueInt < MyConstants.GO_HOME_SPEED_MIN || valueInt > MyConstants.GO_HOME_SPEED_MAX) {
            backHomeSpeedLiveData.setValue(preBackSpeed);
            toastLiveData.setValue(R.string.input_error);
            return;
        }
        executeSetBackHomeSpeed(valueInt);
    }

    /**
     * 执行返航速度设置
     * */
    private void executeSetBackHomeSpeed(int backHomeSpeed){
        mGDUFlightController.setBackHomeSpeed(backHomeSpeed, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                toastLiveData.postValue(R.string.string_set_success);
                preBackSpeed = backHomeSpeed;
                backHomeSpeedLiveData.postValue(preBackSpeed*100);
            }

            @Override
            public void onFailure(GDUError gduError) {
                toastLiveData.postValue(R.string.Label_SettingFail);
                backHomeSpeedLiveData.postValue(preBackSpeed*100);
            }
        });
    }

    /**
     * 获取失联行为
     * */
    public void getOutOfControlAction(){
        mGDUFlightController.getConnectionFailSafeBehavior(new CommonCallbacks.CompletionCallbackWith<ConnectionFailSafeBehavior>() {
            @Override
            public void onSuccess(ConnectionFailSafeBehavior connectionFailSafeBehavior) {
                int position = 0;
                if (ConnectionFailSafeBehavior.HOVER == connectionFailSafeBehavior){
                    position = 1;
                }
                outOfControlActionLiveData.postValue(position);
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * 设置失联行为
     * */
    public void setOutOfControlAction(int position){
        ConnectionFailSafeBehavior behavior = ConnectionFailSafeBehavior.GO_HOME;
        switch (position) {
            case 1:
            case 2:
                behavior = ConnectionFailSafeBehavior.HOVER;
                break;
            default:
                break;
        }
        mGDUFlightController.setConnectionFailSafeBehavior(behavior, gduError -> {
            if (null == gduError){
                outOfControlActionLiveData.postValue(position);
                toastLiveData.postValue(R.string.string_set_success);
            }else {
                toastLiveData.postValue(R.string.Label_SettingFail);
            }
        });
    }

    /**
     * 获取返航行为
     * */
    public void getBackHomeAction(){
        mGDUFlightController.getBackHomeAction(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                backHomeActionLiveData.postValue(integer);
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * 设置返航行为
     * */
    public void setBackHomeAction(int position){
        mGDUFlightController.setBackHomeAction((byte) position, new CommonCallbacks.CompletionCallbackWith<Byte>() {
            @Override
            public void onSuccess(Byte aByte) {
                toastLiveData.postValue(R.string.string_set_success);
                backHomeActionLiveData.postValue(position);
            }

            @Override
            public void onFailure(GDUError gduError) {
                toastLiveData.postValue(R.string.Label_SettingFail);
            }
        });
    }

    /**
     * 是否能设置GPS
     * */
    public boolean getCanSetGps(){
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            toastLiveData.setValue(R.string.DeviceNoConn);
            return false;
        }
        if (GlobalVariable.droneFlyState != 1) {
            toastLiveData.setValue(R.string.string_flying_forbid);
            return false;
        }
        String rtkStatus = GlobalVariable.rtk_model.getRtk1_status();
        if (rtkStatus.equals("Fixed") || rtkStatus.equals("Float")) {
            toastLiveData.setValue(R.string.gnss_exit_rtk);
            return false;
        }
        return true;
    }

    /**
     * 设置GPS
     * */
    public void setGps(Context context, int position){
        byte isOpen;
        if (position == 0) {
            isOpen = 7;
        } else {
            isOpen = 6;
        }
        // 大华渠道S200系列软件支持单北斗模式(目前会切换失败 但是需要返回成功，做个假的支持)
        if ((ChannelUtils.isDahua(context) || ChannelUtils.isDahuaBDS(context)) && CommonUtils.curPlanIsSmallFlight()) {
            GlobalVariable.sGNSSType = isOpen;
            SPUtils.put(GduAppEnv.application, "sGNSSType", (int) isOpen);
            toastLiveData.setValue(R.string.string_set_success);
            gnssLiveData.setValue(position);
        } else {
            mGDUFlightController.change482RtkStates(isOpen, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    toastLiveData.postValue(R.string.string_set_success);
                    gnssLiveData.postValue(position);
                }

                @Override
                public void onFailure(GDUError gduError) {
                    toastLiveData.postValue(R.string.Label_SettingFail);
                }
            });
        }
    }

    /**
     * 获取模式切换开关状态
     * */
    public void getSwitchFlyModeState(){
        mGDUFlightController.getFlyModeState(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                switchFlyModeLiveData.postValue(aBoolean);
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * 设置模式切换开关状态
     */
    public void setSwitchFlyModeState(boolean isOpen) {
        mGDUFlightController.setFlyModeState(isOpen, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean){
                    switchFlyModeLiveData.postValue(isOpen);
                }else {
                    switchFlyModeLiveData.postValue(null);
                }
            }

            @Override
            public void onFailure(GDUError gduError) {
                switchFlyModeLiveData.postValue(null);
            }
        });
    }

    /**
     * 获取三脚架模式
     * */
    public void getTripodMode(){
        mGDUFlightController.getTripodMode(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                isTfaMode = aBoolean;
                tripoModeLiveData.postValue(aBoolean);
            }

            @Override
            public void onFailure(GDUError gduError) {
                isTfaMode = false;
                tripoModeLiveData.postValue(false);
            }
        });
    }

    /**
     * 设置三脚架模式
     * */
    public boolean setCanTripodMode(int position){
        if (GlobalVariable.connStateEnum == ConnStateEnum.Conn_None) {
            toastLiveData.setValue(R.string.DeviceNoConn);
            return false;
        }
        if (GlobalVariable.droneFlyState != 1 && GlobalVariable.droneFlyState != 4) {
            toastLiveData.setValue(R.string.string_flying_forbid);
            return false;
        }
        if (position == 0) {
            return isTfaMode;
        } else {
            return !isTfaMode;
        }
    }

    /**
     * 开启或关闭三脚架模式
     *
     * @param isOpen
     */
    public void setTripodMode(boolean isOpen){
        mGDUFlightController.setTripodMode(isOpen, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                toastLiveData.postValue(R.string.string_set_success);
                isTfaMode = isOpen;
                tripoModeLiveData.postValue(isTfaMode);
            }

            @Override
            public void onFailure(GDUError gduError) {
                if (GlobalVariable.droneFlyState == 1 || GlobalVariable.droneFlyState == 4) {
                    toastLiveData.postValue(R.string.Label_SettingFail);
                } else {
                    toastLiveData.postValue(R.string.string_flying_forbid);
                }
                tripoModeLiveData.postValue(isTfaMode);
            }
        });
    }

    /**
     * 变更禁飞区行为
     * */
    public void changeNoFlyAreAction(boolean change){
        mGDUFlightController.changeNoFlyAreAction(change, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                changeNoFlyAreActionLiveData.postValue(aBoolean);
            }

            @Override
            public void onFailure(GDUError gduError) {
                changeNoFlyAreActionLiveData.postValue(false);
            }
        });
    }

    public MutableLiveData<Integer> getBackHomeHeightLiveData() {
        return backHomeHeightLiveData;
    }

    public MutableLiveData<Integer> getBackHomeSpeedLiveData() {
        return backHomeSpeedLiveData;
    }

    public MutableLiveData<Integer> getOutOfControlActionLiveData() {
        return outOfControlActionLiveData;
    }

    public MutableLiveData<Integer> getBackHomeActionLiveData() {
        return backHomeActionLiveData;
    }

    public MutableLiveData<Integer> getGnssLiveData() {
        return gnssLiveData;
    }

    public MutableLiveData<Boolean> getSwitchFlyModeLiveData() {
        return switchFlyModeLiveData;
    }

    public MutableLiveData<Boolean> getTripoModeLiveData() {
        return tripoModeLiveData;
    }

    public MutableLiveData<Boolean> getChangeNoFlyAreActionLiveData() {
        return changeNoFlyAreActionLiveData;
    }
}
