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
import com.gdu.util.ThreadHelper;

/**
 * @author wuqb
 * @date 2025/2/11
 * @description 指南针校磁
 */
public class CompassCalibrationViewModel extends ViewModel {
    private final GDUFlightController mGDUFlightController;
    private final MutableLiveData<Integer> mXyRectifyStartLiveData;
    /** 校磁状态更新 */
    private final MutableLiveData<Integer> mRectifyUpdateLiveData;
    /** 校磁成功 */
    private final MutableLiveData<Integer> mRectifySuccessLiveData;
    /** XY校磁成功 */
    private final MutableLiveData<Integer> mXyRectifySuccessLiveData;
    /** 校磁失败 */
    private final MutableLiveData<Integer> mRectifyFailLiveData;
    /** 校磁暂停 */
    private final MutableLiveData<Integer> mRectifyStopLiveData;

    /*********************************
     * 校磁的状态: -------ron
     *
     * 1.XY正在校次
     * 2：XY校磁成功
     * 3：XY校磁失败
     * 4.Z轴正字校磁
     * 5：Z校磁成功
     * 6：Z校磁失败
     */
    private byte rectifyStep;
    /** 是否正在校磁中 */
    private boolean isRectifying = false;

    /**
     * 错误类型
     */
    private byte mRectifyErrorType;

    public CompassCalibrationViewModel(){
        mGDUFlightController = SdkDemoApplication.getAircraftInstance().getFlightController();
        mRectifyUpdateLiveData = new MutableLiveData<>();
        mRectifySuccessLiveData = new MutableLiveData<>();
        mXyRectifyStartLiveData = new MutableLiveData<>();
        mXyRectifySuccessLiveData = new MutableLiveData<>();
        mRectifyFailLiveData = new MutableLiveData<>();
        mRectifyStopLiveData = new MutableLiveData<>();
    }

    public int getDroneMagneticHorizontalIcon() {
        if (DroneUtil.isS200Serials()) {//S200系列
            if (GlobalVariable.planType == PlanType.S200
                    || GlobalVariable.planType == PlanType.S200BDS
                    || GlobalVariable.planType == PlanType.S200_SD
                    || GlobalVariable.planType == PlanType.S200_SD_BDS) {
                return R.drawable.s200_magnetic_img1;
            } else if (GlobalVariable.planType == PlanType.S220Pro
                    || GlobalVariable.planType == PlanType.S220ProS
                    || GlobalVariable.planType == PlanType.S220ProH
                    || GlobalVariable.planType == PlanType.S220ProBDS
                    || GlobalVariable.planType == PlanType.S220ProSBDS
                    || GlobalVariable.planType == PlanType.S220ProHBDS) {
                return R.drawable.s220pro_magnetic_img1;
            } else {//经产品确认，默认显示S220，S280和S220一样，所以也走默认流程
                return R.drawable.s220_magnetic_img1;
            }
        } else {//默认S400
            if(GlobalVariable.planType == PlanType.Z4C){
                return R.drawable.z4b_magnetic_img1;
            }else{
                return R.drawable.magnetic_img1;
            }
        }
    }

    public int getDroneMagneticVerticalIcon() {
        if (DroneUtil.isS200Serials()) {//S200系列
            if (GlobalVariable.planType == PlanType.S200
                    || GlobalVariable.planType == PlanType.S200BDS
                    || GlobalVariable.planType == PlanType.S200_SD
                    || GlobalVariable.planType == PlanType.S200_SD_BDS) {
                return R.drawable.s200_magnetic_img2;
            } else if (GlobalVariable.planType == PlanType.S220Pro
                    || GlobalVariable.planType == PlanType.S220ProS
                    || GlobalVariable.planType == PlanType.S220ProH
                    || GlobalVariable.planType == PlanType.S220ProBDS
                    || GlobalVariable.planType == PlanType.S220ProSBDS
                    || GlobalVariable.planType == PlanType.S220ProHBDS) {
                return R.drawable.s220pro_magnetic_img2;
            } else {//经产品确认，默认显示S220，S280和S220一样，所以也走默认流程
                return R.drawable.s220_magnetic_img2;
            }
        } else {//默认S400
            if(GlobalVariable.planType == PlanType.Z4C){
                return R.drawable.z4b_magnetic_img2;
            }else{
                return R.drawable.magnetic_img2;
            }
        }
    }

