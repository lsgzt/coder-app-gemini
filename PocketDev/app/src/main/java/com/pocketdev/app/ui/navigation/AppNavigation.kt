package com.pocketdev.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.pocketdev.app.ui.screens.*
import com.pocketdev.app.viewmodels.EditorViewModel
import com.pocketdev.app.viewmodels.SettingsViewModel

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Editor : BottomNavItem("editor", Icons.Default.Code, "Editor")
    object Projects : BottomNavItem("projects", Icons.Default.FolderOpen, "Projects")
    object Examples : BottomNavItem("examples", Icons.Default.MenuBook, "Examples")
    object Settings : BottomNavItem("settings", Icons.Default.Settings, "Settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val editorViewModel: EditorViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    val navItems = listOf(
        BottomNavItem.Editor,
        BottomNavItem.Projects,
        BottomNavItem.Examples,
        BottomNavItem.Settings
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Projects.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Editor.route) {
                EditorScreen(
                    viewModel = editorViewModel,
                    settingsViewModel = settingsViewModel,
                    onNavigateToProjects = {
                        navController.navigate(BottomNavItem.Projects.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Projects.route) {
                ProjectsScreen(
                    viewModel = editorViewModel,
                    onOpenProject = {
                        navController.navigate(BottomNavItem.Editor.route) {
                            popUpTo(BottomNavItem.Editor.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavItem.Examples.route) {
                ExamplesScreen(
                    onLoadExample = { example ->
                        editorViewModel.newFile(example.language)
                        editorViewModel.updateCode(example.code)
                        navController.navigate(BottomNavItem.Editor.route) {
                            popUpTo(BottomNavItem.Editor.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
