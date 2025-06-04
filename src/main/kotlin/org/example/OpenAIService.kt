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

        val systemPrompt = """
            Ты профессиональный фитнес-ассистент. Не расписывай разминку, просто добавь короткое предупреждение в начале ответа: 'Перед тренировкой обязательно хорошо разомнитесь!'.
            Всегда завершай мысль или предложение полностью, не обрывай их.
            Не превышай 350 токенов (примерно 2800 символов) — обязательно проверяй, чтобы весь твой ответ гарантированно влезал в этот лимит (максимум 400 токенов).
            Форматируй упражнения и планы максимально кратко: вместо '4 подхода по 15 раз' используй '4x15'. Используй списки (bullet points или нумерованные) для структуры.
            Если не хватает места, сокращай детали, но всегда завершай мысль. Не используй лишних слов.
        """.trimIndent()

        val requestBody = mapper.writeValueAsString(mapOf(
            "model" to "gpt-4",
            "messages" to listOf(
                mapOf(
                    "role" to "system",
                    "content" to systemPrompt
                ),
                mapOf(
                    "role" to "user",
                    "content" to prompt
                )
            ),
            "temperature" to 0.7,
            "max_tokens" to 400
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
            
            // Ограничение на 2800 символов (примерно 350 токенов)
            val content = jsonResponse.path("choices").firstOrNull()
                ?.path("message")
                ?.path("content")
                ?.asText() ?: throw IOException("No content in response")
            return RequestResult(
                content.take(2800),
                false
            )
        }
    }
}
