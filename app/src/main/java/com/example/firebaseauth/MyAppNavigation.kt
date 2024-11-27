package com.example.firebaseauth

import AuthViewModel
import DTRViewModel

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebaseauth.activity.LocationHelper
import com.example.firebaseauth.pages.HomePage
import com.example.firebaseauth.pages.LoginPage
import com.example.firebaseauth.viewmodel.AuthState
import com.example.firebaseauth.pages.MapPage
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.pages.Account
import com.example.firebaseauth.pages.AccountAdmin
import com.example.firebaseauth.pages.AdminHomePage
import com.example.firebaseauth.pages.DTR
import com.example.firebaseauth.pages.SignupPage

import com.google.android.gms.location.LocationServices


sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object Map : Screen("map")
}
@Composable
fun MyAppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    currentLocation: LatLng? = null
) {
    val navController = rememberNavController()  // Make sure this is initialized
    val dtrViewModel = DTRViewModel()
    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    val userRole by authViewModel.userRole.observeAsState()
    val context = LocalContext.current
    val locationHelper = remember {
        LocationHelper(
            context = context,
            onLocationUpdate = {} // Pass the location update logic to the MapPage
        )
    }

    // Define the start destination based on auth state
    val startDestination = when (authState) {
        is AuthState.EmployeeAuthenticated -> Screen.Home.route
        is AuthState.AdminAuthenticated -> "adminHome"
        else -> Screen.Login.route
    }

    // Use Scaffold to set up layout
    Scaffold(
        bottomBar = {
            // Only show the bottom navigation on specific screens
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute in listOf("home", "map", "Dtr")) {
                // Pass the navController to the NavigationBar here
            }
        },
        content = { innerPadding ->
            // Provide the content of the screen (NavHost for navigation)
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = modifier.padding(innerPadding) // Apply padding from Scaffold
            ) {
                // Login Page
                composable("login") {
                    LoginPage(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        onLoginSuccess = {
                            val userEmail = authViewModel.auth.currentUser?.email ?: ""
                            val role = authViewModel.assignRoleBasedOnEmail(userEmail)

                            when (role) {
                                "admin" -> navController.navigate("adminHome") {
                                    popUpTo("login") { inclusive = true }
                                }

                                "employee" -> navController.navigate("homepage") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        }
                    )
                }

                // Signup Page
                composable("signup") {
                    SignupPage(
                        modifier = Modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        onSignUpSuccess = {
                            navController.navigate("home")
                        }
                    )
                }

                // Admin Home Page
                composable("adminHome") {
                    AdminHomePage(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        role = "admin"
                    )
                }

                // Employee Home Page
                composable("homepage") {
                    HomePage(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel,
                        role = "employee"
                    )
                }

                // Account Pages
                composable("account") {
                    Account(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                composable("accountAdmin") {
                    AccountAdmin(
                        modifier = modifier,
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }

                // DTR Page (Employee Time Record)
                composable("Dtr") {

                    val dtrViewModel: DTRViewModel = viewModel()
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(LocalContext.current)


                    DTR(
                        viewModel = dtrViewModel,
                        employeeId = "someEmployeeId", // Pass the employeeId as well
                        fusedLocationClient = fusedLocationClient
                    )


                    // Map Page
                    composable(Screen.Map.route) {
                        MapPage(modifier = modifier)
                    }
                }
            }
        }
    )

}
