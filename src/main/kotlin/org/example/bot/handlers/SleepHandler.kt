package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class SleepHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long): BotApiMethod<*> {
        val sleepTips = """
            😴 Рекомендации по здоровому сну:
            
            ⏰ Режим:
            - Ложитесь и вставайте в одно и то же время
            - Спите 7-9 часов в сутки
            - Придерживайтесь режима даже в выходные
            
            🛏 Подготовка ко сну:
            - Проветрите комнату перед сном
            - Температура в спальне 18-21°C
            - Выключите яркий свет за 1-2 часа до сна
            - Уберите гаджеты за 30-60 минут до сна
            
            ❌ Чего избегать:
            - Кофеин после 14:00
            - Тяжелая пища перед сном
            - Интенсивные тренировки вечером
            - Яркие экраны перед сном
            
            ✅ Полезные привычки:
            - Легкая растяжка перед сном
            - Теплая ванна за 1-2 часа до сна
            - Чтение бумажной книги
            - Медитация или дыхательные упражнения
            
            💡 Совет: Качественный сон так же важен для здоровья, как питание и тренировки!
        """.trimIndent()

        val response = SendMessage(chatId.toString(), sleepTips)
        response.replyMarkup = createSleepMenu()
        return response
    }

    private fun createSleepMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("⬅️ Назад в меню"))

        keyboard.keyboard = listOf(row1)
        return keyboard
    }
}
