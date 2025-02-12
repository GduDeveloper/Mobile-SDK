package com.gdu.demo.flight.base;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.AlgorithmMark;
import com.gdu.api.Util.ConnectUtil;
import com.gdu.common.error.GDUError;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.demo.flight.pre.bean.ObstacleStatusBean;
import com.gdu.radar.GDUFlightAssistantObstacleSensingDirection;
import com.gdu.sdk.flightcontroller.flightassistant.FlightAssistant;
import com.gdu.sdk.util.CommonCallbacks;

import org.json.JSONObject;

public class BaseFlightAssistantViewModel extends BaseViewModel {

    private FlightAssistant mFlightAssistant;

    private byte isHorSwitchSelected;
    private byte isTopSwitchSelected;
    private byte isBottomSwitchSelected;

    private int horBrakeDistance;
    private int horWarnDistance;
    private int topBrakeDistance;
    private int topWarnDistance;
    private int bottomBrakeDistance;
    private int bottomWarnDistance;

    //避障子方向开关和距离
    private final MutableLiveData<ObstacleStatusBean> obstacleHorLiveData;
    private final MutableLiveData<ObstacleStatusBean> obstacleTopLiveData;
    //飞行模式切换时视觉感知开关状态监听
    private final MutableLiveData<Boolean> visionSensingLiveData;
    //飞行模式切换时避障策略开关状态监听
    private final MutableLiveData<Boolean> obstacleAvoidanceStrategyLiveData;

//    private final MutableLiveData<VisionSensingBean> visionSensingLiveData;
    private final MutableLiveData<HorizontalVisionObstacleAvoidanceBean> horizontalVisionObstacleAvoidanceLiveData;

    public BaseFlightAssistantViewModel(){
        mFlightAssistant = SdkDemoApplication.getAircraftInstance().getFlightController().getFlightAssistant();
        visionSensingLiveData = new MutableLiveData<>();
        obstacleHorLiveData = new MutableLiveData<>();
        obstacleTopLiveData = new MutableLiveData<>();
        obstacleAvoidanceStrategyLiveData = new MutableLiveData<>();
        horizontalVisionObstacleAvoidanceLiveData = new MutableLiveData<>();
    }

    public void setVisionSensingEnabled(boolean visionSensingEnabled){
        mFlightAssistant.setVisionSensingEnabled(visionSensingEnabled, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error != null) {
                    VisionSensingBean visionSensingBean = new VisionSensingBean();
                    visionSensingBean.setSet(true);
                    visionSensingBean.setSuccess(false);
//                    visionSensingLiveData.postValue(visionSensingBean);
                }
                getVisionSensingEnabled();
            }
        });
    }

    public void getVisionSensingEnabled(){
        mFlightAssistant.getVisionSensingEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean enable) {
                GlobalVariable.obstacleIsOpen = enable;
                AlgorithmMark.getSingleton().ObStacle = enable;
                VisionSensingBean visionSensingBean = new VisionSensingBean();
                visionSensingBean.setSuccess(true);
                visionSensingBean.setVisionSensingEnable(enable);
                visionSensingBean.setObstacleAvoidanceStrategyEnable(GlobalVariable.obstacleStrategyIsOpen);
//                visionSensingLiveData.postValue(visionSensingBean);
            }

            @Override
            public void onFailure(GDUError var1) {

            }
        });
    }

    public void setTopVisionObstacleAvoidanceEnabled(boolean enabled){
        mFlightAssistant.setUpwardVisionObstacleAvoidanceEnabled(enabled, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {

            }
        });
