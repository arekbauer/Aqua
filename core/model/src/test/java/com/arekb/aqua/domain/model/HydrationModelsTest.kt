package com.arekb.aqua.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class HydrationModelsTest {
    @Test
    fun millilitreValuesAreUnchangedInMetricUnit() {
        assertEquals(350, 350.toUserUnit(LiquidUnit.ML))
        assertEquals(350, 350.toSystemMl(LiquidUnit.ML))
    }

    @Test
    fun conversionToOuncesRoundsToNearestWholeNumber() {
        assertEquals(12, 350.toUserUnit(LiquidUnit.OZ))
        assertEquals(17, 500.toUserUnit(LiquidUnit.OZ))
    }

    @Test
    fun conversionFromOuncesRoundsToNearestMillilitre() {
        assertEquals(237, 8.toSystemMl(LiquidUnit.OZ))
    }

    @Test
    fun unitLabelsMatchTheUnit() {
        assertEquals("ml", LiquidUnit.ML.label())
        assertEquals("oz", LiquidUnit.OZ.label())
    }

    @Test
    fun defaultSettingsExposeExactlyThreePresets() {
        assertEquals(listOf(200, 350, 500), HydrationSettings().presetsMl)
    }
}
