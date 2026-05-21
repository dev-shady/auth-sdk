package com.devshady.auth.sdk.di

import android.content.Context
import com.devshady.auth.sdk.data.api.AuthApiService
import com.devshady.auth.sdk.data.local.AuthLocalDataSource
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
    private var smsRetrieverHelper: SmsRetrieverHelper? = null

    private const val BASE_URL = "https://api.example.com/" // Placeholder

    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: createAuthRepository(context).also { authRepository = it }
    }

    fun provideSmsRetrieverHelper(context: Context): SmsRetrieverHelper {
        return smsRetrieverHelper ?: SmsRetrieverHelper(context.applicationContext).also { smsRetrieverHelper = it }
    }

    private fun createAuthRepository(context: Context): AuthRepository {
        //TODO synchronization
        return AuthRepositoryImpl(
            provideAuthApiService(),
            provideAuthLocalDataSource(context)
        )
    }

    private fun provideAuthApiService(): AuthApiService {
        //TODO synchronization
        return authApiService ?: createAuthApiService().also { authApiService = it }
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
        //TODO synchronization
        return authLocalDataSource ?: AuthLocalDataSource(context.applicationContext).also { authLocalDataSource = it }
    }
}
