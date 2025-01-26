package com.gdu.demo.flight.base;

/**
 * 低电量告警
 */
public class LowBatteryWarningBean {
    private boolean isSet;
    private boolean isSuccess;
    private int lowBatteryWarningValue;

    private int seriousLowBatteryWarningValue;

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

    public int getLowBatteryWarningValue() {
        return lowBatteryWarningValue;
    }

    public void setLowBatteryWarningValue(int lowBatteryWarningValue) {
        this.lowBatteryWarningValue = lowBatteryWarningValue;
    }

    public int getSeriousLowBatteryWarningValue() {
        return seriousLowBatteryWarningValue;
    }

    public void setSeriousLowBatteryWarningValue(int seriousLowBatteryWarningValue) {
        this.seriousLowBatteryWarningValue = seriousLowBatteryWarningValue;
    }
}
