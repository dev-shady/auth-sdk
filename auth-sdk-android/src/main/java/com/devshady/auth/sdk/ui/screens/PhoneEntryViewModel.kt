package com.devshady.auth.sdk.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devshady.auth.sdk.domain.model.AuthResult
import com.devshady.auth.sdk.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PhoneEntryViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()

    private val _authState = MutableStateFlow<AuthResult<String>?>(null)
    val authState: StateFlow<AuthResult<String>?> = _authState.asStateFlow()

    fun onPhoneNumberChange(newNumber: String) {
        _phoneNumber.value = newNumber
    }

    fun requestOtp(onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _authState.value = AuthResult.Loading
            val result = repository.requestOtp(_phoneNumber.value)
            _authState.value = result
            if (result is AuthResult.Success) {
                onSuccess(_phoneNumber.value)
            }
        }
    }
}
