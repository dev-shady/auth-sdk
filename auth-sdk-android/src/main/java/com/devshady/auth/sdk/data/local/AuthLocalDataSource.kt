package com.devshady.auth.sdk.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.devshady.auth.sdk.domain.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthLocalDataSource(private val context: Context) {

    private object PreferencesKeys {
        val PHONE_NUMBER = stringPreferencesKey("auth_phone_number")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val IS_AUTHENTICATED = booleanPreferencesKey("auth_is_authenticated")
    }

    val userSession: Flow<UserSession> = context.dataStore.data.map { preferences ->
        UserSession(
            phoneNumber = preferences[PreferencesKeys.PHONE_NUMBER] ?: "",
            authToken = preferences[PreferencesKeys.AUTH_TOKEN],
            isAuthenticated = preferences[PreferencesKeys.IS_AUTHENTICATED] ?: false
        )
    }

    suspend fun saveSession(session: UserSession) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PHONE_NUMBER] = session.phoneNumber
            session.authToken?.let { preferences[PreferencesKeys.AUTH_TOKEN] = it }
            preferences[PreferencesKeys.IS_AUTHENTICATED] = session.isAuthenticated
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
