package com.local.glasshome.apps

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

/**
 * Must-keep order from the app-grid contract. Match is by label, not package id.
 * Accent-insensitive so "Pathé Thuis" and "Pathe Thuis" both hit rank 10.
 */
internal data class MustKeep(
    val rank: Int,
    val needles: List<String>,
)

internal val MustKeeps: List<MustKeep> = listOf(
    MustKeep(1, listOf("youtube")),
    MustKeep(2, listOf("netflix")),
    MustKeep(3, listOf("videoland")),
    MustKeep(4, listOf("npo")),
    MustKeep(5, listOf("prime video")),
    MustKeep(6, listOf("hbo max", "hbo")),
    MustKeep(7, listOf("disney")),
    MustKeep(8, listOf("nlziet")),
    MustKeep(9, listOf("spotify")),
    MustKeep(10, listOf("pathe thuis", "pathe")),
)

internal fun foldLabel(label: String): String {
    val stripped = Normalizer.normalize(label, Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
    return stripped.lowercase(Locale.ROOT).trim()
}

internal fun mustKeepRank(label: String): Int? {
    val folded = foldLabel(label)
    for (keep in MustKeeps) {
        if (keep.needles.any { needle -> folded == needle || folded.contains(needle) }) {
            return keep.rank
        }
    }
    return null
}

internal fun <T> sortInstalled(items: List<T>, labelOf: (T) -> String): List<T> {
    val collator = Collator.getInstance(Locale("nl", "NL")).apply {
        strength = Collator.PRIMARY
    }
    return items.sortedWith { a, b ->
        val rankA = mustKeepRank(labelOf(a)) ?: Int.MAX_VALUE
        val rankB = mustKeepRank(labelOf(b)) ?: Int.MAX_VALUE
        if (rankA != rankB) rankA - rankB else collator.compare(labelOf(a), labelOf(b))
    }
}
