package com.example.roomplan.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.roomplan.data.PlanStorage
import com.example.roomplan.data.SettingsStorage
import com.example.roomplan.engine.AutoLayoutEngine
import com.example.roomplan.engine.RoomCollisionEngine
import com.example.roomplan.engine.RoomPlanGenerator
import com.example.roomplan.model.Column
import com.example.roomplan.model.DimensionType
import com.example.roomplan.model.Door
import com.example.roomplan.model.DoorSwing
import com.example.roomplan.model.DoorType
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.FurnitureType
import com.example.roomplan.model.PlanSettings
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomPlan
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.UnitType
import com.example.roomplan.model.WallSide
import com.example.roomplan.model.Window
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class RoomPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val planStorage = PlanStorage(application)
    private val settingsStorage = SettingsStorage(application)

    private val _uiState = MutableStateFlow(RoomPlanUiState())
    val uiState: StateFlow<RoomPlanUiState> = _uiState.asStateFlow()

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        val settings = settingsStorage.getSettings()
        val allPlans = planStorage.getAllPlans()

        val lastPlanId = planStorage.getLastOpenedPlanId()
        val restoredPlan = if (settings.restoreLastPlan && lastPlanId != null) {
            allPlans.firstOrNull { it.id == lastPlanId }
        } else null

        if (restoredPlan != null) {
            _uiState.update {
                it.copy(
                    unit = restoredPlan.unit,
                    room = restoredPlan.room,
                    roomShape = restoredPlan.shape,
                    dimensionType = restoredPlan.dimensionType,
                    wallThicknessMm = restoredPlan.wallThicknessMm,
                    doors = restoredPlan.doors,
                    windows = restoredPlan.windows,
                    columns = restoredPlan.columns,
                    furniture = restoredPlan.furniture,
                    planName = restoredPlan.name,
                    currentPlanId = restoredPlan.id,
                    notes = restoredPlan.notes,
                    savedPlans = allPlans,
                    settings = settings,
                    showGrid = settings.showGrid,
                    snapToGrid = settings.snapToGrid
                )
            }
        } else {
            // Default sample room: 500 x 400 cm with a door and window
            val initialDoors = listOf(
                Door(
                    id = UUID.randomUUID().toString(),
                    widthMm = 900.0,
                    wall = WallSide.BOTTOM,
                    offsetMm = 2050.0,
                    type = DoorType.HINGED,
                    swing = DoorSwing.INWARD
                )
            )
            val initialWindows = listOf(
                Window(
                    id = UUID.randomUUID().toString(),
                    widthMm = 1500.0,
                    wall = WallSide.TOP,
                    offsetMm = 1750.0,
                    sillHeightMm = 900.0,
                    heightMm = 1200.0
                )
            )

            _uiState.update {
                it.copy(
                    unit = settings.defaultUnit,
                    room = RoomDimensions(
                        lengthMm = settings.defaultRoomLengthMm,
                        widthMm = settings.defaultRoomWidthMm
                    ),
                    wallThicknessMm = settings.defaultWallThicknessMm,
                    doors = initialDoors,
                    windows = initialWindows,
                    savedPlans = allPlans,
                    settings = settings,
                    showGrid = settings.showGrid,
                    snapToGrid = settings.snapToGrid
                )
            }
        }
        recalculateWarnings()
    }

    fun setDimensions(lengthMm: Double, widthMm: Double, heightMm: Double?) {
        val safeLen = lengthMm.coerceAtLeast(500.0)
        val safeWidth = if (_uiState.value.roomShape == RoomShape.SQUARE) safeLen else widthMm.coerceAtLeast(500.0)

        _uiState.update {
            it.copy(
                room = it.room.copy(
                    lengthMm = safeLen,
                    widthMm = safeWidth,
                    heightMm = heightMm
                )
            )
        }
        recalculateWarnings()
    }

    fun setRoomShape(shape: RoomShape) {
        _uiState.update {
            val updatedRoom = if (shape == RoomShape.SQUARE) {
                it.room.copy(widthMm = it.room.lengthMm)
            } else {
                it.room
            }
            it.copy(roomShape = shape, room = updatedRoom)
        }
        recalculateWarnings()
    }

    fun setCutoutDimensions(cutoutLengthMm: Double, cutoutWidthMm: Double) {
        _uiState.update {
            it.copy(
                room = it.room.copy(
                    cutoutLengthMm = cutoutLengthMm.coerceIn(0.0, it.room.lengthMm - 200.0),
                    cutoutWidthMm = cutoutWidthMm.coerceIn(0.0, it.room.widthMm - 200.0)
                )
            )
        }
        recalculateWarnings()
    }

    fun setUnit(unit: UnitType) {
        _uiState.update { it.copy(unit = unit) }
    }

    fun setDimensionType(type: DimensionType) {
        _uiState.update { it.copy(dimensionType = type) }
        recalculateWarnings()
    }

    fun setWallThickness(thicknessMm: Double) {
        _uiState.update { it.copy(wallThicknessMm = thicknessMm.coerceAtLeast(50.0)) }
        recalculateWarnings()
    }

    fun setPlanName(name: String) {
        _uiState.update { it.copy(planName = name) }
    }

    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    // --- DOORS ---

    fun addDoor(door: Door) {
        val current = _uiState.value.doors
        if (current.size >= 4) {
            _uiState.update { it.copy(errorMessage = "Maximum 4 doors allowed.") }
            return
        }
        _uiState.update { it.copy(doors = current + door, errorMessage = null) }
        recalculateWarnings()
    }

    fun updateDoor(door: Door) {
        _uiState.update {
            it.copy(doors = it.doors.map { d -> if (d.id == door.id) door else d })
        }
        recalculateWarnings()
    }

    fun deleteDoor(doorId: String) {
        _uiState.update {
            it.copy(doors = it.doors.filterNot { d -> d.id == doorId })
        }
        recalculateWarnings()
    }

    // --- WINDOWS ---

    fun addWindow(window: Window) {
        val current = _uiState.value.windows
        if (current.size >= 8) {
            _uiState.update { it.copy(errorMessage = "Maximum 8 windows allowed.") }
            return
        }
        _uiState.update { it.copy(windows = current + window, errorMessage = null) }
        recalculateWarnings()
    }

    fun updateWindow(window: Window) {
        _uiState.update {
            it.copy(windows = it.windows.map { w -> if (w.id == window.id) window else w })
        }
        recalculateWarnings()
    }

    fun deleteWindow(windowId: String) {
        _uiState.update {
            it.copy(windows = it.windows.filterNot { w -> w.id == windowId })
        }
        recalculateWarnings()
    }

    // --- COLUMNS ---

    fun addColumn(column: Column) {
        val current = _uiState.value.columns
        if (current.size >= 10) {
            _uiState.update { it.copy(errorMessage = "Maximum 10 columns allowed.") }
            return
        }
        _uiState.update { it.copy(columns = current + column, errorMessage = null) }
        recalculateWarnings()
    }

    fun deleteColumn(columnId: String) {
        _uiState.update {
            it.copy(columns = it.columns.filterNot { c -> c.id == columnId })
        }
        recalculateWarnings()
    }

    // --- FURNITURE ---

    fun addFurniture(item: FurnitureItem) {
        _uiState.update {
            it.copy(
                furniture = it.furniture + item,
                selectedFurnitureId = item.id
            )
        }
        recalculateWarnings()
    }

    fun updateFurniture(item: FurnitureItem) {
        _uiState.update {
            it.copy(furniture = it.furniture.map { f -> if (f.id == item.id) item else f })
        }
        recalculateWarnings()
    }

    fun moveFurniture(itemId: String, newXMm: Double, newYMm: Double) {
        val snap = _uiState.value.snapToGrid
        val gridSpacing = _uiState.value.settings.gridSpacingMm
        val finalX = if (snap) RoomPlanGenerator.snapToGrid(newXMm, gridSpacing) else newXMm
        val finalY = if (snap) RoomPlanGenerator.snapToGrid(newYMm, gridSpacing) else newYMm

        _uiState.update {
            it.copy(
                furniture = it.furniture.map { f ->
                    if (f.id == itemId) f.copy(xMm = finalX, yMm = finalY) else f
                }
            )
        }
        recalculateWarnings()
    }

    fun rotateFurniture(itemId: String) {
        _uiState.update {
            it.copy(
                furniture = it.furniture.map { f ->
                    if (f.id == itemId) {
                        val nextAngle = (f.rotationDegrees + 90) % 360
                        f.copy(rotationDegrees = nextAngle)
                    } else f
                }
            )
        }
        recalculateWarnings()
    }

    fun duplicateFurniture(itemId: String) {
        val target = _uiState.value.furniture.firstOrNull { it.id == itemId } ?: return
        val newId = UUID.randomUUID().toString()
        val offset = 200.0 // slight offset so it is visible
        val duplicated = target.copy(
            id = newId,
            name = "${target.name} (Copy)",
            xMm = target.xMm + offset,
            yMm = target.yMm + offset
        )
        _uiState.update {
            it.copy(
                furniture = it.furniture + duplicated,
                selectedFurnitureId = newId
            )
        }
        recalculateWarnings()
    }

    fun deleteFurniture(itemId: String) {
        _uiState.update {
            it.copy(
                furniture = it.furniture.filterNot { f -> f.id == itemId },
                selectedFurnitureId = if (it.selectedFurnitureId == itemId) null else it.selectedFurnitureId
            )
        }
        recalculateWarnings()
    }

    fun selectFurniture(itemId: String?) {
        _uiState.update { it.copy(selectedFurnitureId = itemId) }
    }

    // --- AUTO LAYOUT ---

    fun autoArrangeFurniture() {
        viewModelScope.launch(Dispatchers.Default) {
            val state = _uiState.value
            val result = AutoLayoutEngine.arrange(
                plan = state.toRoomPlan(),
                wallClearanceMm = state.settings.wallClearanceMm,
                walkingClearanceMm = state.settings.walkingClearanceMm
            )

            if (result.success) {
                _uiState.update {
                    it.copy(
                        furniture = result.items,
                        infoMessage = "Furniture arranged successfully.",
                        errorMessage = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        errorMessage = result.errorMessage ?: "Could not automatically arrange all furniture."
                    )
                }
            }
            recalculateWarnings()
        }
    }

    // --- VIEWPORT / ZOOM / PAN ---

    fun zoomIn() {
        _uiState.update { it.copy(zoomLevel = (it.zoomLevel * 1.25f).coerceAtMost(8.0f)) }
    }

    fun zoomOut() {
        _uiState.update { it.copy(zoomLevel = (it.zoomLevel / 1.25f).coerceAtLeast(0.5f)) }
    }

    fun setZoom(zoom: Float) {
        _uiState.update { it.copy(zoomLevel = zoom.coerceIn(0.5f, 8.0f)) }
    }

    fun updatePan(deltaX: Float, deltaY: Float) {
        _uiState.update {
            it.copy(
                panOffsetX = it.panOffsetX + deltaX,
                panOffsetY = it.panOffsetY + deltaY
            )
        }
    }

    fun resetView() {
        _uiState.update {
            it.copy(
                zoomLevel = 1.0f,
                panOffsetX = 0f,
                panOffsetY = 0f
            )
        }
    }

    // --- SAVED PLANS ---

    fun saveCurrentPlan() {
        val plan = _uiState.value.toRoomPlan()
        val result = planStorage.savePlan(plan)
        result.fold(
            onSuccess = { saved ->
                _uiState.update {
                    it.copy(
                        currentPlanId = saved.id,
                        savedPlans = planStorage.getAllPlans(),
                        infoMessage = "Plan '${saved.name}' saved successfully.",
                        errorMessage = null
                    )
                }
            },
            onFailure = { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        )
    }

    fun loadPlan(plan: RoomPlan) {
        _uiState.update {
            it.copy(
                unit = plan.unit,
                room = plan.room,
                roomShape = plan.shape,
                dimensionType = plan.dimensionType,
                wallThicknessMm = plan.wallThicknessMm,
                doors = plan.doors,
                windows = plan.windows,
                columns = plan.columns,
                furniture = plan.furniture,
                planName = plan.name,
                currentPlanId = plan.id,
                notes = plan.notes,
                selectedFurnitureId = null,
                errorMessage = null,
                infoMessage = "Loaded plan '${plan.name}'"
            )
        }
        resetView()
        recalculateWarnings()
    }

    fun duplicateSavedPlan(planId: String) {
        val dup = planStorage.duplicatePlan(planId)
        if (dup != null) {
            _uiState.update {
                it.copy(
                    savedPlans = planStorage.getAllPlans(),
                    infoMessage = "Plan duplicated as '${dup.name}'"
                )
            }
        } else {
            _uiState.update {
                it.copy(errorMessage = "Cannot duplicate. Maximum plan limit reached (20).")
            }
        }
    }

    fun renameSavedPlan(planId: String, newName: String) {
        val renamed = planStorage.renamePlan(planId, newName)
        if (renamed != null) {
            _uiState.update {
                it.copy(
                    savedPlans = planStorage.getAllPlans(),
                    planName = if (it.currentPlanId == planId) renamed.name else it.planName
                )
            }
        }
    }

    fun deleteSavedPlan(planId: String) {
        val ok = planStorage.deletePlan(planId)
        if (ok) {
            _uiState.update {
                it.copy(
                    savedPlans = planStorage.getAllPlans(),
                    infoMessage = "Plan deleted."
                )
            }
        }
    }

    fun startNewPlan() {
        val settings = _uiState.value.settings
        _uiState.update {
            it.copy(
                unit = settings.defaultUnit,
                room = RoomDimensions(
                    lengthMm = settings.defaultRoomLengthMm,
                    widthMm = settings.defaultRoomWidthMm
                ),
                roomShape = RoomShape.RECTANGLE,
                dimensionType = DimensionType.INSIDE,
                wallThicknessMm = settings.defaultWallThicknessMm,
                doors = emptyList(),
                windows = emptyList(),
                columns = emptyList(),
                furniture = emptyList(),
                planName = "New Room",
                currentPlanId = UUID.randomUUID().toString(),
                notes = "",
                selectedFurnitureId = null,
                errorMessage = null,
                infoMessage = "New plan created."
            )
        }
        resetView()
        recalculateWarnings()
    }

    // --- SETTINGS ---

    fun updateSettings(newSettings: PlanSettings) {
        settingsStorage.saveSettings(newSettings)
        _uiState.update {
            it.copy(
                settings = newSettings,
                showGrid = newSettings.showGrid,
                snapToGrid = newSettings.snapToGrid
            )
        }
        recalculateWarnings()
    }

    fun resetSettings() {
        val def = settingsStorage.resetSettings()
        _uiState.update {
            it.copy(
                settings = def,
                showGrid = def.showGrid,
                snapToGrid = def.snapToGrid,
                infoMessage = "Settings reset to defaults."
            )
        }
    }

    fun toggleGrid(show: Boolean) {
        _uiState.update {
            val updatedSettings = it.settings.copy(showGrid = show)
            settingsStorage.saveSettings(updatedSettings)
            it.copy(showGrid = show, settings = updatedSettings)
        }
    }

    fun toggleSnapToGrid(snap: Boolean) {
        _uiState.update {
            val updatedSettings = it.settings.copy(snapToGrid = snap)
            settingsStorage.saveSettings(updatedSettings)
            it.copy(snapToGrid = snap, settings = updatedSettings)
        }
    }

    fun setGridSpacing(spacingMm: Double) {
        _uiState.update {
            val updatedSettings = it.settings.copy(gridSpacingMm = spacingMm)
            settingsStorage.saveSettings(updatedSettings)
            it.copy(settings = updatedSettings)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    private fun recalculateWarnings() {
        viewModelScope.launch(Dispatchers.Default) {
            val s = _uiState.value
            val warnings = RoomCollisionEngine.checkCollisions(
                room = s.room,
                shape = s.roomShape,
                furniture = s.furniture,
                columns = s.columns,
                doors = s.doors,
                windows = s.windows
            )
            _uiState.update { it.copy(warnings = warnings) }
        }
    }
}
