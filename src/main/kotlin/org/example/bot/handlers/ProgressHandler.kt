package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class ProgressHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            bot.execute(SendMessage(chatId.toString(), "Сначала необходимо создать профиль!"))
            return
        }

        val message = """
            📊 Ваш прогресс:
            
            📈 Текущие показатели:
            Вес: ${profile.weight} кг
            
            💪 Достижения:
            - Регулярные тренировки
            - Правильное питание
            - Здоровый сон
            
            🎯 Следующие цели:
            1. Продолжайте придерживаться режима
            2. Следите за питанием
            3. Не пропускайте тренировки
            
            Продолжайте в том же духе! 💪
        """.trimIndent()

        bot.execute(SendMessage(chatId.toString(), message))
    }
}
