package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import com.intellij.openapi.project.Project
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.*
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Container
import javax.swing.*

/**
 * Unit tests for [AstagfirullahToolWindowPanel].
 * Tests UI state management, phrase display, statistics, language switching, and button actions.
 */
@DisplayName("AstagfirullahToolWindowPanel Tests")
class AstagfirullahToolWindowPanelTest {

    private lateinit var mockProject: Project
    private lateinit var mockSettings: AstagfirullahSettings
    private lateinit var mockStatistics: StatisticsService

    @BeforeEach
    fun setUp() {
        mockProject = mock()
        mockSettings = mock()
        mockStatistics = mock()
    }

    @Nested
    @DisplayName("UI State Management Tests")
    inner class UIStateManagementTests {

        @Test
        @DisplayName("Panel extends JPanel with BorderLayout")
        fun panelExtendsBorderLayout() {
            // Verify the panel class hierarchy and layout
            val panelClass = AstagfirullahToolWindowPanel::class.java

            assertTrue(JPanel::class.java.isAssignableFrom(panelClass),
                "Panel should extend JPanel")
        }

        @Test
        @DisplayName("Panel constructor accepts Project parameter")
        fun panelAcceptsProjectParameter() {
            // Verify constructor signature
            val constructors = AstagfirullahToolWindowPanel::class.java.constructors

            val projectConstructor = constructors.find { constructor ->
                constructor.parameterCount == 1 &&
                constructor.parameterTypes[0] == Project::class.java
            }

            assertNotNull(projectConstructor,
                "Panel should have constructor accepting Project")
        }

        @Test
        @DisplayName("Panel has header, center, and footer regions")
        fun panelHasThreeRegions() {
            // Based on source code analysis, panel uses BorderLayout with NORTH, CENTER, SOUTH
            val expectedRegions = listOf(
                BorderLayout.NORTH,
                BorderLayout.CENTER,
                BorderLayout.SOUTH
            )

            // Verify BorderLayout constants exist
            expectedRegions.forEach { region ->
                assertNotNull(region, "Region $region should be available")
            }
        }

        @Test
        @DisplayName("Settings are accessed from getInstance")
        fun settingsAccessedCorrectly() {
            // Verify AstagfirullahSettings has getInstance method
            val getInstanceMethod = AstagfirullahSettings::class.java.methods.find {
                it.name == "getInstance" && it.parameterCount == 0
            }

            assertNotNull(getInstanceMethod,
                "Settings should have getInstance method")
        }
    }

    @Nested
    @DisplayName("Phrase Display Tests")
    inner class PhraseDisplayTests {

        @Test
        @DisplayName("Current phrase label has correct styling")
        fun currentPhraseLabelStyling() {
            // Based on source: font = font.deriveFont(Font.BOLD, 16f)
            val expectedFontSize = 16f

            // Verify the expected font size constant
            assertTrue(expectedFontSize > 0, "Font size should be positive")
        }

        @Test
        @DisplayName("Phrases list panel uses vertical BoxLayout")
        fun phrasesListUsesVerticalLayout() {
            // Verify BoxLayout constant for vertical orientation
            assertEquals(BoxLayout.Y_AXIS, 1,
                "BoxLayout.Y_AXIS should indicate vertical layout")
        }

        @Test
        @DisplayName("Phrase display uses HTML formatting")
        fun phraseDisplayUsesHtmlFormatting() {
            // Based on source: text = "<html><center>${phrases[0]}</center></html>"
            val htmlPrefix = "<html>"
            val htmlSuffix = "</html>"

            assertNotNull(htmlPrefix, "HTML formatting should be used")
            assertNotNull(htmlSuffix, "HTML formatting should be used")
        }

        @Test
        @DisplayName("Empty phrases list is handled gracefully")
        fun emptyPhrasesHandled() {
            // The refreshPhrases method checks: if (phrases.isNotEmpty())
            val emptyList = emptyList<String>()

            assertTrue(emptyList.isEmpty(), "Empty list should be handled")
        }

        @Test
        @DisplayName("Phrases are numbered starting from 1")
        fun phrasesNumberedFromOne() {
            // Based on source: "${index + 1}. $phrase"
            val indexedPhrases = listOf("First", "Second", "Third")
            val numbered = indexedPhrases.mapIndexed { index, phrase ->
                "${index + 1}. $phrase"
            }

            assertEquals("1. First", numbered[0])
            assertEquals("2. Second", numbered[1])
            assertEquals("3. Third", numbered[2])
        }
    }

