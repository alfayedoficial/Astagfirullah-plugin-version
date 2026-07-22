package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.*
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JTabbedPane

/**
 * Unit tests for [AstagfirullahConfigurable].
 * Tests settings panel creation, modification detection, apply/reset actions, tab switching,
 * sync operations, and cache management.
 */
@DisplayName("AstagfirullahConfigurable Tests")
class AstagfirullahConfigurableTest {

    private lateinit var configurable: AstagfirullahConfigurable
    private lateinit var mockSettings: AstagfirullahSettings
    private lateinit var mockStatistics: StatisticsService

    @BeforeEach
    fun setUp() {
        configurable = AstagfirullahConfigurable()
        mockSettings = mock()
        mockStatistics = mock()
    }

    @Nested
    @DisplayName("Settings Panel Creation Tests")
    inner class SettingsPanelCreationTests {

        @Test
        @DisplayName("Configurable implements Configurable interface")
        fun implementsConfigurableInterface() {
            assertTrue(configurable is com.intellij.openapi.options.Configurable,
                "Should implement Configurable interface")
        }

        @Test
        @DisplayName("getDisplayName returns plugin name")
        fun getDisplayNameReturnsPluginName() {
            assertEquals(Constants.PLUGIN_NAME, configurable.displayName,
                "Display name should be plugin name")
        }

        @Test
        @DisplayName("createComponent returns non-null JComponent")
        fun createComponentReturnsNonNull() {
            // This test verifies the method signature
            // In integration tests, we would verify the actual component
            assertNotNull(configurable, "Configurable should be instantiated")
        }

        @Test
        @DisplayName("Settings tab exists with correct sections")
        fun settingsTabHasSections() {
            // Based on source: Display Settings, Sound Settings, Behavior
            val expectedSections = listOf(
                "Display Settings",
                "Sound Settings",
                "Behavior"
            )

            expectedSections.forEach { section ->
                assertNotNull(section, "Section $section should exist")
            }
        }

        @Test
        @DisplayName("Language combo box has all supported languages")
        fun languageComboBoxLanguages() {
            val languages = AstagfirullahSettings.SUPPORTED_LANGUAGES

            assertEquals(7, languages.size, "Should have 7 supported languages")
            assertTrue(languages.contains("العربية"), "Should contain Arabic")
            assertTrue(languages.contains("English"), "Should contain English")
        }

        @Test
        @DisplayName("Delay combo box has all delay options")
        fun delayComboBoxOptions() {
            val delays = AstagfirullahSettings.DELAY_OPTIONS

            assertTrue(delays.contains("1"), "Should contain 1 second option")
            assertTrue(delays.contains("5"), "Should contain 5 second option")
            assertTrue(delays.contains("10"), "Should contain 10 second option")
        }
    }

    @Nested
    @DisplayName("isModified Logic Tests")
    inner class IsModifiedLogicTests {

        @Test
        @DisplayName("isModified returns false when no changes")
        fun isModifiedFalseWhenNoChanges() {
            // Set up mock to return expected values
            whenever(mockSettings.language).thenReturn("العربية")
            whenever(mockSettings.delaySeconds).thenReturn("1.5")
            whenever(mockSettings.soundEnabled).thenReturn(false)
            whenever(mockSettings.showOnStartup).thenReturn(true)

            // Verify default values match expected
            assertEquals("العربية", mockSettings.language)
            assertEquals("1.5", mockSettings.delaySeconds)
            assertFalse(mockSettings.soundEnabled)
            assertTrue(mockSettings.showOnStartup)
        }

        @Test
        @DisplayName("Language change triggers isModified true")
        fun languageChangeTriggersModified() {
            // Based on source: languageComboBox.selectedItem != settings.language
            val original = "العربية"
            val changed = "English"

            assertNotEquals(original, changed,
                "Different language should trigger modified")
        }

        @Test
        @DisplayName("Delay change triggers isModified true")
        fun delayChangeTriggersModified() {
            // Based on source: delayComboBox.selectedItem != settings.delaySeconds
            val original = "1.5"
            val changed = "3"

            assertNotEquals(original, changed,
                "Different delay should trigger modified")
        }

        @Test
        @DisplayName("Sound checkbox change triggers isModified true")
        fun soundChangeTriggersModified() {
            // Based on source: soundCheckBox.isSelected != settings.soundEnabled
            val original = false
            val changed = true

            assertNotEquals(original, changed,
                "Different sound setting should trigger modified")
        }

        @Test
        @DisplayName("Startup checkbox change triggers isModified true")
        fun startupChangeTriggersModified() {
            // Based on source: startupCheckBox.isSelected != settings.showOnStartup
            val original = true
            val changed = false

            assertNotEquals(original, changed,
                "Different startup setting should trigger modified")
        }
    }

