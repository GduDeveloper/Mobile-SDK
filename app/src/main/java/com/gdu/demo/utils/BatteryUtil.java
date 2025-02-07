package com.gdu.demo.utils;

import com.gdu.drone.PlanType;
import com.gdu.sdk.util.CommonUtils;

public class BatteryUtil {

    /**
     * S400 单电芯最大电压 单位mV
     */
    private static final float MAX_VOLTAGE_S400 = 4400;
    /**
     * S400 单电芯最小电压 单位mV
     */
    private static final float MIN_VOLTAGE_S400 = 3200;
    /**
     * S200 单电芯最大电压 单位mV
     */
    private static final float MAX_VOLTAGE_S200 = 4200;
    /**
     * S200 单电芯最小电压 单位mV
     */
    private static final float MIN_VOLTAGE_S200 = 3000;

    /**
     * 获取单电芯剩余电量百分百
     *
     * @param current 当前电压值
     * @param max     最大满电电压值
     * @param min     最小保护电压值
     * @return 百分百 0~100
     */
    public static int getSingleRemainingPower(float current, float max, float min) {
        float percent = (current - min) / (max - min);
        if (percent > 1) {
            percent = 1;
        }
        if (percent < 0) {
            percent = 0;
        }
        return (int) (percent * 100);
    }

    /**
     * 获取单电芯剩余电量百分百
     *
     * @param planType 飞机类型
     * @param voltage  当前电压值
     * @return
     */
    public static int getSingleRemainingPower(PlanType planType, float voltage) {
        float max = MAX_VOLTAGE_S400;
        float min = MIN_VOLTAGE_S400;
        if (CommonUtils.isSmallFlight(planType)) {
            max = MAX_VOLTAGE_S200;
            min = MIN_VOLTAGE_S200;
        }
        return getSingleRemainingPower(voltage, max, min);
    }

}
