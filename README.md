# Auth SDK (Android)

[![Android CI Pipeline](https://github.com)](https://github.com)
![Platform](https://shields.io)
![Kotlin](https://shields.io)

An enterprise-grade, independent, and plug-and-play Phone Number + OTP Authentication SDK built for Android. This library is designed following **Tier-1 Clean Architecture** boundaries, zero-trust security standards, and complete UI/Logic encapsulation.

The repository layout is future-proofed using a platform-agnostic mono-repo structure, allowing a seamless incremental migration to **Kotlin Multiplatform (KMP)** without breaking existing client integrations.

---

## 🛠 Project Structure

The workspace is split into two isolated modules:
*   `:auth-sdk-android` - The standalone library module compiled into a reusable `.aar` artifact. Houses the core Clean Architecture layers, Jetpack Compose screens, and framework-level integrations.
*   `:sample-app-android` - A developmental sandbox and reference implementation used to dry-run, test, and debug the SDK locally.

---

## 🏗 Architectural Architecture & Tech Stack

This SDK strictly enforces **Unidirectional Data Flow (UDF)** and a zero-framework-coupled domain boundary:
-   **UI Layer:** Built 100% in Jetpack Compose utilizing structural Design Tokens for complete dynamic customization by the host app.
-   **Domain Layer:** Pure Kotlin (Zero `android.*` dependencies). Aggregated into an atomic `AuthUseCases` container injected directly into StateFlow-driven ViewModels.
-   **Data Layer:** Orchestrates remote data (Retrofit + OkHttp + Kotlinx Serialization) and secure local persistence (EncryptedSharedPreferences / Crypto Keystore).
-   **Automated OTP Capture:** Utilizes the Google Play Services **SMS Retriever API** via a dynamic `callbackFlow` lifecycle broadcast interceptor for 0-click onboarding experiences.
-   **Security Hardening:** Ready for multi-layered attestation via the **Google Play Integrity API** using cryptographically bound server-side nonces.

---

## 🚀 Getting Started (Integration Guide)

### 1. Add the Dependency
Add the library coordinate to your consumer application's `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("com.devshady.sdk:auth-sdk-android:1.0.0")
}
```

### 2. Initialize the SDK
Initialize the SDK inside your host application's custom `Application` class. This enforces **Inversion of Control (IoC)**, ensuring no credentials or design systems are hardcoded inside the module binary.

```kotlin
import android.app.Application
import com.devshady.auth.sdk.AuthSdk
import com.devshady.auth.sdk.model.SdkConfiguration
import com.devshady.auth.sdk.model.SdkTheme

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        AuthSdk.initialize(
            config = SdkConfiguration(
                baseUrl = "https://yourfirm.com",
                apiKey = "prod_secure_crypto_key_abc123",
                playIntegrityCloudProjectNumber = 1234567890L,
                themeConfiguration = SdkTheme(
                    primaryColorLong = 0xFF6200EE, // Inject client native brand color
                    buttonCornerRadiusDp = 8,
                    brandLogoResId = R.drawable.app_brand_logo
                )
            )
        )
    }
}
```

### 3. Launch the Authentication Flow
Trigger the visual onboarding wizard from any Activity or Fragment context. The SDK returns execution control cleanly back to the application via an atomic callback result, letting the host handle its own localized navigation routing.

```kotlin
class LauncherActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val authSdk = AuthSdk.getInstance()

        // Launch the visual SDK flow interactively
        authSdk.launchLoginFlow(context = this) { result ->
            when (result) {
                is AuthSdkResult.Success -> {
                    // Route to your native dashboard screen with session tokens
                    navigateToHomeDashboard(result.accessToken, result.userId)
                }
                is AuthSdkResult.Cancelled -> {
                    // Handle case where user voluntarily closed or backed out of the SDK screen
                }
                is AuthSdkResult.Failure -> {
                    // Handle catastrophic network or API errors gracefully
                    showErrorSnackbar(result.errorDescription)
                }
            }
        }
    }
}
```

### 4. Monitor Global Session States
You can listen to session modifications reactively from anywhere inside the host application using the public exposed state flow pipeline:

```kotlin
lifecycleScope.launch {
    authSdk.sessionState.collect { sessionState ->
        when (sessionState) {
            is SdkSessionState.LoggedIn -> // User session is active
            is SdkSessionState.LoggedOut -> // User is unauthenticated / Token expired
        }
    }
}
```

---

## 🔒 ProGuard / Obfuscation Rules
The library bundles its own consumer-level proguard directives (`consumer-rules.pro`). When the host application compiles a production release, the compiler automatically preserves internal network serialization paths against breaking optimization compressions.

---

## 📄 License
Internal Proprietary Software Engine. All structural and architectural patterns are owned explicitly by the Engineering Core Division.
