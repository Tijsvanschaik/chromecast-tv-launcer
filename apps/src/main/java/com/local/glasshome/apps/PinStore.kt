package com.local.glasshome.apps

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local pin order. Survives process death. Key is packageName/activityName.
 * First launch seeds installed must-keeps; later unpin is remembered.
 */
class PinStore(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.applicationContext.preferencesDataStoreFile("glass_pins") },
    )

    val order: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[PINS].orEmpty().lines().filter { it.isNotBlank() }
    }

    suspend fun recordRefresh(installedKeys: Set<String>, seedKeys: List<String>) {
        dataStore.edit { prefs ->
            if (prefs[INITIALIZED] != true) {
                prefs[PINS] = seedKeys.distinct().take(SOFT_CAP).joinToString("\n")
                prefs[INITIALIZED] = true
            }
            val kept = prefs[PINS].orEmpty()
                .lines()
                .filter { it.isNotBlank() && it in installedKeys }
            prefs[PINS] = kept.joinToString("\n")
        }
    }

    suspend fun setPinned(componentKey: String, pinned: Boolean) {
        dataStore.edit { prefs ->
            prefs[INITIALIZED] = true
            val current = prefs[PINS].orEmpty().lines().filter { it.isNotBlank() }.toMutableList()
            if (pinned) {
                if (componentKey !in current) current.add(componentKey)
            } else {
                current.remove(componentKey)
            }
            prefs[PINS] = current.joinToString("\n")
        }
    }

    private companion object {
        val INITIALIZED = booleanPreferencesKey("pins_initialized")
        val PINS = stringPreferencesKey("pinned_components")
        const val SOFT_CAP = 12
    }
}
