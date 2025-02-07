package com.gdu.demo.flight.setting.bean

/**
 * 目标识别算法类型
 */
data class TargetDetectLabel(
    val index: Int,
    val id: String,
    val name: String,
    var isChecked: Boolean
)
