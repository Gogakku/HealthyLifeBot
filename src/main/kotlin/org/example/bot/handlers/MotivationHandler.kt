package org.example.bot.handlers

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.example.org.example.HealthyLifeBot

class MotivationHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        val text = """
            🔥 Мотивация на сегодня:
            "Не важно, как медленно ты идешь — ты всё равно обгоняешь тех, кто сидит на месте."
            — Не сдавайся! Маленькие шаги ведут к большим победам 💪
        """.trimIndent()
        bot.execute(SendMessage(chatId.toString(), text))
    }
}