    /**
     * 指南针校磁监听
     * */
    public void addCompassCalibrationCallback(){
        mGDUFlightController.addCompassCalibrationCallback(new CommonCallbacks.CompletionCallbackWith<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                if (!isRectifying) {
                    isRectifying = true;
                }
                mRectifyUpdateLiveData.postValue(0);
                switch (data) {
                    case 0:  //校磁成功
                        // 添加延时1s更新处理，以用来给状态更新一点时间
                        ThreadHelper.runOnUiThreadDelayed(() -> {
                            isRectifying = false;
                            mRectifySuccessLiveData.postValue(0);
                        }, 1000);
                        break;
                    case 1:  //xy校磁中
                        rectifyStep = 1;
                        break;
                    case 2:
                    case 4:  //Z校磁
                        if (rectifyStep == 1) {
                            isRectifying = true;
                            mXyRectifySuccessLiveData.postValue(0);
                        }
                        rectifyStep = 4;
                        break;
                    case 5:
                    case 6:
                    case 7:
                        mRectifyErrorType = data.byteValue();
                        isRectifying = false;
                        onRectifyFail();
                        break;
                    case 21:
                        isRectifying = false;
                        mRectifyStopLiveData.postValue(0);
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    private void onRectifyFail(){
        int fail = 0;
        if (mRectifyErrorType == 3) {
            fail = R.string.sting_horizontal_not_right;
        } else if(mRectifyErrorType == 4){
            fail = R.string.string_vertical_not_right_case;
        } else if(mRectifyErrorType == 5){
            fail = R.string.string_magnetic_check_overtime;
        } else if(mRectifyErrorType == 6){
            fail = R.string.string_horizontal_check_fail;
        } else if(mRectifyErrorType == 7){
            fail = R.string.string_vertical_check_fail;
        }
        mRectifyFailLiveData.postValue(fail);
    }


    /**
     * 取消指南针校磁监听
     * */
    public void cancelCompassCalibrationCallback(){
        mGDUFlightController.cancelCompassCalibrationCallback();
    }

    /**
     * 开始校磁
     * */
    public void startCompassCalibration(byte start){
        isRectifying = true;
        mGDUFlightController.startCompassMagneticCalibration(start, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean){
                    isRectifying = true;
                    mXyRectifyStartLiveData.postValue(0);
                }else {
                    isRectifying = false;
                    onRectifyFail();
                }
            }

            @Override
            public void onFailure(GDUError gduError) {
                isRectifying = false;
                onRectifyFail();
            }
        });
    }

    /**
     * 停止校磁
     * */
    public void stopCompassCalibration(){
        mGDUFlightController.startCompassMagneticCalibration((byte) 0x00, new CommonCallbacks.CompletionCallbackWith<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {

            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    public MutableLiveData<Integer> getRectifyUpdateLiveData() {
        return mRectifyUpdateLiveData;
    }

    public MutableLiveData<Integer> getRectifySuccessLiveData() {
        return mRectifySuccessLiveData;
    }

    public MutableLiveData<Integer> getXyRectifyStartLiveData() {
        return mXyRectifyStartLiveData;
    }

    public MutableLiveData<Integer> getXyRectifySuccessLiveData() {
        return mXyRectifySuccessLiveData;
    }

    public MutableLiveData<Integer> getRectifyFailLiveData() {
        return mRectifyFailLiveData;
    }

    public MutableLiveData<Integer> getRectifyStopLiveData() {
        return mRectifyStopLiveData;
    }

    public boolean isRectifying() {
        return isRectifying;
    }
}
