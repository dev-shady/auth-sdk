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
import com.devshady.auth.sdk.util.SmsRetrieverHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ServiceLocator {

    private var authRepository: AuthRepository? = null
    private var authApiService: AuthApiService? = null
    private var authLocalDataSource: AuthLocalDataSource? = null
    private var authRemoteDataSource: AuthRemoteDataSource? = null
    private var smsRetrieverHelper: SmsRetrieverHelper? = null

    private const val BASE_URL = "https://api.example.com/" // Placeholder

    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            authRepository ?: createAuthRepository(context).also { authRepository = it }
        }
    }

    fun provideSmsRetrieverHelper(context: Context): SmsRetrieverHelper {
        return smsRetrieverHelper ?: synchronized(this) {
            smsRetrieverHelper ?: SmsRetrieverHelper(context.applicationContext).also { smsRetrieverHelper = it }
        }
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
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
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
