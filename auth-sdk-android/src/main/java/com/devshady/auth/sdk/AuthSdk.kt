package com.devshady.auth.sdk

import android.content.Context
import android.content.Intent
import com.devshady.auth.sdk.di.ServiceLocator
import com.devshady.auth.sdk.domain.model.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AuthSdk {

    @Volatile
    internal var configuration: AuthConfiguration? = null
        private set

    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authResult = MutableSharedFlow<UserSession>(
        extraBufferCapacity = 1,
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val authResult: SharedFlow<UserSession> = _authResult.asSharedFlow()

    fun initialize(context: Context, config: AuthConfiguration = AuthConfiguration()) {
        this.configuration = config
        ServiceLocator.init(context)

        // Reset cached network/repo singletons so any new AuthConfiguration settings (e.g. baseUrl, useMockData) take effect
        ServiceLocator.resetCachedDependencies()

        // Bind DataStore reactive flow directly to AuthSdk.authResult on Dispatchers.IO
        sdkScope.launch {
            ServiceLocator.provideAuthRepository(context).getUserSession().collect { session ->
                _authResult.emit(session)
            }
        }
    }

    @Deprecated(
        message = "Use initialize(context, config) to properly initialize AuthSDK with context.",
        replaceWith = ReplaceWith("AuthSdk.initialize(context, config)")
    )
    fun initialize(config: AuthConfiguration) {
        this.configuration = config
    }

    fun startAuth(context: Context) {
        val intent = Intent(context, AuthActivity::class.java).apply {
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }

    suspend fun logout(context: Context? = null) = withContext(Dispatchers.IO) {
        ServiceLocator.provideAuthRepository(context).clearSession()
    }

    internal fun notifyAuthSuccess(session: UserSession? = null) {
        session?.let {
            _authResult.tryEmit(it)
        }
    }
}
