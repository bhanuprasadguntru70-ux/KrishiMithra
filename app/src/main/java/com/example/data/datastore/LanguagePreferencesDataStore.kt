package com.example.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_language_prefs")

data class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
)

class LanguagePreferencesDataStore(private val context: Context) {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("app_language")
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_TELUGU = "te"
        const val LANGUAGE_HINDI = "hi"
        const val LANGUAGE_TAMIL = "ta"
        const val LANGUAGE_KANNADA = "kn"

        val SUPPORTED_LANGUAGES = listOf(
            AppLanguage(LANGUAGE_ENGLISH, "English", "English"),
            AppLanguage(LANGUAGE_TELUGU, "Telugu", "తెలుగు"),
            AppLanguage(LANGUAGE_HINDI, "Hindi", "हिंदी"),
            AppLanguage(LANGUAGE_TAMIL, "Tamil", "தமிழ்"),
            AppLanguage(LANGUAGE_KANNADA, "Kannada", "కన్నడ")
        )
    }

    val languageFlow: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            preferences[LANGUAGE_KEY] ?: LANGUAGE_ENGLISH
        }

    suspend fun saveLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_KEY] = languageCode
        }
    }
}

