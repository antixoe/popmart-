package com.example.popmart

data class PopmartItem(
    val id: Long,
    val name: String,
    val series: String,
    val rarity: String,
    val dateAcquired: String,
    val quantity: Int,
    val unitPrice: Double,
    val notes: String
)
