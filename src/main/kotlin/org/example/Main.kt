package org.example

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import org.telegram.telegrambots.updatesreceivers.DefaultWebhook
import org.telegram.telegrambots.meta.generics.Webhook
import org.telegram.telegrambots.meta.generics.WebhookBot

fun main() {
    try {
        val webhook = DefaultWebhook()
        // Настраиваем внутренний URL для вебхука
        val port = System.getenv("PORT") ?: "8080"
        webhook.setInternalUrl("http://0.0.0.0:$port")
        
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java, webhook)
        val bot = HealthyLifeBot()
        
        // Настраиваем внешний URL для вебхука
        val webhookUrl = System.getenv("RAILWAY_STATIC_URL") ?: run {
            println("⚠️ RAILWAY_STATIC_URL не установлен. Используем локальный режим.")
            println("❗ Для работы вебхука в локальном режиме:")
            println("1. Установите и запустите ngrok: ngrok http 8080")
            println("2. Скопируйте полученный HTTPS URL")
            println("3. Установите его в .env файл как RAILWAY_STATIC_URL")
            println("4. Перезапустите приложение")
            throw IllegalStateException("Требуется настройка RAILWAY_STATIC_URL для работы вебхука")
        }

        val setWebhook = SetWebhook.builder()
            .url("$webhookUrl/webhook")
            .build()
            
        botsApi.registerBot(bot, setWebhook)
        println("✅ Бот запущен и готов к работе!")
        println("🌐 Вебхук установлен на: $webhookUrl/webhook")
        println("🚀 Порт: $port")
        
    } catch (e: TelegramApiException) {
        println("❌ Ошибка при запуске бота: ${e.message}")
        e.printStackTrace()
        throw e
    }
}
