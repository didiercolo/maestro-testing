package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.Product
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProductRepositoryTest {

    private val mockSource: JsonDataSource = mock()
    private lateinit var repo: ProductRepository

    private val sampleProducts = listOf(
        Product(1, "iPhone 12",  "Apple",   799.0, 9, "iphone_12",  "Desc"),
        Product(2, "Galaxy S21", "Samsung", 799.0, 9, "samsung_s21", "Desc"),
        Product(3, "Pixel 5",    "Google",  699.0, 9, "pixel_5",     "Desc"),
        Product(4, "OnePlus 9",  "OnePlus", 729.0, 9, "oneplus_9",   "Desc"),
        Product(5, "iPhone SE",  "Apple",   399.0, 9, "iphone_se",   "Desc")
    )

    @Before
    fun setup() {
        whenever(mockSource.loadProducts()).thenReturn(sampleProducts)
        repo = ProductRepository(mockSource)
    }

    @Test
    fun `getAll returns all products`() {
        assertEquals(5, repo.getAll().size)
    }

    @Test
    fun `getByVendor filters correctly`() {
        val appleProducts = repo.getByVendor("Apple")
        assertEquals(2, appleProducts.size)
        assertTrue(appleProducts.all { it.vendor == "Apple" })
    }

    @Test
    fun `getById returns correct product`() {
        val product = repo.getById(2)
        assertNotNull(product)
        assertEquals("Galaxy S21", product!!.name)
    }

    @Test
    fun `getById returns null for missing id`() {
        assertNull(repo.getById(99))
    }

    @Test
    fun `getVendors returns unique vendor list`() {
        val vendors = repo.getVendors()
        assertEquals(listOf("Apple", "Samsung", "Google", "OnePlus"), vendors)
    }
}
