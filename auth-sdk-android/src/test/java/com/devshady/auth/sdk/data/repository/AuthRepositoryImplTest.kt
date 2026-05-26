package com.devshady.auth.sdk.data.repository

import com.devshady.auth.sdk.data.local.AuthLocalDataSource
import com.devshady.auth.sdk.data.model.VerifyResponse
import com.devshady.auth.sdk.data.remote.AuthRemoteDataSource
import com.devshady.auth.sdk.domain.model.AuthResult
import com.devshady.auth.sdk.domain.model.UserSession
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryImplTest {

    private val remoteDataSource: AuthRemoteDataSource = mockk()
    private val localDataSource: AuthLocalDataSource = mockk(relaxed = true)

    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setup() {
        repository = AuthRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localDataSource = localDataSource
        )
    }

    @Test
    fun `verifyOtp returns success and saves session data in local source when remote api succeeds`() =
        runTest {
            //Arrange
            val number = "1234567890"
            val otp = "123456"
            val mockResponse = Response.success(
                VerifyResponse(
                  authToken = "mock_jwt_token", success = true, message = "Success"
                )
            )

            coEvery {remoteDataSource.verifyOtp(number, otp)} returns mockResponse

            //Act
            val result = repository.verifyOtp(number, otp)

            //Assert
            val expectedResult = UserSession(phoneNumber = number, authToken = "mock_jwt_token", isAuthenticated = true)
            assertThat(result).isInstanceOf(AuthResult.Success::class.java)
            assertThat((result as AuthResult.Success).data).isEqualTo(expectedResult)
            coVerify(exactly = 1) {localDataSource.saveSession(expectedResult)}
        }

    @Test
    fun `verifyOtp returns error when remote api fails`() = runTest {
        // Arrange
        val number = "1234567890"
        val otp = "123456"
        val errorMessage = "Invalid OTP"
        coEvery { remoteDataSource.verifyOtp(number, otp) } throws Exception(errorMessage)

        // Act
        val result = repository.verifyOtp(number, otp)

        // Assert
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        assertThat((result as AuthResult.Error).message).isEqualTo(errorMessage)
        coVerify(exactly = 0) { localDataSource.saveSession(any()) }
    }

    @Test
    fun `verifyOtp returns Error when API response is successful but body indicates failure`() = runTest {
        val number = "1234567890"
        val otp = "123456"
        val errorResponse = Response.success(
            VerifyResponse(
                authToken = null, success = false, message = "Invalid OTP verification code"
            )
        )

        coEvery { remoteDataSource.verifyOtp(number, otp) } returns errorResponse

        //Act
        val result = repository.verifyOtp(number, otp)

        //Assert
        assertThat(result).isInstanceOf(AuthResult.Error::class.java)
        assertThat((result as AuthResult.Error).message).isEqualTo("Invalid OTP verification code")
        coVerify(exactly = 0) { localDataSource.saveSession(any()) }
    }
}