package com.local.glasshome.apps

data class AppEntry(
    val packageName: String,
    val activityName: String,
    val label: String,
    val isLeanback: Boolean,
)

data class AppsUiModel(
    val favorites: List<AppEntry> = emptyList(),
    val grid: List<AppEntry> = emptyList(),
    val hidden: Set<String> = emptySet(),
    val hiddenInstalledCount: Int = 0,
    val showingHidden: Boolean = false,
    val isEmpty: Boolean = false,
    val emptyHint: String? = null,
    val loading: Boolean = true,
)

const val OWN_PACKAGE = "com.local.glasshome"

const val EMPTY_APPS_HINT = "No apps found. Check package queries, then reopen."
