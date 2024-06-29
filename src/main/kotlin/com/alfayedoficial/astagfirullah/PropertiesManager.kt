package com.alfayedoficial.astagfirullah

import com.intellij.ide.util.PropertiesComponent



object PropertiesManager {
    private val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance()
    private const val INSTALL_TIME_KEY = "plugin_install_time"
    private const val FIRST_RATING_TIME_KEY = "first_rating_time"
    private const val RATING_PROMPTED_KEY = "rating_prompted"
    private const val PREFERRED_LANGUAGE_KEY = "preferredLanguage"
    private const val PREFERRED_SOUND_KEY = "preferredSound"
    private const val PREFERRED_DELAY_SECONDS_KEY = "preferredDelaySeconds"

    fun getPreferredLanguage(): String {
        return propertiesComponent.getValue(PREFERRED_LANGUAGE_KEY, "العربية")
    }

    fun setPreferredLanguage(language: String) {
        propertiesComponent.setValue(PREFERRED_LANGUAGE_KEY, language)
    }

    fun isSoundEnabled(): Boolean {
        return propertiesComponent.getValue(PREFERRED_SOUND_KEY, "true") == "true"
    }

    fun setSoundEnabled(enabled: String) {
        propertiesComponent.setValue(PREFERRED_SOUND_KEY, enabled)
    }

    fun getInstallTime(): Long {
        return propertiesComponent.getValue(INSTALL_TIME_KEY, "0").toLong()
    }

    fun setInstallTime(time: Long) {
        propertiesComponent.setValue(INSTALL_TIME_KEY, time.toString())
    }

    fun setFirstTime(status :String= "1") {
        propertiesComponent.setValue(FIRST_RATING_TIME_KEY, status)
    }

    fun isFirstTime(): String {
        return propertiesComponent.getValue(FIRST_RATING_TIME_KEY, "1")
    }

    fun isRatingPrompted(): Boolean {
        return propertiesComponent.getValue(RATING_PROMPTED_KEY, "false") == "true"
    }

    fun setRatingPrompted() {
        propertiesComponent.setValue(RATING_PROMPTED_KEY, "true")
    }

    fun getPreferredDelaySeconds(): String {
        return propertiesComponent.getValue(PREFERRED_DELAY_SECONDS_KEY, "1.5")
    }

    fun setPreferredDelaySeconds(seconds: String) {
        propertiesComponent.setValue(PREFERRED_DELAY_SECONDS_KEY, seconds)
    }
}
