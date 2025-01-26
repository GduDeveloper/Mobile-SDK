package com.gdu.demo.flight.base;

import com.gdu.remotecontroller.AircraftMappingStyle;

/**
 * 摇杆模式
 */
public class AircraftMappingStyleBean {

    private boolean isSet;

    private boolean isSuccess;

    private AircraftMappingStyle aircraftMappingStyle;


    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public AircraftMappingStyle getAircraftMappingStyle() {
        return aircraftMappingStyle;
    }

    public void setAircraftMappingStyle(AircraftMappingStyle aircraftMappingStyle) {
        this.aircraftMappingStyle = aircraftMappingStyle;
    }
}
