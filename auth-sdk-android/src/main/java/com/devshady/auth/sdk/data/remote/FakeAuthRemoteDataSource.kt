package com.devshady.auth.sdk.data.remote

import com.devshady.auth.sdk.data.model.OtpResponse
import com.devshady.auth.sdk.data.model.VerifyResponse
import kotlinx.coroutines.delay
import retrofit2.Response

class FakeAuthRemoteDataSource : AuthRemoteDataSource {

    override suspend fun requestOtp(phoneNumber: String): Response<OtpResponse> {
        delay(1000) // Simulate network delay
        return if (phoneNumber.length >= 10) {
            Response.success(OtpResponse(success = true, message = "Mock: OTP sent to $phoneNumber"))
        } else {
            Response.success(OtpResponse(success = false, message = "Mock: Invalid phone number"))
        }
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String): Response<VerifyResponse> {
        delay(1000) // Simulate network delay
        return if (otp == "123456") {
            Response.success(
                VerifyResponse(
                    authToken = "mock_token_${System.currentTimeMillis()}",
                    success = true,
                    message = "Mock: Verification successful"
                )
            )
        } else {
            Response.success(
                VerifyResponse(
                    authToken = null,
                    success = false,
                    message = "Mock: Invalid OTP (Use 123456)"
                )
            )
        }
    }
}
