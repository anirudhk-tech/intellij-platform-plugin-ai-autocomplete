package com.example.aicompletion.git

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.APP)
class GitIngestService {

    private val cache = ConcurrentHashMap<String, String>()

    suspend fun getOrFetchDigest(project: Project): String? {
        val key = project.basePath ?: return null

        cache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            val repoUrl = "https://github.com/anirudhk-tech/cackle"

            val bodyJson = """
                {
                  "input_text": "$repoUrl",
                  "max_file_size": 5120,
                  "pattern_type": "exclude",
                  "pattern": ""
                }
            """.trimIndent()

            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://gitingest.com/api/ingest"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return@withContext null

            val json = JSONObject(response.body())
            val content = json.optString("content", null)
            if (content != null) {
                val truncated = content.take(100_000)
                cache[key] = truncated
                truncated
            } else null
        }
    }

    companion object {
        fun getInstance(): GitIngestService = service()
    }
}
