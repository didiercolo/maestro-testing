package com.example.stackdemo.ui.checkout

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CheckoutViewModelTest {

    private lateinit var vm: CheckoutViewModel

    @Before fun setup() { vm = CheckoutViewModel() }

    @Test
    fun `initial state has empty form and no errors`() {
        val s = vm.uiState.value
        assertTrue(s.firstName.isEmpty())
        assertTrue(s.errors.isEmpty())
    }

    @Test
    fun `validate returns false when all fields empty`() {
        assertFalse(vm.validate())
        assertEquals(5, vm.uiState.value.errors.size)
    }

    @Test
    fun `validate returns true when all fields filled`() {
        vm.update(firstName = "John", lastName = "Doe", address = "123 Main St",
                  state = "CA", postalCode = "90210")
        assertTrue(vm.validate())
        assertTrue(vm.uiState.value.errors.isEmpty())
    }

    @Test
    fun `invalid postal code sets error`() {
        vm.update(firstName = "John", lastName = "Doe", address = "123 Main St",
                  state = "CA", postalCode = "ABC")
        vm.validate()
        assertNotNull(vm.uiState.value.errors["postalCode"])
    }

    @Test
    fun `missing lastName sets error`() {
        vm.update(firstName = "John", lastName = "", address = "123 Main St",
                  state = "CA", postalCode = "90210")
        vm.validate()
        assertNotNull(vm.uiState.value.errors["lastName"])
    }
}
