package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class MotivationHandler(private val bot: HealthyLifeBot) {
    private val motivationalQuotes = listOf(
        "💪 Каждая тренировка делает тебя сильнее!",
        "🎯 Сфокусируйся на прогрессе, а не на совершенстве",
        "✨ Маленькие шаги каждый день приводят к большим результатам",
        "🌟 Ты сильнее, чем думаешь",
        "🚀 Действуй сейчас, не жди понедельника",
        "🌈 Твое здоровье - твое богатство",
        "🎉 Празднуй каждую маленькую победу",
        "🌱 Прогресс требует терпения",
        "⭐ Верь в себя, даже если это сложно",
        "🔥 Твоя единственная конкуренция - это ты вчерашний"
    )

    fun handle(chatId: Long): BotApiMethod<*> {
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            return SendMessage(chatId.toString(), "Сначала необходимо создать профиль!")
        }

        val randomQuote = motivationalQuotes.random()
        val message = """
            $randomQuote
            
            💫 Помни:
            - Каждый день - это новая возможность
            - Маленький прогресс лучше, чем никакого
            - Ты уже на пути к своей цели
            - Не сравнивай себя с другими
            
            💪 Продолжай двигаться вперед!
        """.trimIndent()

        val response = SendMessage(chatId.toString(), message)
        response.replyMarkup = createMotivationMenu()
        return response
    }

    private fun createMotivationMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("⬅️ Назад в меню"))

        keyboard.keyboard = listOf(row1)
        return keyboard
    }
}
