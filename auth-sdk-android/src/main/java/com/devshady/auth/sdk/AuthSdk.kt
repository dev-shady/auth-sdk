package com.devshady.auth.sdk

import android.content.Context
import android.content.Intent
import com.devshady.auth.sdk.di.ServiceLocator
import com.devshady.auth.sdk.domain.model.UserSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AuthSdk {

    @Volatile
    internal var configuration: AuthConfiguration? = null
        private set

    private val sdkScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authResult = MutableStateFlow(UserSession())
    val authResult: StateFlow<UserSession> = _authResult.asStateFlow()

    val currentSession: UserSession
        get() = authResult.value

    fun initialize(context: Context, config: AuthConfiguration = AuthConfiguration()) {
        this.configuration = config
        ServiceLocator.init(context)

        // Reset cached network/repo singletons so any new AuthConfiguration settings (e.g. baseUrl, useMockData) take effect
        ServiceLocator.resetCachedDependencies()

        // Bind DataStore reactive flow directly to AuthSdk.authResult StateFlow on Dispatchers.IO
        sdkScope.launch {
            ServiceLocator.provideAuthRepository(context).getUserSession().collect { session ->
                _authResult.value = session
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
            _authResult.value = it
        }
    }
}
