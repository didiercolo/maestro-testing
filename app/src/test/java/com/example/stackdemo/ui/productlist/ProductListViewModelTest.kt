package com.example.stackdemo.ui.productlist

import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProductListViewModelTest {

    private val mockRepo: ProductRepository = mock()
    private lateinit var vm: ProductListViewModel

    private val products = listOf(
        Product(1, "iPhone 12",  "Apple",   799.0, 9, "iphone_12",  "Desc"),
        Product(2, "Galaxy S21", "Samsung", 799.0, 9, "samsung_s21", "Desc"),
        Product(3, "Pixel 5",    "Google",  699.0, 9, "pixel_5",     "Desc"),
        Product(4, "OnePlus 9",  "OnePlus", 729.0, 9, "oneplus_9",   "Desc"),
        Product(5, "iPhone SE",  "Apple",   399.0, 9, "iphone_se",   "Desc")
    )

    @Before
    fun setup() {
        whenever(mockRepo.getAll()).thenReturn(products)
        whenever(mockRepo.getVendors()).thenReturn(listOf("Apple", "Samsung", "Google", "OnePlus"))
        whenever(mockRepo.getByVendor("Apple")).thenReturn(products.filter { it.vendor == "Apple" })
        vm = ProductListViewModel(mockRepo)
    }

    @Test
    fun `initial state loads all products`() {
        assertEquals(5, vm.uiState.value.products.size)
    }

    @Test
    fun `initial state has no vendor filter`() {
        assertNull(vm.uiState.value.selectedVendor)
    }

    @Test
    fun `selectVendor filters products`() {
        vm.selectVendor("Apple")
        assertEquals(2, vm.uiState.value.products.size)
        assertTrue(vm.uiState.value.products.all { it.vendor == "Apple" })
    }

    @Test
    fun `selectVendor same vendor clears filter`() {
        vm.selectVendor("Apple")
        vm.selectVendor("Apple")
        assertNull(vm.uiState.value.selectedVendor)
        assertEquals(5, vm.uiState.value.products.size)
    }

    @Test
    fun `sortOrder PRICE_LOW_HIGH sorts ascending`() {
        vm.setSortOrder(SortOrder.PRICE_LOW_HIGH)
        val prices = vm.uiState.value.products.map { it.price }
        assertEquals(prices.sorted(), prices)
    }

    @Test
    fun `sortOrder PRICE_HIGH_LOW sorts descending`() {
        vm.setSortOrder(SortOrder.PRICE_HIGH_LOW)
        val prices = vm.uiState.value.products.map { it.price }
        assertEquals(prices.sortedDescending(), prices)
    }
}
