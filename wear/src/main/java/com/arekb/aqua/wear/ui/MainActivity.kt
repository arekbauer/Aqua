package com.arekb.aqua.wear.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.wear.compose.foundation.rememberAmbientModeManager
import androidx.wear.compose.material3.AppScaffold
import com.arekb.aqua.wear.ui.theme.AquaWearTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AquaWearTheme {
                val ambientManager = rememberAmbientModeManager()
                CompositionLocalProvider(
                    androidx.wear.compose.foundation.LocalAmbientModeManager provides ambientManager
                ) {
                    AppScaffold {
                        TodayScreen()
                    }
                }
            }
        }
    }
}
