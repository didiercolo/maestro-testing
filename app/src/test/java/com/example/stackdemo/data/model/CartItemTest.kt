package com.example.stackdemo.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CartItemTest {

    private val testProduct = Product(
        id = 1,
        name = "iPhone 12",
        vendor = "Apple",
        price = 799.0,
        installments = 9,
        image = "iphone_12",
        description = "Test product"
    )

    @Test
    fun `subtotal equals price times quantity`() {
        val item = CartItem(product = testProduct, quantity = 3)
        assertEquals(2397.0, item.subtotal, 0.01)
    }

    @Test
    fun `installmentPrice equals price divided by installments`() {
        val item = CartItem(product = testProduct, quantity = 1)
        assertEquals(88.78, item.product.installmentPrice, 0.01)
    }

    @Test
    fun `UserType from normal string returns NORMAL`() {
        assertEquals(UserType.NORMAL, UserType.from("normal"))
    }

    @Test
    fun `UserType from locked string returns LOCKED`() {
        assertEquals(UserType.LOCKED, UserType.from("locked"))
    }

    @Test
    fun `locked user isLocked returns true`() {
        val user = User("locked_user", "testingisfun99", "locked")
        assertTrue(user.isLocked)
    }

    @Test
    fun `demouser isLocked returns false`() {
        val user = User("demouser", "testingisfun99", "normal")
        assertFalse(user.isLocked)
    }

    @Test
    fun `broken images user hasBrokenImages returns true`() {
        val user = User("img_user", "testingisfun99", "broken_images")
        assertTrue(user.hasBrokenImages)
    }
}
