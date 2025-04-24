package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class GoalsHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long): BotApiMethod<*> {
        val text = """
            🎯 Примеры целей:
            - Сбросить 5 кг за 2 месяца
            - Бегать 3 раза в неделю по 30 минут
            - Улучшить питание и уменьшить сахар
        Ставь цели реалистично и отслеживай прогресс!
        """.trimIndent()
        return SendMessage(chatId.toString(), text)
    }
}
