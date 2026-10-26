package com.arekb.aqua.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.arekb.aqua.domain.model.WaterIntake
import com.arekb.aqua.ui.model.UserMessage

@Composable
fun UserMessageEffect(
    message: UserMessage?,
    snackbarHostState: SnackbarHostState,
    onUndo: (WaterIntake) -> Unit,
    onMessageShown: (Long) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(message?.id, snackbarHostState, lifecycleOwner) {
        val currentMessage = message ?: return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            val result = snackbarHostState.showSnackbar(
                message = currentMessage.text,
                actionLabel = if (currentMessage.undoIntake != null) "Undo" else null,
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                currentMessage.undoIntake?.let(onUndo)
            }
            onMessageShown(currentMessage.id)
        }
    }
}
