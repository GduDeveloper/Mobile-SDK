package com.gdu.demo.widgetlist.takephoto

/**
 * @author wuqb
 * @date 2025/3/21
 * @description 拍摄类型
 */
enum class ShootStatus {
    INIT,  //初始
    PICTURE_ENABLE,  //拍照-可点击
    PICTURE_UNABLE,  //拍照-不可点击
    ISPHOTOING_ENABLE,  //拍照中-可点击
    ISPHOTOING_UNABLE,  //拍照中-不可点击
    ISSINGLEPHOTOING_UNABLE,  //单拍拍照中-不可点击
    VIDEO_ENABLE,     //录像-可点击
    VIDEO_UNENABLE,     //录像-不可点击
    ISRECORDING_ENABLE,  //正在录像-可点击
    ISRECORDING_UNABLE,  //正在录像-不可点击
    NO_SDCARD,  //无SD卡
    SD_FULL, //卡满
    SD_ERROR, //卡异常
}