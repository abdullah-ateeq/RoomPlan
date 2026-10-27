package com.example.roomplan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomplan.model.PlanSettings
import com.example.roomplan.model.UnitType
import com.example.roomplan.ui.theme.AccentRose
import com.example.roomplan.ui.theme.BlueprintMuted
import com.example.roomplan.ui.theme.PrimaryBlue
import com.example.roomplan.ui.viewmodel.RoomPlanViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: RoomPlanViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val unit = uiState.unit
    val snackbarHostState = remember { SnackbarHostState() }

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // APPEARANCE
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APPEARANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Theme Mode", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PlanSettings.ThemeMode.values().forEach { mode ->
                            val sel = settings.themeMode == mode
                            if (sel) {
                                Button(
                                    onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(mode.name, fontSize = 11.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.updateSettings(settings.copy(themeMode = mode)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(mode.name, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Material Dynamic Color",
                        subtitle = "Use system wallpaper theme on supported Android versions",
                        checked = settings.dynamicColor,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(dynamicColor = it)) }
                    )
                }
            }

            // PLAN DISPLAY SETTINGS
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PLAN DISPLAY & CANVAS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingSwitchRow(
                        title = "Show Grid",
                        subtitle = "Display measurement grid inside the room",
                        checked = settings.showGrid,
                        onCheckedChange = { viewModel.toggleGrid(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Grid Spacing Interval", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(100.0, 250.0, 500.0, 1000.0).forEach { mm ->
                            val sel = settings.gridSpacingMm == mm
                            val label = unit.formatPlain(mm)
                            if (sel) {
                                Button(
                                    onClick = { viewModel.setGridSpacing(mm) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 11.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.setGridSpacing(mm) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(label, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Snap to Grid",
                        subtitle = "Snap furniture positions to active grid interval",
                        checked = settings.snapToGrid,
                        onCheckedChange = { viewModel.toggleSnapToGrid(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Show Dimension Lines",
                        subtitle = "Display architectural dimension lines with unit labels",
                        checked = settings.showDimensions,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showDimensions = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Show Furniture Labels",
                        subtitle = "Draw names and dimensions on furniture blocks",
                        checked = settings.showFurnitureLabels,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showFurnitureLabels = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Show Clearance Zones",
                        subtitle = "Display dashed clearance boundaries around selected furniture",
                        checked = settings.showClearance,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showClearance = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Show Wall Thickness",
                        subtitle = "Render double-line wall thickness on perimeter",
                        checked = settings.showWallThickness,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showWallThickness = it)) }
                    )
                }
            }

            // BEHAVIOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BEHAVIOR & APPLICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingSwitchRow(
                        title = "Live Calculations",
                        subtitle = "Instantly recalculate area and perimeter on input",
                        checked = settings.liveCalculations,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(liveCalculations = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Restore Last Plan",
                        subtitle = "Automatically load the last active plan upon launch",
                        checked = settings.restoreLastPlan,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(restoreLastPlan = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    SettingSwitchRow(
                        title = "Confirm Deletions",
                        subtitle = "Show confirmation dialog before deleting saved plans",
                        checked = settings.confirmDelete,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                    )
                }
            }

            // RESET BUTTON
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                Text("Reset Settings to Default")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- RESET CONFIRMATION DIALOG ---
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = {
                Text("This will restore default settings. Your saved plans will NOT be deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetSettings()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 11.sp, color = BlueprintMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
