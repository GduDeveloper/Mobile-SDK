package com.gdu.demo.flight.calibration;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.common.error.GDUError;
import com.gdu.config.GlobalVariable;
import com.gdu.demo.R;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.drone.PlanType;
import com.gdu.sdk.flightcontroller.GDUFlightController;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.DroneUtil;

/**
 * @author wuqb
 * @date 2025/2/11
 * @description TODO
 */
public class IMUCalibrationViewModel extends ViewModel {

    private final GDUFlightController mGDUFlightController;
    private final MutableLiveData<Integer> recoveryStatusLiveData; //恢复状态
    private final MutableLiveData<Integer> changeStatusLiveData; //状态变更
    private MutableLiveData<Boolean> checkStatusLiveData; //校准状态

    //校准初始位置-1
    private final int INIT_CHECK_POS = -1;
    private int mCurrentPosition = INIT_CHECK_POS;//校准初始位置
    /** 默认没有校准,当收到状态是校准中则为true */
    private boolean isChecking = false;

    public IMUCalibrationViewModel(){
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        recoveryStatusLiveData = new MutableLiveData<>();
        changeStatusLiveData = new MutableLiveData<>();
        checkStatusLiveData = new MutableLiveData<>();
    }

    public Integer[] getIMUPhotos() {
        if (DroneUtil.isS200Serials()) {//S200系列
            if (GlobalVariable.planType == PlanType.S200
                    || GlobalVariable.planType == PlanType.S200BDS
                    || GlobalVariable.planType == PlanType.S200_SD
                    || GlobalVariable.planType == PlanType.S200_SD_BDS) {
                return new Integer[]{
                        R.drawable.drone_s200_first, R.drawable.drone_s200_second,
                        R.drawable.drone_s200_third, R.drawable.drone_s200_four,
                        R.drawable.drone_s200_five, R.drawable.drone_s200_six
                };
            } else if (GlobalVariable.planType == PlanType.S220Pro
                    || GlobalVariable.planType == PlanType.S220ProS
                    || GlobalVariable.planType == PlanType.S220ProH
                    || GlobalVariable.planType == PlanType.S220ProBDS
                    || GlobalVariable.planType == PlanType.S220ProSBDS
                    || GlobalVariable.planType == PlanType.S220ProHBDS) {
                return new Integer[]{
                        R.drawable.drone_s220pro_first, R.drawable.drone_s220pro_second,
                        R.drawable.drone_s220pro_third, R.drawable.drone_s220pro_four,
                        R.drawable.drone_s220pro_five, R.drawable.drone_s220pro_six
                };
            } else {//经产品确认，默认显示S220，S280和S220一样，所以也走默认流程
                return new Integer[]{
                        R.drawable.drone_s220_first, R.drawable.drone_s220_second,
                        R.drawable.drone_s220_third, R.drawable.drone_s220_four,
                        R.drawable.drone_s220_five, R.drawable.drone_s220_six
                };
            }
        } else {//默认S400
            if (GlobalVariable.planType == PlanType.S480) {
                return new Integer[]{
                        R.drawable.drone_s480_first, R.drawable.drone_s480_second,
                        R.drawable.drone_s480_third, R.drawable.drone_s480_four,
                        R.drawable.drone_s480_five, R.drawable.drone_s480_six
                };
            } else {
                return new Integer[]{
                        R.drawable.drone_first, R.drawable.drone_second,
                        R.drawable.drone_third, R.drawable.drone_four,
                        R.drawable.drone_five, R.drawable.drone_six
                };
            }
        }
    }

    /**
     * IMU校准监听
     * */
    public void addIMUCalibrationCallback(){
        mGDUFlightController.addIMUCalibrationCallback(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer pos) {
                if (!isChecking) {//本地状态和飞行器状态不一致，需要恢复状态
                    mCurrentPosition = pos;
                    if (mCurrentPosition > 0 && mCurrentPosition < 13 || mCurrentPosition == -1) {//如果处于校准过程中，则直接进入进行时
                        isChecking = true;
                        recoveryStatusLiveData.postValue(0);
                    }
                    changeStatusLiveData.postValue(0);
                }
                //因为目前是被动接受消息，飞行器会按照指定频率发，所以这里优化处理下，状态不一致时发送状态变更
                if (pos == mCurrentPosition) {
                    return;
                }
                //状态变更的时候再发通知消息
                mCurrentPosition = pos;
                changeStatusLiveData.postValue(0);
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    /**
     * IMU校准
     * */
    public void checkIMU(byte status){
        mGDUFlightController.checkIMUCalibration(status, new CommonCallbacks.CompletionCallbackWith<Byte>() {
            @Override
            public void onSuccess(Byte aByte) {
                //初始化成功，开始校准流程
                if (status == 0) {
                    // 停止校准
                    checkStatusLiveData.postValue(false);
                } else if (status == 1) {
                    // 开始校准
                    isChecking = true;
                    checkStatusLiveData.postValue(true);
                }
            }

            @Override
            public void onFailure(GDUError gduError) {
                checkStatusLiveData.postValue(false);
            }
        });
    }

    public boolean isChecking() {
        return isChecking;
    }

    public void setChecking(boolean checking) {
        isChecking = checking;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    public void setCurrentPosition(int mCurrentPosition) {
        this.mCurrentPosition = mCurrentPosition;
    }

    public MutableLiveData<Integer> getRecoveryStatusLiveData() {
        return recoveryStatusLiveData;
    }

    public MutableLiveData<Integer> getChangeStatusLiveData() {
        return changeStatusLiveData;
    }

    public MutableLiveData<Boolean> getCheckStatusLiveData() {
        return checkStatusLiveData;
    }
}
