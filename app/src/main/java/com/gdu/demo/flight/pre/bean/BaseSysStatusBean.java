package com.gdu.demo.flight.pre.bean;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * @author wuqb
 * @date 2025/1/11
 * @description 飞机系统状态
 *     包含：飞机连接状态属性
 */
public class BaseSysStatusBean {
    private @ColorRes int statusTitleColor;  //系统状态文字颜色
    private @ColorRes int flightStatusColor;  //飞机连接状态文字颜色
    private @StringRes int flightStatusStr;  //飞行状态文字内容
    private @DrawableRes int statusBg;  //系统状态背景
    private @DrawableRes int moreRes;  //系统状态箭头图标

    public int getStatusTitleColor() {
        return statusTitleColor;
    }

    public void setStatusTitleColor(int statusTitleColor) {
        this.statusTitleColor = statusTitleColor;
    }

    public int getFlightStatusColor() {
        return flightStatusColor;
    }

    public void setFlightStatusColor(int flightStatusColor) {
        this.flightStatusColor = flightStatusColor;
    }

    public int getFlightStatusStr() {
        return flightStatusStr;
    }

    public void setFlightStatusStr(int flightStatusStr) {
        this.flightStatusStr = flightStatusStr;
    }

    public int getStatusBg() {
        return statusBg;
    }

    public void setStatusBg(int statusBg) {
        this.statusBg = statusBg;
    }

    public int getMoreRes() {
        return moreRes;
    }

    public void setMoreRes(int moreRes) {
        this.moreRes = moreRes;
    }
}
