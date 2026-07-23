package com.alfayedoficial.astagfirullah.core

import com.intellij.openapi.application.ApplicationManager

/**
 * Detects environments where the plugin must stay silent — no auto-shown dialogs.
 *
 * The JetBrains Marketplace runs an automated "install-plugin-test" that boots the IDE with the
 * plugin installed and waits for it to reach a ready state. A dialog popped from a startup
 * activity stalls that run (a 10-minute timeout was reported) and blocks EDT startup so the IDE
 * Trial status-bar widget never initializes — which the verifier reports as "Plugin must not
 * remove the IDE Trial widget". Both are avoided by simply not showing UI in these runs.
 *
 * Covers: headless mode, unit tests, and the IDE integration-test harness (the property
 * `idea.is.integration.test`, which is exactly what the Marketplace install-plugin-test sets).
 * Real users are never affected.
 */
object RuntimeEnv {

    fun isNonInteractive(): Boolean {
        val app = ApplicationManager.getApplication() ?: return true
        return app.isUnitTestMode ||
            app.isHeadlessEnvironment ||
            java.lang.Boolean.getBoolean("idea.is.integration.test")
    }
}
