package com.gdu.demo.flight.airlink;

public interface IBackupsAirlink {

    /**
     * mqtt连接成功
     */
    int MQTT_CONNECT_SUCCEED = 0x01;

    /**
     * 服务器ID地址
     */
    String SERVER_IP = "server.gdu-tech.com";

    /**
     * 服务器端口号
     */
    int SERVER_PORT = 5672;

    /**
     * 无人机状态信息
     */
    String DRONE_INFO_TO_APP_TOPIC = "gduraw/aircraft2server/datalinkcmd/";

    /**
     * 发送数传数据到服务器的topic
     */
    String APP_CMD_TO_DRONE_TOPIC = "gduraw/app2server/datalinkcmd/";



}
