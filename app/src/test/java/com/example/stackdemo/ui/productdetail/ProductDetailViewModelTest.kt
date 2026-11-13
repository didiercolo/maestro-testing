package com.example.stackdemo.ui.productdetail

import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import org.junit.Assert.*
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ProductDetailViewModelTest {

    private val mockRepo: ProductRepository = mock()
    private val product = Product(1, "iPhone 12", "Apple", 799.0, 9, "iphone_12", "Great phone")

    @Test
    fun `loadProduct puts product in state`() {
        whenever(mockRepo.getById(1)).thenReturn(product)
        val vm = ProductDetailViewModel(mockRepo, productId = 1)
        assertEquals("iPhone 12", vm.uiState.value.product?.name)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun `loadProduct with invalid id leaves product null`() {
        whenever(mockRepo.getById(99)).thenReturn(null)
        val vm = ProductDetailViewModel(mockRepo, productId = 99)
        assertNull(vm.uiState.value.product)
        assertFalse(vm.uiState.value.isLoading)
    }
}
