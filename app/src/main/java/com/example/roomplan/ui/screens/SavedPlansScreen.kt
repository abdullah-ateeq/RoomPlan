package com.example.roomplan.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomplan.data.ExportManager
import com.example.roomplan.engine.RoomCalculator
import com.example.roomplan.model.RoomPlan
import com.example.roomplan.ui.theme.AccentEmerald
import com.example.roomplan.ui.theme.AccentRose
import com.example.roomplan.ui.theme.BlueprintMuted
import com.example.roomplan.ui.theme.PrimaryBlue
import com.example.roomplan.ui.viewmodel.RoomPlanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPlansScreen(
    viewModel: RoomPlanViewModel,
    onNavigateBack: () -> Unit,
    onPlanOpened: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var planToRename by remember { mutableStateOf<RoomPlan?>(null) }
    var planToDelete by remember { mutableStateOf<RoomPlan?>(null) }
    var planToExport by remember { mutableStateOf<RoomPlan?>(null) }

    // Export contracts
    val savePngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        val plan = planToExport
        if (uri != null && plan != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = ExportManager.renderPlanToBitmap(plan)
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                    bitmap.recycle()
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Exported '${plan.name}' as PNG.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Error exporting PNG: ${e.message}")
                    }
                }
            }
        }
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        val plan = planToExport
        if (uri != null && plan != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        ExportManager.exportToPdf(plan, out)
                    }
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Exported '${plan.name}' as PDF.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Error exporting PDF: ${e.message}")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Saved Plans",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${uiState.savedPlans.size} of 20 plans stored",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.startNewPlan()
                        onPlanOpened()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("New Plan")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.startNewPlan()
                    onPlanOpened()
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Plan")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState.savedPlans.size >= 20) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AccentRose.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "Maximum storage limit of 20 plans reached. Please delete an older plan to save new ones.",
                            fontSize = 12.sp,
                            color = AccentRose,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            if (uiState.savedPlans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.padding(bottom = 12.dp), tint = BlueprintMuted)
                            Text("No saved plans yet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Design a room layout and tap Save to store up to 20 plans completely offline.",
                                fontSize = 12.sp,
                                color = BlueprintMuted
                            )
                        }
                    }
                }
            }

            items(uiState.savedPlans, key = { it.id }) { plan ->
                val internalRoom = RoomCalculator.getInternalDimensions(
                    room = plan.room,
                    shape = plan.shape,
                    dimensionType = plan.dimensionType,
                    wallThicknessMm = plan.wallThicknessMm
                )
                val areaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(internalRoom, plan.shape))
                val isCurrentPlan = plan.id == uiState.currentPlanId

                val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(plan.updatedAt))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrentPlan)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = plan.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    if (isCurrentPlan) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ACTIVE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Text(
                                    text = "${plan.unit.format(plan.room.lengthMm)} × ${plan.unit.format(plan.room.widthMm)} • ${plan.shape.displayName}",
                                    fontSize = 12.sp,
                                    color = BlueprintMuted
                                )
                            }

                            // Actions Dropdown Menu
                            var menuExpanded by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Open Plan") },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.loadPlan(plan)
                                            onPlanOpened()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Duplicate") },
                                        onClick = {
                                            menuExpanded = false
                                            viewModel.duplicateSavedPlan(plan.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Rename") },
                                        onClick = {
                                            menuExpanded = false
                                            planToRename = plan
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Export as PNG") },
                                        onClick = {
                                            menuExpanded = false
                                            planToExport = plan
                                            savePngLauncher.launch("${plan.name.replace(" ", "_")}.png")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Export as PDF") },
                                        onClick = {
                                            menuExpanded = false
                                            planToExport = plan
                                            savePdfLauncher.launch("${plan.name.replace(" ", "_")}.pdf")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = AccentRose) },
                                        onClick = {
                                            menuExpanded = false
                                            planToDelete = plan
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = String.format(Locale.US, "Area: %.2f m²", areaM2),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${plan.furniture.size} furniture items",
                                fontSize = 12.sp,
                                color = BlueprintMuted
                            )
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = BlueprintMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.loadPlan(plan)
                                    onPlanOpened()
                                }
                            ) {
                                Text("Open Plan")
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // --- RENAME DIALOG ---
    planToRename?.let { plan ->
        var renameText by remember { mutableStateOf(plan.name) }
        AlertDialog(
            onDismissRequest = { planToRename = null },
            title = { Text("Rename Plan") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("New Plan Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameSavedPlan(plan.id, renameText.trim())
                        }
                        planToRename = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { planToRename = null }) { Text("Cancel") }
            }
        )
    }

    // --- DELETE CONFIRMATION DIALOG ---
    planToDelete?.let { plan ->
        AlertDialog(
            onDismissRequest = { planToDelete = null },
            title = { Text("Delete '${plan.name}'?") },
            text = {
                Text("Are you sure you want to delete this saved floor plan? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSavedPlan(plan.id)
                        planToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRose)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { planToDelete = null }) { Text("Cancel") }
            }
        )
    }
}
