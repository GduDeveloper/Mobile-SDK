package com.gdu.demo.flight.base;

import androidx.lifecycle.ViewModel;

import com.gdu.demo.SdkDemoApplication;
import com.gdu.sdk.battery.Battery;

/**
 * @author wuqb
 * @date 2025/1/13
 * @description TODO
 */
public class BaseBatteryViewModel extends ViewModel {

    private Battery battery;


    public BaseBatteryViewModel() {
        battery = SdkDemoApplication.getAircraftInstance().getBattery();
    }

}