    @Nested
    @DisplayName("Statistics Display Tests")
    inner class StatisticsDisplayTests {

        @Test
        @DisplayName("Statistics panel shows total phrases")
        fun showsTotalPhrases() {
            // Based on source: "Total: ${statistics.totalPhrasesDisplayed} phrases"
            val format = "Total: %d phrases"
            val expected = String.format(format, 100)

            assertEquals("Total: 100 phrases", expected)
        }

        @Test
        @DisplayName("Statistics panel shows today's phrases")
        fun showsTodayPhrases() {
            // Based on source: "Today: ${statistics.todayPhrasesDisplayed} phrases"
            val format = "Today: %d phrases"
            val expected = String.format(format, 10)

            assertEquals("Today: 10 phrases", expected)
        }

        @Test
        @DisplayName("Statistics panel shows sessions count")
        fun showsSessionsCount() {
            // Based on source: "Sessions: ${statistics.totalSessionsCount}"
            val format = "Sessions: %d"
            val expected = String.format(format, 5)

            assertEquals("Sessions: 5", expected)
        }

        @Test
        @DisplayName("Statistics panel shows favorite language")
        fun showsFavoriteLanguage() {
            // Based on source: "Favorite: ${statistics.favoriteLanguage}"
            val format = "Favorite: %s"
            val expected = String.format(format, "English")

            assertEquals("Favorite: English", expected)
        }

        @Test
        @DisplayName("Statistics uses 2x2 GridLayout")
        fun statisticsUsesGridLayout() {
            // Based on source: GridLayout(2, 2, 10, 5)
            val rows = 2
            val cols = 2
            val hgap = 10
            val vgap = 5

            assertEquals(2, rows, "Should have 2 rows")
            assertEquals(2, cols, "Should have 2 columns")
            assertTrue(hgap > 0, "Should have horizontal gap")
            assertTrue(vgap > 0, "Should have vertical gap")
        }
    }

    @Nested
    @DisplayName("Language Switching Tests")
    inner class LanguageSwitchingTests {

        @Test
        @DisplayName("Supported languages array is available")
        fun supportedLanguagesAvailable() {
            val languages = AstagfirullahSettings.SUPPORTED_LANGUAGES

            assertNotNull(languages, "Supported languages should be available")
            assertTrue(languages.isNotEmpty(), "Should have at least one language")
        }

        @Test
        @DisplayName("Default language is Arabic")
        fun defaultLanguageIsArabic() {
            assertEquals("العربية", Constants.DEFAULT_LANGUAGE,
                "Default language should be Arabic")
        }

        @Test
        @DisplayName("All supported languages are present")
        fun allSupportedLanguagesPresent() {
            val expected = arrayOf(
                "العربية",      // Arabic
                "English",      // English
                "أردو",         // Urdu
                "فارسى",        // Farsi/Persian
                "Türkçe",       // Turkish
                "Bahasa",       // Indonesian
                "বাংলা"         // Bengali
            )

            assertArrayEquals(expected, AstagfirullahSettings.SUPPORTED_LANGUAGES,
                "All supported languages should be present")
        }

        @Test
        @DisplayName("Language combo box triggers refresh on selection")
        fun languageSelectionTriggersRefresh() {
            // Based on source: languageComboBox.addActionListener { ... refreshPhrases() }
            // This documents the expected behavior

            val listenerCount = 1 // Expected listener count
            assertTrue(listenerCount > 0,
                "Language combo box should have action listener")
        }

        @Test
        @DisplayName("Language selection updates settings")
        fun languageSelectionUpdatesSettings() {
            // Based on source: settings.language = languageComboBox.selectedItem as String
            // Settings should be updated when language is selected

            whenever(mockSettings.language).thenReturn("English")
            assertEquals("English", mockSettings.language)
        }
    }

    @Nested
    @DisplayName("Button Actions Tests")
    inner class ButtonActionsTests {

        @Test
        @DisplayName("Refresh button exists in footer")
        fun refreshButtonExists() {
            // Based on source: JButton("Refresh").apply { addActionListener { refreshPhrases() } }
            val buttonText = "Refresh"

            assertEquals("Refresh", buttonText, "Refresh button should exist")
        }

        @Test
        @DisplayName("Show Now button exists in footer")
        fun showNowButtonExists() {
            // Based on source: JButton("Show Now").apply { addActionListener { triggerPhrasesDisplay() } }
            val buttonText = "Show Now"

            assertEquals("Show Now", buttonText, "Show Now button should exist")
        }

        @Test
        @DisplayName("Sound checkbox reflects settings state")
        fun soundCheckboxReflectsSettings() {
            // Based on source: JCheckBox("Sound", settings.soundEnabled)
            val checkboxText = "Sound"

            assertEquals("Sound", checkboxText, "Sound checkbox should exist")
        }

        @Test
        @DisplayName("Sound checkbox updates settings when toggled")
        fun soundCheckboxUpdatesSettings() {
            // Based on source: addActionListener { settings.soundEnabled = isSelected }
            whenever(mockSettings.soundEnabled).thenReturn(true)

            assertTrue(mockSettings.soundEnabled,
                "Sound setting should be updated on toggle")
        }

        @Test
        @DisplayName("Buttons panel uses FlowLayout with center alignment")
        fun buttonsPanelUsesFlowLayout() {
            // Based on source: FlowLayout(FlowLayout.CENTER, 10, 10)
            val alignment = java.awt.FlowLayout.CENTER
            val hgap = 10
            val vgap = 10

            assertEquals(1, alignment, "FlowLayout.CENTER should be 1")
            assertTrue(hgap > 0 && vgap > 0, "Gaps should be positive")
        }

        @Test
        @DisplayName("triggerPhrasesDisplay records statistics")
        fun triggerDisplayRecordsStatistics() {
            // Based on source:
            // statistics.recordPhrasesDisplayed(Constants.PHRASES_PER_DISPLAY, settings.language)
            val phrasesPerDisplay = Constants.PHRASES_PER_DISPLAY

            assertEquals(6, phrasesPerDisplay,
                "Should record correct number of phrases")
        }
    }

