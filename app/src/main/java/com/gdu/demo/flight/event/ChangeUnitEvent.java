package com.gdu.demo.flight.event;

/**
 * Created by yuhao on 2017/4/13.
 */
public class ChangeUnitEvent {

    private final boolean isMetric;
    public ChangeUnitEvent(boolean isMetric){
        this.isMetric=isMetric;
    }

    public boolean getUnit(){
        return isMetric;
    }
}
