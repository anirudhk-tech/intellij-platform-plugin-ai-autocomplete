package com.example.aicompletion.api

import com.example.aicompletion.settings.GeminiSettingsState
import com.google.genai.Client
import kotlinx.coroutines.runBlocking

class GeminiClient {
    private val settings = GeminiSettingsState.getInstance()

    private val client = Client.builder()
        .apiKey(settings.apiKey)
        .build()

    fun getCompletion(prefix: String, suffix: String = "", repoContext: String? = null): String? {
        if (settings.apiKey.isEmpty() || !settings.isEnabled) return null

        return try {
            val contextBlock = repoContext?.let {
                """
                Project context (from GitIngest, truncated):
                $it

                """.trimIndent()
            } ?: ""


            val promptText = """
                You are a code completion engine.

                $contextBlock
                Complete the following code snippet. Only provide the completion text, nothing else.

                <CODE_PREFIX>
                $prefix
                </CODE_PREFIX>

                <CODE_SUFFIX>
                $suffix
                </CODE_SUFFIX>
            """.trimIndent()

            val response = client.models.generateContent(
                settings.modelName,  // e.g., "gemini-1.5-flash"
                promptText,
                null
            )

            response.text()?.trim()?.let { text ->
                text.substringBefore('\n').take(100)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
