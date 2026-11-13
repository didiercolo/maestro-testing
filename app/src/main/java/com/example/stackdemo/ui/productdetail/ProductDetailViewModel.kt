package com.example.stackdemo.ui.productdetail

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = true
)

class ProductDetailViewModel(
    private val repo: ProductRepository,
    productId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(product = repo.getById(productId), isLoading = false) }
    }
}
