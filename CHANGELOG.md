# Changelog

All notable changes to the Astagfirullah plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
