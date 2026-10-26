package com.arekb.aqua.ui.model

import com.arekb.aqua.domain.model.WaterIntake

data class UserMessage(
    val id: Long,
    val text: String,
    val undoIntake: WaterIntake? = null
)
