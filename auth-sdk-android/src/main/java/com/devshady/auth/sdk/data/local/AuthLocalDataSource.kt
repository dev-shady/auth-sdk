package com.devshady.auth.sdk.data.local

import com.devshady.auth.sdk.domain.model.UserSession
import kotlinx.coroutines.flow.Flow

interface AuthLocalDataSource {
    val userSession: Flow<UserSession>
    suspend fun saveSession(session: UserSession)
    suspend fun clearSession()
}
