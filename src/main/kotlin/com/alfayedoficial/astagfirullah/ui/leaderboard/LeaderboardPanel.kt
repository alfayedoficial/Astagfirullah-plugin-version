package com.alfayedoficial.astagfirullah.ui.leaderboard

import com.alfayedoficial.astagfirullah.data.cache.AuthCacheService
import com.alfayedoficial.astagfirullah.data.model.LeaderboardEntry
import com.alfayedoficial.astagfirullah.data.model.LeaderboardPeriod
import com.alfayedoficial.astagfirullah.data.sync.AuthSyncService
import com.alfayedoficial.astagfirullah.data.sync.LeaderboardSyncService
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.*
import javax.swing.*

/**
 * Main leaderboard panel displaying top users with filtering and pagination.
 */
class LeaderboardPanel(
    private val onNavigateToLogin: (() -> Unit)? = null
) : JPanel(BorderLayout()) {

    private val refreshButton = JButton()
    private val leaderboardList = JBList<LeaderboardEntry>()
    private val loadMoreButton = JButton("Load More")
    private val currentUserRankPanel = CurrentUserRankPanel()

    private val loadingPanel = JPanel()
    private val errorPanel = JPanel()
    private val emptyPanel = JPanel()
    private val authRequiredPanel = JPanel()
    private val contentPanel = JPanel(CardLayout())

    private val listModel = DefaultListModel<LeaderboardEntry>()
    private var isLoading = false

    companion object {
        private const val CARD_CONTENT = "content"
        private const val CARD_LOADING = "loading"
        private const val CARD_ERROR = "error"
        private const val CARD_EMPTY = "empty"
        private const val CARD_AUTH_REQUIRED = "auth_required"
    }

    init {
        border = JBUI.Borders.empty(16)

        createHeaderPanel()
        createContentPanels()
        createFooterPanel()

        setupListeners()
        setupAuthStateListener()

        // Check authentication before loading
        checkAuthAndLoad()
    }

    /**
     * Sets up listener for auth state changes to automatically refresh when user logs in.
     */
    private fun setupAuthStateListener() {
        // Store the previous listener if any, and chain with our listener
        val previousListener = AuthSyncService.getInstance().onAuthStateChanged

        AuthSyncService.getInstance().onAuthStateChanged = { state ->
            // Call previous listener first
            previousListener?.invoke(state)

            // Handle auth state changes for leaderboard
            SwingUtilities.invokeLater {
                when (state) {
                    is AuthSyncService.AuthState.LoggedIn -> {
                        // User just logged in, refresh the leaderboard
                        onUserLoggedIn()
                    }
                    is AuthSyncService.AuthState.LoggedOut -> {
                        // User logged out, show auth required
                        showAuthRequired()
                    }
                    is AuthSyncService.AuthState.Loading -> {
                        // Auth operation in progress, keep current state
                    }
                    is AuthSyncService.AuthState.Error -> {
                        // Auth error, keep current state
                    }
                }
            }
        }
    }

    /**
     * Checks if user is authenticated and loads leaderboard or shows login prompt.
     */
    private fun checkAuthAndLoad() {
        if (AuthCacheService.getInstance().isLoggedIn()) {
            loadLeaderboard(forceRefresh = false)
        } else {
            showAuthRequired()
        }
    }

    /**
     * Shows the authentication required panel.
     */
    private fun showAuthRequired() {
        (contentPanel.layout as CardLayout).show(contentPanel, CARD_AUTH_REQUIRED)
        currentUserRankPanel.clear()
        refreshButton.isEnabled = false
    }

    private fun createHeaderPanel() {
        val headerPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(0, 0, 12, 0)
        }

        // Title
        val titleLabel = JBLabel("Leaderboard").apply {
            font = font.deriveFont(Font.BOLD, 16f)
        }

        // Refresh button
        refreshButton.apply {
            icon = AllIcons.Actions.Refresh
            toolTipText = "Refresh leaderboard"
            isBorderPainted = false
            isContentAreaFilled = false
            preferredSize = JBUI.size(28, 28)
        }

        headerPanel.add(titleLabel, BorderLayout.WEST)
        headerPanel.add(refreshButton, BorderLayout.EAST)

        add(headerPanel, BorderLayout.NORTH)
    }

    private fun createContentPanels() {
        // Main list panel
        val listPanel = JPanel(BorderLayout())

        leaderboardList.apply {
            model = listModel
            cellRenderer = LeaderboardEntryRenderer(AuthCacheService.getInstance().getUserId())
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            fixedCellHeight = 48
        }

        val scrollPane = JBScrollPane(leaderboardList).apply {
            border = BorderFactory.createLineBorder(JBColor.border())
        }

        listPanel.add(scrollPane, BorderLayout.CENTER)

        // Load more button panel
        val loadMorePanel = JPanel(FlowLayout(FlowLayout.CENTER)).apply {
            border = JBUI.Borders.empty(8, 0)
        }
        loadMoreButton.isVisible = false
        loadMorePanel.add(loadMoreButton)
        listPanel.add(loadMorePanel, BorderLayout.SOUTH)

        contentPanel.add(listPanel, CARD_CONTENT)

        // Loading panel
        loadingPanel.apply {
            layout = GridBagLayout()
            border = JBUI.Borders.empty(40)
            add(JBLabel("Loading leaderboard...").apply {
                font = font.deriveFont(Font.ITALIC, 14f)
                foreground = JBColor.GRAY
            })
        }
        contentPanel.add(loadingPanel, CARD_LOADING)

        // Error panel
        errorPanel.apply {
            layout = GridBagLayout()
            border = JBUI.Borders.empty(40)
        }
        contentPanel.add(errorPanel, CARD_ERROR)

        // Empty panel
        emptyPanel.apply {
            layout = GridBagLayout()
            border = JBUI.Borders.empty(40)
            add(JBLabel("No entries yet. Be the first!").apply {
                font = font.deriveFont(14f)
                foreground = JBColor.GRAY
            })
        }
        contentPanel.add(emptyPanel, CARD_EMPTY)

        // Auth required panel
        authRequiredPanel.apply {
            layout = GridBagLayout()
            border = JBUI.Borders.empty(40)

            val gbc = GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                insets = JBUI.insets(8)
            }

            // Lock icon
            add(JBLabel(AllIcons.Nodes.SecurityRole).apply {
                horizontalAlignment = SwingConstants.CENTER
            }, gbc)

            gbc.gridy++
            gbc.insets = JBUI.insets(12, 8, 8, 8)
            add(JBLabel("Authentication Required").apply {
                font = font.deriveFont(Font.BOLD, 16f)
                horizontalAlignment = SwingConstants.CENTER
            }, gbc)

            gbc.gridy++
            gbc.insets = JBUI.insets(4, 8, 16, 8)
            add(JBLabel("<html><center>Please log in to view the leaderboard<br>and see your ranking.</center></html>").apply {
                foreground = JBColor.GRAY
                horizontalAlignment = SwingConstants.CENTER
            }, gbc)

            gbc.gridy++
            gbc.insets = JBUI.insets(8)
            add(JButton("Go to Login").apply {
                cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                addActionListener {
                    onNavigateToLogin?.invoke()
                }
            }, gbc)
        }
        contentPanel.add(authRequiredPanel, CARD_AUTH_REQUIRED)

        add(contentPanel, BorderLayout.CENTER)
    }

    private fun createFooterPanel() {
        val footerPanel = JPanel(BorderLayout())
        footerPanel.add(currentUserRankPanel, BorderLayout.CENTER)
        add(footerPanel, BorderLayout.SOUTH)
    }

    private fun setupListeners() {
        refreshButton.addActionListener {
            loadLeaderboard(forceRefresh = true)
        }

        loadMoreButton.addActionListener {
            loadMoreEntries()
        }
    }

    private fun loadLeaderboard(forceRefresh: Boolean) {
        if (isLoading) return
        isLoading = true

        setLoading(true)

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = LeaderboardSyncService.getInstance().fetchLeaderboard(
                period = LeaderboardPeriod.ALL_TIME,
                page = 1,
                forceRefresh = forceRefresh
            )

            SwingUtilities.invokeLater {
                isLoading = false
                setLoading(false)
                handleFetchResult(result)
            }
        }
    }

    private fun loadMoreEntries() {
        if (isLoading) return
        isLoading = true

        loadMoreButton.isEnabled = false
        loadMoreButton.text = "Loading..."

        ApplicationManager.getApplication().executeOnPooledThread {
            val result = LeaderboardSyncService.getInstance().loadMore()

            SwingUtilities.invokeLater {
                isLoading = false
                loadMoreButton.isEnabled = true
                loadMoreButton.text = "Load More"
                handleFetchResult(result, append = true)
            }
        }
    }

    private fun handleFetchResult(result: LeaderboardSyncService.FetchResult, append: Boolean = false) {
        when (result) {
            is LeaderboardSyncService.FetchResult.Success -> {
                if (result.entries.isEmpty()) {
                    showEmpty()
                } else {
                    setEntries(result.entries, append)
                    loadMoreButton.isVisible = result.hasMore
                    currentUserRankPanel.setRank(result.currentUserRank)
                    showContent()
                }
            }
            is LeaderboardSyncService.FetchResult.Error -> {
                showError(result.message)
            }
        }
    }

    /**
     * Sets loading state.
     */
    private fun setLoading(loading: Boolean) {
        if (loading && listModel.isEmpty) {
            (contentPanel.layout as CardLayout).show(contentPanel, CARD_LOADING)
        }
        refreshButton.isEnabled = !loading
    }

    /**
     * Shows the content panel.
     */
    private fun showContent() {
        (contentPanel.layout as CardLayout).show(contentPanel, CARD_CONTENT)
        refreshButton.isEnabled = true
    }

    /**
     * Shows the empty state.
     */
    private fun showEmpty() {
        (contentPanel.layout as CardLayout).show(contentPanel, CARD_EMPTY)
        currentUserRankPanel.clear()
    }

    /**
     * Sets the leaderboard entries.
     */
    private fun setEntries(entries: List<LeaderboardEntry>, append: Boolean = false) {
        if (!append) {
            listModel.clear()
        }
        entries.forEach { listModel.addElement(it) }

        // Update renderer with current user ID
        leaderboardList.cellRenderer = LeaderboardEntryRenderer(AuthCacheService.getInstance().getUserId())
    }

    /**
     * Shows an error message.
     */
    fun showError(message: String) {
        errorPanel.removeAll()
        errorPanel.add(JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                insets = JBUI.insets(4)
            }

            add(JBLabel(AllIcons.General.Error).apply {
                horizontalAlignment = SwingConstants.CENTER
            }, gbc)

            gbc.gridy++
            add(JBLabel(message).apply {
                foreground = JBColor(Color(244, 67, 54), Color(244, 67, 54))
            }, gbc)

            gbc.gridy++
            gbc.insets = JBUI.insets(12, 4, 4, 4)
            add(JButton("Retry").apply {
                addActionListener { loadLeaderboard(forceRefresh = true) }
            }, gbc)
        })

        (contentPanel.layout as CardLayout).show(contentPanel, CARD_ERROR)
        currentUserRankPanel.clear()
    }

    /**
     * Refreshes the leaderboard data.
     * Re-checks authentication status before loading.
     */
    fun refresh() {
        checkAuthAndLoad()
    }

    /**
     * Called when user logs in. Refreshes the panel to show leaderboard.
     */
    fun onUserLoggedIn() {
        refreshButton.isEnabled = true
        loadLeaderboard(forceRefresh = true)
    }
}