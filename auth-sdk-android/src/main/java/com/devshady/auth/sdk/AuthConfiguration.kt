package com.devshady.auth.sdk

data class AuthConfiguration(
    val baseUrl: String? = null,
    val appHash: String? = null, // For SMS Retriever API if needed
    val enableSmsRetriever: Boolean = true,
    val useMockData: Boolean = false
)
