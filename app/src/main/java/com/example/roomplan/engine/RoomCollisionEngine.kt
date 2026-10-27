package com.example.roomplan.engine

import com.example.roomplan.model.CollisionWarning
import com.example.roomplan.model.Column
import com.example.roomplan.model.Door
import com.example.roomplan.model.DoorSwing
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.WallSide
import com.example.roomplan.model.Window

object RoomCollisionEngine {

    data class Rect(
        val left: Double,
        val top: Double,
        val right: Double,
        val bottom: Double
    ) {
        val width: Double get() = right - left
        val height: Double get() = bottom - top

        fun overlaps(other: Rect): Boolean {
            return left < other.right && right > other.left &&
                    top < other.bottom && bottom > other.top
        }

        fun contains(other: Rect): Boolean {
            return left <= other.left && right >= other.right &&
                    top <= other.top && bottom >= other.bottom
        }
    }

    fun getFurnitureRect(item: FurnitureItem): Rect {
        return Rect(
            left = item.xMm,
            top = item.yMm,
            right = item.xMm + item.effectiveWidthMm,
            bottom = item.yMm + item.effectiveDepthMm
        )
    }

    fun getColumnRect(column: Column): Rect {
        return Rect(
            left = column.xMm,
            top = column.yMm,
            right = column.xMm + column.widthMm,
            bottom = column.yMm + column.depthMm
        )
    }

    fun getDoorClearanceZone(door: Door, room: RoomDimensions): Rect {
        val swingDepth = door.widthMm
        return when (door.wall) {
            WallSide.TOP -> Rect(
                left = door.offsetMm,
                top = 0.0,
                right = door.offsetMm + door.widthMm,
                bottom = if (door.swing == DoorSwing.INWARD) swingDepth else 50.0
            )
            WallSide.BOTTOM -> Rect(
                left = door.offsetMm,
                top = if (door.swing == DoorSwing.INWARD) room.widthMm - swingDepth else room.widthMm - 50.0,
                right = door.offsetMm + door.widthMm,
                bottom = room.widthMm
            )
            WallSide.LEFT -> Rect(
                left = 0.0,
                top = door.offsetMm,
                right = if (door.swing == DoorSwing.INWARD) swingDepth else 50.0,
                bottom = door.offsetMm + door.widthMm
            )
            WallSide.RIGHT -> Rect(
                left = if (door.swing == DoorSwing.INWARD) room.lengthMm - swingDepth else room.lengthMm - 50.0,
                top = door.offsetMm,
                right = room.lengthMm,
                bottom = door.offsetMm + door.widthMm
            )
        }
    }

    fun validateDoor(door: Door, room: RoomDimensions): Boolean {
        val wallLength = when (door.wall) {
            WallSide.TOP, WallSide.BOTTOM -> room.lengthMm
            WallSide.LEFT, WallSide.RIGHT -> room.widthMm
        }
        return door.offsetMm >= 0.0 && (door.offsetMm + door.widthMm) <= wallLength
    }

    fun validateWindow(window: Window, room: RoomDimensions): Boolean {
        val wallLength = when (window.wall) {
            WallSide.TOP, WallSide.BOTTOM -> room.lengthMm
            WallSide.LEFT, WallSide.RIGHT -> room.widthMm
        }
        return window.offsetMm >= 0.0 && (window.offsetMm + window.widthMm) <= wallLength
    }

    fun isInsideRoom(rect: Rect, room: RoomDimensions, shape: RoomShape): Boolean {
        if (rect.left < 0.0 || rect.top < 0.0 ||
            rect.right > room.lengthMm || rect.bottom > room.widthMm
        ) {
            return false
        }

        if (shape == RoomShape.L_SHAPE && room.cutoutLengthMm > 0 && room.cutoutWidthMm > 0) {
            // Cutout is in the top-right corner:
            // X in [length - cutoutLength, length], Y in [0, cutoutWidth]
            val cutoutRect = Rect(
                left = room.lengthMm - room.cutoutLengthMm,
                top = 0.0,
                right = room.lengthMm,
                bottom = room.cutoutWidthMm
            )
            if (rect.overlaps(cutoutRect)) {
                return false
            }
        }
        return true
    }

