package com.devshady.auth.sdk.ui.screens

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devshady.auth.sdk.domain.model.AuthResult
import com.devshady.auth.sdk.domain.model.UserSession
import com.devshady.auth.sdk.domain.repository.AuthRepository
import com.devshady.auth.sdk.util.SmsRetrieverHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OtpVerificationViewModel(
    private val repository: AuthRepository,
    private val smsRetrieverHelper: SmsRetrieverHelper
) : ViewModel() {

    private val _otp = MutableStateFlow("")
    val otp: StateFlow<String> = _otp.asStateFlow()

    private val _verificationState = MutableStateFlow<AuthResult<UserSession>?>(null)
    val verificationState: StateFlow<AuthResult<UserSession>?> = _verificationState.asStateFlow()

    fun startSmsRetriever() {
        smsRetrieverHelper.startListening { retrievedOtp ->
            _otp.value = retrievedOtp
        }
    }

    fun stopSmsRetriever() {
        smsRetrieverHelper.stopListening()
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        stopSmsRetriever()
    }

    fun onOtpChange(newOtp: String) {
        _otp.value = newOtp
    }

    fun verifyOtp(phoneNumber: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _verificationState.value = AuthResult.Loading
            val result = repository.verifyOtp(phoneNumber, _otp.value)
            _verificationState.value = result
            if (result is AuthResult.Success) {
                onSuccess()
            }
        }
    }
}
