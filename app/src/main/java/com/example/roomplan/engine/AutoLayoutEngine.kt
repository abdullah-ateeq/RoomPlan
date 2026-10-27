package com.example.roomplan.engine

import com.example.roomplan.model.Column
import com.example.roomplan.model.Door
import com.example.roomplan.model.FurnitureItem
import com.example.roomplan.model.FurnitureType
import com.example.roomplan.model.RoomDimensions
import com.example.roomplan.model.RoomPlan
import com.example.roomplan.model.RoomShape
import com.example.roomplan.model.Window
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object AutoLayoutEngine {

    data class LayoutResult(
        val success: Boolean,
        val items: List<FurnitureItem>,
        val errorMessage: String? = null
    )

    fun arrange(
        plan: RoomPlan,
        wallClearanceMm: Double = 100.0,
        walkingClearanceMm: Double = 800.0
    ): LayoutResult {
        return arrange(
            room = plan.room,
            shape = plan.shape,
            furniture = plan.furniture,
            doors = plan.doors,
            windows = plan.windows,
            columns = plan.columns,
            wallClearanceMm = wallClearanceMm,
            walkingClearanceMm = walkingClearanceMm
        )
    }

    fun arrange(
        room: RoomDimensions,
        shape: RoomShape,
        furniture: List<FurnitureItem>,
        doors: List<Door>,
        windows: List<Window>,
        columns: List<Column>,
        wallClearanceMm: Double = 100.0,
        walkingClearanceMm: Double = 800.0
    ): LayoutResult {
        if (furniture.isEmpty()) {
            return LayoutResult(success = true, items = emptyList())
        }

        // Sort items deterministically: wall-preferred first, then by area descending
        val sortedToPlace = furniture.sortedWith(
            compareByDescending<FurnitureItem> { it.type.isWallPreferred }
                .thenByDescending { it.widthMm * it.depthMm }
                .thenBy { it.name }
        )

        val placedItems = mutableListOf<FurnitureItem>()
        val doorClearanceZones = doors.map { RoomCollisionEngine.getDoorClearanceZone(it, room) }
        val columnRects = columns.map { RoomCollisionEngine.getColumnRect(it) }

        for (item in sortedToPlace) {
            val bestPlacement = findBestPlacement(
                item = item,
                room = room,
                shape = shape,
                alreadyPlaced = placedItems,
                doorZones = doorClearanceZones,
                columnRects = columnRects,
                wallClearanceMm = wallClearanceMm,
                walkingClearanceMm = walkingClearanceMm
            )

            if (bestPlacement == null) {
                return LayoutResult(
                    success = false,
                    items = furniture, // keep original
                    errorMessage = "No suitable automatic arrangement found. Try removing an item or reducing clearance."
                )
            }
            placedItems.add(bestPlacement)
        }

        // Return sorted back into original order IDs
        val placedMap = placedItems.associateBy { it.id }
        val orderedResult = furniture.map { placedMap[it.id] ?: it }

        return LayoutResult(
            success = true,
            items = orderedResult
        )
    }

    private fun findBestPlacement(
        item: FurnitureItem,
        room: RoomDimensions,
        shape: RoomShape,
        alreadyPlaced: List<FurnitureItem>,
        doorZones: List<RoomCollisionEngine.Rect>,
        columnRects: List<RoomCollisionEngine.Rect>,
        wallClearanceMm: Double,
        walkingClearanceMm: Double
    ): FurnitureItem? {
        val candidateRotations = if (item.type.isWallPreferred) {
            listOf(0, 90, 180, 270)
        } else {
            listOf(0, 90)
        }

        var bestCandidate: FurnitureItem? = null
        var bestScore = Double.NEGATIVE_INFINITY

        val stepMm = 200.0 // Grid step for evaluation

        for (rot in candidateRotations) {
            val candidateItemTemplate = item.copy(rotationDegrees = rot)
            val effWidth = candidateItemTemplate.effectiveWidthMm
            val effDepth = candidateItemTemplate.effectiveDepthMm

            if (effWidth > room.lengthMm || effDepth > room.widthMm) {
                continue
            }

            // Generate candidates
            val candidatePositions = mutableListOf<Pair<Double, Double>>()

            if (item.type == FurnitureType.DINING_CHAIR) {
                // Place near already placed dining table if one exists
                val table = alreadyPlaced.firstOrNull { it.type == FurnitureType.DINING_TABLE }
                if (table != null) {
                    val tLeft = table.xMm
                    val tRight = table.xMm + table.effectiveWidthMm
                    val tTop = table.yMm
                    val tBottom = table.yMm + table.effectiveDepthMm

                    // Along top edge of table
                    candidatePositions.add(Pair(tLeft + (table.effectiveWidthMm - effWidth) / 2, max(0.0, tTop - effDepth - 50.0)))
                    // Along bottom edge
                    candidatePositions.add(Pair(tLeft + (table.effectiveWidthMm - effWidth) / 2, tBottom + 50.0))
                    // Along left edge
                    candidatePositions.add(Pair(max(0.0, tLeft - effWidth - 50.0), tTop + (table.effectiveDepthMm - effDepth) / 2))
                    // Along right edge
                    candidatePositions.add(Pair(tRight + 50.0, tTop + (table.effectiveDepthMm - effDepth) / 2))
                }
            }

            if (candidatePositions.isEmpty()) {
                if (item.type.isWallPreferred) {
                    // Try wall positions
                    // Top wall: Y = wallClearanceMm
                    var x = wallClearanceMm
                    while (x + effWidth <= room.lengthMm - wallClearanceMm) {
                        candidatePositions.add(Pair(x, wallClearanceMm))
                        x += stepMm
                    }
                    // Bottom wall: Y = room.width - effDepth - wallClearanceMm
                    x = wallClearanceMm
                    val bottomY = max(0.0, room.widthMm - effDepth - wallClearanceMm)
                    while (x + effWidth <= room.lengthMm - wallClearanceMm) {
                        candidatePositions.add(Pair(x, bottomY))
                        x += stepMm
                    }
                    // Left wall: X = wallClearanceMm
                    var y = wallClearanceMm
                    while (y + effDepth <= room.widthMm - wallClearanceMm) {
                        candidatePositions.add(Pair(wallClearanceMm, y))
                        y += stepMm
                    }
                    // Right wall: X = room.length - effWidth - wallClearanceMm
                    y = wallClearanceMm
                    val rightX = max(0.0, room.lengthMm - effWidth - wallClearanceMm)
                    while (y + effDepth <= room.widthMm - wallClearanceMm) {
                        candidatePositions.add(Pair(rightX, y))
                        y += stepMm
                    }
                } else {
                    // Center / intermediate positions for tables, chairs, etc.
                    val startX = min(room.lengthMm / 2 - effWidth / 2, max(wallClearanceMm, room.lengthMm * 0.25))
                    val endX = max(startX, room.lengthMm * 0.75 - effWidth)
                    val startY = min(room.widthMm / 2 - effDepth / 2, max(wallClearanceMm, room.widthMm * 0.25))
                    val endY = max(startY, room.widthMm * 0.75 - effDepth)

                    var cx = startX
                    while (cx <= endX) {
                        var cy = startY
                        while (cy <= endY) {
                            candidatePositions.add(Pair(cx, cy))
                            cy += stepMm
                        }
                        cx += stepMm
                    }
                }
            }

            // Fallback grid sampling if no candidates yet
            if (candidatePositions.isEmpty()) {
                var gx = wallClearanceMm
                while (gx + effWidth <= room.lengthMm - wallClearanceMm) {
                    var gy = wallClearanceMm
                    while (gy + effDepth <= room.widthMm - wallClearanceMm) {
                        candidatePositions.add(Pair(gx, gy))
                        gy += stepMm * 2
                    }
                    gx += stepMm * 2
                }
            }

            // Evaluate each position
            for ((candX, candY) in candidatePositions) {
                val candItem = item.copy(xMm = candX, yMm = candY, rotationDegrees = rot)
                val candRect = RoomCollisionEngine.getFurnitureRect(candItem)

                // 1. Must be completely inside room
                if (!RoomCollisionEngine.isInsideRoom(candRect, room, shape)) {
                    continue
                }

                // 2. Must not collide with columns
                if (columnRects.any { it.overlaps(candRect) }) {
                    continue
                }

                // 3. Must not block doors
                if (doorZones.any { it.overlaps(candRect) }) {
                    continue
                }

                // 4. Must not overlap already placed furniture
                var overlapsExisting = false
                for (placed in alreadyPlaced) {
                    val pRect = RoomCollisionEngine.getFurnitureRect(placed)
                    if (pRect.overlaps(candRect)) {
                        overlapsExisting = true
                        break
                    }
                }
                if (overlapsExisting) {
                    continue
                }

                // Calculate deterministic score
                val score = computePlacementScore(
                    candItem = candItem,
                    candRect = candRect,
                    room = room,
                    alreadyPlaced = alreadyPlaced,
                    doorZones = doorZones,
                    wallClearanceMm = wallClearanceMm
                )

                if (score > bestScore) {
                    bestScore = score
                    bestCandidate = candItem
                }
            }
        }

        return bestCandidate
    }

    private fun computePlacementScore(
        candItem: FurnitureItem,
        candRect: RoomCollisionEngine.Rect,
        room: RoomDimensions,
        alreadyPlaced: List<FurnitureItem>,
        doorZones: List<RoomCollisionEngine.Rect>,
        wallClearanceMm: Double
    ): Double {
        var score = 1000.0

        // Distance to closest wall
        val distToLeft = candRect.left
        val distToTop = candRect.top
        val distToRight = room.lengthMm - candRect.right
        val distToBottom = room.widthMm - candRect.bottom
        val minDistToWall = minOf(distToLeft, distToTop, distToRight, distToBottom)

        if (candItem.type.isWallPreferred) {
            // Reward alignment against wall
            val wallOffsetDiff = abs(minDistToWall - wallClearanceMm)
            score += max(0.0, 500.0 - wallOffsetDiff * 0.5)
        } else {
            // Non-wall items (tables) prefer more central placement
            val centerDistX = abs((candRect.left + candRect.right) / 2 - room.lengthMm / 2)
            val centerDistY = abs((candRect.top + candRect.bottom) / 2 - room.widthMm / 2)
            score += max(0.0, 400.0 - (centerDistX + centerDistY) * 0.2)
        }

        // Keep distance from doors
        for (doorZone in doorZones) {
            val dx = max(0.0, max(doorZone.left - candRect.right, candRect.left - doorZone.right))
            val dy = max(0.0, max(doorZone.top - candRect.bottom, candRect.top - doorZone.bottom))
            val dist = dx + dy
            if (dist < 400.0) {
                score -= (400.0 - dist) * 2.0
            }
        }

        // Distance from other placed furniture (avoid suffocating crowding)
        for (placed in alreadyPlaced) {
            val pRect = RoomCollisionEngine.getFurnitureRect(placed)
            val dx = max(0.0, max(pRect.left - candRect.right, candRect.left - pRect.right))
            val dy = max(0.0, max(pRect.top - candRect.bottom, candRect.top - pRect.bottom))
            val dist = dx + dy
            if (dist < 100.0) {
                score -= 100.0
            }
        }

        return score
    }
}
