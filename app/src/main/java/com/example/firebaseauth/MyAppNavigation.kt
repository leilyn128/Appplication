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
    val userRole by remember { mutableStateOf("employee") } // Default to employee (you'll fetch this dynamically)

    // Create a LocationHelper instance
    val locationHelper = remember {
        LocationHelper(
            context = context,
            onLocationUpdate = {} // We will pass the update logic to the MapPage
        )
    }

    // Fetch user role from Firestore or some authentication service
    LaunchedEffect(Unit) {
        // Ideally, you'd fetch the role from Firestore or an authenticated user profile
        // Example: userRole = firestoreHelper.getUserRole(userId)
    }

    // Determine the starting destination based on authentication and role
    val startDestination = if (authState is AuthState.Authenticated) {
        if (userRole == "admin") Screen.Home.route else Screen.Map.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Login Page
        composable(Screen.Login.route) {
            LoginPage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // After login, fetch the user role and navigate accordingly
                    // Set the userRole variable dynamically here (e.g., from Firestore)
                    navController.navigate(if (userRole == "admin") Screen.Home.route else Screen.Map.route) {
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
                    // Navigate to Home on successful signup
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Page (Admin or Employee view)
        composable(Screen.Home.route) {
            HomePage(
                modifier = modifier,
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Map Page (Employee view or any specific geofencing functionality)
        composable(Screen.Map.route) {
            MapPage(
                modifier = modifier
            )
        }
    }
}
