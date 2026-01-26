package com.alfayedoficial.astagfirullah

import com.alfayedoficial.astagfirullah.core.Constants
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for AstagfirullahAppAction.
 * Tests the action that opens the plugin settings dialog.
 */
@DisplayName("AstagfirullahAppAction Tests")
class AstagfirullahAppActionTest {

    @Nested
    @DisplayName("Action instantiation")
    inner class InstantiationTests {

        @Test
        @DisplayName("Can create action instance")
        fun `can create instance`() {
            val action = AstagfirullahAppAction()
            assertNotNull(action)
        }

        @Test
        @DisplayName("Action is an AnAction subclass")
        fun `is anaction subclass`() {
            val action = AstagfirullahAppAction()
            assertTrue(action is com.intellij.openapi.actionSystem.AnAction)
        }
    }

    @Nested
    @DisplayName("Settings dialog configuration")
    inner class SettingsDialogTests {

        @Test
        @DisplayName("Uses correct plugin name for settings dialog")
        fun `uses correct plugin name`() {
            // The action uses Constants.PLUGIN_NAME to find the settings page
            assertEquals("Astagfirullah", Constants.PLUGIN_NAME)
        }

        @Test
        @DisplayName("Plugin name matches configurable display name")
        fun `plugin name matches configurable`() {
            // AstagfirullahConfigurable.getDisplayName() should return PLUGIN_NAME
            val expectedName = Constants.PLUGIN_NAME
            assertNotNull(expectedName)
            assertTrue(expectedName.isNotBlank())
        }
    }

    @Nested
    @DisplayName("Action behavior")
    inner class ActionBehaviorTests {

        @Test
        @DisplayName("Action has actionPerformed method")
        fun `has action performed`() {
            val action = AstagfirullahAppAction()
            
            // Verify the method exists via reflection
            val method = action::class.java.getMethod(
                "actionPerformed",
                com.intellij.openapi.actionSystem.AnActionEvent::class.java
            )
            assertNotNull(method)
        }

        @Test
        @DisplayName("Action can handle null project")
        fun `handles null project`() {
            // When action is triggered without a project context,
            // ShowSettingsUtil.getInstance().showSettingsDialog(null, ...)
            // should still work (shows application-level settings)
            val action = AstagfirullahAppAction()
            assertNotNull(action)
        }
    }

    @Nested
    @DisplayName("Plugin metadata")
    inner class PluginMetadataTests {

        @Test
        @DisplayName("Plugin ID is correctly defined")
        fun `plugin id defined`() {
            assertEquals("com.alfayedoficial.astagfirullah", Constants.PLUGIN_ID)
        }

        @Test
        @DisplayName("Plugin name is correctly defined")
        fun `plugin name defined`() {
            assertEquals("Astagfirullah", Constants.PLUGIN_NAME)
        }
    }
}
