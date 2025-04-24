package org.example.bot.handlers

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.example.org.example.HealthyLifeBot

class NutritionHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        val text = """
            🍎 Пример здорового питания:
            - Завтрак: овсянка с фруктами и орехами
            - Обед: куриная грудка, овощи, немного риса
            - Ужин: рыба, салат из свежих овощей
            - Перекусы: орехи, йогурт, фрукты
        """.trimIndent()
        bot.execute(SendMessage(chatId.toString(), text))
    }
}
