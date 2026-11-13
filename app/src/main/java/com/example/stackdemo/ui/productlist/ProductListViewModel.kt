package com.example.stackdemo.ui.productlist

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.Product
import com.example.stackdemo.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SortOrder { DEFAULT, PRICE_LOW_HIGH, PRICE_HIGH_LOW }

data class ProductListUiState(
    val products: List<Product>  = emptyList(),
    val vendors: List<String>    = emptyList(),
    val selectedVendor: String?  = null,
    val sortOrder: SortOrder     = SortOrder.DEFAULT,
    val isLoading: Boolean       = true
)

class ProductListViewModel(private val repo: ProductRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        _uiState.update { it.copy(
            products  = repo.getAll(),
            vendors   = repo.getVendors(),
            isLoading = false
        ) }
    }

    fun selectVendor(vendor: String) {
        _uiState.update { state ->
            val newVendor = if (state.selectedVendor == vendor) null else vendor
            val base      = if (newVendor == null) repo.getAll() else repo.getByVendor(newVendor)
            state.copy(
                selectedVendor = newVendor,
                products       = applySortOrder(base, state.sortOrder)
            )
        }
    }

    fun setSortOrder(order: SortOrder) {
        _uiState.update { state ->
            val base = state.selectedVendor?.let(repo::getByVendor) ?: repo.getAll()
            state.copy(
                sortOrder = order,
                products  = applySortOrder(base, order)
            )
        }
    }

    private fun applySortOrder(list: List<Product>, order: SortOrder): List<Product> =
        when (order) {
            SortOrder.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            SortOrder.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            SortOrder.DEFAULT        -> list
        }
}
