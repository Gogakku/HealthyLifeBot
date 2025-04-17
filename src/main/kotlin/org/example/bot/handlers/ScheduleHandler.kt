package org.example.bot.handlers

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.example.org.example.HealthyLifeBot

class ScheduleHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        val text = """
            📆 Пример идеального распорядка дня:
            - 07:00 — Подъём, стакан воды
            - 07:30 — Завтрак
            - 08:00 — Тренировка / Работа
            - 13:00 — Обед
            - 14:00 — Прогулка / Отдых
            - 18:00 — Ужин
            - 22:00 — Подготовка ко сну
        """.trimIndent()
        bot.execute(SendMessage(chatId.toString(), text))
    }
}
