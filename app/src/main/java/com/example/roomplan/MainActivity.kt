package com.example.roomplan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roomplan.ui.navigation.Screen
import com.example.roomplan.ui.screens.AboutScreen
import com.example.roomplan.ui.screens.FurnitureScreen
import com.example.roomplan.ui.screens.HomeScreen
import com.example.roomplan.ui.screens.PlanScreen
import com.example.roomplan.ui.screens.SavedPlansScreen
import com.example.roomplan.ui.screens.SettingsScreen
import com.example.roomplan.ui.screens.SplashScreen
import com.example.roomplan.ui.theme.RoomPlanTheme
import com.example.roomplan.ui.viewmodel.RoomPlanViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: RoomPlanViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            RoomPlanTheme(
                themeMode = uiState.settings.themeMode,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RoomPlanAppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun RoomPlanAppNavigation(viewModel: RoomPlanViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToPlan = { navController.navigate(Screen.Plan.route) },
                onNavigateToFurniture = { navController.navigate(Screen.Furniture.route) },
                onNavigateToSavedPlans = { navController.navigate(Screen.SavedPlans.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Plan.route) {
            PlanScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFurniture = { navController.navigate(Screen.Furniture.route) }
            )
        }

        composable(Screen.Furniture.route) {
            FurnitureScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlan = { navController.navigate(Screen.Plan.route) }
            )
        }

        composable(Screen.SavedPlans.route) {
            SavedPlansScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlanOpened = { navController.navigate(Screen.Plan.route) }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
