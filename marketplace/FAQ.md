# Frequently Asked Questions

## General Questions

### What is Astagfirullah plugin?

Astagfirullah is an IntelliJ IDEA plugin that displays Islamic remembrance phrases (Dhikr) during IDE waiting periods like Gradle builds and project synchronization. It helps developers utilize idle time for spiritual reflection.

### Which languages are supported?

The plugin supports 7 languages:
- Arabic (العربية)
- English
- Urdu (أردو)
- Farsi (فارسى)
- Turkish (Türkçe)
- Indonesian (Bahasa)
- Bengali (বাংলা)

### Is the plugin free?

Yes, the plugin is completely free and open source under the Apache 2.0 License.

### Which IDEs are supported?

All JetBrains IDEs based on IntelliJ Platform are supported:
- IntelliJ IDEA (Community & Ultimate)
- Android Studio
- WebStorm, PhpStorm, PyCharm
- RubyMine, CLion, GoLand
- Rider, DataGrip
- And more...

---

## Installation & Setup

### How do I install the plugin?

1. Open **Settings → Plugins → Marketplace**
2. Search for "Astagfirullah"
3. Click **Install**
4. Restart your IDE

### How do I change the language?

1. Go to **Settings → Tools → Astagfirullah**
2. Select your preferred language from the dropdown
3. Click **Apply**

### How do I access settings?

There are multiple ways:
- **Settings → Tools → Astagfirullah**
- **Tools Menu → Astagfirullah**
- **View → Tool Windows → Astagfirullah** (then click settings)

---

## Features

### When do phrases appear?

Phrases automatically appear during:
- Gradle builds
- Project synchronization
- Other IDE background tasks

### Can I trigger phrases manually?

Yes! Open the Tool Window via **View → Tool Windows → Astagfirullah** and click the "Show Phrase" button.

### How do I enable/disable the sound?

1. Go to **Settings → Tools → Astagfirullah**
2. Toggle the "Enable Sound" checkbox
3. Click **Apply**

### What sound does the plugin play?

The plugin plays "صلى على سيدنا محمد" (Send blessings upon Prophet Muhammad) when enabled.

### How do I adjust display duration?

1. Go to **Settings → Tools → Astagfirullah**
2. Use the slider to set duration (1-10 seconds)
3. Click **Apply**

---

## Statistics & Data

### What statistics does the plugin track?

- Total phrases displayed (lifetime)
- Today's phrase count
- Current session count
- Days using the plugin
- Favorite language

### Where is the data stored?

All data is stored locally on your machine in the IDE's configuration directory. No data is sent to external servers.

### How do I reset statistics?

Use the "Clear Cache" button in **Settings → Tools → Astagfirullah → Statistics tab**.

---

## Technical Questions

### Does the plugin affect IDE performance?

No, the plugin is lightweight and only activates during waiting periods. It has minimal impact on IDE performance.

### Does the plugin require internet connection?

The plugin works offline with cached phrases. Internet is only needed for:
- Initial phrase sync (first install)
- Daily phrase updates (optional)

### How does phrase syncing work?

- Phrases sync automatically once per day
- New phrases are downloaded from the server
- Cached locally for offline use
- Version-based sync to avoid redundant downloads

### Can I use the plugin in offline environments?

Yes! The plugin includes built-in phrases and caches synced phrases locally. It works fully offline after initial setup.

---

## Troubleshooting

### Phrases are not showing during builds

1. Ensure the plugin is enabled in **Settings → Plugins**
2. Check that a language is selected in settings
3. Restart the IDE
4. Try clearing cache and restarting

### The setup wizard didn't appear

You can access settings manually via **Settings → Tools → Astagfirullah** to configure the plugin.

### Sound is not playing

1. Check if sound is enabled in settings
2. Verify your system audio is not muted
3. Some operating systems may require audio permissions

### Plugin not compatible with my IDE version

Check the compatibility:
- **Minimum**: IntelliJ 2023.1 (Build 231)
- **Maximum**: IntelliJ 2025.3 (Build 253.*)

Update your IDE or check for a compatible plugin version.

---

## Contributing

### How can I contribute?

1. Fork the [GitHub repository](https://github.com/alfayedoficial/Astagfirullah-plugin-version)
2. Create a feature branch
3. Make your changes
4. Submit a pull request

### How can I add a new language?

1. Add phrases to `TranslatePhrases.kt`
2. Update `AstagfirullahSettings.SUPPORTED_LANGUAGES`
3. Add sample phrase to `FirstRunSetupDialog.samplePhrases`
4. Submit a pull request

### How do I report a bug?

Open an issue on [GitHub Issues](https://github.com/alfayedoficial/Astagfirullah-plugin-version/issues) with:
- Clear description of the problem
- Steps to reproduce
- IDE version and OS information
- Screenshots if applicable

---

## Contact

### Who developed this plugin?

**Ali Al-Shahat Ali**
- LinkedIn: [@alfayedoficial](https://www.linkedin.com/in/alfayedoficial)
- Email: alialfayed.official@gmail.com
- GitHub: [@alfayedoficial](https://github.com/alfayedoficial)

### How can I request a feature?

Open an issue on [GitHub](https://github.com/alfayedoficial/Astagfirullah-plugin-version/issues) with the "Feature Request" label.
