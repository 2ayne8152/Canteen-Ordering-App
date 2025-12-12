package com.example.canteen.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState = _authState.asStateFlow()

    fun register(email: String, password: String, username: String, role: String, phoneNumber: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Log.d("AuthViewModel", "Starting registration for email: $email")

            try {
                // Validate inputs first
                if (email.isEmpty() || password.isEmpty() || username.isEmpty() || phoneNumber.isEmpty()) {
                    _authState.value = AuthState.Error("Please fill in all fields")
                    return@launch
                }

                if (password.length < 6) {
                    _authState.value = AuthState.Error("Password must be at least 6 characters")
                    return@launch
                }

                Log.d("AuthViewModel", "Creating user with Firebase...")
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    Log.d("AuthViewModel", "User created successfully, UID: ${firebaseUser.uid}")

                    val user = hashMapOf(
                        "UserID" to firebaseUser.uid,
                        "Email" to email,
                        "Name" to username,
                        "Role" to role,
                        "PhoneNumber" to phoneNumber
                    )

                    Log.d("AuthViewModel", "Saving user data to Firestore...")
                    db.collection("users").document(firebaseUser.uid).set(user).await()
                    Log.d("AuthViewModel", "User data saved successfully")

                    // Sign out immediately after registration
                    auth.signOut()
                    Log.d("AuthViewModel", "User signed out after registration")

                    _authState.value = AuthState.RegistrationSuccess("Registration successful! You can now log in.")
                    Log.d("AuthViewModel", "Registration state set to Success")

                } else {
                    Log.e("AuthViewModel", "Registration failed: user is null")
                    _authState.value = AuthState.Error("Registration failed: user is null.")
                }

            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration error: ${e.message}", e)

                val errorMessage = when {
                    e.message?.contains("already in use") == true ->
                        "This email is already registered."
                    e.message?.contains("badly formatted") == true ->
                        "Please enter a valid email address."
                    e.message?.contains("at least 6 characters") == true ->
                        "Password should be at least 6 characters."
                    else -> e.message ?: "An unknown error occurred."
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
                    val userRole = userDoc.getString("Role") // <-- The fix is here

                    Log.d("AuthViewModel", "User role from Firestore: $userRole, Expected role: $role")

                    if (userRole == role) {
                        _authState.value = AuthState.LoggedIn(role)
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

    fun resetAuthState() {
        Log.d("AuthViewModel", "Resetting auth state to LoggedOut")
        _authState.value = AuthState.LoggedOut
    }
}

// Enhanced AuthState sealed class
sealed class AuthState {
    data class LoggedIn(val role: String) : AuthState()
    data class RegistrationSuccess(val message: String = "Registration successful!") : AuthState()
    object LoggedOut : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}
