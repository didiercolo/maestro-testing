package com.example.stackdemo.data.model

import kotlinx.serialization.Serializable

enum class UserType {
    NORMAL, BROKEN_IMAGES, EXISTING_ORDERS, FAVOURITES, LOCKED;

    companion object {
        fun from(value: String): UserType = when (value) {
            "broken_images"   -> BROKEN_IMAGES
            "existing_orders" -> EXISTING_ORDERS
            "favourites"      -> FAVOURITES
            "locked"          -> LOCKED
            else              -> NORMAL
        }
    }
}

@Serializable
data class User(
    val username: String,
    val password: String,
    val type: String
) {
    val userType: UserType get() = UserType.from(type)
    val isLocked: Boolean get() = userType == UserType.LOCKED
    val hasBrokenImages: Boolean get() = userType == UserType.BROKEN_IMAGES
}
