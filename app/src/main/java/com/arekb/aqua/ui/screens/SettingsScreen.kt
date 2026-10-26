package com.arekb.aqua.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.arekb.aqua.domain.model.HydrationSettings

@Composable
fun SettingsScreen(
    settings: HydrationSettings,
    modifier: Modifier,
    onSave: (HydrationSettings) -> Unit,
    onReset: () -> Unit
) {
    var goalText by remember(settings.dailyGoalMl) { mutableStateOf(settings.dailyGoalMl.toString()) }
    var presetTexts by remember(settings.presetsMl) { mutableStateOf(settings.presetsMl.map { it.toString() }) }
    val goal = goalText.toIntOrNull()
    val presets = presetTexts.mapNotNull(String::toIntOrNull)
    val valid = goal != null && goal > 0 && presets.size == 4 && presets.all { it > 0 }

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(24.dp))
        Text("Make it yours", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Set a goal that fits your day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        SettingsCard("Daily goal", "Your target for each day") {
            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it.filter(Char::isDigit).take(5) },
                modifier = Modifier.fillMaxWidth(), label = { Text("Daily target") }, suffix = { Text("ml") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Spacer(Modifier.height(14.dp))
        SettingsCard("Quick add", "Choose four amounts for your buttons") {
            presetTexts.forEachIndexed { index, value ->
                OutlinedTextField(
                    value = value,
                    onValueChange = { updated -> presetTexts = presetTexts.toMutableList().also { it[index] = updated.filter(Char::isDigit).take(5) } },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 9.dp),
                    label = { Text("Preset ${index + 1}") }, suffix = { Text("ml") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = {
                onSave(HydrationSettings(dailyGoalMl = goal!!, presetsMl = presets))
            },
            enabled = valid, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(20.dp)
        ) { Text("Save changes") }
        Spacer(Modifier.height(24.dp))
        SettingsCard("A fresh start", "Remove today’s entries and keep your past days") {
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Text("Reset today’s total")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

