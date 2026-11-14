package com.arekb.aqua.wear.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ConfirmationDialog
import androidx.wear.compose.material3.ConfirmationDialogDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.arekb.aqua.domain.model.LiquidUnit
import com.arekb.aqua.wear.domain.TodayState

@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    TodayScreenContent(
        state = state,
        onLogDrink = { amountMl ->
            viewModel.logDrink(amountMl)
        }
    )
}

@Composable
fun TodayScreenContent(
    state: TodayState?,
    onLogDrink: (Int) -> Unit
) {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    var showConfirmation by remember { mutableStateOf(false) }

    ScreenScaffold(
        scrollState = columnState
    ) { contentPadding ->
        if (state == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { if (state.goalMl > 0) state.totalMl.toFloat() / state.goalMl else 0f },
                            modifier = Modifier.size(120.dp),
                            strokeWidth = 8.dp
                        )
                        val totalDisplay = if (state.liquidUnit == LiquidUnit.OZ) {
                            (state.totalMl / 29.5735f).toInt()
                        } else {
                            state.totalMl
                        }
                        Text(
                            text = "$totalDisplay ${state.liquidUnit.name}",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (state.isSyncNeeded) {
                    item {
                        ListHeader(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, transformationSpec),
                            transformation = SurfaceTransformation(transformationSpec)
                        ) {
                            Text(text = "Sync with phone")
                        }
                    }
                }

                items(state.presetsMl.size) { index ->
                    val ml = state.presetsMl[index]
                    val displayVal = if (state.liquidUnit == LiquidUnit.OZ) {
                        (ml / 29.5735f).toInt()
                    } else {
                        ml
                    }
                    val view = LocalView.current

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .minimumVerticalContentPadding(ButtonDefaults.minimumVerticalListContentPadding),
                        transformation = SurfaceTransformation(transformationSpec),
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.CONFIRM)
                            onLogDrink(ml)
                            showConfirmation = true
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add"
                            )
                        }
                    ) {
                        Text(text = "+$displayVal ${state.liquidUnit.name}")
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        visible = showConfirmation,
        onDismissRequest = { showConfirmation = false },
        text = { Text("Logged!") }
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Success",
            modifier = Modifier.size(ConfirmationDialogDefaults.IconSize)
        )
    }
}
