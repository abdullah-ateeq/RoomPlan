package com.example.roomplan.model

data class RoomDimensions(
    val lengthMm: Double,
    val widthMm: Double,
    val heightMm: Double? = null,
    val cutoutLengthMm: Double = 0.0,
    val cutoutWidthMm: Double = 0.0
)

data class Door(
    val id: String,
    val widthMm: Double,
    val wall: WallSide,
    val offsetMm: Double,
    val type: DoorType = DoorType.HINGED,
    val swing: DoorSwing = DoorSwing.INWARD
)

data class Window(
    val id: String,
    val widthMm: Double,
    val wall: WallSide,
    val offsetMm: Double,
    val sillHeightMm: Double? = null,
    val heightMm: Double? = null
)

data class Column(
    val id: String,
    val widthMm: Double,
    val depthMm: Double,
    val xMm: Double,
    val yMm: Double
)

data class FurnitureItem(
    val id: String,
    val type: FurnitureType,
    val name: String,
    val widthMm: Double,
    val depthMm: Double,
    val heightMm: Double? = null,
    val xMm: Double,
    val yMm: Double,
    val rotationDegrees: Int = 0
) {
    /**
     * Width along room X axis taking rotation into account (0, 90, 180, 270)
     */
    val effectiveWidthMm: Double
        get() = if (rotationDegrees % 180 == 0) widthMm else depthMm

    /**
     * Depth along room Y axis taking rotation into account (0, 90, 180, 270)
     */
    val effectiveDepthMm: Double
        get() = if (rotationDegrees % 180 == 0) depthMm else widthMm
}

data class RoomPlan(
    val id: String,
    val name: String,
    val room: RoomDimensions,
    val unit: UnitType,
    val shape: RoomShape,
    val dimensionType: DimensionType = DimensionType.INSIDE,
    val wallThicknessMm: Double = 150.0,
    val doors: List<Door> = emptyList(),
    val windows: List<Window> = emptyList(),
    val columns: List<Column> = emptyList(),
    val furniture: List<FurnitureItem> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
