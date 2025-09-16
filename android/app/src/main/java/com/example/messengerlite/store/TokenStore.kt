package com.example.messengerlite.store

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.remove
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth")

@Singleton
class TokenStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val ACCESS = stringPreferencesKey("access")
    private val REFRESH = stringPreferencesKey("refresh")

    fun setTokens(access: String, refresh: String) = runBlocking {
        context.dataStore.edit { pref ->
            pref[ACCESS] = access
            pref[REFRESH] = refresh
        }
    }

    fun clear() = runBlocking { context.dataStore.edit { it.remove(ACCESS); it.remove(REFRESH) } }

    fun accessToken(): String? = runBlocking { context.dataStore.data.first()[ACCESS] }
    fun refreshToken(): String? = runBlocking { context.dataStore.data.first()[REFRESH] }
}

