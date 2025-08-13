package com.neniukov.tradebot.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.io.Serializable
import javax.inject.Inject
import com.google.gson.Gson

class SharedPrefs @Inject constructor(
    private val context: Context,
    private val cryptoManager: CryptoManager
) {

    private val Context.dataStore by preferencesDataStore(name = PREF_FILE_NAME)

    suspend fun saveApiKey(key: String) {
        val encryptedKey = cryptoManager.encrypt(key)
        Log.e("SharedPrefs", "Encrypted API Key: $encryptedKey")
        save(API_KEY, encryptedKey)
    }

    suspend fun getApiKey(): String? {
        return getPreferences()[stringPreferencesKey(API_KEY)]?.let(cryptoManager::decrypt)
    }

    suspend fun saveSecretKey(key: String) {
        val encryptedKey = cryptoManager.encrypt(key)
        Log.e("SharedPrefs", "Encrypted API Key: $encryptedKey")
        save(API_SECRET_KEY, encryptedKey)
    }

    suspend fun getSecretKey(): String? {
        return getPreferences()[stringPreferencesKey(API_SECRET_KEY)]?.let(cryptoManager::decrypt)
    }

    suspend fun saveTickers(stringList: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[stringSetPreferencesKey(TICKERS)] = stringList.toSet()
        }
    }

    suspend fun getTickers(): List<String> {
        return getPreferences()[stringSetPreferencesKey(TICKERS)]?.toList() ?: emptyList()
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun saveLastTicker(symbol: String, qty: String, leverage: Int) {
        val tickerJson = Gson().toJson(mapOf("symbol" to symbol, "qty" to qty, "leverage" to leverage))
        save(LAST_TICKER, tickerJson)
    }

    suspend fun getLastTicker(): Triple<String, String, Int>? {
        val tickerJson = getPreferences()[stringPreferencesKey(LAST_TICKER)] ?: return null
        val map = Gson().fromJson<Map<String, Any>>(tickerJson, Map::class.java)
        val symbol = map["symbol"] as? String ?: return null
        val qty = map["qty"] as? String ?: return null
        val leverage = (map["leverage"] as? Double)?.toInt() ?: return null
        return Triple(symbol, qty, leverage)
    }

    private suspend fun <T : Serializable> save(key: String, value: T?) {
        context.dataStore.edit { prefs ->
            when (value) {
                is String -> prefs[stringPreferencesKey(key)] = value
                is Int -> prefs[intPreferencesKey(key)] = value
                is Boolean -> prefs[booleanPreferencesKey(key)] = value
                is Float -> prefs[floatPreferencesKey(key)] = value
                is Long -> prefs[longPreferencesKey(key)] = value
            }
        }
    }

    private suspend fun getPreferences() = context.dataStore.data.first()

    companion object {
        private const val PREF_FILE_NAME = "secure_prefs"
        private const val API_KEY = "API_KEY"
        private const val API_SECRET_KEY = "API_SECRET_KEY"
        private const val TICKERS = "TICKERS"
        private const val LAST_TICKER = "LAST_TICKER"
    }
}