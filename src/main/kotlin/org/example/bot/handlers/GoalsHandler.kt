package org.example.bot.handlers

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.example.org.example.HealthyLifeBot

class GoalsHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        val text = """
            🎯 Примеры целей:
            - Сбросить 5 кг за 2 месяца
            - Бегать 3 раза в неделю по 30 минут
            - Улучшить питание и уменьшить сахар
        Ставь цели реалистично и отслеживай прогресс!
        """.trimIndent()
        bot.execute(SendMessage(chatId.toString(), text))
    }
}
