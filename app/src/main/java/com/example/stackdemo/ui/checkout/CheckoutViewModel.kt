package com.example.stackdemo.ui.checkout

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CheckoutUiState(
    val firstName:  String = "",
    val lastName:   String = "",
    val address:    String = "",
    val state:      String = "",
    val postalCode: String = "",
    val errors: Map<String, String> = emptyMap()
)

class CheckoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    fun update(
        firstName:  String = _uiState.value.firstName,
        lastName:   String = _uiState.value.lastName,
        address:    String = _uiState.value.address,
        state:      String = _uiState.value.state,
        postalCode: String = _uiState.value.postalCode
    ) {
        _uiState.update { it.copy(
            firstName  = firstName,
            lastName   = lastName,
            address    = address,
            state      = state,
            postalCode = postalCode,
            errors     = emptyMap()
        ) }
    }

    fun validate(): Boolean {
        val s = _uiState.value
        val errors = buildMap<String, String> {
            if (s.firstName.isBlank())  put("firstName",  "Required")
            if (s.lastName.isBlank())   put("lastName",   "Required")
            if (s.address.isBlank())    put("address",    "Required")
            if (s.state.isBlank())      put("state",      "Required")
            if (s.postalCode.isBlank()) put("postalCode", "Required")
            else if (!s.postalCode.matches(Regex("\\d{5,}")))
                put("postalCode", "Enter a valid postal code")
        }
        _uiState.update { it.copy(errors = errors) }
        return errors.isEmpty()
    }
}
