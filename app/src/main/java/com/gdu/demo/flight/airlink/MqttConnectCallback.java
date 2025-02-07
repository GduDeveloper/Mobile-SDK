package com.gdu.demo.flight.airlink;

/**
 * @Author: lixiqiang
 * @Date: 2022/5/19
 */
public interface MqttConnectCallback {

    void onConnectSuccess();

    void onConnectFail();
}
