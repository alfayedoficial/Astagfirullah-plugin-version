package com.alfayedoficial.astagfirullah

/**
 * Single source of truth for the current release's highlights.
 *
 * Used by the What's New dialog (shown once on upgrade), the About tab, and the first-run
 * wizard, so all three stay in sync. Update this list when cutting a new version.
 */
object WhatsNew {

    /** Headline for the What's New dialog / About tab. */
    const val TAGLINE = "Now on the latest IDEs, with the Quran and a daily moment of remembrance"

    /** The 3.0.0 highlights, most important first. */
    val HIGHLIGHTS: List<String> = listOf(
        "Quran audio player — pick from 200+ reciters, search any surah, and play inside the IDE",
        "A dhikr window every time you open a project, closing itself after 7 seconds",
        "Runs on IntelliJ 2024.2 through 2026.2, including the newest release",
        "Right-to-left rendering for Arabic, Urdu and Farsi",
        "Reciter and surah names follow your chosen language",
        "Astagfirullah is also on Android, iOS and as browser extensions",
        "Faster, lighter sign-in — one less network round-trip",
    )
}
