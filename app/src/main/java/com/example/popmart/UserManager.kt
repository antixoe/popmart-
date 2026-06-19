package com.example.popmart

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UserManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUsers(users: List<User>) {
        val json = gson.toJson(users)
        sharedPreferences.edit().putString("users_key", json).apply()
    }

    fun loadUsers(): List<User> {
        val json = sharedPreferences.getString("users_key", null)
        if (json == null) {
            val initialUsers = listOf(
                User(1L, "superadmin", "super@popmart.com", "SuperAdmin"),
                User(2L, "admin", "admin@popmart.com", "Admin"),
                User(3L, "manager", "manager@popmart.com", "Manager"),
                User(4L, "collector1", "collector1@mail.com", "Collector")
            )
            saveUsers(initialUsers)
            return initialUsers
        }
        val type = object : TypeToken<List<User>>() {}.type
        return gson.fromJson(json, type)
    }

    fun nextId(users: List<User>): Long {
        return (users.maxOfOrNull { it.id } ?: 0L) + 1L
    }
}
