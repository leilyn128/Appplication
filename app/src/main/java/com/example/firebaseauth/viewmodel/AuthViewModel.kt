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
//import com.example.firebaseauth.login.AccountType
//import com.google.android.gms.common.internal.AccountType


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    private val _authState = MutableLiveData<AuthState>(AuthState.Unauthenticated)
    val authState: LiveData<AuthState> get() = _authState

    private val _userDetails = MutableLiveData<UserModel>(UserModel())
    val userDetails: LiveData<UserModel> get() = _userDetails

    private val _userModel = mutableStateOf(UserModel())
    val userModel: State<UserModel> = _userModel

    val user = FirebaseAuth.getInstance().currentUser


    val authStatus: MutableLiveData<AuthState.AuthResult> = MutableLiveData()
    val errorMessage: MutableLiveData<String> = MutableLiveData()
    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    //val isAuthenticated by authViewModel.authState.observeAsState(AuthState.Unauthenticated)

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
                // Optionally log an error or ignore unsupported fields
                UserModel.value
            }
        }
    }


    // Updated login function
    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading // Indicate loading state

        auth.signInWithEmailAndPassword(email, password) // Use signIn for login
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Fetch the user role from Firestore after successful login
                        db.collection("users")
                            .document(user.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val role = document.getString("role") ?: "employee" // Default to "employee"
                                    _authState.value = AuthState.Authenticated(user)
                                    // Optionally, store the role in user details or elsewhere
                                    // For example, you can use a MutableLiveData to store the role:
                                    // _userRole.value = role
                                } else {
                                    _authState.value = AuthState.Unauthenticated
                                }
                            }
                            .addOnFailureListener { exception ->
                                _authState.value = AuthState.Error("Failed to get user role: ${exception.message}")
                            }
                    } else {
                        _authState.value = AuthState.Unauthenticated
                    }
                } else {
                    _authState.value = AuthState.Error("Authentication failed: ${task.exception?.message}")
                }
            }
    }



    // Register a new account (sign-up)
        // Register a new account (sign-up)
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
                        if (userId != null) {
                            // Prepare user data from UserModel
                            val userData = mapOf(
                                "employeeID" to UserModel.value.employeeID,
                                "username" to UserModel.value.username,
                                "address" to UserModel.value.address,
                                "contactNumber" to UserModel.value.contactNo,
                                "email" to UserModel.value.email,
                                "role" to if (email == "admin@example.com") "admin" else "employee" // Assign role based on email
                            )

                            // Save user data to Firestore
                            db.collection("users")
                                .document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    _authState.value = AuthState.Authenticated(auth.currentUser!!)
                                    onSignUpSuccess() // Notify the success callback
                                }
                                .addOnFailureListener { e ->
                                    onSignUpFailure("Failed to save user data: ${e.message}") // Provide failure feedback
                                }
                        } else {
                            onSignUpFailure("User ID is null. Could not save user data.")
                        }
                    } else {
                        onSignUpFailure(
                            task.exception?.message ?: "Unknown error occurred during sign-up."
                        )
                    }
                }
                .addOnFailureListener { e ->
                    onSignUpFailure("Sign-up failed: ${e.message}")
                }
        }


    // Save user data to Firestore
        private fun saveUserToFirestore(
            user: FirebaseUser,
            userModel: UserModel,
            onSuccess: () -> Unit,
            onFailure: (String) -> Unit
        ) {
            val userRef = db.collection("users").document(user.uid)

            userRef.set(userModel)
                .addOnSuccessListener {
                    Log.d("SignUp", "User data saved successfully to Firestore")
                    onSuccess()
                }
                .addOnFailureListener { exception ->
                    val errorMsg = exception.message ?: "Unknown error"
                    Log.e("SignUp", "Firestore Error: $errorMsg")
                    onFailure("Error saving user data: $errorMsg")
                }


            val currentUser: FirebaseUser? = auth.currentUser
            currentUser?.let {
                val userRef = db.collection("users").document(it.uid)
                userRef.set(user)
                    .addOnSuccessListener {
                        authStatus.value = AuthState.AuthResult.Success(currentUser)
                    }
                    .addOnFailureListener { exception ->
                        errorMessage.value = "Error saving data: ${exception.message}"
                        authStatus.value = AuthState.AuthResult.Failure(exception.message)
                    }
            }
        }

        fun fetchUserDetails(userId: String) {
            db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(UserModel::class.java)
                        _userDetails.value = UserModel() // Update LiveData with user data
                    } else {
                        _userDetails.value = UserModel() // Empty user model if no data found
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("AuthViewModel", "Error getting user details: ${exception.message}")
                }
        }



    // Call this method when user is authenticated




    // Sign out the current user
        fun signOut() {
            try {
                auth.signOut()
                _authState.value = AuthState.Unauthenticated
                authStatus.value = AuthState.AuthResult.LoggedOut
            } catch (e: Exception) {
                errorMessage.value = "Error logging out: ${e.message}"
            }
        }

        // Get the current authenticated user
        fun getCurrentUser(): FirebaseUser? {
            return auth.currentUser
        }
    }
