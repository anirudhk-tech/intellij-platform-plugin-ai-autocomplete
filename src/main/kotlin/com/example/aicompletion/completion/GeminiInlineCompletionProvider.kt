package com.example.aicompletion.completion

import com.example.aicompletion.api.GeminiClient
import com.example.aicompletion.settings.GeminiSettingsState
import com.example.aicompletion.git.GitIngestService
import com.intellij.codeInsight.inline.completion.InlineCompletionEvent
import com.intellij.codeInsight.inline.completion.InlineCompletionProvider
import com.intellij.codeInsight.inline.completion.InlineCompletionProviderID
import com.intellij.codeInsight.inline.completion.InlineCompletionRequest
import com.intellij.codeInsight.inline.completion.elements.InlineCompletionGrayTextElement
import com.intellij.codeInsight.inline.completion.suggestion.InlineCompletionSingleSuggestion
import com.intellij.codeInsight.inline.completion.suggestion.InlineCompletionSuggestion
import com.intellij.codeInsight.inline.completion.suggestion.InlineCompletionVariant
import com.intellij.openapi.diagnostic.Logger

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class GeminiSuggestion(private val completionText: String) : InlineCompletionSingleSuggestion {
    override suspend fun getVariant(): InlineCompletionVariant {
        return InlineCompletionVariant.build {
            emit(InlineCompletionGrayTextElement(completionText))
        }
    }
}

object EmptySuggestion : InlineCompletionSuggestion {
    override suspend fun getVariants(): List<InlineCompletionVariant> = emptyList()
}

class GeminiInlineCompletionProvider : InlineCompletionProvider {
    private val geminiClient = GeminiClient()
    private val settings = GeminiSettingsState.getInstance()
    private val gitIngestService = GitIngestService.getInstance()
    private val log = Logger.getInstance(GeminiInlineCompletionProvider::class.java)

    override val id: InlineCompletionProviderID
        get() = InlineCompletionProviderID("GeminiAICompletion")

    override fun isEnabled(event: InlineCompletionEvent): Boolean {
        return settings.isEnabled && settings.apiKey.isNotEmpty()
    }

    override suspend fun getSuggestion(
        request: InlineCompletionRequest
    ): InlineCompletionSuggestion {
        return try {
            delay(1000)


            log.info("GeminiInlineCompletionProvider.getSuggestion called")
            val document = request.document
            val offset = request.endOffset
            val project = request.file.project

            // Extract prefix (everything before cursor)
            val prefix = document.text.substring(0, offset)

            // Extract suffix (everything after cursor, max 500 chars)
            val suffix = document.text.substring(
                offset,
                minOf(offset + 500, document.textLength)
            )

            // val repoContext = withContext(Dispatchers.IO) {
            //     gitIngestService.getOrFetchDigest(project)
            // }

            val repoContext = ""

            // Get completion from Gemini API
            val completionText = withContext(Dispatchers.IO) {
                geminiClient.getCompletion(prefix, suffix, repoContext)
            }

            if (!completionText.isNullOrEmpty()) {
                GeminiSuggestion(completionText)
            } else {
                EmptySuggestion
            }
        } catch (e: Exception) {
            e.printStackTrace()
            EmptySuggestion
        }
    }
}
