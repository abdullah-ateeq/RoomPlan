package com.example.roomplan.engine

import com.example.roomplan.model.DimensionType
import com.example.roomplan.model.Door
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.Window
import kotlin.math.max

object RoomCalculator {

    /**
     * Calculates the usable internal room dimensions based on dimension type.
     * If INSIDE: internal room has the exact input dimensions.
     * If OUTSIDE: internal room subtracts 2 * wallThickness from length and width.
     */
    fun getInternalDimensions(
        room: RoomDimensions,
        shape: RoomShape,
        dimensionType: DimensionType,
        wallThicknessMm: Double
    ): RoomDimensions {
        val effectiveThickness = if (dimensionType == DimensionType.OUTSIDE) wallThicknessMm else 0.0
        val internalLength = max(100.0, room.lengthMm - 2 * effectiveThickness)
        val internalWidth = when (shape) {
            RoomShape.SQUARE -> internalLength
            else -> max(100.0, room.widthMm - 2 * effectiveThickness)
        }
        val cutoutL = if (shape == RoomShape.L_SHAPE) {
            max(0.0, room.cutoutLengthMm)
        } else 0.0
        val cutoutW = if (shape == RoomShape.L_SHAPE) {
            max(0.0, room.cutoutWidthMm)
        } else 0.0

        return RoomDimensions(
            lengthMm = internalLength,
            widthMm = internalWidth,
            heightMm = room.heightMm,
            cutoutLengthMm = cutoutL,
            cutoutWidthMm = cutoutW
        )
    }

    /**
     * Calculates floor area in square millimeters (mm²).
     */
    fun calculateArea(
        room: RoomDimensions,
        shape: RoomShape = RoomShape.RECTANGLE
    ): Double {
        return when (shape) {
            RoomShape.RECTANGLE -> room.lengthMm * room.widthMm
            RoomShape.SQUARE -> room.lengthMm * room.lengthMm
            RoomShape.L_SHAPE -> {
                val fullArea = room.lengthMm * room.widthMm
                val cutoutArea = room.cutoutLengthMm * room.cutoutWidthMm
                max(0.0, fullArea - cutoutArea)
            }
        }
    }

    /**
     * Calculates perimeter in millimeters (mm).
     * For orthogonal L-shape where cutout is on a corner, perimeter is equal to 2 * (L + W).
     */
    fun calculatePerimeter(
        room: RoomDimensions,
        shape: RoomShape = RoomShape.RECTANGLE
    ): Double {
        return when (shape) {
            RoomShape.RECTANGLE -> 2.0 * (room.lengthMm + room.widthMm)
            RoomShape.SQUARE -> 4.0 * room.lengthMm
            RoomShape.L_SHAPE -> 2.0 * (room.lengthMm + room.widthMm)
        }
    }

    /**
     * Calculates gross wall surface area in square millimeters (mm²).
     * Wall Area = Perimeter × Wall Height
     */
    fun calculateWallArea(
        room: RoomDimensions,
        wallHeightMm: Double,
        shape: RoomShape = RoomShape.RECTANGLE
    ): Double {
        val perimeter = calculatePerimeter(room, shape)
        return perimeter * wallHeightMm
    }

    /**
     * Calculates total area of door openings in mm².
     */
    fun calculateDoorsArea(doors: List<Door>, defaultHeightMm: Double = 2000.0): Double {
        return doors.sumOf { it.widthMm * defaultHeightMm }
    }

    /**
     * Calculates total area of window openings in mm².
     */
    fun calculateWindowsArea(windows: List<Window>, defaultHeightMm: Double = 1200.0): Double {
        return windows.sumOf { it.widthMm * (it.heightMm ?: defaultHeightMm) }
    }

    /**
     * Calculates net wall area (Gross Wall Area - Doors Area - Windows Area).
     */
    fun calculateNetWallArea(
        room: RoomDimensions,
        wallHeightMm: Double,
        doors: List<Door>,
        windows: List<Window>,
        shape: RoomShape = RoomShape.RECTANGLE
    ): Double {
        val gross = calculateWallArea(room, wallHeightMm, shape)
        val doorsArea = calculateDoorsArea(doors, wallHeightMm.coerceAtMost(2000.0))
        val windowsArea = calculateWindowsArea(windows)
        return max(0.0, gross - doorsArea - windowsArea)
    }

    /**
     * Calculates total linear length of openings:
     * returns Pair(doorsTotalLengthMm, windowsTotalLengthMm)
     */
    fun calculateOpeningsLength(
        doors: List<Door>,
        windows: List<Window>
    ): Pair<Double, Double> {
        val doorLen = doors.sumOf { it.widthMm }
        val windowLen = windows.sumOf { it.widthMm }
        return Pair(doorLen, windowLen)
    }

    /**
     * Calculates total furniture footprint area in mm².
     */
    fun calculateFurnitureFootprint(furniture: List<FurnitureItem>): Double {
        return furniture.sumOf { it.widthMm * it.depthMm }
    }

    /**
     * Calculates room utilization percentage (total furniture footprint / room floor area * 100%).
     */
    fun calculateRoomUtilization(
        furnitureAreaMm2: Double,
        roomFloorAreaMm2: Double
    ): Double {
        if (roomFloorAreaMm2 <= 0.0) return 0.0
        return (furnitureAreaMm2 / roomFloorAreaMm2) * 100.0
    }

    /**
     * Convert mm² to m²
     */
    fun mm2ToM2(mm2: Double): Double = mm2 / 1_000_000.0

    /**
     * Convert mm² to ft² (1 m² = 10.7639 ft²)
     */
    fun mm2ToFt2(mm2: Double): Double = mm2ToM2(mm2) * 10.76391041671
}
