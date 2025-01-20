package com.gdu.demo.widgetlist.battery.bean


/**
 * @author wuqb
 * @date 2024/11/12
 * @description TODO
 */
enum class BatteryStatus {
    DEFAULT,  //默认状态，或无人机未连接状态
    NORMAL,    //无人机连接后正常显示状态
    WARNING_LEVEL_1, //一级低电量报警
    WARNING_LEVEL_2, //二级低电量报警
    ERROR,   //电池异常
    OVERHEATING  //电池过热，温度过高
}