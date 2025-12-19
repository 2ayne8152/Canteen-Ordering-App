package com.example.canteen.repository

import com.example.canteen.DAO.MenuItemDao
import com.example.canteen.data.MenuItem
import com.google.firebase.firestore.ListenerRegistration

class MenuRepository {

    private val dao = MenuItemDao()

    suspend fun getMenuItems(): List<MenuItem> {
        return dao.getAllMenuItems()
    }

    fun listenMenuItems(
        onUpdate: (List<MenuItem>) -> Unit,
        onError: (Throwable) -> Unit
    ): ListenerRegistration {
        return dao.listenMenuItems(onUpdate, onError)
    }
}
