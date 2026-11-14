package com.local.glasshome.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppSortingTest {
    @Test
    fun mustKeepsSortAheadOfEverythingElse() {
        val labels = listOf("Zebra", "Spotify", "Pathé Thuis", "Netflix", "YouTube", "Apple")
        val sorted = sortInstalled(labels) { it }
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
    fun castIsNotAMustKeep() {
        assertNull(mustKeepRank("Google Cast"))
    }
}
