package com.devshady.auth.sdk.data.remote

import com.devshady.auth.sdk.data.api.AuthApiService
import com.devshady.auth.sdk.data.model.OtpRequest
import com.devshady.auth.sdk.data.model.OtpResponse
import com.devshady.auth.sdk.data.model.VerifyRequest
import com.devshady.auth.sdk.data.model.VerifyResponse
import retrofit2.Response

class AuthRemoteDataSourceImpl(
    private val apiService: AuthApiService
) : AuthRemoteDataSource {

    override suspend fun requestOtp(phoneNumber: String): Response<OtpResponse> {
        return apiService.requestOtp(OtpRequest(phoneNumber))
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): Response<VerifyResponse> {
        return apiService.verifyOtp(VerifyRequest(phoneNumber, otp))
    }
}
