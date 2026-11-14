package com.local.glasshome.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InstalledAppsRepository(
    private val context: Context,
    private val pinStore: PinStore = PinStore(context),
    private val selfPackageName: String = context.packageName,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val installed = MutableStateFlow<List<AppEntry>>(emptyList())
    private var receiverRegistered = false

    private val packageReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            scope.launch { refresh() }
        }
    }

    private val uiModel = combine(
        installed,
        pinStore.pinnedOrder(),
    ) { apps, pins ->
        buildUiModel(apps, pins)
    }.stateIn(scope, SharingStarted.Eagerly, AppsUiModel())

    fun observe(): Flow<AppsUiModel> = uiModel

    fun start() {
        if (!receiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_CHANGED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            context.registerReceiver(packageReceiver, filter)
            receiverRegistered = true
        }
        scope.launch { refresh() }
    }

    fun stop() {
        if (receiverRegistered) {
            runCatching { context.unregisterReceiver(packageReceiver) }
            receiverRegistered = false
        }
    }

    suspend fun refresh() {
        val apps = withContext(Dispatchers.IO) { queryInstalled() }
        installed.value = apps
        val keys = apps.map { it.componentKey }.toSet()
        pinStore.dropMissing(keys)
        maybeSeedMustKeepPins(apps)
    }

    suspend fun pin(entry: AppEntry) = pinStore.pin(entry)

    suspend fun unpin(entry: AppEntry) = pinStore.unpin(entry)

    suspend fun reorder(from: Int, to: Int) = pinStore.reorder(from, to)

    private suspend fun maybeSeedMustKeepPins(apps: List<AppEntry>) {
        val existing = pinStore.pinnedOrder().first()
        if (existing.isNotEmpty()) return
        val seeds = MustKeepLabels.sortGrid(apps)
            .filter { MustKeepLabels.mustKeepIndex(it.label) != null }
            .take(PinStore.SOFT_CAP)
            .map { it.componentName }
        if (seeds.isNotEmpty()) {
            pinStore.replaceAll(seeds)
        }
    }

    private fun queryInstalled(): List<AppEntry> {
        val pm = context.packageManager
        val leanbackIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val leanback = pm.queryIntentActivities(leanbackIntent, PackageManager.MATCH_ALL)
        val launcher = pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)

        val byPackage = LinkedHashMap<String, AppEntry>()

        fun ingest(infos: List<ResolveInfo>, leanbackResolved: Boolean) {
            for (info in infos) {
                val activityInfo = info.activityInfo ?: continue
                val packageName = activityInfo.packageName
                if (packageName == selfPackageName) continue
                if (packageName in byPackage && byPackage.getValue(packageName).isLeanback) {
                    // Prefer leanback activity already stored.
                    continue
                }
                if (packageName in byPackage && !leanbackResolved) {
                    continue
                }
                val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
                if (label.isEmpty()) continue
                val icon = runCatching { info.loadIcon(pm) }.getOrNull()
                byPackage[packageName] = AppEntry(
                    packageName = packageName,
                    activityName = activityInfo.name,
                    label = label,
                    icon = icon,
                    isLeanback = leanbackResolved,
                )
            }
        }

        ingest(leanback, leanbackResolved = true)
        ingest(launcher, leanbackResolved = false)
        return byPackage.values.toList()
    }

    private fun buildUiModel(
        apps: List<AppEntry>,
        pins: List<android.content.ComponentName>,
    ): AppsUiModel {
        if (apps.isEmpty()) return AppsUiModel.Empty

        val byComponent = apps.associateBy { it.componentKey }
        val byPackage = apps.associateBy { it.packageName }

        val favorites = pins.mapNotNull { component ->
            byComponent["${component.packageName}/${component.className}"]
                ?: byPackage[component.packageName]
        }

        return AppsUiModel(
            favorites = favorites,
            grid = MustKeepLabels.sortGrid(apps),
            isEmpty = false,
            emptyHint = null,
        )
    }
}
