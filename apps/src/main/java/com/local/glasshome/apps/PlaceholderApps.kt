package com.local.glasshome.apps

/**
 * Visual MVP placeholders — real PackageManager query lands in Phase 3.
 * Labels mirror must-keep order from the app-grid contract.
 */
data class PlaceholderApp(
    val id: String,
    val label: String,
)

object PlaceholderCatalog {
    val favorites: List<PlaceholderApp> = listOf(
        PlaceholderApp("fav_youtube", "YouTube"),
        PlaceholderApp("fav_netflix", "Netflix"),
        PlaceholderApp("fav_prime", "Prime Video"),
        PlaceholderApp("fav_spotify", "Spotify"),
    )

    val grid: List<PlaceholderApp> = listOf(
        PlaceholderApp("app_youtube", "YouTube"),
        PlaceholderApp("app_netflix", "Netflix"),
        PlaceholderApp("app_videoland", "Videoland"),
        PlaceholderApp("app_npo", "NPO"),
        PlaceholderApp("app_prime", "Prime Video"),
        PlaceholderApp("app_hbo", "HBO Max"),
        PlaceholderApp("app_disney", "Disney+"),
        PlaceholderApp("app_nlziet", "NLZIET"),
        PlaceholderApp("app_spotify", "Spotify"),
        PlaceholderApp("app_pathe", "Pathé Thuis"),
    )
}