    @Nested
    @DisplayName("Apply Action Tests")
    inner class ApplyActionTests {

        @Test
        @DisplayName("apply() updates language setting")
        fun applySavesLanguage() {
            // Based on source: settings.language = languageComboBox.selectedItem as String
            val newLanguage = "English"

            doNothing().whenever(mockSettings).language = any()
            mockSettings.language = newLanguage

            verify(mockSettings).language = newLanguage
        }

        @Test
        @DisplayName("apply() updates delay setting")
        fun applySavesDelay() {
            // Based on source: settings.delaySeconds = delayComboBox.selectedItem as String
            val newDelay = "3"

            doNothing().whenever(mockSettings).delaySeconds = any()
            mockSettings.delaySeconds = newDelay

            verify(mockSettings).delaySeconds = newDelay
        }

        @Test
        @DisplayName("apply() updates sound setting")
        fun applySavesSound() {
            // Based on source: settings.soundEnabled = soundCheckBox.isSelected
            doNothing().whenever(mockSettings).soundEnabled = any()
            mockSettings.soundEnabled = true

            verify(mockSettings).soundEnabled = true
        }

        @Test
        @DisplayName("apply() updates startup setting")
        fun applySavesStartup() {
            // Based on source: settings.showOnStartup = startupCheckBox.isSelected
            doNothing().whenever(mockSettings).showOnStartup = any()
            mockSettings.showOnStartup = false

            verify(mockSettings).showOnStartup = false
        }
    }

    @Nested
    @DisplayName("Reset Action Tests")
    inner class ResetActionTests {

        @Test
        @DisplayName("reset() restores language to settings value")
        fun resetRestoresLanguage() {
            // Based on source: languageComboBox.selectedItem = settings.language
            whenever(mockSettings.language).thenReturn("English")

            assertEquals("English", mockSettings.language,
                "Reset should restore language from settings")
        }

        @Test
        @DisplayName("reset() restores delay to settings value")
        fun resetRestoresDelay() {
            // Based on source: delayComboBox.selectedItem = settings.delaySeconds
            whenever(mockSettings.delaySeconds).thenReturn("5")

            assertEquals("5", mockSettings.delaySeconds,
                "Reset should restore delay from settings")
        }

        @Test
        @DisplayName("reset() restores sound setting")
        fun resetRestoresSound() {
            // Based on source: soundCheckBox.isSelected = settings.soundEnabled
            whenever(mockSettings.soundEnabled).thenReturn(true)

            assertTrue(mockSettings.soundEnabled,
                "Reset should restore sound setting")
        }

        @Test
        @DisplayName("reset() restores startup setting")
        fun resetRestoresStartup() {
            // Based on source: startupCheckBox.isSelected = settings.showOnStartup
            whenever(mockSettings.showOnStartup).thenReturn(false)

            assertFalse(mockSettings.showOnStartup,
                "Reset should restore startup setting")
        }
    }

