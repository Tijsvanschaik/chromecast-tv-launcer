package com.local.glasshome.apps

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

/**
 * Must-keep labels from the app-grid contract. Match by label on device;
 * do not hard-require package ids before on-device discovery.
 */
object MustKeepLabels {
    val ordered: List<String> = listOf(
        "YouTube",
        "Netflix",
        "Videoland",
        "NPO",
        "Prime Video",
        "HBO Max",
        "Disney+",
        "NLZIET",
        "Spotify",
        "Pathé Thuis",
    )

    private val dutchCollator: Collator =
        Collator.getInstance(Locale("nl", "NL")).apply {
            strength = Collator.PRIMARY
        }

    fun normalize(label: String): String {
        val trimmed = label.trim().lowercase(Locale.ROOT)
        val decomposed = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
        return decomposed.replace("\\p{Mn}+".toRegex(), "")
    }

    /** Index in must-keep table, or null if not a must-keep. */
    fun mustKeepIndex(label: String): Int? {
        val n = normalize(label)
        ordered.forEachIndexed { index, keep ->
            val kn = normalize(keep)
            if (n == kn || n.contains(kn) || kn.contains(n)) {
                // Prefer real streaming titles; avoid tiny accidental contains.
                if (n == kn || n.startsWith(kn) || n.contains("$kn ") || n.contains(" $kn")) {
                    return index
                }
                if (kn.length >= 3 && n.contains(kn)) return index
            }
        }
        // Aliases observed on Android TV builds
        return when {
            n.contains("youtube") -> 0
            n.contains("netflix") -> 1
            n.contains("videoland") -> 2
            n == "npo" || n.startsWith("npo ") || n.contains("npo start") -> 3
            n.contains("prime video") || n == "amazon prime video" -> 4
            n.contains("hbo") || n == "max" -> 5
            n.contains("disney") -> 6
            n.contains("nlziet") -> 7
            n.contains("spotify") -> 8
            n.contains("pathe") -> 9
            else -> null
        }
    }

    fun sortGrid(apps: List<AppEntry>): List<AppEntry> {
        return apps.sortedWith(
            compareBy<AppEntry> { mustKeepIndex(it.label) ?: Int.MAX_VALUE }
                .thenBy(dutchCollator) { it.label },
        )
    }
}
