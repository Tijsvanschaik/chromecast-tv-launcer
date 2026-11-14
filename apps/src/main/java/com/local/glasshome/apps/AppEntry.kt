package com.local.glasshome.apps

import android.content.ComponentName
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable

@Immutable
data class AppEntry(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: Drawable?,
    val isLeanback: Boolean,
) {
    val componentName: ComponentName
        get() = ComponentName(packageName, activityName)

    val componentKey: String
        get() = "$packageName/$activityName"
}

@Immutable
data class AppsUiModel(
    val favorites: List<AppEntry> = emptyList(),
    val grid: List<AppEntry> = emptyList(),
    val isEmpty: Boolean = false,
    val emptyHint: String? = null,
) {
    companion object {
        val Empty = AppsUiModel(
            isEmpty = true,
            emptyHint = "No apps found. Check package queries, then reopen.",
        )
    }
}
