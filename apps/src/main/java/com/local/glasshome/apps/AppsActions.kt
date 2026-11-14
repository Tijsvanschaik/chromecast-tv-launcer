package com.local.glasshome.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

class AppsActions(
    private val context: Context,
    private val repository: InstalledAppsRepository,
) {
    fun launch(entry: AppEntry) {
        val intent = Intent().apply {
            component = entry.componentName
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            hint("Couldn't open ${entry.label}")
        } catch (_: SecurityException) {
            hint("Couldn't open ${entry.label}")
        }
    }

    fun openAppInfo(entry: AppEntry) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", entry.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // Contract: omit action if unresolved; do not open Play Store.
        }
    }

    fun openSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            hint("Couldn't open Settings")
        }
    }

    suspend fun pin(entry: AppEntry) = repository.pin(entry)

    suspend fun unpin(entry: AppEntry) = repository.unpin(entry)

    suspend fun reorder(from: Int, to: Int) = repository.reorder(from, to)

    private fun hint(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
