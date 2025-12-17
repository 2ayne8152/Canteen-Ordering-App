package com.example.canteen.repository

import com.example.canteen.DAO.MenuItemDao
import com.example.canteen.data.MenuItem

class MenuRepository {

    private val dao = MenuItemDao()

    suspend fun getMenuItems(): List<MenuItem> {
        return dao.getAllMenuItems()
    }
}
