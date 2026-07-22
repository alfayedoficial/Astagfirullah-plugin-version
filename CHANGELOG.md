# Changelog

All notable changes to the Astagfirullah plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-07-22

### Added
- Daily dhikr window on IDE startup — a random remembrance phrase with a countdown, closing itself after 5 seconds. Shown at most once per calendar day, non-modal, and right-to-left for Arabic, Urdu and Farsi
- Setting to turn the daily dhikr window off (Settings → Tools → Astagfirullah → Behavior)
- Continuous integration: build, unit tests, and the JetBrains Plugin Verifier run on every push and pull request — the repository previously had none

### Changed
- **Supported IDE range is now 2024.2 through 2026.2** (build 242–262.*). Support for 2026.1 and 2026.2 is verified by the Plugin Verifier, not merely declared
- **Minimum supported IDE raised from 2023.2 to 2024.2.** IntelliJ Platform 2024.2+ requires Java 21, and Java 21 bytecode cannot load on older IDEs. Users on 2023.2–2024.1 remain on 2.0.1
- Migrated from the deprecated Gradle IntelliJ Plugin 1.x to IntelliJ Platform Gradle Plugin 2.x (Gradle 9, Kotlin 2.4, Java 21)
- The plugin version now has a single source of truth (`project.version`); it previously lived in three files and had drifted

### Removed
- Firebase anonymous sign-in. It was used only to obtain a random identifier that was passed to the backend as an opaque `social_id` — no Firebase token was ever sent, so nothing about it was ever verified. A locally generated UUID provides the same guarantee without a network round-trip or a bundled Google API key. Anonymous registration is unchanged from the user's point of view

### Fixed
- The plugin could not be installed on IntelliJ 2026.1 or 2026.2 at all, because it declared compatibility only up to 2025.3
- A stale hardcoded version could make the plugin prompt users to "update" to the build they were already running
- Removed a Google API key that was committed to the public repository

### Security
- Removed a committed OpenSSH private key and a JetBrains Marketplace publish token from the working tree; both have been revoked/rotated
- Enabled GitHub secret scanning on the repository

## [2.0.0] - 2026-01-18

### Added
- Professional Settings UI accessible from Settings → Tools → Astagfirullah
- Tabbed interface with Settings, Statistics, and About sections
- Beautiful Statistics dashboard with visual stat cards
- Cache information panel showing sync status and version
- "Days using plugin" tracker
- Clear Cache button for troubleshooting
- "Don't Ask Again" option in rating prompt
- API integration for dynamic phrases from server
- Offline caching with version-based sync
- Daily auto-sync for Arabic and English phrases
- Comprehensive README with contribution guidelines
- CONTRIBUTING.md with detailed PR process
- Apache 2.0 LICENSE file

### Changed
- Rating prompt flow - shows only once per session
- Production-ready logging (debug logs hidden in release)
- Plugin description with better documentation and links
- Architecture with clean data layer separation

### Fixed
- JSON parsing error for API responses (interpretation field)
- Rating notification showing repeatedly

## [1.3.0] - 2025-XX-XX

### Added
- Dedicated Tool Window panel (View → Tool Windows → Astagfirullah)
- Usage statistics tracking (total phrases, daily count, sessions)
- Manual trigger button to show phrases anytime
- Quick language switch from Tool Window
- Sound toggle from Tool Window

## [1.2.0] - 2025-XX-XX

### Added
- 3 new languages: Turkish (Türkçe), Indonesian (Bahasa), Bengali (বাংলা)

### Changed
- Improved settings architecture with PersistentStateComponent
- Added dedicated AudioService for better audio management
- Improved code quality with proper logging

### Fixed
- Sound enable/disable logic
- Duplicate startup activity registration

## [1.1.4] - 2025-XX-XX

### Changed
- Extended IDE compatibility to version 252

## [1.1.3] - 2024-XX-XX

### Added
- Configurable delay settings (1-10 seconds)

## [1.1.1] - 2024-XX-XX

### Added
- Sound enable/disable option in settings

## [1.0.0] - 2024-XX-XX

### Added
- Initial release
- Display Islamic remembrance phrases during builds
- Support for Arabic, English, Urdu, Farsi languages
- Blessing sound upon the Prophet Muhammad
- First-run setup wizard
- Basic settings dialog

---

## Legend

- **Added** - New features
- **Changed** - Changes in existing functionality
- **Deprecated** - Soon-to-be removed features
- **Removed** - Removed features
- **Fixed** - Bug fixes
- **Security** - Vulnerability fixes
