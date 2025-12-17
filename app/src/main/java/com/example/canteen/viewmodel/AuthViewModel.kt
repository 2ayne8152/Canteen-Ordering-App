package com.example.canteen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    // --- State for Password Reset ---
    private val _passwordResetStatus = MutableStateFlow<String?>(null)
    val passwordResetStatus: StateFlow<String?> = _passwordResetStatus.asStateFlow()

    private val _isLoadingPasswordReset = MutableStateFlow(false)
    val isLoadingPasswordReset: StateFlow<Boolean> = _isLoadingPasswordReset.asStateFlow()
    // ---

    init {
        // Check for existing user session when ViewModel is created
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // User is already logged in, fetch their role from Firestore
                try {
                    Log.d("AuthViewModel", "Found existing user: ${currentUser.uid}")

                    val userDoc = db.collection("users").document(currentUser.uid).get().await()
                    val userRole = userDoc.getString("Role") ?: "user"

                    Log.d("AuthViewModel", "User role from Firestore: $userRole")
                    _authState.value = AuthState.LoggedIn(
                        userId = currentUser.uid,
                        role = userRole
                    )
                    Log.d("AuthViewModel", "Auto-login successful with role: $userRole")

                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error fetching user data on auto-login: ${e.message}")
                    // If we can't fetch user data, sign them out
                    auth.signOut()
                    _authState.value = AuthState.LoggedOut
                }
            } else {
                // No user is logged in
                _authState.value = AuthState.LoggedOut
                Log.d("AuthViewModel", "No existing user session found")
            }
        }
    }
    fun register(email: String, password: String, username: String, role: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "Starting registration for email: $email")

            try {
                // Validate inputs first
                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || phoneNumber.isEmpty()) {
                    Log.d("AuthViewModel", "Validation failed: empty fields")
                    _authState.value = AuthState.Error("Please fill in all fields")
                    return@launch
                }

                if (password.length < 6) {
                    Log.d("AuthViewModel", "Validation failed: password too short")
                    _authState.value = AuthState.Error("Password must be at least 6 characters")
                    return@launch
                }

                Log.d("AuthViewModel", "All validation passed, creating Firebase user...")
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    Log.d("AuthViewModel", "Firebase user created successfully, UID: ${firebaseUser.uid}")

                    val user = hashMapOf(
                        "UserID" to firebaseUser.uid,
                        "Email" to email,
                        "Name" to username,
                        "Role" to role,
                        "PhoneNumber" to phoneNumber
                    )

                    Log.d("AuthViewModel", "Saving user data to Firestore...")
                    db.collection("users").document(firebaseUser.uid).set(user).await()
                    Log.d("AuthViewModel", "User data saved successfully to Firestore")

                    _authState.value = AuthState.RegistrationSuccess("Registration successful! You can now log in.")
                    Log.d("AuthViewModel", "Registration complete - showing success message")

                } else {
                    Log.e("AuthViewModel", "Registration failed: Firebase user is null")
                    _authState.value = AuthState.Error("Registration failed: user is null.")
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error: ${e.javaClass.name} - ${e.message}", e)

                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                        "This email is already registered."
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                        "Please enter a valid email address."
                    is com.google.firebase.FirebaseNetworkException ->
                        "Network error. Please check your connection."
                    else -> {
                        Log.e("AuthViewModel", "Unknown error type: ${e.javaClass.name}")
                        e.message ?: "An unknown error occurred."
                    }
                }

                _authState.value = AuthState.Error(errorMessage)
                Log.d("AuthViewModel", "Registration state set to Error: $errorMessage")
            }
        }
    }

    fun login(email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "Starting login for email: $email")

            try {
                // Validate inputs
                if (email.isEmpty() || password.isEmpty()) {
                    _authState.value = AuthState.Error("Please fill in all fields")
                    return@launch
                }

                Log.d("AuthViewModel", "Signing in with Firebase...")
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    Log.d("AuthViewModel", "User signed in successfully, UID: ${firebaseUser.uid}")

                    val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                    val userRole = userDoc.getString("Role")

                    Log.d("AuthViewModel", "User role from Firestore: $userRole, Expected role: $role")

                    if (userRole == role) {
                        _authState.value = AuthState.LoggedIn(
                            userId = firebaseUser.uid,
                            role = role
                        )
                        Log.d("AuthViewModel", "Login state set to LoggedIn with role: $role")
                    } else {
                        Log.e("AuthViewModel", "Role mismatch: $userRole != $role")
                        _authState.value = AuthState.Error("Role mismatch. Please use the correct login portal.")
                        auth.signOut()
                    }
                } else {
                    Log.e("AuthViewModel", "Login failed: user is null")
                    _authState.value = AuthState.Error("Login failed: user is null.")
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login error: ${e.message}", e)

                val errorMessage = when {
                    e.message?.contains("invalid") == true || e.message?.contains("wrong password") == true ->
                        "Invalid email or password."
                    e.message?.contains("no user record") == true ->
                        "No account found with this email."
                    else -> e.message ?: "An unknown error occurred."
                }

                _authState.value = AuthState.Error(errorMessage)
                Log.d("AuthViewModel", "Login state set to Error: $errorMessage")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.LoggedOut
    }

    fun sendPasswordResetEmail(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _passwordResetStatus.value = "Please enter a valid email address to reset password."
            return
        }

        _isLoadingPasswordReset.value = true
        _passwordResetStatus.value = null // Clear previous status

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _isLoadingPasswordReset.value = false
                if (task.isSuccessful) {
                    _passwordResetStatus.value = "Password reset email sent to $email. Please check your inbox (and spam folder)."
                } else {
                    val exception = task.exception
                    when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            _passwordResetStatus.value = "No account found with this email address."
                        }
                        is FirebaseNetworkException -> {
                            _passwordResetStatus.value = "Network error. Please check your connection."
                        }
                        // Add more specific error handling if needed
                        else -> {
                            _passwordResetStatus.value = "Failed to send password reset email. Please try again. (${exception?.message ?: "Unknown error"})"
                        }
                    }
                }
            }
    }

    fun clearPasswordResetStatus() {
        _passwordResetStatus.value = null
    }

    fun resetAuthState() {
        Log.d("AuthViewModel", "Resetting auth state to LoggedOut")
        _authState.value = AuthState.LoggedOut
    }
}

// Enhanced AuthState sealed class
sealed class AuthState {
    data class LoggedIn(val userId: String, val role: String) : AuthState()
    data class RegistrationSuccess(val message: String = "Registration successful!") : AuthState()
    object Initial : AuthState() // Added Initial state
    object LoggedOut : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}