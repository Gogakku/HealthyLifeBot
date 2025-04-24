package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class ScheduleHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long): BotApiMethod<*> {
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            return SendMessage(chatId.toString(), "Сначала необходимо создать профиль!")
        }

        val schedule = when (profile.goal) {
            "Похудеть" -> """
                📅 Рекомендуемый распорядок дня для похудения:

                🌅 06:00 - Подъем
                🚰 06:15 - Стакан воды + легкая разминка
                🏃‍♂️ 06:30 - Утренняя кардио тренировка (30 мин)
                🍳 07:15 - Здоровый завтрак
                
                🌞 12:00 - Легкий перекус
                🥗 13:00 - Обед (следите за порциями)
                
                🏋️‍♂️ 17:00 - Силовая тренировка
                🥑 18:30 - Легкий ужин
                
                🌙 21:00 - Последний прием пищи (если нужно)
                😴 22:00 - Подготовка ко сну
                🛏 22:30 - Сон
            """.trimIndent()

            "Набрать мышечную массу" -> """
                📅 Рекомендуемый распорядок дня для набора массы:

                🌅 07:00 - Подъем
                🥛 07:15 - Протеиновый коктейль
                🍳 07:30 - Плотный завтрак
                
                🥜 10:00 - Перекус (орехи, протеиновый батончик)
                🍗 13:00 - Обед (высокое содержание белка)
                
                🏋️‍♂️ 16:00 - Силовая тренировка
                🥩 17:30 - Пост-тренировочное питание
                🍚 19:00 - Ужин
                
                🥛 21:00 - Казеиновый протеин
                😴 22:30 - Сон
            """.trimIndent()

            else -> """
                📅 Рекомендуемый распорядок дня:

                🌅 07:00 - Подъем
                🧘‍♂️ 07:15 - Утренняя зарядка
                🍳 08:00 - Завтрак
                
                🚶‍♂️ 12:00 - Прогулка
                🥗 13:00 - Обед
                
                🏃‍♂️ 17:00 - Тренировка
                🍽 19:00 - Ужин
                
                🧘‍♀️ 21:00 - Вечерняя растяжка
                😴 22:00 - Сон
            """.trimIndent()
        }

        val response = SendMessage(chatId.toString(), schedule)
        response.replyMarkup = createScheduleMenu()
        return response
    }

    private fun createScheduleMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("⬅️ Назад в меню"))

        keyboard.keyboard = listOf(row1)
        return keyboard
    }
}
