package com.alfayedoficial.astagfirullah

import com.intellij.ide.util.PropertiesComponent

object PropertiesManager {
    private val propertiesComponent: PropertiesComponent = PropertiesComponent.getInstance()

    fun getValue(key: String, defaultValue: String): String {
        return propertiesComponent.getValue(key, defaultValue)
    }

    fun setValue(key: String, value: String) {
        propertiesComponent.setValue(key, value)
    }
}
