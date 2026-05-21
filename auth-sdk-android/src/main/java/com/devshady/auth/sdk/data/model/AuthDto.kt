package com.devshady.auth.sdk.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OtpRequest(
    @Json(name = "phone_number") val phoneNumber: String
)

@JsonClass(generateAdapter = true)
data class OtpResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyRequest(
    @Json(name = "phone_number") val phoneNumber: String,
    @Json(name = "otp") val otp: String
)

@JsonClass(generateAdapter = true)
data class VerifyResponse(
    @Json(name = "auth_token") val authToken: String?,
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String? = null
)
