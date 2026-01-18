# Astagfirullah - Documentation

## Overview

**Astagfirullah** is an IntelliJ IDEA plugin that displays beautiful Islamic remembrance phrases (Dhikr) during Gradle builds, project sync, and other waiting periods. Transform idle moments into opportunities for spiritual reflection.

---

## Installation

### From JetBrains Marketplace (Recommended)

1. Open your JetBrains IDE (IntelliJ IDEA, Android Studio, WebStorm, etc.)
2. Go to **Settings/Preferences → Plugins → Marketplace**
3. Search for "**Astagfirullah**"
4. Click **Install** and restart the IDE

### Manual Installation

1. Download the latest release `.zip` file
2. Go to **Settings/Preferences → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the downloaded `.zip` file
4. Restart the IDE

---

## Getting Started

### First Run Setup

When you first install the plugin, a setup wizard will appear:

1. **Select your preferred language** from 7 available options:
   - Arabic (العربية)
   - English
   - Urdu (أردو)
   - Farsi (فارسى)
   - Turkish (Türkçe)
   - Indonesian (Bahasa)
   - Bengali (বাংলা)

2. **Configure sound settings** - Enable or disable the blessing sound

3. **Set display duration** - Choose how long each phrase appears (1-10 seconds)

### Automatic Display

Phrases automatically appear during:
- Gradle builds
- Project synchronization
- Other IDE waiting periods

---

## Features

### Tool Window

Access the dedicated tool window via **View → Tool Windows → Astagfirullah**

The tool window provides:
- Current phrase display
- Manual trigger button to show phrases anytime
- Quick language switch
- Usage statistics

### Settings Page

Access settings via **Settings → Tools → Astagfirullah**

#### Settings Tab
- **Language Selection**: Choose from 7 languages
- **Sound Toggle**: Enable/disable blessing sound
- **Display Duration**: Adjust phrase display time (1-10 seconds)

#### Statistics Tab
- **Total Phrases Displayed**: Lifetime count
- **Today's Count**: Daily usage
- **Session Count**: Current session statistics
- **Days Using Plugin**: Track your journey
- **Favorite Language**: Most used language

#### Cache Tab
- **Sync Status**: Last sync date and time
- **Version**: Current phrase version
- **Clear Cache**: Reset cached data if needed

### Tools Menu

Quick access via **Tools → Astagfirullah** to open settings

---

## Phrase Categories

The plugin includes 31+ authentic Islamic phrases:

### Istighfar (Seeking Forgiveness)
- أستغفر الله العظيم
- أستغفر الله وأتوب إليه

### Tasbih (Glorification)
- سبحان الله
- سبحان الله وبحمده
- سبحان الله العظيم

### Tahmid (Praise)
- الحمد لله
- الحمد لله رب العالمين

### Takbir (Magnification)
- الله أكبر
- لا إله إلا الله

### Salawat (Blessings upon the Prophet)
- اللهم صل على سيدنا محمد
- صلى الله عليه وسلم

### Supplications (Dua)
- لا حول ولا قوة إلا بالله
- حسبنا الله ونعم الوكيل

---

## API Integration

The plugin syncs phrases from a server to provide:
- Dynamic phrase updates
- New phrases without plugin updates
- Daily automatic synchronization
- Offline support with local caching

---

## Keyboard Shortcuts

Currently, the plugin does not have default keyboard shortcuts. You can assign custom shortcuts:

1. Go to **Settings → Keymap**
2. Search for "Astagfirullah"
3. Right-click and assign your preferred shortcut

---

## Compatibility

### Supported IDEs
- IntelliJ IDEA (Community & Ultimate)
- Android Studio
- WebStorm
- PhpStorm
- PyCharm
- RubyMine
- CLion
- GoLand
- Rider
- DataGrip
- All JetBrains IDEs based on IntelliJ Platform

### Supported Versions
- **From**: 2023.1 (Build 231)
- **To**: 2025.3 (Build 253.*)

---

## Troubleshooting

### Phrases Not Appearing

1. Check if the plugin is enabled in **Settings → Plugins**
2. Verify language is selected in **Settings → Tools → Astagfirullah**
3. Try clearing cache in the Settings → Statistics tab

### Sound Not Playing

1. Check if sound is enabled in settings
2. Verify system volume is not muted
3. Some systems may block audio from applications

### Plugin Not Loading

1. Restart the IDE
2. Check IDE version compatibility
3. Try reinstalling the plugin

---

## Privacy

- The plugin only syncs Islamic phrases from the server
- No personal data is collected or transmitted
- Usage statistics are stored locally only
- No analytics or tracking

---

## Open Source

This plugin is open source under the Apache 2.0 License.

- **GitHub**: [Astagfirullah Repository](https://github.com/alfayedoficial/Astagfirullah-plugin-version)
- **Issues**: [Report Bugs](https://github.com/alfayedoficial/Astagfirullah-plugin-version/issues)
- **Contribute**: [Contributing Guide](https://github.com/alfayedoficial/Astagfirullah-plugin-version/blob/main/CONTRIBUTING.md)
