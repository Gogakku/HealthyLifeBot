package org.example

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class OpenAIService(private val apiKey: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val mapper = jacksonObjectMapper()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    fun generateResponse(prompt: String): String {
        var lastError: Exception? = null
        val maxRetries = 3
        
        for (attempt in 1..maxRetries) {
            try {
                val response = makeRequest(prompt)
                if (response.isRetry) {
                    val waitTime = attempt * 5000L
                    println("Ожидание $waitTime мс перед следующей попыткой...")
                    Thread.sleep(waitTime)
                    continue
                }
                return response.text
            } catch (e: Exception) {
                println("Попытка $attempt: Ошибка: ${e.message}")
                lastError = e
                
                val waitTime = attempt * 5000L
                println("Ожидание $waitTime мс перед следующей попыткой...")
                Thread.sleep(waitTime)
            }
        }
        
        return "Извините, сервис временно недоступен. Пожалуйста, попробуйте позже. (${lastError?.message})"
    }

    private data class RequestResult(val text: String, val isRetry: Boolean)

    private fun makeRequest(prompt: String): RequestResult {
        val url = "https://api.proxyapi.ru/openai/v1/chat/completions"

        val requestBody = mapper.writeValueAsString(mapOf(
            "model" to "gpt-4",
            "messages" to listOf(
                mapOf(
                    "role" to "system",
                    "content" to """You are a professional fitness trainer. Create detailed responses with:
                        |1. Short introduction
                        |2. Specific recommendations or plan
                        |3. Motivational note
                        |Keep responses practical and under 150 words.""".trimMargin()
                ),
                mapOf(
                    "role" to "user",
                    "content" to prompt
                )
            ),
            "temperature" to 0.7,
            "max_tokens" to 200
        ))

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody(JSON))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                if (response.code == 429 || response.code >= 500) {
                    return RequestResult("", true)
                }
                throw IOException("Unexpected response: ${response.code} $errorBody")
            }

            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            val jsonResponse = mapper.readTree(responseBody)
            
            return RequestResult(
                jsonResponse.path("choices").firstOrNull()
                    ?.path("message")
                    ?.path("content")
                    ?.asText() ?: throw IOException("No content in response"),
                false
            )
        }
    }
}
