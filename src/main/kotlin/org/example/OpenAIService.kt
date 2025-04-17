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
                    // Ждем перед следующей попыткой
                    val waitTime = attempt * 5000L
                    println("Ожидание $waitTime мс перед следующей попыткой...")
                    Thread.sleep(waitTime)
                    continue
                }
                return response.text
            } catch (e: Exception) {
                println("Попытка $attempt: Ошибка: ${e.message}")
                lastError = e
                
                // Ждем перед следующей попыткой
                val waitTime = attempt * 5000L
                println("Ожидание $waitTime мс перед следующей попыткой...")
                Thread.sleep(waitTime)
            }
        }
        
        return "Извините, сервис временно недоступен. Пожалуйста, попробуйте позже. (${lastError?.message})"
    }

    private data class RequestResult(val text: String, val isRetry: Boolean)

    private fun makeRequest(prompt: String): RequestResult {
        // Используем T5 для лучшей генерации структурированных ответов
        val url = "https://api-inference.huggingface.co/models/google/flan-t5-small"

        val requestBody = mapper.writeValueAsString(mapOf(
            "inputs" to """You are a professional fitness trainer. Create a detailed response in the following format:
                          |
                          |Task: $prompt
                          |
                          |Response format:
                          |1. First, give a short introduction
                          |2. Then, provide specific recommendations or a plan
                          |3. Finally, add a motivational note
                          |
                          |Remember to be specific and practical. Keep the total response under 150 words.""".trimMargin(),
            "parameters" to mapOf(
                "max_new_tokens" to 200,
                "temperature" to 0.7,
                "top_p" to 0.95,
                "do_sample" to true,
                "num_return_sequences" to 1
            )
        )).toRequestBody(JSON)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .header("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                println("Ошибка API: ${response.code} - $errorBody")
                
                if (response.code == 503) {
                    return RequestResult("", true)
                }
                
                return RequestResult("Извините, произошла ошибка при обработке запроса (${response.code})", false)
            }

            val responseBody = response.body?.string() ?: return RequestResult("Нет ответа от сервера", false)
            println("Ответ от сервера: $responseBody") // Для отладки
            
            val result = mapper.readTree(responseBody)
            val generatedText = result[0]["generated_text"]?.asText() ?: return RequestResult("Не удалось получить ответ", false)
            
            // Очищаем ответ от промпта и переводим на русский
            return RequestResult(translateToRussian(generatedText), false)
        }
    }

    private fun translateToRussian(text: String): String {
        // Расширенный словарь для фитнес-терминологии
        val dictionary = mapOf(
            // Базовые слова
            "yes" to "да",
            "no" to "нет",
            "exercise" to "упражнение",
            "exercises" to "упражнения",
            "diet" to "диета",
            "healthy" to "здоровый",
            "fitness" to "фитнес",
            "weight" to "вес",
            "food" to "еда",
            "water" to "вода",
            "sleep" to "сон",
            
            // Фитнес термины
            "workout" to "тренировка",
            "training" to "тренировка",
            "cardio" to "кардио",
            "strength" to "сила",
            "repetitions" to "повторения",
            "sets" to "подходы",
            "rest" to "отдых",
            "muscles" to "мышцы",
            "stretching" to "растяжка",
            "warm-up" to "разминка",
            "cool-down" to "заминка",
            
            // Временные интервалы
            "minutes" to "минут",
            "hours" to "часов",
            "daily" to "ежедневно",
            "weekly" to "еженедельно",
            "regularly" to "регулярно",
            "times" to "раз",
            "per" to "в",
            "week" to "неделю",
            
            // Общие фразы
            "recommended" to "рекомендуется",
            "important" to "важно",
            "should" to "следует",
            "need" to "нужно",
            "can" to "можно",
            "good" to "хорошо",
            "bad" to "плохо",
            "help" to "помощь",
            "start" to "начните",
            "begin" to "начните",
            "continue" to "продолжайте",
            "increase" to "увеличьте",
            "decrease" to "уменьшите",
            
            // Мотивационные фразы
            "great" to "отлично",
            "amazing" to "замечательно",
            "perfect" to "превосходно",
            "keep" to "продолжайте",
            "going" to "двигаться",
            "forward" to "вперед",
            "achieve" to "достигнете",
            "goals" to "цели",
            "success" to "успех",
            "motivation" to "мотивация"
        )
        
        var translated = text.lowercase()
        dictionary.forEach { (eng, rus) -> 
            translated = translated.replace(eng, rus)
        }
        
        return translated.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
