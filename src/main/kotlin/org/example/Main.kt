package org.example

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook

fun main() {
    val bot = HealthyLifeBot()
    
    try {
        // Пропускаем ошибку удаления вебхука
        try {
            bot.execute(DeleteWebhook())
        } catch (e: Exception) {
            println("Игнорируем ошибку удаления вебхука: ${e.message}")
        }
        
        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
        botsApi.registerBot(bot)
        println("✅ Бот запущен и готов к работе!")
    } catch (e: Exception) {
        println("❌ Ошибка при запуске бота: ${e.message}")
        throw e
    }
}
