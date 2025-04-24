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
        val webhookUrl = System.getenv("RAILWAY_STATIC_URL") ?: throw IllegalStateException("RAILWAY_STATIC_URL не установлен")
        val setWebhook = SetWebhook.builder()
            .url("$webhookUrl/webhook")
            .build()
            
        botsApi.registerBot(bot, setWebhook)
        println("✅ Бот запущен и готов к работе! Вебхук установлен на: $webhookUrl/webhook")
        
    } catch (e: TelegramApiException) {
        println("❌ Ошибка при запуске бота: ${e.message}")
        e.printStackTrace()
        throw e
    }
}
