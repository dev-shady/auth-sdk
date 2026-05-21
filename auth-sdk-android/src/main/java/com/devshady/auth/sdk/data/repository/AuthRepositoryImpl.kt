package com.devshady.auth.sdk.data.repository

import com.devshady.auth.sdk.data.api.AuthApiService
import com.devshady.auth.sdk.data.local.AuthLocalDataSource
import com.devshady.auth.sdk.data.model.OtpRequest
import com.devshady.auth.sdk.data.model.VerifyRequest
import com.devshady.auth.sdk.domain.model.AuthResult
import com.devshady.auth.sdk.domain.model.UserSession
import com.devshady.auth.sdk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
    private val apiService: AuthApiService,
    private val localDataSource: AuthLocalDataSource
) : AuthRepository {

    override suspend fun requestOtp(phoneNumber: String): AuthResult<String> {
        return try {
            val response = apiService.requestOtp(OtpRequest(phoneNumber))
            if (response.isSuccessful && response.body()?.success == true) {
                AuthResult.Success(response.body()?.message ?: "OTP sent successfully")
            } else {
                AuthResult.Error(response.body()?.message ?: "Failed to request OTP")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): AuthResult<UserSession> {
        return try {
            val response = apiService.verifyOtp(VerifyRequest(phoneNumber, otp))
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.authToken != null) {
                val session = UserSession(
                    phoneNumber = phoneNumber,
                    authToken = body.authToken,
                    isAuthenticated = true
                )
                localDataSource.saveSession(session)
                AuthResult.Success(session)
            } else {
                AuthResult.Error(body?.message ?: "OTP verification failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override fun getUserSession(): Flow<UserSession> = localDataSource.userSession

    override suspend fun clearSession() {
        localDataSource.clearSession()
    }
}
