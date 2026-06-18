package com.gdu.demo.flight.setting.bean

/**
 * 目标识别算法模型
 */
data class TargetDetectModel(
    val id: Int,
    val name: String,
    val labels: MutableList<TargetDetectLabel>
)