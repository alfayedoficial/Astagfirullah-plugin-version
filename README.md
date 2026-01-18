# Astagfirullah - Islamic Remembrance Plugin

<div align="center">
  <img src="src/main/resources/icons/pluginIcon.svg" width="120" alt="Astagfirullah Logo"/>
</div>

<div align="center">
  <strong>Transform Your Waiting Time into Worship</strong>
</div>

<div align="center">
  <a href="https://plugins.jetbrains.com/plugin/24628-astagfirullah">
    <img src="https://img.shields.io/jetbrains/plugin/v/24628-astagfirullah.svg" alt="Version"/>
  </a>
  <a href="https://plugins.jetbrains.com/plugin/24628-astagfirullah">
    <img src="https://img.shields.io/jetbrains/plugin/d/24628-astagfirullah.svg" alt="Downloads"/>
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"/>
  </a>
</div>

---

## Overview

**Astagfirullah** is an IntelliJ IDEA plugin that displays beautiful Islamic remembrance phrases (Dhikr) during Gradle builds, project sync, and other waiting periods. Turn idle moments into opportunities for spiritual reflection with supplications, glorification, and blessings upon the Prophet Muhammad (peace be upon him).

## Features

### Multilingual Support
- **7 Languages**: Arabic (العربية), English, Urdu (أردو), Farsi (فارسى), Turkish (Türkçe), Indonesian (Bahasa), Bengali (বাংলা)
- **31+ authentic Islamic supplications** and phrases
- **Dynamic phrases** synced from server with offline support

### Smart Integration
- Automatically displays during **Gradle builds** and **project sync**
- Dedicated **Tool Window** for quick access
- **Settings page** under Settings → Tools → Astagfirullah
- **Manual trigger** to show phrases anytime

### Customization
- Configurable display duration (1-10 seconds per phrase)
- Optional blessing sound "صلى على سيدنا محمد"
- First-run setup wizard for easy configuration

### Statistics & Tracking
- Track total phrases displayed
- Daily usage count
- Session statistics
- Favorite language tracking

## Installation

### From JetBrains Marketplace (Recommended)
1. Open your JetBrains IDE (IntelliJ IDEA, Android Studio, etc.)
2. Go to **Settings/Preferences → Plugins → Marketplace**
3. Search for "**Astagfirullah**"
4. Click **Install** and restart the IDE

### Manual Installation
1. Download the latest release from [Releases](https://github.com/alfayedoficial/Astagfirullah-plugin-version/releases)
2. Go to **Settings/Preferences → Plugins → ⚙️ → Install Plugin from Disk**
3. Select the downloaded `.zip` file
4. Restart the IDE

## Usage

### Quick Start
1. Install the plugin and restart your IDE
2. Complete the **setup wizard** to choose your language
3. Start a **Gradle build** or **sync** to see phrases
4. Access settings via **Tools → Astagfirullah**

### Tool Window
- Open via **View → Tool Windows → Astagfirullah**
- View current phrases and statistics
- Quick language switch
- Manual trigger button

### Settings
- **Settings → Tools → Astagfirullah**
- Or **Tools Menu → Astagfirullah**

## Building from Source

### Prerequisites
- JDK 17 or higher
- Gradle 8.x

### Build Commands

```bash
# Clone the repository
git clone https://github.com/alfayedoficial/Astagfirullah-plugin-version.git
cd Astagfirullah-plugin-version

# Build the plugin
./gradlew build

# Run the plugin in a sandbox IDE
./gradlew runIde

# Build plugin distribution (ZIP)
./gradlew buildPlugin
```

The built plugin will be located at `build/distributions/Astagfirullah-{version}.zip`

## Project Structure

```
src/main/kotlin/com/alfayedoficial/astagfirullah/
├── core/                    # Constants and utilities
│   ├── Constants.kt
│   └── BrowserUtil.kt
├── data/                    # Data layer
│   ├── api/                 # API service
│   ├── cache/               # Local caching
│   ├── model/               # Data models
│   └── sync/                # Sync service
├── AstagfirullahSettings.kt # Plugin settings
├── AstagfirullahConfigurable.kt # Settings UI
├── BuildProgressService.kt  # Build listener
├── TranslatePhrases.kt      # Phrases provider
├── StatisticsService.kt     # Usage statistics
├── AudioService.kt          # Sound playback
└── ...
```

## Contributing

We welcome contributions from the community! Here's how you can help:

### Reporting Issues
1. Check if the issue already exists in [Issues](https://github.com/alfayedoficial/Astagfirullah-plugin-version/issues)
2. Create a new issue with:
   - Clear title and description
   - Steps to reproduce (for bugs)
   - IDE version and OS information
   - Screenshots if applicable

### Feature Requests
1. Open an issue with the **"Feature Request"** label
2. Describe the feature and its use case
3. Discuss with maintainers before implementing

### Pull Requests

#### Setup
1. **Fork** the repository
2. **Clone** your fork:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Astagfirullah-plugin-version.git
   ```
3. Create a **feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

#### Development Guidelines
- Follow **Kotlin coding conventions**
- Write **clean, readable code** with proper comments
- Keep commits **atomic** and well-described
- Test your changes with `./gradlew runIde`
- Ensure the build passes: `./gradlew build`

#### Submitting PR
1. **Push** your changes:
   ```bash
   git push origin feature/your-feature-name
   ```
2. Create a **Pull Request** to the `main` branch
3. Fill in the PR template with:
   - Description of changes
   - Related issue (if any)
   - Screenshots (for UI changes)
4. Wait for **code review**

### Code Style
- Use **4 spaces** for indentation
- Follow **Kotlin official style guide**
- Use **meaningful** variable and function names
- Add **KDoc comments** for public APIs

### Adding New Languages
1. Add phrases to `TranslatePhrases.kt`
2. Update `AstagfirullahSettings.SUPPORTED_LANGUAGES`
3. Add sample phrase to `FirstRunSetupDialog.samplePhrases`
4. Test with `./gradlew runIde`

## Changelog

See [CHANGELOG](CHANGELOG.md) for version history.

### Latest: Version 2.0.0
- Professional Settings UI with tabbed interface
- Statistics dashboard with visual cards
- API integration for dynamic phrases
- Improved rating prompt flow
- Production-ready logging

## License

```
Copyright 2024-2026 Ali Al-Shahat Ali

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Author

**Ali Al-Shahat Ali**

- LinkedIn: [@alfayedoficial](https://www.linkedin.com/in/alfayedoficial)
- Email: alialfayed.official@gmail.com

---

<div align="center">
  <i>"Mention me in your good deeds"</i>
</div>

<div align="center">
  If you find this plugin useful, please consider:
  <br/>
  <a href="https://plugins.jetbrains.com/plugin/24628-astagfirullah">Rate on Marketplace</a> •
  <a href="https://github.com/alfayedoficial/Astagfirullah-plugin-version">Star on GitHub</a>
</div>
