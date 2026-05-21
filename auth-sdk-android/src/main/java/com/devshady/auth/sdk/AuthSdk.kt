package com.devshady.auth.sdk

import android.content.Context
import android.content.Intent
import com.devshady.auth.sdk.domain.model.UserSession
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthSdk {

    private var configuration: AuthConfiguration? = null
    private val _authResult = MutableSharedFlow<UserSession>(extraBufferCapacity = 1)
    val authResult: SharedFlow<UserSession> = _authResult.asSharedFlow()

    fun initialize(config: AuthConfiguration) {
        this.configuration = config
    }

    fun startAuth(context: Context) {
        val intent = Intent(context, AuthActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    internal fun notifyAuthSuccess() {
        // In a real app, we might pass the actual user session here
        // For now, we'll just emit a dummy session or let the host app read from DataStore
    }
}
