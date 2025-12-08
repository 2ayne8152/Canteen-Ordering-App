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

    fun register(email: String, password: String, username: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val user = hashMapOf(
                        "uid" to firebaseUser.uid,
                        "email" to email,
                        "username" to username,
                        "role" to role
                    )
                    db.collection("users").document(firebaseUser.uid).set(user).await()
                    auth.signOut() // Sign out the user immediately after registration
                    _authState.value = AuthState.RegistrationSuccess
                } else {
                    _authState.value = AuthState.Error("Registration failed: user is null.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
                Log.e("AuthViewModel", "Registration failed", e)
            }
        }
    }

    fun login(email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    val userDoc = db.collection("users").document(firebaseUser.uid).get().await()
                    val userRole = userDoc.getString("role")
                    if (userRole == role) {
                        _authState.value = AuthState.LoggedIn(role)
                    } else {
                        _authState.value = AuthState.Error("Role mismatch. Please use the correct login portal.")
                        auth.signOut()
                    }
                } else {
                    _authState.value = AuthState.Error("Login failed: user is null.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
                Log.e("AuthViewModel", "Login failed", e)
            }
        }
    }
    fun resetAuthState() {
        _authState.value = AuthState.LoggedOut
    }
}

sealed class AuthState {
    data class LoggedIn(val role: String) : AuthState()
    object RegistrationSuccess : AuthState()
    object LoggedOut : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}