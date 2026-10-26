package com.arekb.aqua.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.arekb.aqua.R

/** Display face: slightly condensed proportions for bold, compact dashboard metrics. */
@OptIn(ExperimentalTextApi::class)
val AquaDisplayFontFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.width(88f))
    )
)

/** Reading face: standard width keeps paragraphs and longer settings labels comfortable. */
@OptIn(ExperimentalTextApi::class)
val AquaBodyFontFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.width(100f))
    )
)

/** Label face: a modestly condensed cut gives compact controls a distinct voice. */
@OptIn(ExperimentalTextApi::class)
val AquaLabelFontFamily = FontFamily(
    Font(
        resId = R.font.google_sans_flex,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.width(92f))
    )
)

private fun typeStyle(
    family: FontFamily,
    weight: FontWeight,
    size: Int,
    lineHeight: Int,
    letterSpacing: Float = 0f
) = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp
)

val Typography = Typography(
    displayLarge = typeStyle(AquaDisplayFontFamily, FontWeight.ExtraBold, 57, 64, -0.5f),
    displayMedium = typeStyle(AquaDisplayFontFamily, FontWeight.ExtraBold, 45, 52, -0.25f),
    displaySmall = typeStyle(AquaDisplayFontFamily, FontWeight.Bold, 36, 44),
    headlineLarge = typeStyle(AquaDisplayFontFamily, FontWeight.Bold, 32, 40),
    headlineMedium = typeStyle(AquaDisplayFontFamily, FontWeight.Bold, 28, 36),
    headlineSmall = typeStyle(AquaDisplayFontFamily, FontWeight.SemiBold, 24, 32),
    titleLarge = typeStyle(AquaBodyFontFamily, FontWeight.SemiBold, 22, 28),
    titleMedium = typeStyle(AquaBodyFontFamily, FontWeight.Medium, 16, 24, 0.1f),
    titleSmall = typeStyle(AquaBodyFontFamily, FontWeight.Medium, 14, 20, 0.1f),
    bodyLarge = typeStyle(AquaBodyFontFamily, FontWeight.Normal, 16, 24, 0.15f),
    bodyMedium = typeStyle(AquaBodyFontFamily, FontWeight.Normal, 14, 20, 0.15f),
    bodySmall = typeStyle(AquaBodyFontFamily, FontWeight.Normal, 12, 16, 0.2f),
    labelLarge = typeStyle(AquaLabelFontFamily, FontWeight.Medium, 14, 20, 0.1f),
    labelMedium = typeStyle(AquaLabelFontFamily, FontWeight.Medium, 12, 16, 0.3f),
    labelSmall = typeStyle(AquaLabelFontFamily, FontWeight.Medium, 11, 16, 0.3f)
)

