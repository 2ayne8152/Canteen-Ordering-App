package com.example.canteen.viewmodels

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

    fun register(email: String, password: String, username: String) {
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
                    )
                    db.collection("users").document(firebaseUser.uid).set(user).await()
                    _authState.value = AuthState.LoggedIn
                } else {
                    _authState.value = AuthState.Error("Registration failed: user is null.")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
                Log.e("AuthViewModel", "Registration failed", e)
            }
        }
    }
     fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.LoggedIn
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "An unknown error occurred.")
                Log.e("AuthViewModel", "Login failed", e)
            }
        }
    }
}

sealed class AuthState {
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}