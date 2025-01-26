package com.gdu.demo.flight.pre.bean;

/**
 * @author wuqb
 * @date 2025/1/10
 * @description TODO
 */
public class ObstacleStatusBean {
    private boolean isSelect;
    private int stopValue;
    private int alarmValue;
    private boolean obsOpen;
    private boolean obsBottomOpen;

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public int getStopValue() {
        return stopValue;
    }

    public void setStopValue(int stopValue) {
        this.stopValue = stopValue;
    }

    public int getAlarmValue() {
        return alarmValue;
    }

    public void setAlarmValue(int alarmValue) {
        this.alarmValue = alarmValue;
    }

    public boolean isObsOpen() {
        return obsOpen;
    }

    public void setObsOpen(boolean obsOpen) {
        this.obsOpen = obsOpen;
    }

    public boolean isObsBottomOpen() {
        return obsBottomOpen;
    }

    public void setObsBottomOpen(boolean obsBottomOpen) {
        this.obsBottomOpen = obsBottomOpen;
    }
}
