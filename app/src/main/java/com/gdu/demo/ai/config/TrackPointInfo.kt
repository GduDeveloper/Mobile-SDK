package com.gdu.ai.flight.p.smarttrack.config

/**
 * @Author: fuchi
 * @Date : 2026/7/22 - 13:49
 * @Desc :
 */
data class TrackPointInfo(
    /**
     * 0: updateUi
     * 1: updateUiWithRed
     * 2: updateTargetLoseUi
     */
    val type: Int,
    val pointX: Short,
    val pointY: Short,
    val width: Short,
    val height: Short
)