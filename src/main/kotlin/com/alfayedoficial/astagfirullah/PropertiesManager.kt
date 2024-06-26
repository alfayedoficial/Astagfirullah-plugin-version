package com.alfayedoficial.astagfirullah

import com.intellij.ide.util.PropertiesComponent

object PropertiesManager {
    private val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance()

    fun getPreferredLanguage(): String {
        return propertiesComponent.getValue("preferredLanguage", "العربية")
    }

    fun setPreferredLanguage(language: String) {
        propertiesComponent.setValue("preferredLanguage", language)
    }

    fun isSoundEnabled(): Boolean {
        return propertiesComponent.getValue("preferredSound", "true") == "true"
    }

    fun setSoundEnabled(enabled: String) {
        propertiesComponent.setValue("preferredSound", enabled)
    }
}
