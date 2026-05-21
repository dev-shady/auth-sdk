# Auth SDK for Android

[![Platform](https://img.shields.io/badge/platform-android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.7.0-orange.svg)](https://developer.android.com/jetpack/compose)

A modular, modern Authentication SDK for Android applications, built with Jetpack Compose and following Clean Architecture principles. This SDK provides a seamless phone-based authentication flow with OTP verification.

## 🚀 Features

- **Phone Number Authentication:** Easy-to-use entry for mobile numbers.
- **OTP Verification:** Secure verification with automatic SMS retrieval support.
- **Modern UI:** Built entirely with Jetpack Compose and Material 3.
- **Adaptive Layout:** Supports various screen sizes (phones, tablets, foldables) using Compose Material 3 Adaptive.
- **Clean Architecture:** Separated into Data, Domain, and UI layers for maintainability and testability.
- **Secure Storage:** Session management using EncryptedSharedPreferences (via DataStore).
- **Dependency Injection:** Lightweight Service Locator pattern for module management.

## 🛠 Tech Stack

- **UI:** Jetpack Compose, Material 3, Adaptive Layouts
- **Asynchronous:** Kotlin Coroutines & Flow
- **Networking:** Retrofit & OkHttp
- **Serialization:** Kotlinx Serialization
- **Storage:** Jetpack DataStore (Preferences)
- **Navigation:** Navigation3 (Beta)

## 📦 Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.devshady.auth:auth-sdk:1.0.0")
}
```
*(Note: Replace with your actual implementation method, e.g., JitPack or Maven Central)*

## 🚦 Quick Start

### 1. Initialize the SDK

Initialize the SDK in your `Application` class or main `Activity`.

```kotlin
AuthSdk.initialize(
    AuthConfiguration(
        enableSmsRetriever = true,
        baseUrl = "https://api.yourdomain.com/"
    )
)
```

### 2. Start the Authentication Flow

Launch the authentication UI from anywhere in your app:

```kotlin
AuthSdk.startAuth(context)
```

### 3. Observe Authentication Results

Listen for the authentication result using `SharedFlow`:

```kotlin
lifecycleScope.launch {
    AuthSdk.authResult.collect { session ->
        if (session.isAuthenticated) {
            // Proceed to your app's main screen
            val token = session.authToken
            val phone = session.phoneNumber
        }
    }
}
```

Or using Compose:

```kotlin
val authResult by AuthSdk.authResult.collectAsState(initial = null)

LaunchedEffect(authResult) {
    if (authResult?.isAuthenticated == true) {
        // Handle success
    }
}
```

## ⚙️ Configuration

The `AuthConfiguration` class allows you to customize the SDK behavior:

| Property | Type | Description |
| :--- | :--- | :--- |
| `baseUrl` | `String?` | The base URL for your authentication API. |
| `appHash` | `String?` | Your app's hash string for the SMS Retriever API. |
| `enableSmsRetriever` | `Boolean` | Whether to enable automatic OTP filling via SMS. |

## 🧪 Testing

The library includes both Unit tests and Instrumented tests. Run them using:

```bash
./gradlew :auth-sdk-android:test
./gradlew :auth-sdk-android:connectedAndroidTest
```

## 📱 Sample App

A complete sample application is available in the `sample-app-android` directory, demonstrating the integration and usage of the SDK.

## 📄 License

```text
Copyright 2024 Dev Shady

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
