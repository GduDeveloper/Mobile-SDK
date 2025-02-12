package com.gdu.demo.flight.setting.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.gdu.common.error.GDUError;
import com.gdu.demo.SdkDemoApplication;
import com.gdu.sdk.battery.GDUBattery;
import com.gdu.sdk.util.CommonCallbacks;
import com.gdu.util.TextUtil;

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
        GDUBattery battery =  SdkDemoApplication.getAircraftInstance().getBattery();
        battery.getSerialNumber(new CommonCallbacks.CompletionCallbackWith<String>() {
            @Override
            public void onSuccess(String s) {
                if (!TextUtil.isEmptyString(s))
                    batterySNLiveData.postValue(s);
            }

            @Override
            public void onFailure(GDUError gduError) {

            }
        });
    }

    public MutableLiveData<String> getBatterySNLiveData() {
        return batterySNLiveData;
    }
}
