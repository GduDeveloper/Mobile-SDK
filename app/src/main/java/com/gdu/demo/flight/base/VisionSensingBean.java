package com.gdu.demo.flight.base;

/**
 * 视觉感知和刹停
 */
public class VisionSensingBean {


    /**
     * 是否设置
     */
    private boolean isSet;

    /**
     * 操作是否成功
     */
    private boolean isSuccess;

    /**
     * 视觉感知
     */
    private boolean visionSensingEnable;

    /**
     * 主动刹停
     */
    private boolean obstacleAvoidanceStrategyEnable;


    public boolean isVisionSensingEnable() {
        return visionSensingEnable;
    }

    public void setVisionSensingEnable(boolean visionSensingEnable) {
        this.visionSensingEnable = visionSensingEnable;
    }

    public boolean isObstacleAvoidanceStrategyEnable() {
        return obstacleAvoidanceStrategyEnable;
    }

    public void setObstacleAvoidanceStrategyEnable(boolean obstacleAvoidanceStrategyEnable) {
        this.obstacleAvoidanceStrategyEnable = obstacleAvoidanceStrategyEnable;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }


    public boolean isSet() {
        return isSet;
    }

    public void setSet(boolean set) {
        isSet = set;
    }
}
