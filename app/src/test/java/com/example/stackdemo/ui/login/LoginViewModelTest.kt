package com.example.stackdemo.ui.login

import com.example.stackdemo.data.model.User
import com.example.stackdemo.data.repository.UserRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginViewModelTest {

    private val mockRepo: UserRepository = mock()
    private lateinit var vm: LoginViewModel

    private val users = listOf(
        User("demouser",    "testingisfun99", "normal"),
        User("locked_user", "testingisfun99", "locked")
    )

    @Before
    fun setup() {
        whenever(mockRepo.getAll()).thenReturn(users)
        whenever(mockRepo.getPassword()).thenReturn("testingisfun99")
        vm = LoginViewModel(mockRepo)
    }

    @Test
    fun `initial state has no selection`() {
        assertNull(vm.uiState.value.selectedUser)
        assertNull(vm.uiState.value.selectedPassword)
        assertFalse(vm.uiState.value.canLogin)
    }

    @Test
    fun `selecting user and password enables login`() {
        vm.selectUser(users[0])
        vm.selectPassword("testingisfun99")
        assertTrue(vm.uiState.value.canLogin)
    }

    @Test
    fun `login with demouser succeeds`() {
        vm.selectUser(users[0])
        vm.selectPassword("testingisfun99")
        assertTrue(vm.login())
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `login with locked_user sets error`() {
        vm.selectUser(users[1])
        vm.selectPassword("testingisfun99")
        assertFalse(vm.login())
        assertEquals("Your account has been locked.", vm.uiState.value.error)
    }

    @Test
    fun `users list loaded from repository`() {
        assertEquals(2, vm.uiState.value.users.size)
    }
}
