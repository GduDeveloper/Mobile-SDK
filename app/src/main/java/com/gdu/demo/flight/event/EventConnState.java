package com.gdu.demo.flight.event;

import com.gdu.config.ConnStateEnum;

/**
 * Created by ron on 2017/6/3.
 * <p>连接状态的EventBus --- ron</p>
 */
public class EventConnState
{
    public ConnStateEnum connStateEnum;

    public EventConnState(ConnStateEnum connStateEnum)
    {
        this.connStateEnum = connStateEnum;
    }
}