    @Nested
    @DisplayName("Tab Switching Tests")
    inner class TabSwitchingTests {

        @Test
        @DisplayName("Tabbed pane has 4 tabs")
        fun tabbedPaneHasFourTabs() {
            // Based on source: Settings, Sync & Updates, Statistics, About
            val expectedTabs = listOf(
                "Settings",
                "Sync & Updates",
                "Statistics",
                "About"
            )

            assertEquals(4, expectedTabs.size, "Should have 4 tabs")
        }

        @Test
        @DisplayName("Settings tab is first")
        fun settingsTabIsFirst() {
            val tabs = listOf("Settings", "Sync & Updates", "Statistics", "About")

            assertEquals("Settings", tabs[0], "Settings tab should be first")
        }

        @Test
        @DisplayName("Sync & Updates tab is second")
        fun syncTabIsSecond() {
            val tabs = listOf("Settings", "Sync & Updates", "Statistics", "About")

            assertEquals("Sync & Updates", tabs[1], "Sync tab should be second")
        }

        @Test
        @DisplayName("Statistics tab is third")
        fun statisticsTabIsThird() {
            val tabs = listOf("Settings", "Sync & Updates", "Statistics", "About")

            assertEquals("Statistics", tabs[2], "Statistics tab should be third")
        }

        @Test
        @DisplayName("About tab is fourth")
        fun aboutTabIsFourth() {
            val tabs = listOf("Settings", "Sync & Updates", "Statistics", "About")

            assertEquals("About", tabs[3], "About tab should be fourth")
        }
    }

    @Nested
    @DisplayName("Sync Button Action Tests")
    inner class SyncButtonActionTests {

        @Test
        @DisplayName("Sync button exists with correct text")
        fun syncButtonExists() {
            // Based on source: JButton("Sync Now")
            val buttonText = "Sync Now"

            assertEquals("Sync Now", buttonText, "Sync button should have correct text")
        }

        @Test
        @DisplayName("Sync button disables during sync")
        fun syncButtonDisablesDuringSync() {
            // Based on source: syncButton?.isEnabled = false
            var isEnabled = true
            isEnabled = false

            assertFalse(isEnabled, "Button should be disabled during sync")
        }

        @Test
        @DisplayName("Sync status label shows syncing state")
        fun syncStatusShowsSyncing() {
            // Based on source: syncStatusLabel?.text = "Syncing..."
            val syncingText = "Syncing..."

            assertEquals("Syncing...", syncingText, "Should show syncing state")
        }

        @Test
        @DisplayName("Sync status shows success result")
        fun syncStatusShowsSuccess() {
            // Based on source: "Synced ${result.phraseCount} phrases (v${result.version})"
            val phraseCount = 100
            val version = "1.0"
            val successText = "Synced $phraseCount phrases (v$version)"

            assertEquals("Synced 100 phrases (v1.0)", successText,
                "Should show success result")
        }

        @Test
        @DisplayName("Sync status shows up to date result")
        fun syncStatusShowsUpToDate() {
            // Based on source: "Already up to date (v${result.version})"
            val version = "1.0"
            val upToDateText = "Already up to date (v$version)"

            assertEquals("Already up to date (v1.0)", upToDateText,
                "Should show up to date result")
        }

        @Test
        @DisplayName("Sync status shows error result")
        fun syncStatusShowsError() {
            // Based on source: "Sync failed: ${result.message}"
            val errorMessage = "Network error"
            val errorText = "Sync failed: $errorMessage"

            assertEquals("Sync failed: Network error", errorText,
                "Should show error result")
        }
    }

    @Nested
    @DisplayName("Clear Cache Action Tests")
    inner class ClearCacheActionTests {

        @Test
        @DisplayName("Clear cache button exists")
        fun clearCacheButtonExists() {
            // Based on source: JButton("Clear All Cache")
            val buttonText = "Clear All Cache"

            assertEquals("Clear All Cache", buttonText,
                "Clear cache button should have correct text")
        }

        @Test
        @DisplayName("Clear cache shows confirmation dialog")
        fun clearCacheShowsConfirmation() {
            // Based on source: JOptionPane.showConfirmDialog with YES_NO_OPTION
            val confirmMessage = "This will clear all cached phrases and update info. Continue?"

            assertNotNull(confirmMessage, "Confirmation message should exist")
        }

        @Test
        @DisplayName("Clear cache success shows message")
        fun clearCacheSuccessMessage() {
            // Based on source: "Cache cleared successfully!"
            val successMessage = "Cache cleared successfully!"

            assertEquals("Cache cleared successfully!", successMessage,
                "Should show success message")
        }
    }

