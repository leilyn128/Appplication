package com.example.firebaseauth

import AuthViewModel
import CameraPage
import android.util.Log
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
import com.example.googlemappage.MapPage
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.*
import com.example.firebaseauth.pages.Account
import com.example.firebaseauth.pages.AccountAdmin
import com.example.firebaseauth.pages.AdminHomePage


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
    val navController = rememberNavController()

    // Observe the auth state
    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    val userRole by authViewModel.userRole.observeAsState()
    val context = LocalContext.current
    val locationHelper = remember {
        LocationHelper(
            context = context,
            onLocationUpdate = {} // Pass the location update logic to the MapPage
        )
    }

    // Determine the start destination based on the authentication state and role
    val startDestination = when (authState) {
        is AuthState.EmployeeAuthenticated -> Screen.Home.route
        is AuthState.AdminAuthenticated -> "adminHome" // Admin's custom home route
        else -> Screen.Login.route
    }
    NavHost(
        navController = navController,
        startDestination = startDestination
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

        // Map Page
        composable(Screen.Map.route) {
            MapPage(modifier = modifier)
        }

        // Camera Page
        composable("camera_page") {
            CameraPage(
                onImageCaptured = { bitmap ->
                    Log.d("CameraPage", "Image captured successfully.")
                },
                onBack = {
                    navController.popBackStack()
                    Log.d("BackPressed", "Navigated back from CameraPage.")
                }
            )
        }
    }
}