package com.example.roomplan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomplan.engine.RoomCalculator
import com.example.roomplan.model.DimensionType
import com.example.roomplan.model.Door
import com.example.roomplan.model.DoorSwing
import com.example.roomplan.model.DoorType
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.UnitType
import com.example.roomplan.model.WallSide
import com.example.roomplan.model.Window
import com.example.roomplan.ui.theme.AccentAmber
import com.example.roomplan.ui.theme.AccentEmerald
import com.example.roomplan.ui.theme.AccentRose
import com.example.roomplan.ui.theme.AccentSky
import com.example.roomplan.ui.theme.BlueprintDark
import com.example.roomplan.ui.theme.BlueprintMuted
import com.example.roomplan.ui.theme.PrimaryBlue
import com.example.roomplan.ui.viewmodel.RoomPlanViewModel
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RoomPlanViewModel,
    onNavigateToPlan: () -> Unit,
    onNavigateToFurniture: () -> Unit,
    onNavigateToSavedPlans: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
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

    var showDoorDialog by remember { mutableStateOf(false) }
    var showWindowDialog by remember { mutableStateOf(false) }
    var showColumnDialog by remember { mutableStateOf(false) }

    val activeUnit = uiState.unit

    // Live display inputs formatted with active unit
    var lengthInput by remember(uiState.room.lengthMm, activeUnit) {
        mutableStateOf(activeUnit.formatPlain(uiState.room.lengthMm))
    }
    var widthInput by remember(uiState.room.widthMm, activeUnit) {
        mutableStateOf(activeUnit.formatPlain(uiState.room.widthMm))
    }
    var heightInput by remember(uiState.room.heightMm, activeUnit) {
        mutableStateOf(uiState.room.heightMm?.let { activeUnit.formatPlain(it) } ?: "")
    }
    var cutoutLengthInput by remember(uiState.room.cutoutLengthMm, activeUnit) {
        mutableStateOf(activeUnit.formatPlain(uiState.room.cutoutLengthMm))
    }
    var cutoutWidthInput by remember(uiState.room.cutoutWidthMm, activeUnit) {
        mutableStateOf(activeUnit.formatPlain(uiState.room.cutoutWidthMm))
    }
    var wallThicknessInput by remember(uiState.wallThicknessMm, activeUnit) {
        mutableStateOf(activeUnit.formatPlain(uiState.wallThicknessMm))
    }

    // Usable internal dimensions
    val internalRoom = RoomCalculator.getInternalDimensions(
        room = uiState.room,
        shape = uiState.roomShape,
        dimensionType = uiState.dimensionType,
        wallThicknessMm = uiState.wallThicknessMm
    )

    val floorAreaMm2 = RoomCalculator.calculateArea(internalRoom, uiState.roomShape)
    val floorAreaM2 = RoomCalculator.mm2ToM2(floorAreaMm2)
    val floorAreaFt2 = RoomCalculator.mm2ToFt2(floorAreaMm2)
    val perimeterMm = RoomCalculator.calculatePerimeter(internalRoom, uiState.roomShape)
    val perimeterM = perimeterMm / 1000.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RoomPlan",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Calculate dimensions & generate proportional layout",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSavedPlans) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Saved Plans")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(Icons.Default.Info, contentDescription = "About")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Primary Action Navigation Bar
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
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onNavigateToFurniture,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Chair, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Furniture (${uiState.furniture.size})")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onNavigateToPlan,
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("View Plan")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PLAN NAME & SAVE
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.planName,
                    onValueChange = { viewModel.setPlanName(it) },
                    label = { Text("Room Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = { viewModel.saveCurrentPlan() },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                    Text("Save")
                }
            }

            // UNIT SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("UNIT SYSTEM", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = BlueprintMuted)
                        Text(activeUnit.displayName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded }
                    ) {
                        OutlinedButton(
                            onClick = { unitExpanded = true },
                            modifier = Modifier.menuAnchor()
                        ) {
                            Text(activeUnit.symbol.uppercase(Locale.US))
                        }
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            UnitType.values().forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.displayName} (${u.symbol})") },
                                    onClick = {
                                        viewModel.setUnit(u)
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ROOM SHAPE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ROOM SHAPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = BlueprintMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoomShape.values().forEach { shape ->
                            val isSelected = uiState.roomShape == shape
                            if (isSelected) {
                                Button(
                                    onClick = { viewModel.setRoomShape(shape) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Text(shape.displayName, fontSize = 12.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { viewModel.setRoomShape(shape) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(shape.displayName, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ROOM DIMENSIONS INPUTS
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ROOM DIMENSIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Length
                        OutlinedTextField(
                            value = lengthInput,
                            onValueChange = { newVal ->
                                lengthInput = newVal
                                newVal.toDoubleOrNull()?.let {
                                    val mm = activeUnit.toMm(it)
                                    viewModel.setDimensions(
                                        lengthMm = mm,
                                        widthMm = if (uiState.roomShape == RoomShape.SQUARE) mm else uiState.room.widthMm,
                                        heightMm = uiState.room.heightMm
                                    )
                                }
                            },
                            label = { Text("Length (${activeUnit.symbol})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        // Width
                        OutlinedTextField(
                            value = if (uiState.roomShape == RoomShape.SQUARE) lengthInput else widthInput,
                            onValueChange = { newVal ->
                                widthInput = newVal
                                newVal.toDoubleOrNull()?.let {
                                    val mm = activeUnit.toMm(it)
                                    viewModel.setDimensions(
                                        lengthMm = uiState.room.lengthMm,
                                        widthMm = mm,
                                        heightMm = uiState.room.heightMm
                                    )
                                }
                            },
                            label = { Text("Width (${activeUnit.symbol})") },
                            enabled = uiState.roomShape != RoomShape.SQUARE,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Height (Optional)
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { newVal ->
                            heightInput = newVal
                            val hMm = newVal.toDoubleOrNull()?.let { activeUnit.toMm(it) }
                            viewModel.setDimensions(
                                lengthMm = uiState.room.lengthMm,
                                widthMm = uiState.room.widthMm,
                                heightMm = hMm
                            )
                        },
                        label = { Text("Ceiling Height (${activeUnit.symbol}) [Optional]") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    // L-Shape Cutout inputs if L_SHAPE
                    AnimatedVisibility(visible = uiState.roomShape == RoomShape.L_SHAPE) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("CORNER CUTOUT (TOP-RIGHT)", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = BlueprintMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = cutoutLengthInput,
                                    onValueChange = { newVal ->
                                        cutoutLengthInput = newVal
                                        newVal.toDoubleOrNull()?.let {
                                            viewModel.setCutoutDimensions(
                                                activeUnit.toMm(it),
                                                uiState.room.cutoutWidthMm
                                            )
                                        }
                                    },
                                    label = { Text("Cutout Length") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = cutoutWidthInput,
                                    onValueChange = { newVal ->
                                        cutoutWidthInput = newVal
                                        newVal.toDoubleOrNull()?.let {
                                            viewModel.setCutoutDimensions(
                                                uiState.room.cutoutLengthMm,
                                                activeUnit.toMm(it)
                                            )
                                        }
                                    },
                                    label = { Text("Cutout Width") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }

            // LIVE CALCULATED AREA & PERIMETER DISPLAY
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BlueprintDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("ROOM AREA", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                            Text(
                                text = String.format(Locale.US, "%.2f m²", floorAreaM2),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text(
                                text = String.format(Locale.US, "%.2f ft²", floorAreaFt2),
                                fontSize = 13.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("PERIMETER", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color(0xFF94A3B8))
                            Text(
                                text = String.format(Locale.US, "%.2f m", perimeterM),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f ft", perimeterM * 3.28084),
                                fontSize = 13.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    // Wall surface area if height is specified
                    uiState.room.heightMm?.let { hMm ->
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF334155))
                        Spacer(modifier = Modifier.height(8.dp))
                        val grossWallAreaM2 = RoomCalculator.mm2ToM2(
                            RoomCalculator.calculateWallArea(internalRoom, hMm, uiState.roomShape)
                        )
                        val netWallAreaM2 = RoomCalculator.mm2ToM2(
                            RoomCalculator.calculateNetWallArea(internalRoom, hMm, uiState.doors, uiState.windows, uiState.roomShape)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = String.format(Locale.US, "Gross Wall: %.1f m²", grossWallAreaM2),
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = String.format(Locale.US, "Net Wall (excl. openings): %.1f m²", netWallAreaM2),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }
            }

            // WALL THICKNESS & DIMENSION TYPE
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WALL SPECIFICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = BlueprintMuted)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = wallThicknessInput,
                            onValueChange = { newVal ->
                                wallThicknessInput = newVal
                                newVal.toDoubleOrNull()?.let {
                                    viewModel.setWallThickness(activeUnit.toMm(it))
                                }
                            },
                            label = { Text("Wall Thickness (${activeUnit.symbol})") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show Wall", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = uiState.settings.showWallThickness,
                                onCheckedChange = {
                                    viewModel.updateSettings(uiState.settings.copy(showWallThickness = it))
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("DIMENSIONS REFER TO", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = BlueprintMuted)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = uiState.dimensionType == DimensionType.INSIDE,
                                onClick = { viewModel.setDimensionType(DimensionType.INSIDE) }
                            )
                            Text("Inside wall-to-wall", fontSize = 13.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            RadioButton(
                                selected = uiState.dimensionType == DimensionType.OUTSIDE,
                                onClick = { viewModel.setDimensionType(DimensionType.OUTSIDE) }
                            )
                            Text("Outside wall-to-wall", fontSize = 13.sp)
                        }
                    }
                }
            }

            // ROOM FEATURES (DOORS, WINDOWS, COLUMNS)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ROOM FEATURES", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OutlinedButton(
                                onClick = { showDoorDialog = true },
                                enabled = uiState.doors.size < 4
                            ) {
                                Text("+ Door")
                            }
                            OutlinedButton(
                                onClick = { showWindowDialog = true },
                                enabled = uiState.windows.size < 8
                            ) {
                                Text("+ Window")
                            }
                            OutlinedButton(
                                onClick = { showColumnDialog = true },
                                enabled = uiState.columns.size < 10
                            ) {
                                Text("+ Column")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Doors list
                    if (uiState.doors.isNotEmpty()) {
                        Text("DOORS (${uiState.doors.size}/4)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AccentEmerald)
                        uiState.doors.forEach { door ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Door: ${activeUnit.format(door.widthMm)} on ${door.wall.displayName}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Offset: ${activeUnit.format(door.offsetMm)} | Swing: ${door.swing.displayName}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteDoor(door.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Door", tint = AccentRose)
                                }
                            }
                        }
                    }

                    // Windows list
                    if (uiState.windows.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("WINDOWS (${uiState.windows.size}/8)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = AccentSky)
                        uiState.windows.forEach { win ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Window: ${activeUnit.format(win.widthMm)} on ${win.wall.displayName}",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Offset: ${activeUnit.format(win.offsetMm)}" + (win.heightMm?.let { " | H: ${activeUnit.format(it)}" } ?: ""),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteWindow(win.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Window", tint = AccentRose)
                                }
                            }
                        }
                    }

                    // Columns list
                    if (uiState.columns.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("COLUMNS (${uiState.columns.size}/10)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueprintMuted)
                        uiState.columns.forEach { col ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Column: ${activeUnit.format(col.widthMm)} × ${activeUnit.format(col.depthMm)} at (${activeUnit.format(col.xMm)}, ${activeUnit.format(col.yMm)})",
                                    fontSize = 12.sp
                                )
                                IconButton(onClick = { viewModel.deleteColumn(col.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Column", tint = AccentRose)
                                }
                            }
                        }
                    }

                    if (uiState.doors.isEmpty() && uiState.windows.isEmpty() && uiState.columns.isEmpty()) {
                        Text(
                            "No doors, windows, or columns added yet. Tap above to add features.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // QUICK AUTO ARRANGE BUTTON
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AUTOMATIC LAYOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                        Text("Arrange furniture automatically using deterministic placement rules.", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.autoArrangeFurniture() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("Auto Arrange")
                    }
                }
            }

            // NOTES
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = { viewModel.setNotes(it) },
                label = { Text("Room Notes & Specifications") },
                placeholder = { Text("e.g. Master bedroom layout with king bed and wardrobe...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // --- ADD DOOR DIALOG ---
    if (showDoorDialog) {
        var doorWidthInput by remember { mutableStateOf(activeUnit.formatPlain(900.0)) }
        var doorOffsetInput by remember { mutableStateOf(activeUnit.formatPlain(500.0)) }
        var selectedWall by remember { mutableStateOf(WallSide.BOTTOM) }
        var selectedSwing by remember { mutableStateOf(DoorSwing.INWARD) }
        var doorError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showDoorDialog = false },
            title = { Text("Add Door") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = doorWidthInput,
                        onValueChange = { doorWidthInput = it },
                        label = { Text("Width (${activeUnit.symbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = doorOffsetInput,
                        onValueChange = { doorOffsetInput = it },
                        label = { Text("Distance from Corner (${activeUnit.symbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Text("Wall:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        WallSide.values().forEach { w ->
                            val sel = selectedWall == w
                            if (sel) {
                                Button(onClick = { selectedWall = w }) { Text(w.name, fontSize = 11.sp) }
                            } else {
                                OutlinedButton(onClick = { selectedWall = w }) { Text(w.name, fontSize = 11.sp) }
                            }
                        }
                    }

                    Text("Swing:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DoorSwing.values().forEach { s ->
                            val sel = selectedSwing == s
                            if (sel) {
                                Button(onClick = { selectedSwing = s }) { Text(s.name, fontSize = 11.sp) }
                            } else {
                                OutlinedButton(onClick = { selectedSwing = s }) { Text(s.name, fontSize = 11.sp) }
                            }
                        }
                    }

                    doorError?.let {
                        Text(it, color = AccentRose, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wVal = doorWidthInput.toDoubleOrNull()
                        val oVal = doorOffsetInput.toDoubleOrNull()
                        if (wVal != null && oVal != null) {
                            val wMm = activeUnit.toMm(wVal)
                            val oMm = activeUnit.toMm(oVal)
                            val wallLen = when (selectedWall) {
                                WallSide.TOP, WallSide.BOTTOM -> uiState.room.lengthMm
                                WallSide.LEFT, WallSide.RIGHT -> uiState.room.widthMm
                            }
                            if (oMm + wMm > wallLen) {
                                doorError = "Door does not fit at this position (${activeUnit.format(oMm + wMm)} > ${activeUnit.format(wallLen)})."
                            } else {
                                viewModel.addDoor(
                                    Door(
                                        id = UUID.randomUUID().toString(),
                                        widthMm = wMm,
                                        wall = selectedWall,
                                        offsetMm = oMm,
                                        swing = selectedSwing
                                    )
                                )
                                showDoorDialog = false
                            }
                        }
                    }
                ) {
                    Text("Add Door")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDoorDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- ADD WINDOW DIALOG ---
    if (showWindowDialog) {
        var winWidthInput by remember { mutableStateOf(activeUnit.formatPlain(1500.0)) }
        var winOffsetInput by remember { mutableStateOf(activeUnit.formatPlain(1000.0)) }
        var winHeightInput by remember { mutableStateOf(activeUnit.formatPlain(1200.0)) }
        var winSillInput by remember { mutableStateOf(activeUnit.formatPlain(900.0)) }
        var selectedWall by remember { mutableStateOf(WallSide.TOP) }
        var winError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showWindowDialog = false },
            title = { Text("Add Window") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = winWidthInput,
                        onValueChange = { winWidthInput = it },
                        label = { Text("Width (${activeUnit.symbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = winOffsetInput,
                        onValueChange = { winOffsetInput = it },
                        label = { Text("Distance from Corner (${activeUnit.symbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = winHeightInput,
                        onValueChange = { winHeightInput = it },
                        label = { Text("Window Height (${activeUnit.symbol})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    Text("Wall:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        WallSide.values().forEach { w ->
                            val sel = selectedWall == w
                            if (sel) {
                                Button(onClick = { selectedWall = w }) { Text(w.name, fontSize = 11.sp) }
                            } else {
                                OutlinedButton(onClick = { selectedWall = w }) { Text(w.name, fontSize = 11.sp) }
                            }
                        }
                    }

                    winError?.let {
                        Text(it, color = AccentRose, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wVal = winWidthInput.toDoubleOrNull()
                        val oVal = winOffsetInput.toDoubleOrNull()
                        val hVal = winHeightInput.toDoubleOrNull()
                        if (wVal != null && oVal != null) {
                            val wMm = activeUnit.toMm(wVal)
                            val oMm = activeUnit.toMm(oVal)
                            val wallLen = when (selectedWall) {
                                WallSide.TOP, WallSide.BOTTOM -> uiState.room.lengthMm
                                WallSide.LEFT, WallSide.RIGHT -> uiState.room.widthMm
                            }
                            if (oMm + wMm > wallLen) {
                                winError = "Window does not fit at this position (${activeUnit.format(oMm + wMm)} > ${activeUnit.format(wallLen)})."
                            } else {
                                viewModel.addWindow(
                                    Window(
                                        id = UUID.randomUUID().toString(),
                                        widthMm = wMm,
                                        wall = selectedWall,
                                        offsetMm = oMm,
                                        heightMm = hVal?.let { activeUnit.toMm(it) }
                                    )
                                )
                                showWindowDialog = false
                            }
                        }
                    }
                ) {
                    Text("Add Window")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWindowDialog = false }) { Text("Cancel") }
            }
        )
    }

    // --- ADD COLUMN DIALOG ---
    if (showColumnDialog) {
        var colWInput by remember { mutableStateOf(activeUnit.formatPlain(300.0)) }
        var colDInput by remember { mutableStateOf(activeUnit.formatPlain(300.0)) }
        var colXInput by remember { mutableStateOf(activeUnit.formatPlain(1000.0)) }
        var colYInput by remember { mutableStateOf(activeUnit.formatPlain(1000.0)) }

        AlertDialog(
            onDismissRequest = { showColumnDialog = false },
            title = { Text("Add Structural Column") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = colWInput,
                            onValueChange = { colWInput = it },
                            label = { Text("Width (${activeUnit.symbol})") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = colDInput,
                            onValueChange = { colDInput = it },
                            label = { Text("Depth (${activeUnit.symbol})") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = colXInput,
                            onValueChange = { colXInput = it },
                            label = { Text("X Position (${activeUnit.symbol})") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = colYInput,
                            onValueChange = { colYInput = it },
                            label = { Text("Y Position (${activeUnit.symbol})") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = colWInput.toDoubleOrNull()?.let { activeUnit.toMm(it) } ?: 300.0
                        val d = colDInput.toDoubleOrNull()?.let { activeUnit.toMm(it) } ?: 300.0
                        val x = colXInput.toDoubleOrNull()?.let { activeUnit.toMm(it) } ?: 0.0
                        val y = colYInput.toDoubleOrNull()?.let { activeUnit.toMm(it) } ?: 0.0
                        viewModel.addColumn(
                            com.example.roomplan.model.Column(
                                id = UUID.randomUUID().toString(),
                                widthMm = w,
                                depthMm = d,
                                xMm = x,
                                yMm = y
                            )
                        )
                        showColumnDialog = false
                    }
                ) {
                    Text("Add Column")
                }
            },
            dismissButton = {
                TextButton(onClick = { showColumnDialog = false }) { Text("Cancel") }
            }
        )
    }
}
