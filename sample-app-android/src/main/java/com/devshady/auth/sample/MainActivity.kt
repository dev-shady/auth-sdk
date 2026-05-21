package com.devshady.auth.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devshady.auth.sdk.AuthSdk
import com.devshady.auth.sdk.AuthConfiguration
import com.devshady.auth.sdk.ui.theme.AuthSdkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hashHelper = AppSignatureHelper(this)
        Log.d("sms auto fill SDK_HASH_TEST", "Your Dynamic Debug Hash Code is: ${hashHelper.appSignatures.firstOrNull()}")


        // Initialize SDK
        AuthSdk.initialize(AuthConfiguration(
            enableSmsRetriever = true,
            useMockData = true
            ))
        
        enableEdgeToEdge()
        setContent {
            AuthSdkTheme {
                val authResult by AuthSdk.authResult.collectAsState(initial = null)

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (authResult?.isAuthenticated == true) {
                            Text(
                                text = "Authenticated as: ${authResult?.phoneNumber}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { /* Handle logout */ }) {
                                Text("Logout")
                            }
                        } else {
                            Text(
                                text = "Welcome to Authentication Module",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 32.dp)
                            )
                            Button(onClick = {
                                AuthSdk.startAuth(this@MainActivity)
                            }) {
                                Text("Start Authentication")
                            }
                        }
                    }
                }
            }
        }
    }
}
