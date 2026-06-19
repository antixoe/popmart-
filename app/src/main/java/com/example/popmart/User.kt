package com.example.popmart

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val role: String, // SuperAdmin, Admin, Collector
    val password: String = "123456" // Simple dummy password for demonstration
)
