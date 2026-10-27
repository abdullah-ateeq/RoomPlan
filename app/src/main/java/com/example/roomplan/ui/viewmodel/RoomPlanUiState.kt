package com.example.roomplan.ui.viewmodel

import com.example.roomplan.model.CollisionWarning
import com.example.roomplan.model.Column
import com.example.roomplan.model.DimensionType
import com.example.roomplan.model.Door
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.PlanSettings
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomPlan
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.UnitType
import com.example.roomplan.model.Window
import java.util.UUID

data class RoomPlanUiState(
    val unit: UnitType = UnitType.CM,
    val room: RoomDimensions = RoomDimensions(
        lengthMm = 5000.0,
        widthMm = 4000.0,
        heightMm = null,
        cutoutLengthMm = 0.0,
        cutoutWidthMm = 0.0
    ),
    val roomShape: RoomShape = RoomShape.RECTANGLE,
    val dimensionType: DimensionType = DimensionType.INSIDE,
    val wallThicknessMm: Double = 150.0,
    val doors: List<Door> = emptyList(),
    val windows: List<Window> = emptyList(),
    val columns: List<Column> = emptyList(),
    val furniture: List<FurnitureItem> = emptyList(),
    val selectedFurnitureId: String? = null,
    val showGrid: Boolean = true,
    val snapToGrid: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val planName: String = "Living Room",
    val currentPlanId: String = UUID.randomUUID().toString(),
    val notes: String = "",
    val warnings: List<CollisionWarning> = emptyList(),
    val savedPlans: List<RoomPlan> = emptyList(),
    val settings: PlanSettings = PlanSettings(),
    val zoomLevel: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f
) {
    val selectedFurniture: FurnitureItem?
        get() = furniture.firstOrNull { it.id == selectedFurnitureId }

    fun toRoomPlan(): RoomPlan {
        return RoomPlan(
            id = currentPlanId,
            name = planName,
            room = room,
            unit = unit,
            shape = roomShape,
            dimensionType = dimensionType,
            wallThicknessMm = wallThicknessMm,
            doors = doors,
            windows = windows,
            columns = columns,
            furniture = furniture,
            notes = notes,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
    }
}
