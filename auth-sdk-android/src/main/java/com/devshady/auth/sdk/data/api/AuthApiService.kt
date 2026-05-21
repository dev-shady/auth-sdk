package com.devshady.auth.sdk.data.api

import com.devshady.auth.sdk.data.model.OtpRequest
import com.devshady.auth.sdk.data.model.OtpResponse
import com.devshady.auth.sdk.data.model.VerifyRequest
import com.devshady.auth.sdk.data.model.VerifyResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/request-otp")
    suspend fun requestOtp(@Body request: OtpRequest): Response<OtpResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyRequest): Response<VerifyResponse>
}
