package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class MenuHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long): BotApiMethod<*> {
        val profile = bot.getUserProfile(chatId)
        val welcomeMessage = if (profile == null) {
            """
            👋 Добро пожаловать в HealthyLifeBot!
            
            🎯 Чтобы начать, создайте свой профиль с помощью команды /settings
            
            Это поможет мне лучше подобрать рекомендации для вас!
            """.trimIndent()
        } else {
            """
            👋 С возвращением, ${profile.name}!
            
            🎯 Ваша цель: ${profile.goal}
            
            Выберите раздел, который вас интересует:
            """.trimIndent()
        }

        val response = SendMessage(chatId.toString(), welcomeMessage)
        response.replyMarkup = createMainMenu()
        return response
    }

    private fun createMainMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("🏋️‍♂️ Тренировки"))
        row1.add(KeyboardButton("🥗 Питание"))

        val row2 = KeyboardRow()
        row2.add(KeyboardButton("😴 Сон"))
        row2.add(KeyboardButton("💪 Мотивация"))

        val row3 = KeyboardRow()
        row3.add(KeyboardButton("⚙️ Настройки"))

        keyboard.keyboard = listOf(row1, row2, row3)
        return keyboard
    }
}
