package com.gdu.demo.widgetlist.camerapara

data class CameraParaValue(
    val isAutoMode: Boolean,
    val isoValue: Int,
    val esValue: Int,
    val evValue: Int,
    val sdCardState: SDCardState,
    val aeLockValue: Int
)

data class SDCardState(
    var isMultiSDCard: Boolean = false,
    var vlSDState: Int = 0,
    var irSDStatus: Int = 0,
    var reMainCardSum: Float = 0.0f,
    var reMainCard2Sum: Float = 0.0f
)

