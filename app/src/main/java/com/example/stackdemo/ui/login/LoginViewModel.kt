package com.example.stackdemo.ui.login

import androidx.lifecycle.ViewModel
import com.example.stackdemo.data.model.User
import com.example.stackdemo.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val users: List<User>         = emptyList(),
    val password: String          = "",
    val selectedUser: User?       = null,
    val selectedPassword: String? = null,
    val error: String?            = null
) {
    val canLogin: Boolean get() = selectedUser != null && selectedPassword != null
}

class LoginViewModel(private val repo: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(users = repo.getAll(), password = repo.getPassword()) }
    }

    fun selectUser(user: User) {
        _uiState.update { it.copy(selectedUser = user, error = null) }
    }

    fun selectPassword(password: String) {
        _uiState.update { it.copy(selectedPassword = password, error = null) }
    }

    fun login(): Boolean {
        val user = _uiState.value.selectedUser ?: return false
        return if (user.isLocked) {
            _uiState.update { it.copy(error = "Your account has been locked.") }
            false
        } else true
    }
}
