package com.gdu.demo.flight.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.common.error.GDUError;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.remotecontroller.AircraftMappingStyle;
import com.gdu.sdk.remotecontroller.GDURemoteController;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description 遥控器相关功能viewmodel
 */
public class BaseRCViewModel extends ViewModel {

    private GDURemoteController mGDURemoteController;

    private final MutableLiveData<AircraftMappingStyleBean> aircraftMappingStyleLiveData;

    public BaseRCViewModel() {
        mGDURemoteController = SdkDemoApplication.getAircraftInstance().getRemoteController();
        aircraftMappingStyleLiveData = new MutableLiveData<>();
    }

    /**
     * 设置摇杆模式
     */
    public void setAircraftMappingStyle(AircraftMappingStyle style){
        mGDURemoteController.setAircraftMappingStyle(style, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(GDUError error) {
                AircraftMappingStyleBean bean = new AircraftMappingStyleBean();
                bean.setSet(true);
                if (error == null) {
                    bean.setAircraftMappingStyle(style);
                    bean.setSuccess(true);
                } else {
                    bean.setSuccess(false);
                }
                aircraftMappingStyleLiveData.postValue(bean);
            }
        });
    }

    public void getAircraftMappingStyle(){
        mGDURemoteController.getAircraftMappingStyle(new CommonCallbacks.CompletionCallbackWith<AircraftMappingStyle>() {
            @Override
            public void onSuccess(AircraftMappingStyle style) {
                AircraftMappingStyleBean bean = new AircraftMappingStyleBean();
                bean.setSet(false);
                bean.setAircraftMappingStyle(style);
                aircraftMappingStyleLiveData.postValue(bean);
            }

            @Override
            public void onFailure(GDUError var1) {

            }
        });
    }

    public MutableLiveData<AircraftMappingStyleBean> getAircraftMappingStyleLiveData() {
        return aircraftMappingStyleLiveData;
    }

}
