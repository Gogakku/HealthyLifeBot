package org.example

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import org.example.service.DatabaseService

fun main() {
    DatabaseService.init()
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)
    botsApi.registerBot(HealthyLifeBot())
    println("✅ Бот запущен и готов к работе!")
}
