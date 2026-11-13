package com.example.stackdemo.ui.cart

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.CartItem
import com.example.stackdemo.data.model.Order
import com.example.stackdemo.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CartUiState(
    val items: List<CartItem> = emptyList()
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
    val itemCount: Int   get() = items.sumOf { it.quantity }
}

class CartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    fun addProduct(product: Product) {
        _uiState.update { state ->
            val existing = state.items.firstOrNull { it.product.id == product.id }
            val newItems = if (existing != null) {
                state.items.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.items + CartItem(product, 1)
            }
            state.copy(items = newItems)
        }
    }

    fun increaseQuantity(productId: Int) {
        _uiState.update { state ->
            state.copy(items = state.items.map {
                if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
            })
        }
    }

    fun decreaseQuantity(productId: Int) {
        _uiState.update { state ->
            val updated = state.items.mapNotNull {
                if (it.product.id == productId) {
                    if (it.quantity <= 1) null else it.copy(quantity = it.quantity - 1)
                } else it
            }
            state.copy(items = updated)
        }
    }

    fun removeProduct(productId: Int) {
        _uiState.update { state ->
            state.copy(items = state.items.filter { it.product.id != productId })
        }
    }

    fun clear() {
        _uiState.value = CartUiState()
    }

    fun snapshotOrder(): Order {
        val state = _uiState.value
        return Order(
            orderNumber = (10000..99999).random(),
            items       = state.items.toList(),
            total       = state.subtotal
        )
    }
}
