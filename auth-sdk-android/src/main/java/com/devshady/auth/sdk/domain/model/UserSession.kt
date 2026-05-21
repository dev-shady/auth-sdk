package com.devshady.auth.sdk.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val phoneNumber: String,
    val authToken: String? = null,
    val isAuthenticated: Boolean = false
)