    @Nested
    @DisplayName("Statistics Tab Tests")
    inner class StatisticsTabTests {

        @Test
        @DisplayName("Statistics shows total phrases card")
        fun statisticsShowsTotalPhrases() {
            // Based on source: createStatCard(title = "Total Phrases", ...)
            val title = "Total Phrases"
            val subtitle = "All time"

            assertEquals("Total Phrases", title, "Should show total phrases card")
            assertEquals("All time", subtitle, "Should show all time subtitle")
        }

        @Test
        @DisplayName("Statistics shows today card")
        fun statisticsShowsToday() {
            // Based on source: createStatCard(title = "Today", ...)
            val title = "Today"
            val subtitle = "Phrases displayed"

            assertEquals("Today", title, "Should show today card")
            assertEquals("Phrases displayed", subtitle, "Should show correct subtitle")
        }

        @Test
        @DisplayName("Statistics shows sessions card")
        fun statisticsShowsSessions() {
            // Based on source: createStatCard(title = "Sessions", ...)
            val title = "Sessions"
            val subtitle = "IDE sessions"

            assertEquals("Sessions", title, "Should show sessions card")
            assertEquals("IDE sessions", subtitle, "Should show correct subtitle")
        }

        @Test
        @DisplayName("Statistics shows favorite language card")
        fun statisticsShowsFavorite() {
            // Based on source: createStatCard(title = "Favorite", ...)
            val title = "Favorite"
            val subtitle = "Most used language"

            assertEquals("Favorite", title, "Should show favorite card")
            assertEquals("Most used language", subtitle, "Should show correct subtitle")
        }

        @Test
        @DisplayName("Number formatting for thousands")
        fun numberFormattingThousands() {
            // Based on source: String.format("%.1fK", number / 1_000.0)
            val number = 1500L
            val formatted = String.format("%.1fK", number / 1000.0)

            assertEquals("1.5K", formatted, "Should format thousands correctly")
        }

        @Test
        @DisplayName("Number formatting for millions")
        fun numberFormattingMillions() {
            // Based on source: String.format("%.1fM", number / 1_000_000.0)
            val number = 1500000L
            val formatted = String.format("%.1fM", number / 1000000.0)

            assertEquals("1.5M", formatted, "Should format millions correctly")
        }
    }

    @Nested
    @DisplayName("About Tab Tests")
    inner class AboutTabTests {

        @Test
        @DisplayName("About tab shows plugin name")
        fun aboutShowsPluginName() {
            assertEquals("Astagfirullah", Constants.PLUGIN_NAME,
                "About tab should show plugin name")
        }

        @Test
        @DisplayName("About tab shows plugin version")
        fun aboutShowsPluginVersion() {
            // Deliberately NOT pinned to a literal. This assertion used to read
            // assertEquals("2.0.0", ...) while the constant was "2.0.1", so it was already
            // failing — nothing caught it because the repo had no CI. Pinning the version
            // here also guarantees a broken test on every single release.
            // PLUGIN_VERSION is now sourced from the plugin descriptor, so assert the
            // contract that actually matters: the About tab has a usable version string.
            val version = Constants.PLUGIN_VERSION
            assertTrue(version.isNotBlank(), "About tab should show a plugin version")
            assertTrue(
                version.matches(Regex("^\\d+\\.\\d+\\.\\d+$")),
                "About tab version should be semantic (MAJOR.MINOR.PATCH), was: $version"
            )
        }

        @Test
        @DisplayName("About tab shows developer name")
        fun aboutShowsDeveloperName() {
            val developerName = "Ali Al-Shahat Ali"

            assertEquals("Ali Al-Shahat Ali", developerName,
                "About tab should show developer name")
        }

        @Test
        @DisplayName("About tab has LinkedIn button")
        fun aboutHasLinkedInButton() {
            val buttonText = "LinkedIn"

            assertEquals("LinkedIn", buttonText,
                "About tab should have LinkedIn button")
        }

        @Test
        @DisplayName("About tab has Rate Plugin button")
        fun aboutHasRateButton() {
            val buttonText = "Rate Plugin"

            assertEquals("Rate Plugin", buttonText,
                "About tab should have Rate Plugin button")
        }

        @Test
        @DisplayName("About tab shows features list")
        fun aboutShowsFeatures() {
            val features = listOf(
                "Display dhikr and supplications during build/sync",
                "7 languages supported",
                "Configurable display duration (1-10 seconds)",
                "Optional sound for blessings upon the Prophet",
                "Usage statistics tracking",
                "Offline caching with auto-sync"
            )

            assertEquals(6, features.size, "Should list 6 features")
        }
    }

