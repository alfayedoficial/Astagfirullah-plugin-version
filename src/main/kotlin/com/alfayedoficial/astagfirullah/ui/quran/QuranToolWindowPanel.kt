package com.alfayedoficial.astagfirullah.ui.quran

import com.alfayedoficial.astagfirullah.AstagfirullahSettings
import com.alfayedoficial.astagfirullah.data.quran.QuranAudioPlayer
import com.alfayedoficial.astagfirullah.data.quran.QuranCatalogService
import com.alfayedoficial.astagfirullah.data.quran.QuranReciter
import com.alfayedoficial.astagfirullah.data.quran.QuranSurah
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.ComponentOrientation
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListModel
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent

/**
 * The Quran audio player UI: pick a reciter, search or scroll to a surah, and play it inside
 * the IDE. Surah and reciter names are localized to the plugin's current language setting.
 */
class QuranToolWindowPanel {

    private val player = QuranAudioPlayer.getInstance()

    private val reciterCombo = ComboBox<QuranReciter>()
    private val searchField = JBTextField()
    private val surahListModel = DefaultListModel<QuranSurah>()
    private val surahList = JBList(surahListModel)

    private val nowPlayingLabel = JBLabel(" ")
    private val elapsedLabel = JBLabel("0:00")
    private val totalLabel = JBLabel("0:00")
    private val progressBar = JProgressBar(0, 1000)
    private val playPauseButton = JButton("▶  Play")
    private val stopButton = JButton("■")
    private val prevButton = JButton("⏮")
    private val nextButton = JButton("⏭")
    private val statusLabel = JBLabel("Loading reciters…")
    private val reloadButton = JButton("↻")

    private var allSurahs: List<QuranSurah> = emptyList()
    private var loadedLanguage: String = ""

    val component: JComponent = buildUi()

    init {
        wirePlayer()
        loadCatalog()
    }

