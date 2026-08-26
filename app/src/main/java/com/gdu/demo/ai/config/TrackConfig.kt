package com.gdu.ai.flight.p.smarttrack.config

/**
 * @Author: fuchi
 * @Date : 2026/7/22 - 9:23
 * @Desc :
 */
class TrackConfig {



}

/**
 * 当前智能跟踪状态
 */
enum class AlgorithmType {
    /** 算法类型_GPS跟踪  */
    Track_GPS,

    /** 算法类型_视频跟踪  */
    Track_VIDEO,

    /** 算法类型_手势  */
    Gesture,

    /** 算法类型_GPS环绕  */
    Surrond_GPS,

    /** 算法类型_视觉环绕  */
    Surrond_IMG,

    /** 算法类型_全景  */
    Panorrama,

    /** 算法类型_垂直拉升  */
    Vertical_pull,

    /** 算法类型_渐远自拍  */
    Inverted_Photo,

    /** 算法类型_无  */
    NONE,

    /** 算法类型_器件识别  */
    DEVICE_RECOGNISE;
}

/**
 * 跟踪相关状态
 */
enum class TrackMsg(val type : Int, val obj : Boolean = false){
    NONE(0),

    FOLLOWING(101),

    /** 跟踪指令发送失败 */
    TRACK_CMD_SEND_FAIL(102),

    /** 关闭双目成功后的反馈---------ron  */
    WhatCloseObstacle(0x199),

    /** 关闭视觉跟踪成功  */
    CLOSE_VIDEO_TRACK_SUCCEED(0x201),

    /** 退出视觉跟踪  */
    STATE_QUIT_VIDEO_TRACK(0x202),

    /** 停止视觉跟踪  */
    STATE_STOP_VIDEO_TRACK(0x203),

    /** 开始视觉跟踪  */
    STATE_START_VIDEO_TRACK(0x204),
    MULTIPLE_TARGET_TRACK(0x205),
    MULTIPLE_TARGET_FAIL(0x206),
    START_FOLLOW(0x207),
    STOP_FOLLOW(0x208),
    SMART_FOLLOW_MSG(221),   //Initiative：主动
//    SMART_FOLLOW_MSG_PASSIVE(222),      //Passive: 被动
    SMART_FOLLOW_GPS_MSG(230),
}