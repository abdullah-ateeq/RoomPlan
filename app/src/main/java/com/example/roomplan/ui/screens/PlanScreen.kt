package com.example.roomplan.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roomplan.data.ExportManager
import com.example.roomplan.engine.RoomCalculator
import com.example.roomplan.engine.RoomCollisionEngine
import com.example.roomplan.engine.RoomPlanGenerator
import com.example.roomplan.model.CollisionWarning
import com.example.roomplan.model.DimensionType
import com.example.roomplan.model.Door
import com.example.roomplan.model.DoorSwing
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.UnitType
import com.example.roomplan.model.WallSide
import com.example.roomplan.model.Window
import com.example.roomplan.ui.theme.AccentAmber
import com.example.roomplan.ui.theme.AccentEmerald
import com.example.roomplan.ui.theme.AccentRose
import com.example.roomplan.ui.theme.AccentSky
import com.example.roomplan.ui.theme.BlueprintBorder
import com.example.roomplan.ui.theme.BlueprintCanvasBg
import com.example.roomplan.ui.theme.BlueprintDark
import com.example.roomplan.ui.theme.BlueprintMuted
import com.example.roomplan.ui.theme.BlueprintNavy
import com.example.roomplan.ui.theme.BlueprintSlate
import com.example.roomplan.ui.theme.PrimaryBlue
import com.example.roomplan.ui.theme.PrimaryBlueDark
import com.example.roomplan.ui.theme.PrimaryBlueLight
import com.example.roomplan.ui.viewmodel.RoomPlanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(
    viewModel: RoomPlanViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFurniture: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showStatsDialog by remember { mutableStateOf(false) }
    var showWarningsDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }

    // Export contracts
    val savePngLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val bitmap = ExportManager.renderPlanToBitmap(uiState.toRoomPlan())
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                    }
                    bitmap.recycle()
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Floor plan exported as PNG.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Failed to export PNG: ${e.message}")
                    }
                }
            }
        }
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        ExportManager.exportToPdf(uiState.toRoomPlan(), out)
                    }
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Floor plan exported as PDF.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Failed to export PDF: ${e.message}")
                    }
                }
            }
        }
    }

    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val jsonStr = com.example.roomplan.data.PlanStorage
                        .serializePlan(uiState.toRoomPlan())
                        .toString(2)
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        out.write(jsonStr.toByteArray())
                    }
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Plan exported as JSON.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Failed to export JSON: ${e.message}")
                    }
                }
            }
        }
    }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val csvStr = ExportManager.exportToCsv(uiState.toRoomPlan())
                    context.contentResolver.openOutputStream(it)?.use { out ->
                        out.write(csvStr.toByteArray())
                    }
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Furniture list exported as CSV.")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar("Failed to export CSV: ${e.message}")
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
                            text = uiState.planName,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${uiState.unit.format(uiState.room.lengthMm)} × ${uiState.unit.format(uiState.room.widthMm)} (${uiState.roomShape.displayName})",
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
                    // Collision Warnings Badge
                    if (uiState.warnings.isNotEmpty()) {
                        IconButton(onClick = { showWarningsDialog = true }) {
                            Icon(Icons.Default.Warning, contentDescription = "Warnings", tint = AccentRose)
                        }
                    }

                    // Room Stats
                    IconButton(onClick = { showStatsDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Room Statistics")
                    }

                    // Export Menu
                    Box {
                        IconButton(onClick = { showExportMenu = true }) {
                            Icon(Icons.Default.Share, contentDescription = "Export")
                        }
                        DropdownMenu(
                            expanded = showExportMenu,
                            onDismissRequest = { showExportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export as PNG Image") },
                                onClick = {
                                    showExportMenu = false
                                    savePngLauncher.launch("${uiState.planName.replace(" ", "_")}_floorplan.png")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as PDF Document") },
                                onClick = {
                                    showExportMenu = false
                                    savePdfLauncher.launch("${uiState.planName.replace(" ", "_")}_floorplan.pdf")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as JSON (Full Plan)") },
                                onClick = {
                                    showExportMenu = false
                                    saveJsonLauncher.launch("${uiState.planName.replace(" ", "_")}.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Furniture as CSV") },
                                onClick = {
                                    showExportMenu = false
                                    saveCsvLauncher.launch("${uiState.planName.replace(" ", "_")}_furniture.csv")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share Text Summary") },
                                onClick = {
                                    showExportMenu = false
                                    val summary = ExportManager.createShareSummary(uiState.toRoomPlan())
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "RoomPlan: ${uiState.planName}")
                                        putExtra(Intent.EXTRA_TEXT, summary)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Plan Summary"))
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Selected Furniture Inspector or Quick Tool Bar
            val selectedItem = uiState.selectedFurniture
            if (selectedItem != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedItem.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "${uiState.unit.formatPlain(selectedItem.effectiveWidthMm)} × ${uiState.unit.formatPlain(selectedItem.effectiveDepthMm)} | Pos: (${uiState.unit.formatPlain(selectedItem.xMm)}, ${uiState.unit.formatPlain(selectedItem.yMm)}) | Rot: ${selectedItem.rotationDegrees}°",
                                    fontSize = 11.sp,
                                    color = BlueprintMuted
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { viewModel.rotateFurniture(selectedItem.id) }) {
                                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = PrimaryBlue)
                                }
                                IconButton(onClick = { viewModel.duplicateFurniture(selectedItem.id) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = AccentEmerald)
                                }
                                IconButton(onClick = { viewModel.deleteFurniture(selectedItem.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AccentRose)
                                }
                            }
                        }
                    }
                }
            } else {
                // Secondary Quick Actions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tap any furniture item to select & drag",
                            fontSize = 12.sp,
                            color = BlueprintMuted
                        )

                        OutlinedButton(onClick = onNavigateToFurniture) {
                            Icon(Icons.Default.Chair, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage Furniture")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BlueprintCanvasBg)
        ) {
            // Interactive Compose Canvas
            FloorPlanCanvas(
                uiState = uiState,
                onMoveFurniture = { id, x, y -> viewModel.moveFurniture(id, x, y) },
                onSelectFurniture = { id -> viewModel.selectFurniture(id) },
                onPan = { dx, dy -> viewModel.updatePan(dx, dy) },
                onZoom = { factor -> viewModel.setZoom(uiState.zoomLevel * factor) }
            )

            // Canvas Zoom & Fit Control Toolbar (Floating Top-Right)
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardElevation().let { CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)) },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(onClick = { viewModel.zoomIn() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                    }
                    IconButton(onClick = { viewModel.zoomOut() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                    }
                    IconButton(onClick = { viewModel.resetView() }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.FitScreen, contentDescription = "Fit Plan")
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    IconButton(
                        onClick = { viewModel.toggleGrid(!uiState.showGrid) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.GridOn,
                            contentDescription = "Toggle Grid",
                            tint = if (uiState.showGrid) PrimaryBlue else Color.Gray
                        )
                    }
                }
            }

            // Grid Spacing & Snap Floating Chip (Floating Bottom-Left)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Grid: ${uiState.unit.formatPlain(uiState.settings.gridSpacingMm)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = if (uiState.snapToGrid) "SNAP: ON" else "SNAP: OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (uiState.snapToGrid) AccentEmerald else Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    // --- ROOM STATISTICS DIALOG ---
    if (showStatsDialog) {
        val internalRoom = RoomCalculator.getInternalDimensions(
            room = uiState.room,
            shape = uiState.roomShape,
            dimensionType = uiState.dimensionType,
            wallThicknessMm = uiState.wallThicknessMm
        )
        val areaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(internalRoom, uiState.roomShape))
        val areaFt2 = RoomCalculator.mm2ToFt2(RoomCalculator.calculateArea(internalRoom, uiState.roomShape))
        val perimeterM = RoomCalculator.calculatePerimeter(internalRoom, uiState.roomShape) / 1000.0
        val furnFootprintM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateFurnitureFootprint(uiState.furniture))
        val roomFloorAreaM2 = RoomCalculator.mm2ToM2(RoomCalculator.calculateArea(internalRoom, uiState.roomShape))
        val utilPct = RoomCalculator.calculateRoomUtilization(furnFootprintM2, roomFloorAreaM2)
        val remainingFloorM2 = max(0.0, roomFloorAreaM2 - furnFootprintM2)
        val (doorsLenMm, windowsLenMm) = RoomCalculator.calculateOpeningsLength(uiState.doors, uiState.windows)

        AlertDialog(
            onDismissRequest = { showStatsDialog = false },
            title = {
                Text(
                    text = "Room Statistics & Dimensions",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow("Room Name", uiState.planName)
                    StatRow("Length", uiState.unit.format(internalRoom.lengthMm))
                    StatRow("Width", uiState.unit.format(internalRoom.widthMm))
                    StatRow("Floor Area", String.format(Locale.US, "%.2f m² (%.1f ft²)", areaM2, areaFt2))
                    StatRow("Perimeter", String.format(Locale.US, "%.2f m", perimeterM))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    StatRow("Furniture Items", "${uiState.furniture.size}")
                    StatRow("Furniture Footprint", String.format(Locale.US, "%.2f m²", furnFootprintM2))
                    StatRow("Room Utilization", String.format(Locale.US, "%.1f%%", utilPct))
                    StatRow("Available Floor Area", String.format(Locale.US, "%.2f m²", remainingFloorM2))

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    StatRow("Doors Total Opening", String.format(Locale.US, "%.2f m", doorsLenMm / 1000.0))
                    StatRow("Windows Total Opening", String.format(Locale.US, "%.2f m", windowsLenMm / 1000.0))
                    StatRow(
                        "Remaining Wall Length",
                        String.format(Locale.US, "%.2f m", max(0.0, perimeterM - (doorsLenMm + windowsLenMm) / 1000.0))
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "* Note: Utilization is total footprint / floor area. Not a measure of functional spaciousness.",
                        fontSize = 10.sp,
                        color = BlueprintMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showStatsDialog = false }) { Text("Close") }
            }
        )
    }

    // --- WARNINGS DIALOG ---
    if (showWarningsDialog) {
        AlertDialog(
            onDismissRequest = { showWarningsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRose)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Collision & Boundary Alerts (${uiState.warnings.size})")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.warnings.forEach { w ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (w.severity == CollisionWarning.Severity.ERROR)
                                    AccentRose.copy(alpha = 0.15f)
                                else AccentAmber.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(w.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(w.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWarningsDialog = false }) { Text("Dismiss") }
            }
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = BlueprintMuted)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun FloorPlanCanvas(
    uiState: com.example.roomplan.ui.viewmodel.RoomPlanUiState,
    onMoveFurniture: (id: String, xMm: Double, yMm: Double) -> Unit,
    onSelectFurniture: (id: String?) -> Unit,
    onPan: (dx: Float, dy: Float) -> Unit,
    onZoom: (factor: Float) -> Unit
) {
    var draggingItemId by remember { mutableStateOf<String?>(null) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var itemOriginalPos by remember { mutableStateOf(Pair(0.0, 0.0)) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        if (draggingItemId == null) {
            onZoom(zoomChange)
            onPan(offsetChange.x, offsetChange.y)
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .transformable(state = transformableState)
            .pointerInput(uiState.furniture, uiState.room, uiState.zoomLevel, uiState.panOffsetX, uiState.panOffsetY) {
                detectTapGestures(
                    onTap = { tapOffset ->
                        val viewport = RoomPlanGenerator.calculateViewport(
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat(),
                            room = uiState.room,
                            wallThicknessMm = uiState.wallThicknessMm,
                            showWallThickness = uiState.settings.showWallThickness,
                            zoomLevel = uiState.zoomLevel,
                            panOffsetX = uiState.panOffsetX,
                            panOffsetY = uiState.panOffsetY
                        )

                        // Convert tap to mm
                        val tapMmX = viewport.canvasToMmX(tapOffset.x)
                        val tapMmY = viewport.canvasToMmY(tapOffset.y)

                        // Check which furniture was tapped (topmost first)
                        val hit = uiState.furniture.reversed().firstOrNull { item ->
                            val r = RoomCollisionEngine.getFurnitureRect(item)
                            tapMmX >= r.left && tapMmX <= r.right && tapMmY >= r.top && tapMmY <= r.bottom
                        }

                        onSelectFurniture(hit?.id)
                    }
                )
            }
            .pointerInput(uiState.furniture, uiState.selectedFurnitureId, uiState.zoomLevel, uiState.panOffsetX, uiState.panOffsetY) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val viewport = RoomPlanGenerator.calculateViewport(
                            canvasWidth = size.width.toFloat(),
                            canvasHeight = size.height.toFloat(),
                            room = uiState.room,
                            wallThicknessMm = uiState.wallThicknessMm,
                            showWallThickness = uiState.settings.showWallThickness,
                            zoomLevel = uiState.zoomLevel,
                            panOffsetX = uiState.panOffsetX,
                            panOffsetY = uiState.panOffsetY
                        )
                        val tapMmX = viewport.canvasToMmX(startOffset.x)
                        val tapMmY = viewport.canvasToMmY(startOffset.y)

                        // Prioritize selected furniture or find hit item
                        val selected = uiState.selectedFurniture
                        val hitItem = if (selected != null && isPointInsideFurniture(tapMmX, tapMmY, selected)) {
                            selected
                        } else {
                            uiState.furniture.reversed().firstOrNull { isPointInsideFurniture(tapMmX, tapMmY, it) }
                        }

                        if (hitItem != null) {
                            draggingItemId = hitItem.id
                            dragStartOffset = startOffset
                            itemOriginalPos = Pair(hitItem.xMm, hitItem.yMm)
                            onSelectFurniture(hitItem.id)
                        } else {
                            draggingItemId = null
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val currentDragging = draggingItemId
                        if (currentDragging != null) {
                            val viewport = RoomPlanGenerator.calculateViewport(
                                canvasWidth = size.width.toFloat(),
                                canvasHeight = size.height.toFloat(),
                                room = uiState.room,
                                wallThicknessMm = uiState.wallThicknessMm,
                                showWallThickness = uiState.settings.showWallThickness,
                                zoomLevel = uiState.zoomLevel,
                                panOffsetX = uiState.panOffsetX,
                                panOffsetY = uiState.panOffsetY
                            )
                            val deltaMmX = dragAmount.x / viewport.scale
                            val deltaMmY = dragAmount.y / viewport.scale

                            val currentItem = uiState.furniture.firstOrNull { it.id == currentDragging }
                            if (currentItem != null) {
                                val newX = currentItem.xMm + deltaMmX
                                val newY = currentItem.yMm + deltaMmY
                                onMoveFurniture(currentDragging, newX, newY)
                            }
                        } else {
                            onPan(dragAmount.x, dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        draggingItemId = null
                    },
                    onDragCancel = {
                        draggingItemId = null
                    }
                )
            }
    ) {
        val viewport = RoomPlanGenerator.calculateViewport(
            canvasWidth = size.width,
            canvasHeight = size.height,
            room = uiState.room,
            wallThicknessMm = uiState.wallThicknessMm,
            showWallThickness = uiState.settings.showWallThickness,
            zoomLevel = uiState.zoomLevel,
            panOffsetX = uiState.panOffsetX,
            panOffsetY = uiState.panOffsetY
        )

        // 1. Draw Grid inside room
        if (uiState.showGrid) {
            drawRoomGrid(viewport, uiState.room, uiState.settings.gridSpacingMm)
        }

        // 2. Draw Room Base (Walls and Floor)
        drawRoomFloorAndWalls(
            viewport = viewport,
            room = uiState.room,
            shape = uiState.roomShape,
            wallThicknessMm = uiState.wallThicknessMm,
            showWallThickness = uiState.settings.showWallThickness
        )

        // 3. Draw Windows
        drawWindows(viewport, uiState.windows, uiState.room, uiState.wallThicknessMm)

        // 4. Draw Doors
        drawDoors(viewport, uiState.doors, uiState.room, uiState.wallThicknessMm)

        // 5. Draw Columns
        drawColumns(viewport, uiState.columns)

        // 6. Draw Furniture Items & Clearance
        drawFurniture(
            viewport = viewport,
            furniture = uiState.furniture,
            selectedId = uiState.selectedFurnitureId,
            unit = uiState.unit,
            showClearance = uiState.settings.showClearance,
            wallClearanceMm = uiState.settings.wallClearanceMm,
            showLabels = uiState.settings.showFurnitureLabels
        )

        // 7. Draw Dimension Lines
        if (uiState.settings.showDimensions) {
            drawDimensionLines(viewport, uiState.room, uiState.unit)
        }
    }
}

private fun isPointInsideFurniture(tapX: Double, tapY: Double, item: FurnitureItem): Boolean {
    val r = RoomCollisionEngine.getFurnitureRect(item)
    return tapX >= r.left && tapX <= r.right && tapY >= r.top && tapY <= r.bottom
}

private fun DrawScope.drawRoomGrid(
    viewport: RoomPlanGenerator.ViewportTransform,
    room: RoomDimensions,
    gridSpacingMm: Double
) {
    if (gridSpacingMm <= 0) return
    val gridColor = Color(0xFFCBD5E1).copy(alpha = 0.5f)

    var x = 0.0
    while (x <= room.lengthMm) {
        val cx = viewport.mmToCanvasX(x)
        drawLine(
            color = gridColor,
            start = Offset(cx, viewport.mmToCanvasY(0.0)),
            end = Offset(cx, viewport.mmToCanvasY(room.widthMm)),
            strokeWidth = 1f
        )
        x += gridSpacingMm
    }

    var y = 0.0
    while (y <= room.widthMm) {
        val cy = viewport.mmToCanvasY(y)
        drawLine(
            color = gridColor,
            start = Offset(viewport.mmToCanvasX(0.0), cy),
            end = Offset(viewport.mmToCanvasX(room.lengthMm), cy),
            strokeWidth = 1f
        )
        y += gridSpacingMm
    }
}

private fun DrawScope.drawRoomFloorAndWalls(
    viewport: RoomPlanGenerator.ViewportTransform,
    room: RoomDimensions,
    shape: RoomShape,
    wallThicknessMm: Double,
    showWallThickness: Boolean
) {
    val rLeft = viewport.mmToCanvasX(0.0)
    val rTop = viewport.mmToCanvasY(0.0)
    val rRight = viewport.mmToCanvasX(room.lengthMm)
    val rBottom = viewport.mmToCanvasY(room.widthMm)

    val wallColor = BlueprintNavy
    val floorFill = Color.White
    val wallFill = Color(0xFFE2E8F0)

    val wallThicknessPx = if (showWallThickness) (wallThicknessMm.toFloat() * viewport.scale) else 0f

    if (shape == RoomShape.L_SHAPE && room.cutoutLengthMm > 0 && room.cutoutWidthMm > 0) {
        val cutX = viewport.mmToCanvasX(room.lengthMm - room.cutoutLengthMm)
        val cutY = viewport.mmToCanvasY(room.cutoutWidthMm)

        val innerPath = Path().apply {
            moveTo(rLeft, rTop)
            lineTo(cutX, rTop)
            lineTo(cutX, cutY)
            lineTo(rRight, cutY)
            lineTo(rRight, rBottom)
            lineTo(rLeft, rBottom)
            close()
        }

        drawPath(innerPath, floorFill)
        drawPath(innerPath, wallColor, style = Stroke(width = if (showWallThickness) 3f else 6f))

        if (showWallThickness && wallThicknessPx > 0) {
            val outerPath = Path().apply {
                moveTo(rLeft - wallThicknessPx, rTop - wallThicknessPx)
                lineTo(cutX + wallThicknessPx, rTop - wallThicknessPx)
                lineTo(cutX + wallThicknessPx, cutY - wallThicknessPx)
                lineTo(rRight + wallThicknessPx, cutY - wallThicknessPx)
                lineTo(rRight + wallThicknessPx, rBottom + wallThicknessPx)
                lineTo(rLeft - wallThicknessPx, rBottom + wallThicknessPx)
                close()
            }
            drawPath(outerPath, wallColor, style = Stroke(width = 4f))
        }
    } else {
        // Rectangle / Square
        val floorRect = androidx.compose.ui.geometry.Rect(rLeft, rTop, rRight, rBottom)
        drawRect(floorFill, floorRect.topLeft, floorRect.size)
        drawRect(wallColor, floorRect.topLeft, floorRect.size, style = Stroke(width = if (showWallThickness) 3f else 6f))

        if (showWallThickness && wallThicknessPx > 0) {
            val outerRect = androidx.compose.ui.geometry.Rect(
                rLeft - wallThicknessPx,
                rTop - wallThicknessPx,
                rRight + wallThicknessPx,
                rBottom + wallThicknessPx
            )
            drawRect(wallColor, outerRect.topLeft, outerRect.size, style = Stroke(width = 4f))
        }
    }
}

private fun DrawScope.drawWindows(
    viewport: RoomPlanGenerator.ViewportTransform,
    windows: List<Window>,
    room: RoomDimensions,
    wallThicknessMm: Double
) {
    val windowPaintColor = AccentSky
    for (win in windows) {
        val wStart = win.offsetMm
        val wEnd = win.offsetMm + win.widthMm
        when (win.wall) {
            WallSide.TOP -> {
                val y = viewport.mmToCanvasY(0.0)
                val x1 = viewport.mmToCanvasX(wStart)
                val x2 = viewport.mmToCanvasX(wEnd)
                drawLine(Color.White, Offset(x1, y), Offset(x2, y), strokeWidth = 8f) // opening cut
                drawLine(windowPaintColor, Offset(x1, y - 4f), Offset(x2, y - 4f), strokeWidth = 3f)
                drawLine(windowPaintColor, Offset(x1, y + 4f), Offset(x2, y + 4f), strokeWidth = 3f)
            }
            WallSide.BOTTOM -> {
                val y = viewport.mmToCanvasY(room.widthMm)
                val x1 = viewport.mmToCanvasX(wStart)
                val x2 = viewport.mmToCanvasX(wEnd)
                drawLine(Color.White, Offset(x1, y), Offset(x2, y), strokeWidth = 8f)
                drawLine(windowPaintColor, Offset(x1, y - 4f), Offset(x2, y - 4f), strokeWidth = 3f)
                drawLine(windowPaintColor, Offset(x1, y + 4f), Offset(x2, y + 4f), strokeWidth = 3f)
            }
            WallSide.LEFT -> {
                val x = viewport.mmToCanvasX(0.0)
                val y1 = viewport.mmToCanvasY(wStart)
                val y2 = viewport.mmToCanvasY(wEnd)
                drawLine(Color.White, Offset(x, y1), Offset(x, y2), strokeWidth = 8f)
                drawLine(windowPaintColor, Offset(x - 4f, y1), Offset(x - 4f, y2), strokeWidth = 3f)
                drawLine(windowPaintColor, Offset(x + 4f, y1), Offset(x + 4f, y2), strokeWidth = 3f)
            }
            WallSide.RIGHT -> {
                val x = viewport.mmToCanvasX(room.lengthMm)
                val y1 = viewport.mmToCanvasY(wStart)
                val y2 = viewport.mmToCanvasY(wEnd)
                drawLine(Color.White, Offset(x, y1), Offset(x, y2), strokeWidth = 8f)
                drawLine(windowPaintColor, Offset(x - 4f, y1), Offset(x - 4f, y2), strokeWidth = 3f)
                drawLine(windowPaintColor, Offset(x + 4f, y1), Offset(x + 4f, y2), strokeWidth = 3f)
            }
        }
    }
}

private fun DrawScope.drawDoors(
    viewport: RoomPlanGenerator.ViewportTransform,
    doors: List<Door>,
    room: RoomDimensions,
    wallThicknessMm: Double
) {
    val doorColor = AccentEmerald
    val arcColor = AccentEmerald.copy(alpha = 0.6f)

    for (door in doors) {
        val dWidthPx = (door.widthMm.toFloat() * viewport.scale)
        val dStartMm = door.offsetMm
        val dEndMm = door.offsetMm + door.widthMm

        when (door.wall) {
            WallSide.BOTTOM -> {
                val y = viewport.mmToCanvasY(room.widthMm)
                val x1 = viewport.mmToCanvasX(dStartMm)
                val x2 = viewport.mmToCanvasX(dEndMm)
                drawLine(Color.White, Offset(x1, y), Offset(x2, y), strokeWidth = 8f) // opening break
                // Door leaf
                drawLine(doorColor, Offset(x1, y - dWidthPx), Offset(x1, y), strokeWidth = 4f)
                // Swing arc
                drawArc(
                    color = arcColor,
                    startAngle = 270f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(x1 - dWidthPx, y - dWidthPx),
                    size = Size(dWidthPx * 2, dWidthPx * 2),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }
            WallSide.TOP -> {
                val y = viewport.mmToCanvasY(0.0)
                val x1 = viewport.mmToCanvasX(dStartMm)
                val x2 = viewport.mmToCanvasX(dEndMm)
                drawLine(Color.White, Offset(x1, y), Offset(x2, y), strokeWidth = 8f)
                drawLine(doorColor, Offset(x1, y), Offset(x1, y + dWidthPx), strokeWidth = 4f)
                drawArc(
                    color = arcColor,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(x1 - dWidthPx, y - dWidthPx),
                    size = Size(dWidthPx * 2, dWidthPx * 2),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }
            WallSide.LEFT -> {
                val x = viewport.mmToCanvasX(0.0)
                val y1 = viewport.mmToCanvasY(dStartMm)
                val y2 = viewport.mmToCanvasY(dEndMm)
                drawLine(Color.White, Offset(x, y1), Offset(x, y2), strokeWidth = 8f)
                drawLine(doorColor, Offset(x, y1), Offset(x + dWidthPx, y1), strokeWidth = 4f)
                drawArc(
                    color = arcColor,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(x - dWidthPx, y1 - dWidthPx),
                    size = Size(dWidthPx * 2, dWidthPx * 2),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }
            WallSide.RIGHT -> {
                val x = viewport.mmToCanvasX(room.lengthMm)
                val y1 = viewport.mmToCanvasY(dStartMm)
                val y2 = viewport.mmToCanvasY(dEndMm)
                drawLine(Color.White, Offset(x, y1), Offset(x, y2), strokeWidth = 8f)
                drawLine(doorColor, Offset(x - dWidthPx, y1), Offset(x, y1), strokeWidth = 4f)
                drawArc(
                    color = arcColor,
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(x - dWidthPx, y1 - dWidthPx),
                    size = Size(dWidthPx * 2, dWidthPx * 2),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                )
            }
        }
    }
}

private fun DrawScope.drawColumns(
    viewport: RoomPlanGenerator.ViewportTransform,
    columns: List<com.example.roomplan.model.Column>
) {
    val colFill = BlueprintSlate
    val colBorder = BlueprintNavy

    for (col in columns) {
        val cx = viewport.mmToCanvasX(col.xMm)
        val cy = viewport.mmToCanvasY(col.yMm)
        val cw = col.widthMm.toFloat() * viewport.scale
        val cd = col.depthMm.toFloat() * viewport.scale

        drawRect(colFill, Offset(cx, cy), Size(cw, cd))
        drawRect(colBorder, Offset(cx, cy), Size(cw, cd), style = Stroke(width = 2f))

        // Diagonal architectural hatching cross
        drawLine(colBorder.copy(alpha = 0.5f), Offset(cx, cy), Offset(cx + cw, cy + cd), strokeWidth = 1f)
        drawLine(colBorder.copy(alpha = 0.5f), Offset(cx + cw, cy), Offset(cx, cy + cd), strokeWidth = 1f)
    }
}

private fun DrawScope.drawFurniture(
    viewport: RoomPlanGenerator.ViewportTransform,
    furniture: List<FurnitureItem>,
    selectedId: String?,
    unit: UnitType,
    showClearance: Boolean,
    wallClearanceMm: Double,
    showLabels: Boolean
) {
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.rgb(15, 23, 42)
        textSize = 24f
        isFakeBoldText = true
    }

    val subTextPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.rgb(100, 116, 139)
        textSize = 18f
    }

    for (item in furniture) {
        val isSelected = item.id == selectedId
        val fx = viewport.mmToCanvasX(item.xMm)
        val fy = viewport.mmToCanvasY(item.yMm)
        val fw = item.effectiveWidthMm.toFloat() * viewport.scale
        val fd = item.effectiveDepthMm.toFloat() * viewport.scale

        // Clearance zone around selected item
        if (isSelected && showClearance && wallClearanceMm > 0) {
            val clrPx = (wallClearanceMm.toFloat() * viewport.scale)
            drawRoundRect(
                color = AccentAmber.copy(alpha = 0.8f),
                topLeft = Offset(fx - clrPx, fy - clrPx),
                size = Size(fw + 2 * clrPx, fd + 2 * clrPx),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
            )
        }

        // Furniture Fill & Stroke
        val fillCol = if (isSelected) PrimaryBlue.copy(alpha = 0.25f) else Color(0xFFDBEAFE).copy(alpha = 0.6f)
        val strokeCol = if (isSelected) PrimaryBlue else PrimaryBlueDark
        val strokeW = if (isSelected) 4f else 2.5f

        drawRoundRect(
            color = fillCol,
            topLeft = Offset(fx, fy),
            size = Size(fw, fd),
            cornerRadius = CornerRadius(6f, 6f)
        )
        drawRoundRect(
            color = strokeCol,
            topLeft = Offset(fx, fy),
            size = Size(fw, fd),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = strokeW)
        )

        // Architectural details by category
        drawFurnitureDetails(viewport, item, fx, fy, fw, fd, strokeCol)

        // Labels
        if (showLabels && fw > 30f && fd > 20f) {
            val cx = fx + fw / 2f
            val cy = fy + fd / 2f

            drawContext.canvas.nativeCanvas.drawText(
                item.name,
                cx,
                if (fd > 45f) cy - 4f else cy + 8f,
                textPaint
            )

            if (fd > 45f) {
                val dimStr = "${unit.formatPlain(item.effectiveWidthMm)} × ${unit.formatPlain(item.effectiveDepthMm)}"
                drawContext.canvas.nativeCanvas.drawText(dimStr, cx, cy + 18f, subTextPaint)
            }
        }
    }
}

private fun DrawScope.drawFurnitureDetails(
    viewport: RoomPlanGenerator.ViewportTransform,
    item: FurnitureItem,
    fx: Float,
    fy: Float,
    fw: Float,
    fd: Float,
    color: Color
) {
    val detailColor = color.copy(alpha = 0.4f)
    val type = item.type

    when {
        type.name.contains("BED") -> {
            // Draw headboard line
            drawLine(detailColor, Offset(fx, fy + 8f), Offset(fx + fw, fy + 8f), strokeWidth = 2f)
            // Draw pillows
            if (fw > 40f && fd > 50f) {
                val pillowW = fw * 0.35f
                val pillowH = fd * 0.2f
                drawRoundRect(detailColor, Offset(fx + fw * 0.1f, fy + 12f), Size(pillowW, pillowH), CornerRadius(4f, 4f), style = Stroke(width = 1.5f))
                drawRoundRect(detailColor, Offset(fx + fw * 0.55f, fy + 12f), Size(pillowW, pillowH), CornerRadius(4f, 4f), style = Stroke(width = 1.5f))
            }
        }
        type.name.contains("SOFA") -> {
            // Backrest line
            drawLine(detailColor, Offset(fx, fy + 10f), Offset(fx + fw, fy + 10f), strokeWidth = 3f)
            // Armrests
            drawLine(detailColor, Offset(fx + 10f, fy), Offset(fx + 10f, fy + fd), strokeWidth = 2f)
            drawLine(detailColor, Offset(fx + fw - 10f, fy), Offset(fx + fw - 10f, fy + fd), strokeWidth = 2f)
        }
        type.name.contains("WARDROBE") -> {
            // Clothes hanger diagonal slash
            drawLine(detailColor, Offset(fx, fy), Offset(fx + fw, fy + fd), strokeWidth = 1.5f)
            drawLine(detailColor, Offset(fx, fy + fd), Offset(fx + fw, fy), strokeWidth = 1.5f)
        }
        type.name.contains("TABLE") -> {
            // Inner bevel
            drawRoundRect(detailColor, Offset(fx + 6f, fy + 6f), Size(fw - 12f, fd - 12f), CornerRadius(4f, 4f), style = Stroke(width = 1.5f))
        }
    }
}

private fun DrawScope.drawDimensionLines(
    viewport: RoomPlanGenerator.ViewportTransform,
    room: RoomDimensions,
    unit: UnitType
) {
    val dimPaint = Color(0xFF475569)
    val rLeft = viewport.mmToCanvasX(0.0)
    val rTop = viewport.mmToCanvasY(0.0)
    val rRight = viewport.mmToCanvasX(room.lengthMm)
    val rBottom = viewport.mmToCanvasY(room.widthMm)

    val offset = 32f

    // Top dimension (Length)
    val topDimY = rTop - offset
    drawLine(dimPaint, Offset(rLeft, topDimY), Offset(rRight, topDimY), strokeWidth = 2f)
    // Extension lines
    drawLine(dimPaint.copy(alpha = 0.5f), Offset(rLeft, rTop - 8f), Offset(rLeft, topDimY - 10f), strokeWidth = 1.5f)
    drawLine(dimPaint.copy(alpha = 0.5f), Offset(rRight, rTop - 8f), Offset(rRight, topDimY - 10f), strokeWidth = 1.5f)
    // Architectural 45-degree slash ticks
    drawLine(dimPaint, Offset(rLeft - 6f, topDimY + 6f), Offset(rLeft + 6f, topDimY - 6f), strokeWidth = 2f)
    drawLine(dimPaint, Offset(rRight - 6f, topDimY + 6f), Offset(rRight + 6f, topDimY - 6f), strokeWidth = 2f)

    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.rgb(30, 41, 59)
        textSize = 22f
        isFakeBoldText = true
    }

    drawContext.canvas.nativeCanvas.drawText(
        unit.format(room.lengthMm),
        (rLeft + rRight) / 2f,
        topDimY - 8f,
        textPaint
    )

    // Left dimension (Width)
    val leftDimX = rLeft - offset
    drawLine(dimPaint, Offset(leftDimX, rTop), Offset(leftDimX, rBottom), strokeWidth = 2f)
    drawLine(dimPaint.copy(alpha = 0.5f), Offset(rLeft - 8f, rTop), Offset(leftDimX - 10f, rTop), strokeWidth = 1.5f)
    drawLine(dimPaint.copy(alpha = 0.5f), Offset(rLeft - 8f, rBottom), Offset(leftDimX - 10f, rBottom), strokeWidth = 1.5f)
    drawLine(dimPaint, Offset(leftDimX - 6f, rTop + 6f), Offset(leftDimX + 6f, rTop - 6f), strokeWidth = 2f)
    drawLine(dimPaint, Offset(leftDimX - 6f, rBottom + 6f), Offset(leftDimX + 6f, rBottom - 6f), strokeWidth = 2f)

    drawContext.canvas.nativeCanvas.save()
    drawContext.canvas.nativeCanvas.rotate(-90f, leftDimX - 12f, (rTop + rBottom) / 2f)
    drawContext.canvas.nativeCanvas.drawText(
        unit.format(room.widthMm),
        leftDimX - 12f,
        (rTop + rBottom) / 2f,
        textPaint
    )
    drawContext.canvas.nativeCanvas.restore()
}
