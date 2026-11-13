package com.example.stackdemo.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    val name: String,
    val vendor: String,
    val price: Double,
    val installments: Int,
    val image: String,
    val description: String
) {
    val installmentPrice: Double get() = price / installments
}