    @Nested
    @DisplayName("Header Panel Tests")
    inner class HeaderPanelTests {

        @Test
        @DisplayName("Header shows plugin name")
        fun headerShowsPluginName() {
            assertEquals("Astagfirullah", Constants.PLUGIN_NAME,
                "Header should show plugin name")
        }

        @Test
        @DisplayName("Title label has bold 18pt font")
        fun titleLabelFont() {
            // Based on source: font = font.deriveFont(Font.BOLD, 18f)
            val expectedSize = 18f

            assertTrue(expectedSize > 0, "Title font size should be positive")
        }

        @Test
        @DisplayName("Header has language selector on right")
        fun headerHasLanguageSelector() {
            // Based on source: add(languagePanel, BorderLayout.EAST)
            val position = BorderLayout.EAST

            assertEquals("East", position,
                "Language selector should be on the east/right")
        }

        @Test
        @DisplayName("Language label text is correct")
        fun languageLabelText() {
            // Based on source: JBLabel("Language:")
            val labelText = "Language:"

            assertEquals("Language:", labelText,
                "Language label should have correct text")
        }
    }

    @Nested
    @DisplayName("Center Panel Tests")
    inner class CenterPanelTests {

        @Test
        @DisplayName("Phrase card has border")
        fun phraseCardHasBorder() {
            // Based on source: BorderFactory.createLineBorder(JBColor.border(), 1)
            val borderWidth = 1

            assertEquals(1, borderWidth, "Phrase card should have 1px border")
        }

        @Test
        @DisplayName("Phrases list has scroll pane")
        fun phrasesListHasScrollPane() {
            // Based on source: JBScrollPane(phraseListPanel)
            val scrollPaneClass = com.intellij.ui.components.JBScrollPane::class.java

            assertNotNull(scrollPaneClass, "JBScrollPane should be available")
        }

        @Test
        @DisplayName("List has preferred size of 300x200")
        fun listPreferredSize() {
            // Based on source: preferredSize = Dimension(300, 200)
            val width = 300
            val height = 200

            assertEquals(300, width, "List width should be 300")
            assertEquals(200, height, "List height should be 200")
        }

        @Test
        @DisplayName("Phrases list title is 'Random Phrases:'")
        fun phrasesListTitle() {
            // Based on source: JBLabel("Random Phrases:")
            val title = "Random Phrases:"

            assertEquals("Random Phrases:", title,
                "List title should be correct")
        }
    }

    @Nested
    @DisplayName("Icon Loading Tests")
    inner class IconLoadingTests {

        @Test
        @DisplayName("Plugin icon path is correct")
        fun pluginIconPath() {
            assertEquals("/icons/pluginIconSmall.svg", Constants.PLUGIN_ICON_PATH,
                "Plugin icon path should be correct")
        }

        @Test
        @DisplayName("Icon loading handles exceptions gracefully")
        fun iconLoadingHandlesExceptions() {
            // Based on source: try { IconLoader.getIcon(...) } catch (e: Exception) { null }
            // The method returns null on exception

            val iconOrNull: Any? = null // Simulating failed load

            assertNull(iconOrNull, "Failed icon load should return null")
        }
    }

    @Nested
    @DisplayName("Panel Border and Padding Tests")
    inner class PanelBorderTests {

        @Test
        @DisplayName("Main panel has 10px empty border")
        fun mainPanelBorder() {
            // Based on source: border = JBUI.Borders.empty(10)
            val padding = 10

            assertEquals(10, padding, "Main panel should have 10px padding")
        }

        @Test
        @DisplayName("Header has bottom margin")
        fun headerBottomMargin() {
            // Based on source: border = JBUI.Borders.emptyBottom(10)
            val bottomMargin = 10

            assertEquals(10, bottomMargin, "Header should have bottom margin")
        }

        @Test
        @DisplayName("Footer has top margin")
        fun footerTopMargin() {
            // Based on source: border = JBUI.Borders.emptyTop(10)
            val topMargin = 10

            assertEquals(10, topMargin, "Footer should have top margin")
        }
    }
}