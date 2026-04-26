package com.github.israelkli.intellijplugincopyfilewithproblems.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@Service(Level.APP)
@State(
    name = "CopyWithInlineIssues",
    storages = [Storage("copyWithInlineIssues.xml")]
)
class PluginSettings : PersistentStateComponent<PluginSettingsState> {

    private var myState = PluginSettingsState()

    override fun getState(): PluginSettingsState = myState

    override fun loadState(state: PluginSettingsState) {
        XmlSerializerUtil.copyBean(state, myState)
    }

    companion object {
        @JvmStatic
        fun getInstance(): PluginSettings {
            return ApplicationManager.getApplication().getService(PluginSettings::class.java)
        }
    }
}

class PluginSettingsState {
    var severityFilterErrors: Boolean = true
    var severityFilterWarnings: Boolean = true
    var severityFilterWeakWarnings: Boolean = true
    var severityFilterInfo: Boolean = true
    var commentIncompatibleFallback: String = "APPEND_SUMMARY"
    var notificationLevel: String = "STATUS_BAR"
}
