package com.example.metaforge.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Route.Home),
    BottomNavItem("Draft", Icons.Default.Shield, Route.DraftSetup),
    BottomNavItem("Heroes", Icons.Default.MilitaryTech, Route.HeroList),
    BottomNavItem("Settings", Icons.Default.Settings, Route.Settings)
)