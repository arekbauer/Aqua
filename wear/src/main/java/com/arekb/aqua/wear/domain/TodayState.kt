package com.arekb.aqua.wear.domain

import com.arekb.aqua.domain.model.LiquidUnit

data class TodayState(
    val totalMl: Int,
    val goalMl: Int,
    val presetsMl: List<Int>,
    val liquidUnit: LiquidUnit,
    val isSyncNeeded: Boolean
)
