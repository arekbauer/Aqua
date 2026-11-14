package com.arekb.aqua.wear.ui

import androidx.compose.runtime.Composable
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.wear.domain.TodayState
import com.arekb.aqua.wear.ui.theme.AquaWearTheme

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun TodayScreenPreview() {
    AquaWearTheme {
        TodayScreenContent(
            state = TodayState(
                totalMl = 400,
                goalMl = 2000,
                presetsMl = listOf(200, 350, 500),
                liquidUnit = LiquidUnit.ML,
                isSyncNeeded = false
            ),
            onLogDrink = {}
        )
    }
}
