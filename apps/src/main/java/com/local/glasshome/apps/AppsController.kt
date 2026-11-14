package com.local.glasshome.apps

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsController(context: Context) : AutoCloseable {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val repository = InstalledAppsRepository(appContext.packageManager)
    private val pins = PinStore(appContext)

    private val installed = MutableStateFlow<List<AppEntry>>(emptyList())
    private val loaded = MutableStateFlow(false)
    private val _model = MutableStateFlow(AppsUiModel())
    val model: StateFlow<AppsUiModel> = _model.asStateFlow()

    private val _banner = MutableStateFlow<String?>(null)
    val banner: StateFlow<String?> = _banner.asStateFlow()

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refresh()
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= 33) {
            appContext.registerReceiver(packageReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(packageReceiver, filter)
        }
        scope.launch {
            combine(installed, pins.order, loaded) { apps, order, isLoaded ->
                if (!isLoaded) {
                    AppsUiModel(loading = true)
                } else {
                    val favorites = order.mapNotNull { key -> apps.find { it.componentKey == key } }
                    val empty = apps.isEmpty()
                    AppsUiModel(
                        favorites = favorites,
                        grid = apps,
                        isEmpty = empty,
                        emptyHint = if (empty) EMPTY_APPS_HINT else null,
                        loading = false,
                    )
                }
            }.collect { _model.value = it }
        }
        refresh()
    }

    fun refresh() {
        scope.launch {
            delay(150)
            val apps = withContext(Dispatchers.IO) { repository.load() }
            val seeds = apps
                .filter { mustKeepRank(it.label) != null }
                .map { it.componentKey }
            pins.recordRefresh(
                installedKeys = apps.map { it.componentKey }.toSet(),
                seedKeys = seeds,
            )
            installed.value = apps
            loaded.value = true
        }
    }

    fun launch(entry: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(entry.packageName, entry.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addCategory(
                if (entry.isLeanback) {
                    Intent.CATEGORY_LEANBACK_LAUNCHER
                } else {
                    Intent.CATEGORY_LAUNCHER
                },
            )
        }
        runCatching { appContext.startActivity(intent) }
            .onFailure { showBanner("Couldn't open ${entry.label}") }
    }

    fun openAppInfo(entry: AppEntry) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${entry.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (intent.resolveActivity(appContext.packageManager) == null) return
        runCatching { appContext.startActivity(intent) }
            .onFailure { showBanner("Couldn't open ${entry.label}") }
    }

    fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { appContext.startActivity(intent) }
            .onFailure { showBanner("Couldn't open Settings") }
    }

    fun setPinned(entry: AppEntry, pinned: Boolean) {
        scope.launch { pins.setPinned(entry.componentKey, pinned) }
    }

    private fun showBanner(text: String) {
        _banner.value = text
        scope.launch {
            delay(2_500)
            if (_banner.value == text) _banner.value = null
        }
    }

    override fun close() {
        runCatching { appContext.unregisterReceiver(packageReceiver) }
        scope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
    }
}
