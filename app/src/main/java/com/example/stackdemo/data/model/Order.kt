package com.example.stackdemo.data.model

data class Order(
    val orderNumber: Int,
    val items: List<CartItem>,
    val total: Double
)
