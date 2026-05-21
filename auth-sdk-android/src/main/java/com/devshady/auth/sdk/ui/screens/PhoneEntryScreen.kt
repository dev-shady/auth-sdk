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
fun PhoneEntryScreen(
    viewModel: PhoneEntryViewModel,
    onNext: (String) -> Unit
) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val authState by viewModel.authState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.auth_sdk_phone_entry_title)) }
            )
        }
    ) { innerPadding ->
        AdaptiveContent(
            modifier = Modifier.padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.auth_sdk_phone_entry_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { viewModel.onPhoneNumberChange(it) },
                label = { Text(stringResource(R.string.auth_sdk_phone_number_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                enabled = authState !is AuthResult.Loading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.requestOtp(onNext) },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.isNotBlank() && authState !is AuthResult.Loading
            ) {
                if (authState is AuthResult.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.auth_sdk_next_button))
                }
            }

            if (authState is AuthResult.Error) {
                Text(
                    text = (authState as AuthResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
