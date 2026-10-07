package com.devshady.auth.sdk.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devshady.auth.sdk.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthLocalDataSourceImpl(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.dataStore
) : AuthLocalDataSource {

    private object PreferencesKeys {
        val PHONE_NUMBER = stringPreferencesKey("auth_phone_number")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val IS_AUTHENTICATED = booleanPreferencesKey("auth_is_authenticated")
        val EXPIRES_AT = longPreferencesKey("auth_expires_at")
    }

    override val userSession: Flow<UserSession> = dataStore.data.map { preferences ->
        val phoneNumber = preferences[PreferencesKeys.PHONE_NUMBER] ?: ""
        val rawToken = preferences[PreferencesKeys.AUTH_TOKEN]
        val isAuthenticated = preferences[PreferencesKeys.IS_AUTHENTICATED] ?: false
        val expiresAt = preferences[PreferencesKeys.EXPIRES_AT]

        // Stale data check
        if (expiresAt != null && System.currentTimeMillis() > expiresAt) {
            UserSession()
        } else {
            val decryptedToken = rawToken?.let { decrypt(it) } ?: rawToken
            UserSession(
                phoneNumber = phoneNumber,
                authToken = decryptedToken,
                isAuthenticated = isAuthenticated && !decryptedToken.isNullOrEmpty(),
                expiresAt = expiresAt
            )
        }
    }

    override suspend fun saveSession(session: UserSession) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PHONE_NUMBER] = session.phoneNumber
            session.authToken?.let {
                preferences[PreferencesKeys.AUTH_TOKEN] = encrypt(it)
            }
            preferences[PreferencesKeys.IS_AUTHENTICATED] = session.isAuthenticated
            session.expiresAt?.let { preferences[PreferencesKeys.EXPIRES_AT] = it }
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        private const val KEY_ALIAS = "auth_sdk_secret_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"

        private fun getSecretKey(): SecretKey? {
            return try {
                val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
                if (!keyStore.containsAlias(KEY_ALIAS)) {
                    val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        ANDROID_KEYSTORE
                    )
                    val spec = KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                    keyGenerator.init(spec)
                    keyGenerator.generateKey()
                } else {
                    val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
                    entry?.secretKey
                }
            } catch (_: Exception) {
                null
            }
        }

        private fun encrypt(plainText: String): String {
            return try {
                val secretKey = getSecretKey() ?: return plainText
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                val iv = cipher.iv
                val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                val combined = ByteArray(iv.size + encryptedBytes.size)
                System.arraycopy(iv, 0, combined, 0, iv.size)
                System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
                Base64.encodeToString(combined, Base64.NO_WRAP)
            } catch (_: Exception) {
                plainText
            }
        }

        private fun decrypt(encryptedText: String): String {
            return try {
                val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
                val secretKey = getSecretKey() ?: return encryptedText
                val cipher = Cipher.getInstance(TRANSFORMATION)
                val ivSize = 12 // Standard GCM IV length
                if (combined.size <= ivSize) return encryptedText
                val spec = GCMParameterSpec(128, combined, 0, ivSize)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                val decryptedBytes = cipher.doFinal(combined, ivSize, combined.size - ivSize)
                String(decryptedBytes, Charsets.UTF_8)
            } catch (_: Exception) {
                encryptedText
            }
        }
    }
}
