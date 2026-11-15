package com.local.glasshome.apps

import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val packageManager = appContext.packageManager
    private val repository = InstalledAppsRepository(packageManager)
    private val pins = PinStore(appContext.glassDataStore)

    private val installed = MutableStateFlow<List<AppEntry>>(emptyList())
    private val loaded = MutableStateFlow(false)
    private val showingHidden = MutableStateFlow(false)

    private val _banner = MutableStateFlow<String?>(null)
    val banner: StateFlow<String?> = _banner.asStateFlow()

    private val errors = CoroutineExceptionHandler { _, _ -> showBanner("Couldn't load apps") }

    val model: StateFlow<AppsUiModel> =
        combine(installed, pins.state, loaded, showingHidden) { apps, state, isLoaded, showHidden ->
            if (!isLoaded) return@combine AppsUiModel(loading = true)
            val byPackage = apps.associateBy { it.packageName }
            val empty = apps.isEmpty()
            AppsUiModel(
                favorites = state.pins.mapNotNull { byPackage[it] }.filter { it.packageName !in state.hidden },
                grid = if (showHidden) apps else apps.filter { it.packageName !in state.hidden },
                hidden = state.hidden,
                hiddenInstalledCount = apps.count { it.packageName in state.hidden },
                showingHidden = showHidden,
                isEmpty = empty,
                emptyHint = if (empty) EMPTY_APPS_HINT else null,
                loading = false,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AppsUiModel())

    private var refreshJob: Job? = null

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.data?.schemeSpecificPart
            if (pkg != null) AppArtCache.evict(pkg)
            if (intent?.action == Intent.ACTION_PACKAGE_FULLY_REMOVED && pkg != null) {
                viewModelScope.launch(errors) { pins.prune(pkg) }
            }
            refresh(debounce = true)
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= 33) {
            appContext.registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(packageReceiver, filter)
        }
        refresh()
    }

    fun refresh(debounce: Boolean = false) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch(errors) {
            if (debounce) delay(300)
            val apps = withContext(Dispatchers.IO) { repository.load() }
            pins.recordRefresh(seedPackages = mustKeepSeeds(apps))
            installed.value = apps
            loaded.value = true
        }
    }

    fun launch(entry: AppEntry) {
        val intent = packageManager.getLeanbackLaunchIntentForPackage(entry.packageName)
            ?: packageManager.getLaunchIntentForPackage(entry.packageName)
            ?: Intent(Intent.ACTION_MAIN).setComponent(ComponentName(entry.packageName, entry.activityName))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        runCatching { appContext.startActivity(intent) }
            .onFailure { showBanner("Couldn't open ${entry.label}") }
    }

    fun openAppInfo(entry: AppEntry) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${entry.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { appContext.startActivity(intent) }
            .onFailure { showBanner("Couldn't open app info for ${entry.label}") }
    }

    fun openSettings() = openSettingsPanel(Settings.ACTION_SETTINGS)

    fun openWifi() = openSettingsPanel(Settings.ACTION_WIFI_SETTINGS)

    fun openBluetooth() = openSettingsPanel(Settings.ACTION_BLUETOOTH_SETTINGS)

    private fun openSettingsPanel(action: String) {
        val opened = runCatching {
            appContext.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.isSuccess
        if (opened || action == Settings.ACTION_SETTINGS) {
            if (!opened) showBanner("Couldn't open Settings")
            return
        }
        openSettingsPanel(Settings.ACTION_SETTINGS)
    }

    fun setPinned(entry: AppEntry, pinned: Boolean) {
        viewModelScope.launch(errors) { pins.setPinned(entry.packageName, pinned) }
    }

    fun moveFavorite(entry: AppEntry, direction: Int) {
        val visible = model.value.favorites.mapTo(HashSet()) { it.packageName }
        viewModelScope.launch(errors) { pins.move(entry.packageName, direction, visible) }
    }

    fun setHidden(entry: AppEntry, hidden: Boolean) {
        viewModelScope.launch(errors) { pins.setHidden(entry.packageName, hidden) }
    }

    fun toggleShowHidden() {
        showingHidden.value = !showingHidden.value
    }

    private fun showBanner(text: String) {
        _banner.value = text
        viewModelScope.launch {
            delay(2_500)
            if (_banner.value == text) _banner.value = null
        }
    }

    override fun onCleared() {
        runCatching { appContext.unregisterReceiver(packageReceiver) }
    }
}
