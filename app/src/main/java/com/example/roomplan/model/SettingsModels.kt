package com.example.roomplan.model

data class CollisionWarning(
    val title: String,
    val message: String,
    val severity: Severity = Severity.WARNING,
    val involvedItemIds: List<String> = emptyList()
) {
    enum class Severity {
        INFO,
        WARNING,
        ERROR
    }
}

data class PlanSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val showGrid: Boolean = true,
    val gridSpacingMm: Double = 250.0, // 25 cm default
    val snapToGrid: Boolean = true,
    val showDimensions: Boolean = true,
    val showFurnitureLabels: Boolean = true,
    val showClearance: Boolean = true,
    val showWallThickness: Boolean = true,
    val wallClearanceMm: Double = 100.0, // 10 cm default
    val walkingClearanceMm: Double = 800.0, // 80 cm default
    val defaultRoomLengthMm: Double = 5000.0, // 500 cm
    val defaultRoomWidthMm: Double = 4000.0, // 400 cm
    val defaultWallThicknessMm: Double = 150.0, // 15 cm
    val defaultUnit: UnitType = UnitType.CM,
    val liveCalculations: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val confirmDelete: Boolean = true,
    val restoreLastPlan: Boolean = true
) {
    enum class ThemeMode {
        SYSTEM,
        LIGHT,
        DARK
    }
}
