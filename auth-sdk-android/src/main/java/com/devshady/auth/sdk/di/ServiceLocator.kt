package com.devshady.auth.sdk.di

import android.content.Context
import com.devshady.auth.sdk.AuthSdk
import com.devshady.auth.sdk.data.api.AuthApiService
import com.devshady.auth.sdk.data.local.AuthLocalDataSource
import com.devshady.auth.sdk.data.local.AuthLocalDataSourceImpl
import com.devshady.auth.sdk.data.remote.AuthRemoteDataSource
import com.devshady.auth.sdk.data.remote.AuthRemoteDataSourceImpl
import com.devshady.auth.sdk.data.remote.FakeAuthRemoteDataSource
import com.devshady.auth.sdk.data.repository.AuthRepositoryImpl
import com.devshady.auth.sdk.domain.repository.AuthRepository
import com.devshady.auth.sdk.ui.screens.OtpVerificationViewModel
import com.devshady.auth.sdk.ui.screens.PhoneEntryViewModel
import com.devshady.auth.sdk.util.SmsRetrieverHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ServiceLocator {

    @Volatile private var applicationContext: Context? = null
    @Volatile private var authRepository: AuthRepository? = null
    @Volatile private var authApiService: AuthApiService? = null
    @Volatile private var authLocalDataSource: AuthLocalDataSource? = null
    @Volatile private var authRemoteDataSource: AuthRemoteDataSource? = null
    @Volatile private var smsRetrieverHelper: SmsRetrieverHelper? = null

    private const val BASE_URL = "https://api.example.com/" // Placeholder
    private val scope = CoroutineScope(Dispatchers.IO)

    fun init(context: Context) {
        if (applicationContext == null) {
            synchronized(this) {
                if (applicationContext == null) {
                    applicationContext = context.applicationContext
                }
            }
        }
    }

    fun resetCachedDependencies() {
        synchronized(this) {
            authRepository = null
            authApiService = null
            authRemoteDataSource = null
            // Preserve local data source & sms retriever helper if context is valid
        }
    }

    private fun getContext(context: Context? = null): Context {
        return context?.applicationContext
            ?: applicationContext
            ?: error("AuthSDK is not initialized. Please call AuthSdk.initialize(context, config) before using SDK components.")
    }

    fun provideAuthRepository(context: Context? = null): AuthRepository {
        val targetContext = getContext(context)
        return authRepository ?: synchronized(this) {
            authRepository ?: createAuthRepository(targetContext).also { authRepository = it }
        }
    }

    fun provideSmsRetrieverHelper(context: Context? = null): SmsRetrieverHelper {
        val targetContext = getContext(context)
        return smsRetrieverHelper ?: synchronized(this) {
            smsRetrieverHelper ?: SmsRetrieverHelper(targetContext).also { smsRetrieverHelper = it }
        }
    }

    fun providePhoneEntryViewModel(context: Context? = null): PhoneEntryViewModel {
        return PhoneEntryViewModel(provideAuthRepository(context))
    }

    fun provideOtpVerificationViewModel(context: Context? = null): OtpVerificationViewModel {
        return OtpVerificationViewModel(
            provideAuthRepository(context),
            provideSmsRetrieverHelper(context)
        )
    }

    private fun createAuthRepository(context: Context): AuthRepository {
        return AuthRepositoryImpl(
            provideAuthRemoteDataSource(),
            provideAuthLocalDataSource(context)
        )
    }

    private fun provideAuthRemoteDataSource(): AuthRemoteDataSource {
        val useMock = AuthSdk.configuration?.useMockData ?: false
        return authRemoteDataSource ?: synchronized(this) {
            authRemoteDataSource ?: run {
                val dataSource = if (useMock) {
                    FakeAuthRemoteDataSource()
                } else {
                    AuthRemoteDataSourceImpl(provideAuthApiService())
                }
                dataSource.also { authRemoteDataSource = it }
            }
        }
    }

    private fun provideAuthApiService(): AuthApiService {
        return authApiService ?: synchronized(this) {
            authApiService ?: createAuthApiService().also { authApiService = it }
        }
    }

    private fun createAuthApiService(): AuthApiService {
        val clientUrl = AuthSdk.configuration?.baseUrl ?: BASE_URL
        val isDebugMock = AuthSdk.configuration?.useMockData ?: false

        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebugMock) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // 401 Unauthorized Clean-Up Interceptor
        val authInterceptor = Interceptor { chain ->
            val response = chain.proceed(chain.request())
            if (response.code == 401) {
                val context = applicationContext
                if (context != null) {
                    scope.launch {
                        provideAuthLocalDataSource(context).clearSession()
                    }
                }
            }
            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(clientUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(client)
            .build()
            .create(AuthApiService::class.java)
    }

    private fun provideAuthLocalDataSource(context: Context): AuthLocalDataSource {
        return authLocalDataSource ?: synchronized(this) {
            authLocalDataSource ?: AuthLocalDataSourceImpl(context.applicationContext).also { authLocalDataSource = it }
        }
    }
}
