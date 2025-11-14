package com.example.aicompletion.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.openapi.components.Service

@Service(Service.Level.APP)
@State(
    name = "GeminiAISettings",
    storages = [Storage("gemini_ai_settings.xml")]
)
class GeminiSettingsState : PersistentStateComponent<GeminiSettingsState> {
    var apiKey: String = "AIzaSyDJ77CIpX5KY8mx1_SGBSUl7l2xlh3yMcM"
    var modelName: String = "gemini-2.0-flash"
    var isEnabled: Boolean = true
    var temperature: Float = 0.7f
    var maxTokens: Int = 100

    override fun getState(): GeminiSettingsState = this

    override fun loadState(state: GeminiSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): GeminiSettingsState =
            ApplicationManager.getApplication().getService(GeminiSettingsState::class.java)
    }
}
