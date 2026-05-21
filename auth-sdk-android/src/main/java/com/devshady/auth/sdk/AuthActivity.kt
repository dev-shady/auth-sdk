package com.devshady.auth.sdk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.devshady.auth.sdk.ui.navigation.AuthNavKey
import com.devshady.auth.sdk.ui.navigation.AuthNavigation
import com.devshady.auth.sdk.ui.navigation.PhoneEntryKey
import com.devshady.auth.sdk.ui.theme.AuthSdkTheme
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack

class AuthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AuthSdkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val backStack = rememberNavBackStack(PhoneEntryKey)
                    @Suppress("UNCHECKED_CAST")
                    AuthNavigation(
                        backStack = backStack as NavBackStack<AuthNavKey>,
                        onFinish = {
                            // Notify SDK of success and finish
                            AuthSdk.notifyAuthSuccess()
                            finish()
                        }
                    )
                }
            }
        }
    }
}
