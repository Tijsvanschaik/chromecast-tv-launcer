package com.local.glasshome.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppSortingTest {
    @Test
    fun mustKeepsSortAheadOfEverythingElse() {
        val labels = listOf("Zebra", "Spotify", "Pathé Thuis", "Netflix", "YouTube", "Apple")
        val sorted = sortInstalled(labels, { it })
        assertEquals(
            listOf("YouTube", "Netflix", "Spotify", "Pathé Thuis", "Apple", "Zebra"),
            sorted,
        )
    }

    @Test
    fun patheWithoutAccentStillMatches() {
        assertEquals(10, mustKeepRank("Pathe Thuis"))
        assertEquals(10, mustKeepRank("Pathé Thuis"))
    }

    @Test
    fun youtubeForAndroidTvCountsAsYouTube() {
        assertEquals(1, mustKeepRank("YouTube for Android TV"))
    }

    @Test
    fun youtubeSiblingsAreNotMustKeeps() {
        assertNull(mustKeepRank("YouTube Music", "com.google.android.youtube.tvmusic"))
        assertNull(mustKeepRank("YouTube Kids", "com.google.android.youtube.tvkids"))
    }

    @Test
    fun packageIdWinsOverLabel() {
        assertEquals(2, mustKeepRank("Some renamed label", "com.netflix.ninja"))
        assertEquals(6, mustKeepRank("Max", "com.wbd.stream"))
    }

    @Test
    fun shortNamesDoNotMatchBySubstring() {
        assertNull(mustKeepRank("Snapdrop"))
        assertNull(mustKeepRank("HBO Documentaries"))
        assertEquals(4, mustKeepRank("NPO Start"))
    }

    @Test
    fun castIsNotAMustKeep() {
        assertNull(mustKeepRank("Google Cast"))
    }

    @Test
    fun seedsTakeOnePackagePerRankInRankOrder() {
        val apps = listOf(
            app("com.spotify.tv.android", "Spotify"),
            app("com.google.android.youtube.tv", "YouTube"),
            app("com.google.android.youtube.tvmusic", "YouTube Music"),
            app("com.example.youtube2", "YouTube"),
        )
        assertEquals(
            listOf("com.google.android.youtube.tv", "com.spotify.tv.android"),
            mustKeepSeeds(sortInstalled(apps, { it.label }, { it.packageName })),
        )
    }

    private fun app(pkg: String, label: String) = AppEntry(pkg, "$pkg.Main", label, isLeanback = true)
}
