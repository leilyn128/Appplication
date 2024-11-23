
package com.example.firebaseauth.pages

import AuthViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.firebaseauth.viewmodel.AuthState
import com.example.firebaseauth.R
//mport com.example.firebaseauth.viewmodel.AuthViewModel

@Composable
fun Account(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    navController: NavController
) {
    // Observe the authState
    // val authState = authViewModel.authState.observeAsState()
    val authState = authViewModel.authState.observeAsState(AuthState.Unauthenticated)


    // Watch for changes in the authState to navigate when the user logs out
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            // Navigate to login screen after successful logout
            navController.navigate("login") {
                // Clear the back stack so the user can't go back to the account page
                popUpTo("login") { inclusive = true }
                launchSingleTop = true // Prevent creating a new instance of the login page if already on it
            }
        }
    }

    // Handle loading state if required
    val loading = authState.value is AuthState.Loading
    val user = (authState.value as? AuthState.Authenticated)?.user

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF5F8C60))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { /* Handle account details action */ }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account Icon",
                    tint = Color.White
                )
            }

            Text(
                text = "Profile Details",
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            IconButton(onClick = { /* Handle edit action */ }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Icon",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Image(
            painter = painterResource(id = R.drawable.img_1), // Replace with your profile picture resource
            contentDescription = "Profile Picture",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (loading) {
            Text("Loading...")
        } else if (user != null) {
            // User profile information rows
            UserProfileInfo(label = "ID Number:", value = "764539")
            UserProfileInfo(label = "Name:", value = user.displayName ?: "Unknown")
            UserProfileInfo(label = "Email:", value = user.email ?: "No email")
            UserProfileInfo(label = "Address:", value = "456 Elm St, Cityville")
            UserProfileInfo(label = "Contact No:", value = "+9876543210")
            UserProfileInfo(label = "Date of Birth:", value = "February 2, 1992")
            UserProfileInfo(label = "Company Name:", value = "Example Corp")
            UserProfileInfo(label = "Emergency Contact:", value = "+01234")

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    authViewModel.signOut() // Call signout method
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Log Out",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun UserProfileInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}
