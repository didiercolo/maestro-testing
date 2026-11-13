package com.example.stackdemo.data.local

import android.content.Context
import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.model.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class ProductsWrapper(val products: List<Product>)

@Serializable
private data class UsersWrapper(val users: List<User>)

class JsonDataSource(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadProducts(): List<Product> {
        val raw = context.assets.open("products.json").bufferedReader().readText()
        return json.decodeFromString<ProductsWrapper>(raw).products
    }

    fun loadUsers(): List<User> {
        val raw = context.assets.open("users.json").bufferedReader().readText()
        return json.decodeFromString<UsersWrapper>(raw).users
    }
}
