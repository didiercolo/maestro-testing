package com.example.stackdemo.data.local

import androidx.test.platform.app.InstrumentationRegistry
import com.example.stackdemo.data.model.UserType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class JsonDataSourceTest {

    private lateinit var source: JsonDataSource

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        source = JsonDataSource(context)
    }

    @Test
    fun loadProducts_returns25Items() {
        val products = source.loadProducts()
        assertEquals(25, products.size)
    }

    @Test
    fun loadProducts_firstProductIsIPhone12() {
        val first = source.loadProducts().first()
        assertEquals("iPhone 12", first.name)
        assertEquals("Apple", first.vendor)
        assertEquals(799.0, first.price, 0.01)
    }

    @Test
    fun loadUsers_returns5Users() {
        val users = source.loadUsers()
        assertEquals(5, users.size)
    }

    @Test
    fun loadUsers_lockedUserHasCorrectType() {
        val locked = source.loadUsers().first { it.username == "locked_user" }
        assertEquals(UserType.LOCKED, locked.userType)
        assertTrue(locked.isLocked)
    }

    @Test
    fun loadUsers_brokenImagesUserHasCorrectType() {
        val broken = source.loadUsers().first { it.username == "image_not_loading_user" }
        assertTrue(broken.hasBrokenImages)
    }
}
