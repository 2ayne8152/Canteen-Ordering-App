package com.example.canteen.viewmodel.usermenu

import androidx.lifecycle.ViewModel
import com.example.canteen.data.MenuItem
import com.example.canteen.repository.MenuRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserMenuViewModel : ViewModel() {

    private val repo = MenuRepository()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems = _menuItems.asStateFlow()

    private var menuListener: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        menuListener = repo.listenMenuItems(
            onUpdate = { items ->
                _menuItems.value = items
            },
            onError = {
                // optionally log error
            }
        )
    }

    override fun onCleared() {
        menuListener?.remove()
        super.onCleared()
    }
}
