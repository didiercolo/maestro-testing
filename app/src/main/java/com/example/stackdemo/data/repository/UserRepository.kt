package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.User

class UserRepository(private val source: JsonDataSource) {

    private val cache: List<User> by lazy { source.loadUsers() }

    fun getAll(): List<User> = cache

    fun getPassword(): String = cache.firstOrNull()?.password ?: "testingisfun99"

    fun findByUsername(username: String): User? =
        cache.firstOrNull { it.username == username }
}
