package com.example.canteen.viewmodel.staffMenu


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.canteen.DAO.MenuItemDao
import com.example.canteen.data.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {

    private val dao = MenuItemDao()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems

    init {
        loadMenuItems()
    }

    private fun loadMenuItems() {
        viewModelScope.launch {
            _menuItems.value = dao.getAllMenuItems()
        }
    }
}
