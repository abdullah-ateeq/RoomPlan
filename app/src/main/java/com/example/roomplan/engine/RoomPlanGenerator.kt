package com.example.roomplan.engine

import com.example.roomplan.model.DimensionType
import com.example.roomplan.model.Door
import com.example.roomplan.model.DoorSwing
import com.example.roomplan.model.DoorType
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.FurnitureType
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomPlan
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.UnitType
import com.example.roomplan.model.WallSide
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

object RoomPlanGenerator {

    data class ViewportTransform(
        val scale: Float,
        val offsetX: Float,
        val offsetY: Float,
        val canvasWidth: Float,
        val canvasHeight: Float
    ) {
        fun mmToCanvasX(xMm: Double): Float = (xMm.toFloat() * scale) + offsetX
        fun mmToCanvasY(yMm: Double): Float = (yMm.toFloat() * scale) + offsetY
        fun canvasToMmX(canvasX: Float): Double = ((canvasX - offsetX) / scale).toDouble()
        fun canvasToMmY(canvasY: Float): Double = ((canvasY - offsetY) / scale).toDouble()
        fun lengthMmToCanvas(lengthMm: Double): Float = lengthMm.toFloat() * scale
    }

    /**
     * Calculates the display scale and centering offset.
     * Scale = min((canvasWidth - 2 * padding) / totalWidthMm, (canvasHeight - 2 * padding) / totalHeightMm)
     * Preserves exact aspect ratio.
     */
    fun calculateViewport(
        canvasWidth: Float,
        canvasHeight: Float,
        room: RoomDimensions,
        wallThicknessMm: Double,
        showWallThickness: Boolean,
        padding: Float = 60f,
        zoomLevel: Float = 1.0f,
        panOffsetX: Float = 0f,
        panOffsetY: Float = 0f
    ): ViewportTransform {
        val extra = if (showWallThickness) wallThicknessMm * 2 else 0.0
        val totalWidthMm = max(100.0, room.lengthMm + extra)
        val totalHeightMm = max(100.0, room.widthMm + extra)

        val availWidth = max(50f, canvasWidth - 2 * padding)
        val availHeight = max(50f, canvasHeight - 2 * padding)

        val baseScale = min(
            availWidth / totalWidthMm.toFloat(),
            availHeight / totalHeightMm.toFloat()
        )

        val effectiveScale = baseScale * zoomLevel

        // Centering offset
        val drawnWidth = totalWidthMm.toFloat() * effectiveScale
        val drawnHeight = totalHeightMm.toFloat() * effectiveScale

        val baseOffsetX = (canvasWidth - drawnWidth) / 2f
        val baseOffsetY = (canvasHeight - drawnHeight) / 2f

        return ViewportTransform(
            scale = effectiveScale,
            offsetX = baseOffsetX + panOffsetX,
            offsetY = baseOffsetY + panOffsetY,
            canvasWidth = canvasWidth,
            canvasHeight = canvasHeight
        )
    }

    /**
     * Snap coordinate in mm to nearest grid interval.
     */
    fun snapToGrid(valueMm: Double, gridSpacingMm: Double): Double {
        if (gridSpacingMm <= 0.0) return valueMm
        return round(valueMm / gridSpacingMm) * gridSpacingMm
    }

    /**
     * Returns the 4 corner points of a rectangular room or 6 corner points of an L-shaped room.
     */
    fun getRoomPolygonPoints(
        room: RoomDimensions,
        shape: RoomShape
    ): List<Pair<Double, Double>> {
        return when (shape) {
            RoomShape.RECTANGLE, RoomShape.SQUARE -> {
                listOf(
                    Pair(0.0, 0.0),
                    Pair(room.lengthMm, 0.0),
                    Pair(room.lengthMm, room.widthMm),
                    Pair(0.0, room.widthMm)
                )
            }
            RoomShape.L_SHAPE -> {
                val cutL = room.cutoutLengthMm
                val cutW = room.cutoutWidthMm
                if (cutL > 0 && cutW > 0 && cutL < room.lengthMm && cutW < room.widthMm) {
                    listOf(
                        Pair(0.0, 0.0),
                        Pair(room.lengthMm - cutL, 0.0),
                        Pair(room.lengthMm - cutL, cutW),
                        Pair(room.lengthMm, cutW),
                        Pair(room.lengthMm, room.widthMm),
                        Pair(0.0, room.widthMm)
                    )
                } else {
                    listOf(
                        Pair(0.0, 0.0),
                        Pair(room.lengthMm, 0.0),
                        Pair(room.lengthMm, room.widthMm),
                        Pair(0.0, room.widthMm)
                    )
                }
            }
        }
    }
}
