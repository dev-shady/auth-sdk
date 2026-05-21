package com.devshady.auth.sdk.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthNavKey : NavKey

@Serializable
data object PhoneEntryKey : AuthNavKey

@Serializable
data class OtpVerificationKey(val phoneNumber: String) : AuthNavKey

@Serializable
data object AuthSuccessKey : AuthNavKey
