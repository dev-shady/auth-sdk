package com.devshady.auth.sdk.data.remote

import com.devshady.auth.sdk.data.model.OtpResponse
import com.devshady.auth.sdk.data.model.VerifyResponse
import retrofit2.Response

interface AuthRemoteDataSource {
    suspend fun requestOtp(phoneNumber: String): Response<OtpResponse>
    suspend fun verifyOtp(phoneNumber: String, otp: String): Response<VerifyResponse>
}
