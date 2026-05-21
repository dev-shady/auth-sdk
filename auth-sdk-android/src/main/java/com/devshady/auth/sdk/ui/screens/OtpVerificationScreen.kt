package com.devshady.auth.sdk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devshady.auth.sdk.R
import com.devshady.auth.sdk.domain.model.AuthResult
import com.devshady.auth.sdk.ui.components.AdaptiveContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    viewModel: OtpVerificationViewModel,
    onSuccess: () -> Unit
) {
    val otp by viewModel.otp.collectAsState()
    val verificationState by viewModel.verificationState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.auth_sdk_otp_verification_title)) }
            )
        }
    ) { innerPadding ->
        AdaptiveContent(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.auth_sdk_otp_verification_description, phoneNumber),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) viewModel.onOtpChange(it) },
                label = { Text(stringResource(R.string.auth_sdk_otp_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                enabled = verificationState !is AuthResult.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.verifyOtp(phoneNumber, onSuccess) },
                modifier = Modifier.fillMaxWidth(),
                enabled = otp.length == 6 && verificationState !is AuthResult.Loading
            ) {
                if (verificationState is AuthResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.auth_sdk_verify_button))
                }
            }

            if (verificationState is AuthResult.Error) {
                Text(
                    text = (verificationState as AuthResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
