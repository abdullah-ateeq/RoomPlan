package com.example.roomplan.ui.navigation

sealed class Screen(val route: String, val title: String) {
    object Splash : Screen("splash", "Splash")
    object Home : Screen("home", "Room Setup")
    object Plan : Screen("plan", "Floor Plan")
    object Furniture : Screen("furniture", "Furniture")
    object SavedPlans : Screen("saved_plans", "Saved Plans")
    object Settings : Screen("settings", "Settings")
    object About : Screen("about", "About")
}