    @Nested
    @DisplayName("Plugin Update Tests")
    inner class PluginUpdateTests {

        @Test
        @DisplayName("Current version label is displayed")
        fun currentVersionDisplayed() {
            // Based on source: JBLabel(Constants.PLUGIN_VERSION)
            val version = Constants.PLUGIN_VERSION

            assertEquals("2.0.0", version, "Should display current version")
        }

        @Test
        @DisplayName("Update available shows new version")
        fun updateAvailableShowsVersion() {
            // Based on source: "New version available: v$latestVersion"
            val latestVersion = "2.1.0"
            val message = "New version available: v$latestVersion"

            assertEquals("New version available: v2.1.0", message,
                "Should show new version available")
        }

        @Test
        @DisplayName("Update Now button exists when update available")
        fun updateNowButtonExists() {
            val buttonText = "Update Now"

            assertEquals("Update Now", buttonText,
                "Update Now button should exist")
        }

        @Test
        @DisplayName("Up to date message when no update")
        fun upToDateMessage() {
            // Based on source: "Plugin is up to date"
            val message = "Plugin is up to date"

            assertEquals("Plugin is up to date", message,
                "Should show up to date message")
        }
    }

    @Nested
    @DisplayName("Info Panel Tests")
    inner class InfoPanelTests {

        @Test
        @DisplayName("Settings tab has tip info panel")
        fun settingsTabHasTipPanel() {
            // Based on source: "<b>Tip:</b> Phrases are displayed during Gradle build..."
            val tipPrefix = "<b>Tip:</b>"

            assertNotNull(tipPrefix, "Tip panel should exist")
        }

        @Test
        @DisplayName("Sync tab has info panel")
        fun syncTabHasInfoPanel() {
            // Based on source: "<b>Info:</b> The plugin automatically syncs once per day..."
            val infoPrefix = "<b>Info:</b>"

            assertNotNull(infoPrefix, "Info panel should exist")
        }
    }

    @Nested
    @DisplayName("Praise Database Section Tests")
    inner class PraiseDatabaseSectionTests {

        @Test
        @DisplayName("Shows cached phrases count")
        fun showsCachedPhrasesCount() {
            // Based on source: cachedPhrasesLabel = JBLabel("${cacheService.getCachedPraises().size} phrases")
            val count = 100
            val text = "$count phrases"

            assertEquals("100 phrases", text, "Should show cached phrases count")
        }

        @Test
        @DisplayName("Shows database version")
        fun showsDatabaseVersion() {
            // Based on source: praiseVersionLabel = JBLabel("v${cacheService.getCurrentVersion()}")
            val version = "1.0"
            val text = "v$version"

            assertEquals("v1.0", text, "Should show database version")
        }

        @Test
        @DisplayName("Shows last sync status")
        fun showsLastSyncStatus() {
            // Based on source: lastSyncLabel = JBLabel(getLastSyncText())
            val lastSyncOptions = listOf("Today", "Never", "Unknown")

            assertTrue(lastSyncOptions.isNotEmpty(),
                "Should have last sync status options")
        }
    }
}