    /**
     * Checks all collisions and returns a structured list of warnings.
     */
    fun checkCollisions(
        room: RoomDimensions,
        shape: RoomShape,
        furniture: List<FurnitureItem>,
        columns: List<Column>,
        doors: List<Door>,
        windows: List<Window>
    ): List<CollisionWarning> {
        val warnings = mutableListOf<CollisionWarning>()

        // 1. Validate doors against walls
        doors.forEach { door ->
            if (!validateDoor(door, room)) {
                warnings.add(
                    CollisionWarning(
                        title = "Door Position Exceeds Wall",
                        message = "Door (${door.widthMm.toInt()} mm) extends beyond the ${door.wall.displayName} wall.",
                        severity = CollisionWarning.Severity.ERROR,
                        involvedItemIds = listOf(door.id)
                    )
                )
            }
        }

        // 2. Validate windows against walls
        windows.forEach { window ->
            if (!validateWindow(window, room)) {
                warnings.add(
                    CollisionWarning(
                        title = "Window Position Exceeds Wall",
                        message = "Window (${window.widthMm.toInt()} mm) extends beyond the ${window.wall.displayName} wall.",
                        severity = CollisionWarning.Severity.ERROR,
                        involvedItemIds = listOf(window.id)
                    )
                )
            }
        }

        // 3. Check furniture inside room boundary
        val itemRects = furniture.associateWith { getFurnitureRect(it) }
        furniture.forEach { item ->
            val rect = itemRects[item] ?: getFurnitureRect(item)
            if (!isInsideRoom(rect, room, shape)) {
                warnings.add(
                    CollisionWarning(
                        title = "Outside Room Boundary",
                        message = "${item.name} extends outside the room boundaries.",
                        severity = CollisionWarning.Severity.WARNING,
                        involvedItemIds = listOf(item.id)
                    )
                )
            }
        }

        // 4. Check furniture-furniture overlap
        for (i in 0 until furniture.size) {
            for (j in i + 1 until furniture.size) {
                val itemA = furniture[i]
                val itemB = furniture[j]
                val rectA = itemRects[itemA] ?: getFurnitureRect(itemA)
                val rectB = itemRects[itemB] ?: getFurnitureRect(itemB)
                if (rectA.overlaps(rectB)) {
                    warnings.add(
                        CollisionWarning(
                            title = "Furniture Overlap",
                            message = "${itemA.name} overlaps with ${itemB.name}.",
                            severity = CollisionWarning.Severity.WARNING,
                            involvedItemIds = listOf(itemA.id, itemB.id)
                        )
                    )
                }
            }
        }

        // 5. Check furniture-column collision
        val columnRects = columns.map { it to getColumnRect(it) }
        furniture.forEach { item ->
            val fRect = itemRects[item] ?: getFurnitureRect(item)
            columnRects.forEach { (col, cRect) ->
                if (fRect.overlaps(cRect)) {
                    warnings.add(
                        CollisionWarning(
                            title = "Column Collision",
                            message = "${item.name} overlaps a structural column.",
                            severity = CollisionWarning.Severity.WARNING,
                            involvedItemIds = listOf(item.id, col.id)
                        )
                    )
                }
            }
        }

        // 6. Check furniture blocking doors
        doors.forEach { door ->
            val doorZone = getDoorClearanceZone(door, room)
            furniture.forEach { item ->
                val fRect = itemRects[item] ?: getFurnitureRect(item)
                if (fRect.overlaps(doorZone)) {
                    warnings.add(
                        CollisionWarning(
                            title = "Door Clearance Blocked",
                            message = "${item.name} is obstructing the ${door.wall.displayName} door opening / swing path.",
                            severity = CollisionWarning.Severity.WARNING,
                            involvedItemIds = listOf(item.id, door.id)
                        )
                    )
                }
            }
        }

        return warnings
    }
}
