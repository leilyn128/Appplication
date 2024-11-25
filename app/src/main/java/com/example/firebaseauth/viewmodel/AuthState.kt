package com.example.firebaseauth.viewmodel



import com.example.firebaseauth.viewmodel.AuthState.AuthResult
import com.google.firebase.auth.FirebaseUser

// Sealed class to represent different authentication states
sealed class AuthState {

        object Unauthenticated : AuthState()
        object Loading : AuthState()
        data class Authenticated(val user: FirebaseUser? = null) : AuthState()
        data class AdminAuthenticated(val user: FirebaseUser) : AuthState()
        data class EmployeeAuthenticated(val user: FirebaseUser) : AuthState()
        data class Error(val message: String) : AuthState()
        object None : AuthState()
        data class LoggedIn(val email: String, val role: String) : AuthState()

    sealed class AuthResult {
        // Success: Store the FirebaseUser object on successful authentication
        data class Success(val user: FirebaseUser) :
            com.example.firebaseauth.viewmodel.AuthState.AuthResult()

        data class Failure(val message: String?) :
            com.example.firebaseauth.viewmodel.AuthState.AuthResult()

        object LoggedOut : com.example.firebaseauth.viewmodel.AuthState.AuthResult()



    }
}
