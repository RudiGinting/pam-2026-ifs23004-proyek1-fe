package org.delcom.pam_proyek1_ifs23004.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Ekstensi properti untuk membuat instance DataStore (Hanya dibuat 1 kali / Singleton)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_token_prefs")

class AuthTokenPref(private val context: Context) {

    // Key untuk menyimpan token (menggunakan stringPreferencesKey)
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("AUTH_TOKEN_KEY")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("REFRESH_TOKEN_KEY")
    }

    // ==========================================
    // SIMPAN / HAPUS KEDUA TOKEN SEKALIGUS (FIX RACE CONDITION)
    // ==========================================

    // Menyimpan kedua token sekaligus dalam satu transaksi DataStore
    suspend fun saveTokens(authToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = authToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    // Menghapus kedua token sekaligus dalam satu transaksi DataStore
    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    // ==========================================
    // AUTH TOKEN (SINGLE)
    // ==========================================

    // Menyimpan token (Suspend function karena proses asynchronous)
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    // Mengambil token (Mengembalikan Flow agar reaktif)
    fun getAuthToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }
    }

    // Menghapus token
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
        }
    }

    // ==========================================
    // REFRESH TOKEN (SINGLE)
    // ==========================================

    // Menyimpan token
    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    // Mengambil token
    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    // Menghapus token
    suspend fun clearRefreshToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
}