//        mFlightAssistant.setHorizontalVisionObstacleAvoidanceEnabled(enabled, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(GDUError error) {
//                if (error != null) {
//                    HorizontalVisionObstacleAvoidanceBean bean = new HorizontalVisionObstacleAvoidanceBean();
//                    bean.setSet(true);
//                    bean.setSuccess(false);
//                    horizontalVisionObstacleAvoidanceLiveData.postValue(bean);
//                }
//                getHorizontalVisionObstacleAvoidanceEnabled();
//            }
//        });
    }

    public void setTopVisionObstacleAvoidanceDistance(int brakeDistance, int warnDistance){
        mFlightAssistant.setVisualObstaclesAvoidanceDistance(brakeDistance, warnDistance, GDUFlightAssistantObstacleSensingDirection.Upward, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {

            }
        });
    }

    public void setBottomVisionObstacleAvoidanceDistance(int brakeDistance, int warnDistance){
        mFlightAssistant.setVisualObstaclesAvoidanceDistance(brakeDistance, warnDistance, GDUFlightAssistantObstacleSensingDirection.Downward, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {

            }
        });
    }

    public void setHorVisionObstacleAvoidanceDistance(int brakeDistance, int warnDistance){
            Log.d("test ", " setHorVisionObstacleAvoidanceDistance Horizontal: " + brakeDistance + " warnDistance: " + warnDistance );
            mFlightAssistant.setVisualObstaclesAvoidanceDistance(brakeDistance, warnDistance, GDUFlightAssistantObstacleSensingDirection.Horizontal, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(GDUError error) {
                    Log.d("test ", " setHorVisionObstacleAvoidanceDistance error: " + error);
                    if (error == null) {
                        Log.d("test ", " setHorVisionObstacleAvoidanceDistance: ");
                    }
                }
            });
    }

    public void setBottomVisionObstacleAvoidanceEnabled(boolean enabled){

    }

    public void setHorizontalVisionObstacleAvoidanceEnabled(boolean enabled){
        mFlightAssistant.setHorizontalVisionObstacleAvoidanceEnabled(enabled, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error != null) {
                    HorizontalVisionObstacleAvoidanceBean bean = new HorizontalVisionObstacleAvoidanceBean();
                    bean.setSet(true);
                    bean.setSuccess(false);
                    horizontalVisionObstacleAvoidanceLiveData.postValue(bean);
                }
                getHorizontalVisionObstacleAvoidanceEnabled();
            }
        });
    }


    public void getHorizontalVisionObstacleAvoidanceEnabled(){
        mFlightAssistant.getHorizontalVisionObstacleAvoidanceEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean enable) {
                HorizontalVisionObstacleAvoidanceBean bean = new HorizontalVisionObstacleAvoidanceBean();
                bean.setSet(false);
                bean.setSuccess(true);
                bean.setEnable(enable);
                horizontalVisionObstacleAvoidanceLiveData.postValue(bean);
            }

            @Override
            public void onFailure(GDUError var1) {

            }
        });
    }

    /**
     * 设置避障策略开关
     * @param isOn
     */
    public void setObstacleAvoidanceStrategyEnabled(boolean isOn){
        mFlightAssistant.setObstacleAvoidanceStrategyEnabled(isOn, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                if (error == null) {
                    obstacleAvoidanceStrategyLiveData.postValue(isOn);
                }
                getVisionObstacleSwitch();
            }
        });
    }

    public void getVisionObstacleSwitch() {
        //获取视觉感知开关
        mFlightAssistant.getVisionSensingEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean open) {
                GlobalVariable.obstacleIsOpen = open;
                AlgorithmMark.getSingleton().ObStacle = open;
                visionSensingLiveData.postValue(open);
            }

            @Override
            public void onFailure(GDUError var1) {
            }
        });
        //获取避障策略开关
        mFlightAssistant.getObstacleAvoidanceStrategyEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean b) {
                getObstacleCallback();
                GlobalVariable.obstacleStrategyIsOpen = b;
                obstacleAvoidanceStrategyLiveData.postValue(b);
            }

            @Override
            public void onFailure(GDUError var1) {
                getObstacleCallback();
            }
        });
    }

    /**
     * 获取避障子方向开关和距离
     */
    private void getObstacleCallback() {
        if (!ConnectUtil.isConnect()) {
            toastLiveData.setValue(R.string.DeviceNoConn);
            return;
        }
        mFlightAssistant.getObstacleDirectionDistance(new CommonCallbacks.CompletionCallbackWith<JSONObject>() {
            @Override
            public void onSuccess(JSONObject data) {

                isHorSwitchSelected = (byte) data.optInt("isHorSwitchSelected");
                isTopSwitchSelected = (byte) data.optInt("isTopSwitchSelected");
                isBottomSwitchSelected = (byte) data.optInt("isBottomSwitchSelected");

                //水平避障开关
                GlobalVariable.isObsHorSwitchState = isHorSwitchSelected == 0;
                //上视避障开关
                GlobalVariable.isObsTopSwitchState = isTopSwitchSelected == 0;
                //下视避障开关
                GlobalVariable.isObsBottomSwitchState = isBottomSwitchSelected == 0;

                //水平避障刹停距离
                horBrakeDistance =  data.optInt("horBrakeDistance");
                //水平避障告警距离
                horWarnDistance = data.optInt("horWarnDistance");
                //上视避障刹停距离
                topBrakeDistance = data.optInt("topBrakeDistance");
                //上视觉避告警距离
                topWarnDistance = data.optInt("topWarnDistance");
                //下视避障刹停距离
                bottomBrakeDistance =  data.optInt("bottomBrakeDistance");
                //下视避障告警距离
                bottomWarnDistance = data.optInt("bottomWarnDistance");

                ObstacleStatusBean horBean = new ObstacleStatusBean();
                horBean.setSelect(GlobalVariable.isObsHorSwitchState);
                horBean.setObsOpen(GlobalVariable.isObsHorSwitchState);
                if (GlobalVariable.isObsHorSwitchState) {
                    horBean.setStopValue(horBrakeDistance);
                    horBean.setAlarmValue(horWarnDistance);
                }
                obstacleHorLiveData.postValue(horBean);

                ObstacleStatusBean topBean = new ObstacleStatusBean();
                topBean.setSelect(GlobalVariable.isObsTopSwitchState);
                topBean.setObsOpen(GlobalVariable.isObsTopSwitchState);
                if (GlobalVariable.isObsTopSwitchState) {
                    topBean.setStopValue(topBrakeDistance);
                    topBean.setAlarmValue(topWarnDistance);
                }
                obstacleTopLiveData.postValue(topBean);
            }

            @Override
            public void onFailure(GDUError var1) {
                ObstacleStatusBean bean = new ObstacleStatusBean();
                bean.setSelect(false);
                bean.setObsOpen(false);
                bean.setObsBottomOpen(false);
                obstacleHorLiveData.postValue(bean);
                obstacleTopLiveData.postValue(bean);
            }
        });
    }

    public MutableLiveData<Boolean> getVisionSensingLiveData() {
        return visionSensingLiveData;
    }

    public MutableLiveData<Boolean> getObstacleAvoidanceStrategyLiveData() {
        return obstacleAvoidanceStrategyLiveData;
    }

    public MutableLiveData<ObstacleStatusBean> getObstacleHorLiveData() {
        return obstacleHorLiveData;
    }

    public MutableLiveData<ObstacleStatusBean> getObstacleTopLiveData() {
        return obstacleTopLiveData;
    }
}
