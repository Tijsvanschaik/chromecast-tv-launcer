package com.local.glasshome.apps

import android.content.ComponentName
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pinDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "glass_home_pins",
)

class PinStore(private val context: Context) {
    private val key = stringPreferencesKey("pinned_components")

    fun pinnedOrder(): Flow<List<ComponentName>> =
        context.pinDataStore.data.map { prefs ->
            decode(prefs[key].orEmpty())
        }

    suspend fun pin(entry: AppEntry, softCap: Int = SOFT_CAP) {
        context.pinDataStore.edit { prefs ->
            val current = decode(prefs[key].orEmpty()).toMutableList()
            val component = entry.componentName
            current.removeAll { it == component }
            current.add(0, component)
            while (current.size > softCap) {
                current.removeAt(current.lastIndex)
            }
            prefs[key] = encode(current)
        }
    }

    suspend fun unpin(entry: AppEntry) {
        context.pinDataStore.edit { prefs ->
            val current = decode(prefs[key].orEmpty()).toMutableList()
            current.removeAll { it.packageName == entry.packageName && it.className == entry.activityName }
            prefs[key] = encode(current)
        }
    }

    suspend fun reorder(from: Int, to: Int) {
        context.pinDataStore.edit { prefs ->
            val current = decode(prefs[key].orEmpty()).toMutableList()
            if (from !in current.indices || to !in current.indices) return@edit
            val item = current.removeAt(from)
            current.add(to, item)
            prefs[key] = encode(current)
        }
    }

    suspend fun replaceAll(order: List<ComponentName>) {
        context.pinDataStore.edit { prefs ->
            prefs[key] = encode(order)
        }
    }

    suspend fun dropMissing(installedKeys: Set<String>) {
        context.pinDataStore.edit { prefs ->
            val kept = decode(prefs[key].orEmpty()).filter { component ->
                "${component.packageName}/${component.className}" in installedKeys
            }
            prefs[key] = encode(kept)
        }
    }

    companion object {
        const val SOFT_CAP = 12

        fun encode(components: List<ComponentName>): String =
            components.joinToString("\n") { "${it.packageName}/${it.className}" }

        fun decode(raw: String): List<ComponentName> =
            raw.lineSequence()
                .map { it.trim() }
                .filter { it.isNotEmpty() && '/' in it }
                .mapNotNull { line ->
                    val slash = line.indexOf('/')
                    if (slash <= 0 || slash == line.lastIndex) return@mapNotNull null
                    ComponentName(line.substring(0, slash), line.substring(slash + 1))
                }
                .toList()
    }
}
