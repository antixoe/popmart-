package com.example.popmart

import android.content.Context
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_DATA = "user_data"
    }

    fun saveSession(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_DATA, userJson)
            .apply()
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER_DATA, null) ?: return null
        return gson.fromJson(userJson, User::class.java)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserRole(): String {
        return getUser()?.role ?: "Collector"
    }

    fun isAdmin(): Boolean = getUserRole() == "Admin" || getUserRole() == "SuperAdmin"
    fun isSuperAdmin(): Boolean = getUserRole() == "SuperAdmin"
    fun isManager(): Boolean = getUserRole() == "Manager" || isSuperAdmin()
}
