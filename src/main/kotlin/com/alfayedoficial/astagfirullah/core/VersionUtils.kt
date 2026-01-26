package com.alfayedoficial.astagfirullah.core

/**
 * Utility object for version string operations.
 * Provides centralized version comparison functionality used across the plugin.
 */
object VersionUtils {

    /**
     * Compares two semantic version strings.
     * Handles versions with different numbers of parts (e.g., "2.0" vs "2.0.1").
     *
     * @param v1 First version string (e.g., "2.0.0")
     * @param v2 Second version string (e.g., "2.1.0")
     * @return Positive if v1 > v2, negative if v1 < v2, 0 if equal
     */
    fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }

            if (p1 != p2) {
                return p1 - p2
            }
        }
        return 0
    }

    /**
     * Checks if a new version is available (server version is newer than current).
     *
     * @param currentVersion Current installed version
     * @param serverVersion Latest version from server
     * @return true if server version is newer than current version
     */
    fun isUpdateAvailable(currentVersion: String, serverVersion: String): Boolean {
        return compareVersions(serverVersion, currentVersion) > 0
    }
}