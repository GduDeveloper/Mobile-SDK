package com.gdu.demo.flight.setting.viewmodel;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.common.error.Error;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.sdk.battery.Battery;
import com.gdu.sdk.util.CommonCallbacks;

/**
 * @author wuqb
 * @date 2025/2/10
 * @description 电池相关ViewModel
 */
public class SettingBatteryViewModel extends ViewModel {

    private final MutableLiveData<String> batterySNLiveData;  //电池SN信息

    public SettingBatteryViewModel() {
        batterySNLiveData = new MutableLiveData<>();
    }

    public void getBatterFactoryInfo(){
        Battery battery =  SdkDemoApplication.getAircraftInstance().getBattery();
        battery.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(String s) {
                if (!TextUtils.isEmpty(s))
                    batterySNLiveData.postValue(s);
            }

            @Override
            public void onFailure(Error error) {

            }
        });
    }

    public MutableLiveData<String> getBatterySNLiveData() {
        return batterySNLiveData;
    }
}
