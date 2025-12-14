package com.example.canteen.viewmodel.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteen.Repository.UserRepository
import com.example.canteen.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {
// Created by ET to load the user phone number from firebase
// For list
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    // ✅ For single user
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser = _selectedUser.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _users.value = repository.getAllUsers()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    // ✅ Load ONE user by ID
    fun loadUserById(userId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _selectedUser.value = repository.getUser(userId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
