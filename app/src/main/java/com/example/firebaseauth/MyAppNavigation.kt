package com.example.firebaseauth

import AuthViewModel
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
import com.example.firebaseauth.pages.SignupPage
import com.example.firebaseauth.viewmodel.AuthState
import com.example.googlemappage.MapPage
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.*

// Define your screens
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

    val isAuthenticated = authViewModel.isAuthenticated

    val context = LocalContext.current

    val authState by authViewModel.authState.observeAsState(AuthState.Unauthenticated)
    //val authState = authViewModel.authState.observeAsState(AuthState.Unauthenticated).value


    // Create a LocationHelper instance
    val locationHelper = remember {
        LocationHelper(
            context = context,
            onLocationUpdate = {} // We will pass the update logic to the MapPage
        )
    }


    NavHost(
        navController = navController,
        startDestination = if (authState is AuthState.Authenticated) Screen.Home.route else Screen.Login.route
    ) {
        // Define your composable screens here


    // Login Page
        composable(Screen.Login.route) {
            LoginPage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Navigate to Home on successful login
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Signup Page
        composable(Screen.Signup.route) {
            SignupPage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                onSignUpSuccess = {
                    // Navigate to Home on successful login
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }

                }
            )
        }

        // Home Page (includes bottom navigation)
        composable(Screen.Home.route) {
            HomePage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Map Page
        composable(Screen.Map.route) {
            MapPage(
                modifier = modifier,



            )
        }
    }
}
