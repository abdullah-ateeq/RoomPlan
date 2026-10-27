package com.example.roomplan.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.roomplan.model.PlanSettings

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Color.White,
    primaryContainer = BlueprintSlate,
    onPrimaryContainer = Color.White,
    secondary = AccentSky,
    onSecondary = Color.White,
    background = BlueprintDark,
    surface = BlueprintNavy,
    surfaceVariant = BlueprintSlate,
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    outline = BlueprintMuted
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlueDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = PrimaryBlueDark,
    secondary = BlueprintSlate,
    onSecondary = Color.White,
    background = BlueprintSurface,
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = BlueprintDark,
    onSurface = BlueprintDark,
    outline = BlueprintBorder
)

@Composable
fun RoomPlanTheme(
    themeMode: PlanSettings.ThemeMode = PlanSettings.ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        PlanSettings.ThemeMode.SYSTEM -> isSystemInDarkTheme()
        PlanSettings.ThemeMode.LIGHT -> false
        PlanSettings.ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
