package com.example.roomplan.data

import android.content.Context
import android.content.SharedPreferences
import com.example.roomplan.model.PlanSettings
import com.example.roomplan.model.UnitType

class SettingsStorage(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("room_plan_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_THEME = "key_theme"
        private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"
        private const val KEY_SHOW_GRID = "key_show_grid"
        private const val KEY_GRID_SPACING = "key_grid_spacing"
        private const val KEY_SNAP_GRID = "key_snap_grid"
        private const val KEY_SHOW_DIMENSIONS = "key_show_dimensions"
        private const val KEY_SHOW_LABELS = "key_show_labels"
        private const val KEY_SHOW_CLEARANCE = "key_show_clearance"
        private const val KEY_SHOW_WALL_THICKNESS = "key_show_wall_thickness"
        private const val KEY_WALL_CLEARANCE = "key_wall_clearance"
        private const val KEY_WALK_CLEARANCE = "key_walk_clearance"
        private const val KEY_DEF_LENGTH = "key_def_length"
        private const val KEY_DEF_WIDTH = "key_def_width"
        private const val KEY_DEF_WALL_THICKNESS = "key_def_wall_thickness"
        private const val KEY_DEF_UNIT = "key_def_unit"
        private const val KEY_LIVE_CALCS = "key_live_calcs"
        private const val KEY_HAPTICS = "key_haptics"
        private const val KEY_ANIMATIONS = "key_animations"
        private const val KEY_CONFIRM_DELETE = "key_confirm_delete"
        private const val KEY_RESTORE_LAST = "key_restore_last"
    }

    fun getSettings(): PlanSettings {
        val themeStr = prefs.getString(KEY_THEME, PlanSettings.ThemeMode.SYSTEM.name)
        val theme = try {
            PlanSettings.ThemeMode.valueOf(themeStr ?: PlanSettings.ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            PlanSettings.ThemeMode.SYSTEM
        }

        val unitStr = prefs.getString(KEY_DEF_UNIT, UnitType.CM.name)
        val unit = try {
            UnitType.valueOf(unitStr ?: UnitType.CM.name)
        } catch (e: Exception) {
            UnitType.CM
        }

        return PlanSettings(
            themeMode = theme,
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            showGrid = prefs.getBoolean(KEY_SHOW_GRID, true),
            gridSpacingMm = prefs.getFloat(KEY_GRID_SPACING, 250f).toDouble(),
            snapToGrid = prefs.getBoolean(KEY_SNAP_GRID, true),
            showDimensions = prefs.getBoolean(KEY_SHOW_DIMENSIONS, true),
            showFurnitureLabels = prefs.getBoolean(KEY_SHOW_LABELS, true),
            showClearance = prefs.getBoolean(KEY_SHOW_CLEARANCE, true),
            showWallThickness = prefs.getBoolean(KEY_SHOW_WALL_THICKNESS, true),
            wallClearanceMm = prefs.getFloat(KEY_WALL_CLEARANCE, 100f).toDouble(),
            walkingClearanceMm = prefs.getFloat(KEY_WALK_CLEARANCE, 800f).toDouble(),
            defaultRoomLengthMm = prefs.getFloat(KEY_DEF_LENGTH, 5000f).toDouble(),
            defaultRoomWidthMm = prefs.getFloat(KEY_DEF_WIDTH, 4000f).toDouble(),
            defaultWallThicknessMm = prefs.getFloat(KEY_DEF_WALL_THICKNESS, 150f).toDouble(),
            defaultUnit = unit,
            liveCalculations = prefs.getBoolean(KEY_LIVE_CALCS, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS, true),
            confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true),
            restoreLastPlan = prefs.getBoolean(KEY_RESTORE_LAST, true)
        )
    }

    fun saveSettings(settings: PlanSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, settings.dynamicColor)
            .putBoolean(KEY_SHOW_GRID, settings.showGrid)
            .putFloat(KEY_GRID_SPACING, settings.gridSpacingMm.toFloat())
            .putBoolean(KEY_SNAP_GRID, settings.snapToGrid)
            .putBoolean(KEY_SHOW_DIMENSIONS, settings.showDimensions)
            .putBoolean(KEY_SHOW_LABELS, settings.showFurnitureLabels)
            .putBoolean(KEY_SHOW_CLEARANCE, settings.showClearance)
            .putBoolean(KEY_SHOW_WALL_THICKNESS, settings.showWallThickness)
            .putFloat(KEY_WALL_CLEARANCE, settings.wallClearanceMm.toFloat())
            .putFloat(KEY_WALK_CLEARANCE, settings.walkingClearanceMm.toFloat())
            .putFloat(KEY_DEF_LENGTH, settings.defaultRoomLengthMm.toFloat())
            .putFloat(KEY_DEF_WIDTH, settings.defaultRoomWidthMm.toFloat())
            .putFloat(KEY_DEF_WALL_THICKNESS, settings.defaultWallThicknessMm.toFloat())
            .putString(KEY_DEF_UNIT, settings.defaultUnit.name)
            .putBoolean(KEY_LIVE_CALCS, settings.liveCalculations)
            .putBoolean(KEY_HAPTICS, settings.hapticsEnabled)
            .putBoolean(KEY_ANIMATIONS, settings.animationsEnabled)
            .putBoolean(KEY_CONFIRM_DELETE, settings.confirmDelete)
            .putBoolean(KEY_RESTORE_LAST, settings.restoreLastPlan)
            .apply()
    }

    fun resetSettings(): PlanSettings {
        prefs.edit().clear().apply()
        return PlanSettings()
    }
}
