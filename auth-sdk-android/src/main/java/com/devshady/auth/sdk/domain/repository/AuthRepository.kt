package com.devshady.auth.sdk.domain.repository

import com.devshady.auth.sdk.domain.model.AuthResult
import com.devshady.auth.sdk.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun requestOtp(phoneNumber: String): AuthResult<String> // Returns request ID or message
    suspend fun verifyOtp(phoneNumber: String, otp: String): AuthResult<UserSession>
    
    fun getUserSession(): Flow<UserSession>
    suspend fun clearSession()
}
