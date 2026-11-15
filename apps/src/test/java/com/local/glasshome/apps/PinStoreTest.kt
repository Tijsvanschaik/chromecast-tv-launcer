package com.local.glasshome.apps

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PinStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var scope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: PinStore

    @Before
    fun setUp() {
        scope = CoroutineScope(Dispatchers.IO + Job())
        dataStore = PreferenceDataStoreFactory.create(scope = scope) {
            folder.newFile("pins.preferences_pb").also { it.delete() }
        }
        store = PinStore(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun pins() = runBlocking { store.state.first().pins }

    @Test
    fun firstRefreshSeedsOnlyOnce() = runBlocking {
        store.recordRefresh(listOf("a", "b"))
        store.recordRefresh(listOf("c"))
        assertEquals(listOf("a", "b"), pins())
    }

    @Test
    fun emptySeedDoesNotUseUpFirstRunSeeding() = runBlocking {
        store.recordRefresh(emptyList())
        store.recordRefresh(listOf("com.netflix.ninja"))
        assertEquals(listOf("com.netflix.ninja"), pins())
    }

    @Test
    fun unpinPersistsAcrossRefresh() = runBlocking {
        store.recordRefresh(listOf("a", "b"))
        store.setPinned("a", pinned = false)
        store.recordRefresh(listOf("a", "b"))
        assertEquals(listOf("b"), pins())
    }

    @Test
    fun refreshNeverDropsPinsForMissingApps() = runBlocking {
        store.recordRefresh(listOf("a", "b"))
        store.recordRefresh(emptyList())
        assertEquals(listOf("a", "b"), pins())
    }

    @Test
    fun pruneRemovesPinAndHidden() = runBlocking {
        store.recordRefresh(listOf("a", "b"))
        store.setHidden("a", hidden = true)
        store.prune("a")
        val state = store.state.first()
        assertEquals(listOf("b"), state.pins)
        assertEquals(emptySet<String>(), state.hidden)
    }

    @Test
    fun moveSkipsPinsThatAreNotVisible() = runBlocking {
        store.recordRefresh(listOf("a", "gone", "c"))
        store.move("c", direction = -1, visible = setOf("a", "c"))
        assertEquals(listOf("c", "gone", "a"), pins())
        store.move("c", direction = -1, visible = setOf("a", "c"))
        assertEquals(listOf("c", "gone", "a"), pins())
    }

    @Test
    fun hideAndUnhide() = runBlocking {
        store.setHidden("x", hidden = true)
        assertEquals(setOf("x"), store.state.first().hidden)
        store.setHidden("x", hidden = false)
        assertEquals(emptySet<String>(), store.state.first().hidden)
    }

    @Test
    fun v1ComponentPinsMigrateToPackages() = runBlocking {
        dataStore.edit {
            it[stringPreferencesKey("pinned_components")] = "com.netflix.ninja/.Main\ncom.spotify.tv.android/.Tv"
            it[androidx.datastore.preferences.core.booleanPreferencesKey("pins_initialized")] = true
        }
        store.recordRefresh(listOf("ignored"))
        assertEquals(listOf("com.netflix.ninja", "com.spotify.tv.android"), pins())
    }
}
