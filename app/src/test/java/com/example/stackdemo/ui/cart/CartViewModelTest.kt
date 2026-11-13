package com.example.stackdemo.ui.cart

import com.example.stackdemo.data.model.Product
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CartViewModelTest {

    private lateinit var vm: CartViewModel
    private val iphone = Product(1, "iPhone 12", "Apple", 799.0, 9, "iphone_12", "Desc")
    private val pixel  = Product(2, "Pixel 5",   "Google", 699.0, 9, "pixel_5",  "Desc")

    @Before fun setup() { vm = CartViewModel() }

    @Test
    fun `initial state is empty`() {
        assertTrue(vm.uiState.value.items.isEmpty())
        assertEquals(0.0, vm.uiState.value.subtotal, 0.01)
        assertEquals(0, vm.uiState.value.itemCount)
    }

    @Test
    fun `addProduct adds new item with quantity 1`() {
        vm.addProduct(iphone)
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals(1, vm.uiState.value.items.first().quantity)
    }

    @Test
    fun `addProduct twice increments quantity`() {
        vm.addProduct(iphone)
        vm.addProduct(iphone)
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals(2, vm.uiState.value.items.first().quantity)
    }

    @Test
    fun `increaseQuantity increments quantity`() {
        vm.addProduct(iphone)
        vm.increaseQuantity(iphone.id)
        assertEquals(2, vm.uiState.value.items.first().quantity)
    }

    @Test
    fun `decreaseQuantity removes item when quantity reaches 0`() {
        vm.addProduct(iphone)
        vm.decreaseQuantity(iphone.id)
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `removeProduct removes item`() {
        vm.addProduct(iphone)
        vm.addProduct(pixel)
        vm.removeProduct(iphone.id)
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("Pixel 5", vm.uiState.value.items.first().product.name)
    }

    @Test
    fun `subtotal reflects all items`() {
        vm.addProduct(iphone)
        vm.addProduct(pixel)
        assertEquals(1498.0, vm.uiState.value.subtotal, 0.01)
    }

    @Test
    fun `itemCount is sum of all quantities`() {
        vm.addProduct(iphone)
        vm.addProduct(iphone)
        vm.addProduct(pixel)
        assertEquals(3, vm.uiState.value.itemCount)
    }

    @Test
    fun `clear empties the cart`() {
        vm.addProduct(iphone)
        vm.clear()
        assertTrue(vm.uiState.value.items.isEmpty())
    }

    @Test
    fun `snapshotOrder returns Order with correct total and number in range`() {
        vm.addProduct(iphone)
        val order = vm.snapshotOrder()
        assertEquals(1, order.items.size)
        assertEquals(799.0, order.total, 0.01)
        assertTrue(order.orderNumber in 10000..99999)
    }
}
