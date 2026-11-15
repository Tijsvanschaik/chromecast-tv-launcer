package com.local.glasshome.apps

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

/**
 * Must-keep order from the app-grid contract. Match by package id first, then by exact
 * (folded) label alias. No substring matching, so "YouTube Music" or "YouTube Kids" never
 * count as YouTube. Ids are well-known store ids; apps without a listed id rely on the label.
 */
internal data class MustKeep(
    val rank: Int,
    val packages: Set<String>,
    val labels: Set<String>,
)

internal val MustKeeps: List<MustKeep> = listOf(
    MustKeep(1, setOf("com.google.android.youtube.tv"), setOf("youtube", "youtube for android tv")),
    MustKeep(2, setOf("com.netflix.ninja"), setOf("netflix")),
    MustKeep(3, emptySet(), setOf("videoland")),
    MustKeep(4, emptySet(), setOf("npo", "npo start")),
    MustKeep(5, setOf("com.amazon.amazonvideo.livingroom"), setOf("prime video", "amazon prime video")),
    MustKeep(6, setOf("com.wbd.stream", "com.hbo.hbonow"), setOf("hbo max", "max")),
    MustKeep(7, setOf("com.disney.disneyplus"), setOf("disney+", "disney plus")),
    MustKeep(8, emptySet(), setOf("nlziet")),
    MustKeep(9, setOf("com.spotify.tv.android"), setOf("spotify")),
    MustKeep(10, emptySet(), setOf("pathe thuis")),
)

internal fun foldLabel(label: String): String {
    val stripped = Normalizer.normalize(label, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
    return stripped.lowercase(Locale.ROOT).trim().replace(Regex("\\s+"), " ")
}

internal fun mustKeepRank(label: String, packageName: String? = null): Int? {
    if (packageName != null) {
        MustKeeps.firstOrNull { packageName in it.packages }?.let { return it.rank }
    }
    val folded = foldLabel(label)
    return MustKeeps.firstOrNull { folded in it.labels }?.rank
}

internal fun <T> sortInstalled(
    items: List<T>,
    labelOf: (T) -> String,
    packageOf: (T) -> String? = { null },
): List<T> {
    val collator = Collator.getInstance(Locale("nl", "NL")).apply {
        strength = Collator.PRIMARY
    }
    return items.sortedWith { a, b ->
        val rankA = mustKeepRank(labelOf(a), packageOf(a)) ?: Int.MAX_VALUE
        val rankB = mustKeepRank(labelOf(b), packageOf(b)) ?: Int.MAX_VALUE
        if (rankA != rankB) rankA - rankB else collator.compare(labelOf(a), labelOf(b))
    }
}

/** One package per must-keep rank, in rank order, for first-run favorites. */
internal fun mustKeepSeeds(apps: List<AppEntry>): List<String> =
    apps.mapNotNull { app -> mustKeepRank(app.label, app.packageName)?.let { it to app.packageName } }
        .distinctBy { it.first }
        .sortedBy { it.first }
        .map { it.second }
