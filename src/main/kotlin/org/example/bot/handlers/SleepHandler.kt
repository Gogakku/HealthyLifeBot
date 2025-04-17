package org.example.bot.handlers

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.example.org.example.HealthyLifeBot

class SleepHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        val text = """
            😴 Советы для качественного сна:
            - Ложись и вставай в одно и то же время
            - Избегай экранов за час до сна
            - Проветривай комнату
            - Убедись, что в комнате темно и тихо
        """.trimIndent()
        bot.execute(SendMessage(chatId.toString(), text))
    }
}
