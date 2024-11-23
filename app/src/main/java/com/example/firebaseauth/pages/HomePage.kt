package com.example.firebaseauth.pages

import AuthViewModel
import DTRViewModel
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.viewmodel.AuthState
import com.example.firebaseauth.ui.theme.NavItem
//import com.example.firebaseauth.viewmodel.DTRViewModel
import com.example.googlemappage.MapPage
import com.google.android.gms.maps.model.LatLng


@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.observeAsState()
    val dtrViewModel: DTRViewModel = viewModel()

    var selectedIndex by remember { mutableStateOf(0) }
    var timeInput by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // Simulating fetching the current location (replace with actual location logic)
    LaunchedEffect(Unit) {
        currentLocation = LatLng(37.7749, -122.4194) // Example location: San Francisco
    }

    // Handle unauthenticated state
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            Log.d("HomePage", "User is unauthenticated, navigating to login.")
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    // Navigation items for the bottom bar
    val navItemList = listOf(
        NavItem("Map", Icons.Default.LocationOn),
        NavItem("DTR", Icons.Default.DateRange),
        NavItem("Account", Icons.Default.AccountCircle)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                        label = { Text(text = navItem.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            onNavigateToCamera = { onBack ->
                selectedIndex = 3 // Navigate to CameraPage
                onBack(timeInput) // Pass time input back when done
            },
            onBack = { time ->
                timeInput = time // Capture returned time from CameraPage
                selectedIndex = 1 // Navigate back to DTR
            },
            authViewModel = authViewModel,
            navController = navController,
            currentLocation = currentLocation,
            dtrViewModel = dtrViewModel


        )
    }
}

@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onNavigateToCamera: (onBack: (String) -> Unit) -> Unit, // Function that accepts onBack
    onBack: (String) -> Unit, // Function to handle back navigation from CameraPage
    authViewModel: AuthViewModel,
    navController: NavController,
    currentLocation: LatLng?,
    dtrViewModel: DTRViewModel // Pass DTRViewModel as a parameter
) {
    when (selectedIndex) {
        0 -> {
            // Display the map page
            MapPage(modifier = modifier)
        }
        1 -> {
            // Display the DTR page
            DTR(
                onNavigateToCamera = {
                    // Navigate to the camera with a callback to handle back navigation
                    onNavigateToCamera { result ->
                        // Handle the result from the camera if needed
                        println("Photo result: $result")
                    }
                },
                dtrViewModel = dtrViewModel,
                getCurrentMonth = {
                    // Provide the current month as a string
                    java.time.Month.of(java.time.LocalDate.now().monthValue).name.lowercase()
                        .replaceFirstChar { it.uppercase() }
                }
            )
        }
        2 -> {
            // Display the account page
            Account(
                modifier = modifier,
                authViewModel = authViewModel,
                navController = navController
            )
        }
        3 -> {
            // Display the camera page and pass the onBack lambda for navigation
            CameraPage(onBack = onBack)
        }
    }
}

