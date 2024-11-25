import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.firebaseauth.model.UserModel
import com.example.firebaseauth.viewmodel.AuthState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.State
import androidx.navigation.NavController

//import com.example.firebaseauth.login.AccountType
//import com.google.android.gms.common.internal.AccountType


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableLiveData<AuthState>(AuthState.Unauthenticated)
    val authState: LiveData<AuthState> get() = _authState

    private val _userDetails = MutableLiveData<UserModel>(UserModel())
    val userDetails: LiveData<UserModel> get() = _userDetails

    private val _userModel = mutableStateOf(UserModel())
    val userModel: State<UserModel> = _userModel

    val user = FirebaseAuth.getInstance().currentUser
    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> get() = _userRole


    val authStatus: MutableLiveData<AuthState.AuthResult> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    val isAuthenticated: Boolean
        get() = FirebaseAuth.getInstance().currentUser != null

    init {
        checkAuthState()
    }

    var UserModel = mutableStateOf(UserModel())

    private fun checkAuthState() {
        val user = auth.currentUser
        if (user != null) {
            _authState.value = AuthState.Authenticated(user)
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun updateAuthState(newState: AuthState) {
        _authState.value = newState
    }



    fun updateUser(field: String, value: String) {
        UserModel.value = when (field) {
            "email" -> UserModel.value.copy(email = value)
            "username" -> UserModel.value.copy(username = value)
            "employeeId" -> UserModel.value.copy(employeeID = value)
            "address" -> UserModel.value.copy(address = value)
            "contactNo" -> UserModel.value.copy(contactNo = value)
            else -> {
                UserModel.value
            }
        }
    }


    fun login(email: String?, password: String?, navController: NavController) {
        if (email.isNullOrBlank() || password.isNullOrBlank()) {
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userEmail = auth.currentUser?.email ?: ""
                    val role = assignRoleBasedOnEmail(userEmail) // Assign the role based on the email

                    _authState.value = AuthState.LoggedIn(userEmail, role)

                    // Navigate to the appropriate screen based on the role
                    when (role) {
                        "admin" -> {
                            navController.navigate("adminHome") { // Navigate to Admin Home
                                popUpTo("login") { inclusive = true } // Pop the Login screen from stack
                            }
                        }
                        "employee" -> {
                            navController.navigate("homepage") { // Navigate to Employee Home
                                popUpTo("login") { inclusive = true } // Pop the Login screen from stack
                            }
                        }
                        else -> {
                            Log.e("Login", "Unknown role: $role")
                        }
                    }
                } else {
                    // Log the error and optionally update a state for UI feedback
                    Log.e("Login", "Login failed: ${task.exception?.message}")
                }
            }
    }




     fun assignRoleBasedOnEmail(email: String?): String {
        return if (email == "admin10@example.com") { // Replace with the actual admin email
            "admin"
        } else {
            "employee"
        }
    }

    fun signup(
        email: String,
        password: String,
        employeeID: String,
        username: String,
        address: String,
        contactNumber: String,
        onSignUpSuccess: () -> Unit,
        onSignUpFailure: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = task.result?.user?.uid
                    val user = FirebaseAuth.getInstance().currentUser
                    val role = when {
                        user?.email?.endsWith("@company.com") == true -> "admin"
                        user?.email == "admin@company.com" -> "admin"
                        else -> "employee"
                    }

                    val userData = mapOf(
                        "employeeID" to employeeID,
                        "username" to username,
                        "address" to address,
                        "contactNumber" to contactNumber,
                        "email" to email,
                        "role" to role
                    )

                    if (userId != null) {
                        db.collection("users")
                            .document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Authenticated(auth.currentUser!!)
                                onSignUpSuccess() // Notify the success callback
                            }
                            .addOnFailureListener { e ->
                                onSignUpFailure("Failed to save user data: ${e.message}")
                            }
                    }
                } else {
                    onSignUpFailure(task.exception?.message ?: "Unknown error occurred during sign-up.")
                }
            }
            .addOnFailureListener { e ->
                onSignUpFailure("Sign-up failed: ${e.message}")
            }
    }

        fun signOut() {
            try {
                auth.signOut()
                _authState.value = AuthState.Unauthenticated
                authStatus.value = AuthState.AuthResult.LoggedOut
            } catch (e: Exception) {
                errorMessage.value = "Error logging out: ${e.message}"
            }
        }
}