    private fun buildUi(): JComponent {
        val root = JPanel(BorderLayout()).apply { border = JBUI.Borders.empty(8) }

        // --- top: reciter + search ---
        val top = JPanel(BorderLayout(0, 6))
        val reciterRow = JPanel(BorderLayout(6, 0))
        reciterRow.add(JBLabel("Reciter:"), BorderLayout.WEST)
        reciterRow.add(reciterCombo, BorderLayout.CENTER)
        reciterRow.add(reloadButton.apply {
            toolTipText = "Reload for the current language"
            addActionListener { loadCatalog(force = true) }
        }, BorderLayout.EAST)
        reciterCombo.addActionListener { refilter() }

        searchField.emptyText.text = "Search surah by name or number"
        searchField.document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) = refilter()
        })

        top.add(reciterRow, BorderLayout.NORTH)
        top.add(searchField, BorderLayout.SOUTH)
        root.add(top, BorderLayout.NORTH)

        // --- center: surah list ---
        surahList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        surahList.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent) {
                if (e.clickCount == 2) playSelected()
            }
        })
        root.add(JBScrollPane(surahList), BorderLayout.CENTER)

        // --- bottom: player controls ---
        root.add(buildPlayerBar(), BorderLayout.SOUTH)

        applyOrientation()
        return root
    }

    private fun buildPlayerBar(): JComponent {
        val bar = JPanel(BorderLayout(0, 4)).apply { border = JBUI.Borders.emptyTop(8) }

        nowPlayingLabel.font = nowPlayingLabel.font.deriveFont(Font.BOLD)
        bar.add(nowPlayingLabel, BorderLayout.NORTH)

        val progressRow = JPanel(BorderLayout(6, 0))
        progressBar.value = 0
        progressRow.add(elapsedLabel, BorderLayout.WEST)
        progressRow.add(progressBar, BorderLayout.CENTER)
        progressRow.add(totalLabel, BorderLayout.EAST)
        bar.add(progressRow, BorderLayout.CENTER)

        val controls = JPanel(FlowLayout(FlowLayout.CENTER, 6, 0))
        prevButton.toolTipText = "Previous surah"
        prevButton.addActionListener { step(-1) }
        stopButton.toolTipText = "Stop"
        stopButton.addActionListener { player.stop() }
        nextButton.toolTipText = "Next surah"
        nextButton.addActionListener { step(1) }
        playPauseButton.addActionListener { onPlayPause() }
        controls.add(prevButton)
        controls.add(playPauseButton)
        controls.add(stopButton)
        controls.add(nextButton)

        val south = JPanel(BorderLayout())
        south.add(controls, BorderLayout.CENTER)
        statusLabel.foreground = JBColor.GRAY
        south.add(statusLabel, BorderLayout.SOUTH)
        bar.add(south, BorderLayout.SOUTH)

        setControlsEnabled(false)
        return bar
    }

    // --- data loading ---

    private fun loadCatalog(force: Boolean = false) {
        val language = AstagfirullahSettings.getInstance().language
        val code = QuranCatalogService.languageCode(language)
        if (!force && code == loadedLanguage && allSurahs.isNotEmpty()) return

        statusLabel.text = "Loading reciters…"
        reloadButton.isEnabled = false
        ApplicationManager.getApplication().executeOnPooledThread {
            val result = QuranCatalogService.loadCatalog(code)
            ApplicationManager.getApplication().invokeLater {
                reloadButton.isEnabled = true
                when (result) {
                    is QuranCatalogService.Result.Success -> {
                        loadedLanguage = code
                        allSurahs = result.catalog.surahs
                        reciterCombo.model = DefaultComboBoxModel(result.catalog.reciters.toTypedArray())
                        applyOrientation()
                        refilter()
                        statusLabel.text = "${result.catalog.reciters.size} reciters · ${allSurahs.size} surahs"
                    }
                    is QuranCatalogService.Result.Error -> {
                        statusLabel.text = "⚠ ${result.message} — press ↻ to retry"
                    }
                }
            }
        }
    }

    /** Filters the surah list by the reciter's available surahs and the search text. */
    private fun refilter() {
        val reciter = reciterCombo.selectedItem as? QuranReciter
        val query = searchField.text.trim().lowercase()
        val available = reciter?.availableSurahs

        val filtered = allSurahs.filter { s ->
            (available == null || s.id in available) &&
                (query.isEmpty() || s.name.lowercase().contains(query) || s.id.toString() == query ||
                    s.id.toString().padStart(3, '0') == query)
        }

        val previouslySelected = surahList.selectedValue
        surahListModel.clear()
        filtered.forEach { surahListModel.addElement(it) }
        if (previouslySelected != null) {
            val idx = filtered.indexOfFirst { it.id == previouslySelected.id }
            if (idx >= 0) surahList.selectedIndex = idx
        }
        setControlsEnabled(!surahListModel.isEmpty)
    }

    // --- playback ---

    private fun onPlayPause() {
        if (player.isActive) {
            player.playPauseToggle()
        } else {
            playSelected()
        }
    }

    private fun playSelected() {
        val reciter = reciterCombo.selectedItem as? QuranReciter ?: return
        val surah = surahList.selectedValue ?: surahListModel.elementAt(0) ?: return
        surahList.setSelectedValue(surah, true)
        val url = QuranCatalogService.audioUrl(reciter, surah.id)
        player.play(url, "${surah.display} · ${reciter.label}")
    }

    private fun step(direction: Int) {
        if (surahListModel.isEmpty) return
        val current = surahList.selectedIndex.coerceAtLeast(0)
        val next = (current + direction).coerceIn(0, surahListModel.size() - 1)
        surahList.selectedIndex = next
        playSelected()
    }

    private fun wirePlayer() {
        player.onState = { state, title, message ->
            when (state) {
                QuranAudioPlayer.State.LOADING -> {
                    nowPlayingLabel.text = title
                    statusLabel.text = "Buffering…"
                    playPauseButton.text = "▶  Play"
                }
                QuranAudioPlayer.State.PLAYING -> {
                    nowPlayingLabel.text = title
                    statusLabel.text = "Playing"
                    playPauseButton.text = "⏸  Pause"
                }
                QuranAudioPlayer.State.PAUSED -> {
                    statusLabel.text = "Paused"
                    playPauseButton.text = "▶  Resume"
                }
                QuranAudioPlayer.State.ENDED -> {
                    statusLabel.text = "Finished"
                    playPauseButton.text = "▶  Play"
                }
                QuranAudioPlayer.State.IDLE -> {
                    statusLabel.text = "Stopped"
                    playPauseButton.text = "▶  Play"
                    progressBar.value = 0
                    elapsedLabel.text = "0:00"
                }
                QuranAudioPlayer.State.ERROR -> {
                    statusLabel.text = "⚠ ${message ?: "Playback error"}"
                    playPauseButton.text = "▶  Play"
                }
            }
        }
        player.onProgress = { elapsed, total ->
            elapsedLabel.text = QuranAudioPlayer.formatTime(elapsed)
            if (total > 0) {
                totalLabel.text = QuranAudioPlayer.formatTime(total)
                progressBar.isIndeterminate = false
                progressBar.value = ((elapsed.toDouble() / total) * 1000).toInt().coerceIn(0, 1000)
            } else {
                totalLabel.text = "--:--"
            }
        }
    }

    private fun setControlsEnabled(enabled: Boolean) {
        playPauseButton.isEnabled = enabled
        stopButton.isEnabled = enabled
        prevButton.isEnabled = enabled
        nextButton.isEnabled = enabled
    }

    /** Arabic, Urdu and Farsi read right-to-left. */
    private fun applyOrientation() {
        val rtl = AstagfirullahSettings.getInstance().language in RTL_LANGUAGES
        val o = if (rtl) ComponentOrientation.RIGHT_TO_LEFT else ComponentOrientation.LEFT_TO_RIGHT
        surahList.componentOrientation = o
        nowPlayingLabel.componentOrientation = o
    }

    companion object {
        private val RTL_LANGUAGES = setOf("العربية", "أردو", "فارسى")
    }
}
