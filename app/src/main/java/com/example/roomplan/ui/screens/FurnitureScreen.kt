package com.example.roomplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomplan.model.CollisionWarning
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.FurnitureType
import com.example.roomplan.model.UnitType
import com.example.roomplan.ui.theme.AccentAmber
import com.example.roomplan.ui.theme.AccentEmerald
import com.example.roomplan.ui.theme.AccentRose
import com.example.roomplan.ui.theme.BlueprintMuted
import com.example.roomplan.ui.theme.BlueprintNavy
import com.example.roomplan.ui.theme.BlueprintSlate
import com.example.roomplan.ui.theme.PrimaryBlue
import com.example.roomplan.ui.viewmodel.RoomPlanViewModel
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FurnitureScreen(
    viewModel: RoomPlanViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlan: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.infoMessage, uiState.errorMessage) {
        uiState.infoMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearInfoMessage()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<FurnitureItem?>(null) }
    var showClearanceDialog by remember { mutableStateOf(false) }

    val activeUnit = uiState.unit

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Furniture Layout",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${uiState.furniture.size} items in room",
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
                    IconButton(onClick = { showClearanceDialog = true }) {
                        Icon(Icons.Default.Chair, contentDescription = "Clearances")
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
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Furniture")
            }
        },
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.autoArrangeFurniture() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Auto Arrange")
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = onNavigateToPlan,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("View Plan")
                    }
                }
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
                Spacer(modifier = Modifier.height(8.dp))
                // Collision Warnings Banner
                if (uiState.warnings.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AccentRose.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRose)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Collision Alerts (${uiState.warnings.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AccentRose
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            uiState.warnings.take(3).forEach { w ->
                                Text(
                                    text = "• ${w.message}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (uiState.warnings.size > 3) {
                                Text(
                                    text = "+${uiState.warnings.size - 3} more alerts",
                                    fontSize = 10.sp,
                                    color = BlueprintMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Header with Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROOM FURNITURE ITEMS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = BlueprintMuted
                    )

                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("+ Add Furniture")
                    }
                }
            }

            if (uiState.furniture.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Chair, contentDescription = null, modifier = Modifier.padding(bottom = 8.dp), tint = BlueprintMuted)
                            Text("No furniture added to room.", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Choose from 23 standard presets (Bed, Sofa, Table, Wardrobe, etc.) or add a custom block.",
                                fontSize = 12.sp,
                                color = BlueprintMuted
                            )
                        }
                    }
                }
            }

            items(uiState.furniture, key = { it.id }) { item ->
                val isSelected = item.id == uiState.selectedFurnitureId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectFurniture(item.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = item.type.displayName,
                                    fontSize = 11.sp,
                                    color = BlueprintMuted
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { viewModel.rotateFurniture(item.id) }) {
                                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = PrimaryBlue)
                                }
                                IconButton(onClick = { viewModel.duplicateFurniture(item.id) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = AccentEmerald)
                                }
                                IconButton(onClick = { itemToEdit = item }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = BlueprintSlate)
                                }
                                IconButton(onClick = { viewModel.deleteFurniture(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Dimensions", fontSize = 10.sp, color = BlueprintMuted)
                                Text(
                                    "${activeUnit.formatPlain(item.widthMm)} × ${activeUnit.formatPlain(item.depthMm)}" +
                                            (item.heightMm?.let { " × ${activeUnit.formatPlain(it)}" } ?: "") + " ${activeUnit.symbol}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column {
                                Text("Coordinates (X, Y)", fontSize = 10.sp, color = BlueprintMuted)
                                Text(
                                    "(${activeUnit.formatPlain(item.xMm)}, ${activeUnit.formatPlain(item.yMm)})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rotation", fontSize = 10.sp, color = BlueprintMuted)
                                Text(
                                    "${item.rotationDegrees}°",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = PrimaryBlue
                                )
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

    // --- ADD FURNITURE DIALOG ---
    if (showAddDialog) {
        FurnitureFormDialog(
            title = "Add Furniture Item",
            unit = activeUnit,
            initialItem = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { newItem ->
                viewModel.addFurniture(newItem)
                showAddDialog = false
            }
        )
    }

    // --- EDIT FURNITURE DIALOG ---
    itemToEdit?.let { editing ->
        FurnitureFormDialog(
            title = "Edit Furniture: ${editing.name}",
            unit = activeUnit,
            initialItem = editing,
            onDismiss = { itemToEdit = null },
            onConfirm = { updatedItem ->
                viewModel.updateFurniture(updatedItem)
                itemToEdit = null
            }
        )
    }

    // --- CLEARANCE SETTINGS DIALOG ---
    if (showClearanceDialog) {
        var wallClearanceInput by remember { mutableStateOf(activeUnit.formatPlain(uiState.settings.wallClearanceMm)) }
        var walkClearanceInput by remember { mutableStateOf(activeUnit.formatPlain(uiState.settings.walkingClearanceMm)) }

        AlertDialog(
            onDismissRequest = { showClearanceDialog = false },
            title = { Text("Planning Clearance Zones") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Configure planning clearances for auto-layout and visual zones. Note: Planning values are not building-code requirements.",
                        fontSize = 12.sp,
                        color = BlueprintMuted
                    )
                    OutlinedTextField(
                        value = wallClearanceInput,
                        onValueChange = { wallClearanceInput = it },
                        label = { Text("Wall Clearance (${activeUnit.symbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = walkClearanceInput,
                        onValueChange = { walkClearanceInput = it },
                        label = { Text("Walking Clearance (${activeUnit.symbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wc = wallClearanceInput.toDoubleOrNull()?.let { activeUnit.toMm(it) } ?: uiState.settings.wallClearanceMm
                        val walk = walkClearanceInput.toDoubleOrNull()?.let { activeUnit.toMm(it) } ?: uiState.settings.walkingClearanceMm
                        viewModel.updateSettings(
                            uiState.settings.copy(
                                wallClearanceMm = wc,
                                walkingClearanceMm = walk
                            )
                        )
                        showClearanceDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearanceDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FurnitureFormDialog(
    title: String,
    unit: UnitType,
    initialItem: FurnitureItem?,
    onDismiss: () -> Unit,
    onConfirm: (FurnitureItem) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialItem?.type ?: FurnitureType.THREE_SEATER_SOFA) }
    var nameInput by remember { mutableStateOf(initialItem?.name ?: selectedType.displayName) }
    var widthInput by remember { mutableStateOf(unit.formatPlain(initialItem?.widthMm ?: selectedType.defaultWidthMm)) }
    var depthInput by remember { mutableStateOf(unit.formatPlain(initialItem?.depthMm ?: selectedType.defaultDepthMm)) }
    var heightInput by remember {
        mutableStateOf(
            initialItem?.heightMm?.let { unit.formatPlain(it) }
                ?: selectedType.defaultHeightMm?.let { unit.formatPlain(it) }
                ?: ""
        )
    }
    var xInput by remember { mutableStateOf(unit.formatPlain(initialItem?.xMm ?: 500.0)) }
    var yInput by remember { mutableStateOf(unit.formatPlain(initialItem?.yMm ?: 500.0)) }
    var rotation by remember { mutableStateOf(initialItem?.rotationDegrees ?: 0) }

    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Preset Dropdown
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Preset Type") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        FurnitureType.values().forEach { t ->
                            DropdownMenuItem(
                                text = { Text("${t.displayName} (${unit.formatPlain(t.defaultWidthMm)} × ${unit.formatPlain(t.defaultDepthMm)} ${unit.symbol})") },
                                onClick = {
                                    selectedType = t
                                    nameInput = t.displayName
                                    widthInput = unit.formatPlain(t.defaultWidthMm)
                                    depthInput = unit.formatPlain(t.defaultDepthMm)
                                    heightInput = t.defaultHeightMm?.let { unit.formatPlain(it) } ?: ""
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Name
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Name / Label") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dimensions
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = widthInput,
                        onValueChange = { widthInput = it },
                        label = { Text("Length (${unit.symbol})") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = depthInput,
                        onValueChange = { depthInput = it },
                        label = { Text("Depth (${unit.symbol})") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Height (${unit.symbol}) [Optional]") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Coordinates
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = xInput,
                        onValueChange = { xInput = it },
                        label = { Text("X Position (${unit.symbol})") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = yInput,
                        onValueChange = { yInput = it },
                        label = { Text("Y Position (${unit.symbol})") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // Rotation Buttons: 0°, 90°, 180°, 270°
                Text("Rotation:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0, 90, 180, 270).forEach { angle ->
                        val isSel = rotation == angle
                        if (isSel) {
                            Button(onClick = { rotation = angle }, modifier = Modifier.weight(1f)) {
                                Text("$angle°", fontSize = 11.sp)
                            }
                        } else {
                            OutlinedButton(onClick = { rotation = angle }, modifier = Modifier.weight(1f)) {
                                Text("$angle°", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wMm = widthInput.toDoubleOrNull()?.let { unit.toMm(it) } ?: selectedType.defaultWidthMm
                    val dMm = depthInput.toDoubleOrNull()?.let { unit.toMm(it) } ?: selectedType.defaultDepthMm
                    val hMm = heightInput.toDoubleOrNull()?.let { unit.toMm(it) }
                    val xMm = xInput.toDoubleOrNull()?.let { unit.toMm(it) } ?: 0.0
                    val yMm = yInput.toDoubleOrNull()?.let { unit.toMm(it) } ?: 0.0

                    val item = FurnitureItem(
                        id = initialItem?.id ?: UUID.randomUUID().toString(),
                        type = selectedType,
                        name = nameInput.trim().ifBlank { selectedType.displayName },
                        widthMm = wMm,
                        depthMm = dMm,
                        heightMm = hMm,
                        xMm = xMm,
                        yMm = yMm,
                        rotationDegrees = rotation
                    )
                    onConfirm(item)
                }
            ) {
                Text(if (initialItem != null) "Update" else "Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
