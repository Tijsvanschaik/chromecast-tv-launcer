package com.local.glasshome.apps

import androidx.compose.ui.graphics.ImageBitmap

data class AppEntry(
    val packageName: String,
    val activityName: String,
    val label: String,
    val icon: ImageBitmap?,
    val isLeanback: Boolean,
) {
    val componentKey: String get() = "$packageName/$activityName"
}

data class AppsUiModel(
    val favorites: List<AppEntry> = emptyList(),
    val grid: List<AppEntry> = emptyList(),
    val isEmpty: Boolean = false,
    val emptyHint: String? = null,
    val loading: Boolean = true,
)

const val OWN_PACKAGE = "com.local.glasshome"

const val EMPTY_APPS_HINT = "No apps found. Check package queries, then reopen."
