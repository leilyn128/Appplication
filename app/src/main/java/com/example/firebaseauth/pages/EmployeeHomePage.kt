package com.example.firebaseauth.pages

import AuthViewModel
import DTRViewModel
import android.util.Log
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
import com.example.firebaseauth.viewmodel.AuthState
import com.example.firebaseauth.ui.theme.NavItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng


@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    role: String,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.observeAsState()
    val dtrViewModel: DTRViewModel = viewModel()

    // Retrieve user role from authViewModel
    val userRole = authViewModel.userDetails.value?.role ?: "employee"

    var selectedIndex by remember { mutableStateOf(0) }
    var timeInput by remember { mutableStateOf("") }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    // Simulate fetching the current location
    LaunchedEffect(Unit) {
        currentLocation = LatLng(37.7749, -122.4194) // Example location
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

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
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label
                            )
                        },
                        label = { Text(text = navItem.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        ContentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            userRole = userRole, // Pass user role to ContentScreen
            authViewModel = authViewModel,
            navController = navController,
            currentLocation = currentLocation,
            dtrViewModel  = dtrViewModel,
        )
    }
}


@Composable
fun ContentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    authViewModel: AuthViewModel,
    navController: NavController,
    currentLocation: LatLng?,
    dtrViewModel: DTRViewModel,  // Ensure the ViewModel is passed correctly
    userRole: String // Add userRole parameter
) {
    val context = LocalContext.current // Get context

    // Initialize FusedLocationProviderClient
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    when (selectedIndex) {
        0 -> {
            // Display the map page
            MapPage(modifier = modifier)
        }

        1 -> {
            // Display the DTR page
            DTR(
                viewModel = dtrViewModel,  // Pass the viewModel as expected by DTR composable
                employeeId = "someEmployeeId",  // Pass employeeId
                fusedLocationClient = fusedLocationClient  // Pass fusedLocationClient
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
            // Handle other screens
        }
    }
}



