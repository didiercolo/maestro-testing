package com.example.stackdemo.data.repository

import com.example.stackdemo.data.local.JsonDataSource
import com.example.stackdemo.data.model.User
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UserRepositoryTest {

    private val mockSource: JsonDataSource = mock()
    private lateinit var repo: UserRepository

    private val sampleUsers = listOf(
        User("demouser",    "testingisfun99", "normal"),
        User("locked_user", "testingisfun99", "locked")
    )

    @Before
    fun setup() {
        whenever(mockSource.loadUsers()).thenReturn(sampleUsers)
        repo = UserRepository(mockSource)
    }

    @Test
    fun `getAll returns all users`() {
        assertEquals(2, repo.getAll().size)
    }

    @Test
    fun `getPassword returns the shared password`() {
        assertEquals("testingisfun99", repo.getPassword())
    }

    @Test
    fun `findByUsername returns correct user`() {
        val user = repo.findByUsername("locked_user")
        assertNotNull(user)
        assertTrue(user!!.isLocked)
    }

    @Test
    fun `findByUsername returns null for unknown user`() {
        assertNull(repo.findByUsername("ghost"))
    }
}
