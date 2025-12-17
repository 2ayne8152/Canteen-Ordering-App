package com.example.canteen.Repository

import com.example.canteen.DAO.UserDao
import com.example.canteen.data.User

class UserRepository(
    private val userDao: UserDao = UserDao()
) {

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    suspend fun getUser(userId: String): User? {
        return userDao.getUserById(userId)
    }
}
