package com.local.glasshome.apps

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

/**
 * Queries MAIN/LEANBACK_LAUNCHER then MAIN/LAUNCHER.
 * Dedupes by package and prefers the leanback activity. Labels only; art loads lazily per tile.
 */
class InstalledAppsRepository(
    private val packageManager: PackageManager,
) {
    fun load(): List<AppEntry> {
        val byPackage = LinkedHashMap<String, AppEntry>()
        for (info in query(Intent.CATEGORY_LEANBACK_LAUNCHER)) {
            val entry = toEntry(info, leanback = true) ?: continue
            if (entry.packageName == OWN_PACKAGE) continue
            byPackage.putIfAbsent(entry.packageName, entry)
        }
        for (info in query(Intent.CATEGORY_LAUNCHER)) {
            val entry = toEntry(info, leanback = false) ?: continue
            if (entry.packageName == OWN_PACKAGE) continue
            byPackage.putIfAbsent(entry.packageName, entry)
        }
        return sortInstalled(byPackage.values.toList(), { it.label }, { it.packageName })
    }

    private fun query(category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        val matched = queryWith(intent, PackageManager.MATCH_ALL)
        return matched.ifEmpty { queryWith(intent, 0) }
    }

    private fun queryWith(intent: Intent, flags: Int): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(flags.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, flags)
        }
    }

    private fun toEntry(info: ResolveInfo, leanback: Boolean): AppEntry? {
        val activity = info.activityInfo ?: return null
        if (!activity.enabled) return null
        val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
        if (label.isEmpty()) return null
        return AppEntry(
            packageName = activity.packageName,
            activityName = activity.name,
            label = label,
            isLeanback = leanback,
        )
    }
}
