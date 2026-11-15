package com.local.glasshome.apps

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** One DataStore per process; creating a second one on the same file throws. */
val Context.glassDataStore: DataStore<Preferences> by preferencesDataStore(name = "glass_pins")

data class PinState(
    val pins: List<String> = emptyList(),
    val hidden: Set<String> = emptySet(),
)

/**
 * Local pin order and hidden apps, keyed by package name. Entries for packages that are not
 * currently installed stay stored (disabled or mid-update apps keep their pin) and are
 * removed only by [prune] on a full uninstall. First launch seeds installed must-keeps.
 */
class PinStore(private val dataStore: DataStore<Preferences>) {

    val state: Flow<PinState> = dataStore.data.map { prefs ->
        PinState(pins = prefs[PINS].toList(), hidden = prefs[HIDDEN].toList().toSet())
    }

    suspend fun recordRefresh(seedPackages: List<String>) {
        dataStore.edit { prefs ->
            migrateV1(prefs)
            if (prefs[INITIALIZED] != true) {
                prefs[PINS] = seedPackages.distinct().take(SOFT_CAP).joinToString("\n")
                prefs[INITIALIZED] = true
            }
        }
    }

    suspend fun setPinned(packageName: String, pinned: Boolean) {
        dataStore.edit { prefs ->
            prefs[INITIALIZED] = true
            val current = prefs[PINS].toList().toMutableList()
            if (pinned) {
                if (packageName !in current) current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[PINS] = current.joinToString("\n")
        }
    }

    /** Swaps [packageName] with its nearest [visible] neighbour in [direction] (-1 or +1). */
    suspend fun move(packageName: String, direction: Int, visible: Set<String>) {
        dataStore.edit { prefs ->
            val current = prefs[PINS].toList().toMutableList()
            val from = current.indexOf(packageName)
            if (from < 0) return@edit
            var to = from + direction
            while (to in current.indices && current[to] !in visible) to += direction
            if (to !in current.indices) return@edit
            current[from] = current[to]
            current[to] = packageName
            prefs[PINS] = current.joinToString("\n")
        }
    }

    suspend fun setHidden(packageName: String, hidden: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[HIDDEN].toList().toMutableList()
            if (hidden) {
                if (packageName !in current) current.add(packageName)
            } else {
                current.remove(packageName)
            }
            prefs[HIDDEN] = current.joinToString("\n")
        }
    }

    suspend fun prune(packageName: String) {
        dataStore.edit { prefs ->
            prefs[PINS] = prefs[PINS].toList().filter { it != packageName }.joinToString("\n")
            prefs[HIDDEN] = prefs[HIDDEN].toList().filter { it != packageName }.joinToString("\n")
        }
    }

    private fun migrateV1(prefs: MutablePreferences) {
        val legacy = prefs[LEGACY_PINS] ?: return
        if (prefs[PINS] == null) {
            prefs[PINS] = legacy.toList().map { it.substringBefore('/') }.distinct().joinToString("\n")
        }
        prefs.remove(LEGACY_PINS)
    }

    private fun String?.toList(): List<String> =
        orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }

    private companion object {
        val INITIALIZED = booleanPreferencesKey("pins_initialized")
        val LEGACY_PINS = stringPreferencesKey("pinned_components")
        val PINS = stringPreferencesKey("pinned_packages")
        val HIDDEN = stringPreferencesKey("hidden_packages")
        const val SOFT_CAP = 12
    }
}
