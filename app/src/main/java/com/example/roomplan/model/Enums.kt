package com.example.roomplan.model

enum class RoomShape(val displayName: String) {
    RECTANGLE("Rectangle"),
    SQUARE("Square"),
    L_SHAPE("L-Shaped")
}

enum class WallSide(val displayName: String) {
    TOP("Top (North)"),
    RIGHT("Right (East)"),
    BOTTOM("Bottom (South)"),
    LEFT("Left (West)")
}

enum class DoorType(val displayName: String) {
    HINGED("Hinged"),
    SLIDING("Sliding")
}

enum class DoorSwing(val displayName: String) {
    INWARD("Inward"),
    OUTWARD("OutWARD"),
    NONE("None")
}

enum class DimensionType(val displayName: String) {
    INSIDE("Inside wall-to-wall"),
    OUTSIDE("Outside wall-to-wall")
}
