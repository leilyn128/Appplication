package com.example.firebaseauth.pages

import AuthViewModel
import CameraPage
import DTRViewModel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import com.example.firebaseauth.activity.GeofenceHelper
import com.example.firebaseauth.ui.theme.NavItem


@Composable
fun AdminHomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    role: String,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current // Get context for GeofenceHelper
    val geofenceHelper = GeofenceHelper(context) // Pass context to GeofenceHelper
    var selectedIndex by remember { mutableStateOf(0) }

    // Define navigation items with icons
    val navItems = listOf(
        NavItem("Geofence", Icons.Default.LocationOn),
        NavItem("DTR Records", Icons.Default.List),
        NavItem("Account", Icons.Default.AccountCircle)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                        label = { Text(navItem.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Apply innerPadding to the modifier for each page
        when (selectedIndex) {
            0 -> {
                // Admin Geofence Page
                GeofencePage(
                    navController = navController,
                    geofenceHelper = geofenceHelper,
                    onGeofenceAdded = { geofence ->
                        // Handle geofence addition logic
                    },
                    modifier = Modifier.padding(innerPadding) // Apply padding here
                )
            }
            1 -> {
                // Admin DTR Record Page
                DTRRecordPage(
                    modifier = Modifier.padding(innerPadding) // Apply padding here
                )
            }
            2 -> {
                // Admin Account Page
                Account(
                    authViewModel = authViewModel,
                    navController = navController,
                    modifier = Modifier.padding(innerPadding) // Apply padding here
                )
            }
        }
    }
}
