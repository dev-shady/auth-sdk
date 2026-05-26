package com.devshady.auth.sdk.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.devshady.auth.sdk.domain.model.UserSession
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class AuthLocalDataSourceImplTest {

    private lateinit var dataSource: AuthLocalDataSource
    private lateinit var context: Context
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataSource = AuthLocalDataSourceImpl(context)
    }

    @After
    fun tearDown() {
        val datastoreRepository = File(context.filesDir, "datastore")
        if (datastoreRepository.exists()) {
            datastoreRepository.deleteRecursively()
        }
    }

    @Test
    fun `verify datastore emits empty session when uninitialized`() = runTest(testDispatcher) {
        dataSource.userSession.test {
            val userData = awaitItem()
            assertThat(userData.authToken).isNull()
            assertThat(userData.phoneNumber).isEmpty()
            assertThat(userData.isAuthenticated).isFalse()
        }
    }

    @Test
    fun `saveSession updates userSession flow with correct data`() = runTest(testDispatcher) {
        val authToken = "test_token"
        val phoneNumber = "+1234567890"

        dataSource.saveSession(UserSession(
            authToken = authToken,
            phoneNumber = phoneNumber,
            isAuthenticated = true
        ))

        dataSource.userSession.test {
            val userData = awaitItem()
            assertThat(userData.authToken).isEqualTo(authToken)
            assertThat(userData.phoneNumber).isEqualTo(phoneNumber)
            assertThat(userData.isAuthenticated).isTrue()
        }
    }

    @Test
    fun `clearSession wipes out all items and resets state back to default values`() = runTest(testDispatcher) {
        val testSession = UserSession(
            phoneNumber = "+12345",
            authToken = "token",
            isAuthenticated = true
        )

        dataSource.userSession.test {
            // Skip initial state
            awaitItem()

            // Save session data
            dataSource.saveSession(testSession)
            assertThat(awaitItem().isAuthenticated).isTrue()

            // When
            dataSource.clearSession()

            // Then: Verify it falls straight back to clear values
            val clearedItem = awaitItem()
            assertThat(clearedItem.phoneNumber).isEmpty()
            assertThat(clearedItem.authToken).isNull()
            assertThat(clearedItem.isAuthenticated).isFalse()
        }
    }
}
