package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class ProgressHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long): BotApiMethod<*> {
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            return SendMessage(chatId.toString(), "Сначала необходимо создать профиль!")
        }

        val height = profile.height
        val weight = profile.weight
        val age = profile.age

        val progressMessage = if (height == null || weight == null || age == null) {
            """
                📊 Ваш текущий прогресс:

                👤 Профиль:
                - Имя: ${profile.name}
                - Возраст: ${age ?: "Не указан"} 
                - Рост: ${height ?: "Не указан"} 
                - Вес: ${weight ?: "Не указан"} 
                - Цель: ${profile.goal}

                ⚠️ Для получения полной статистики и рекомендаций, пожалуйста, укажите недостающие данные в настройках.
            """.trimIndent()
        } else {
            """
                📊 Ваш текущий прогресс:

                👤 Профиль:
                - Имя: ${profile.name}
                - Возраст: $age лет
                - Рост: $height см
                - Вес: $weight кг
                - Цель: ${profile.goal}

                📈 Статистика:
                - ИМТ: ${calculateBMI(weight, height)}
                - Рекомендуемый вес: ${calculateRecommendedWeight(height)} кг
                
                💪 Рекомендации:
                ${getRecommendations(weight, height)}
                
                💡 Совет: Регулярно обновляйте свои данные для точного отслеживания прогресса
            """.trimIndent()
        }

        val response = SendMessage(chatId.toString(), progressMessage)
        response.replyMarkup = createProgressMenu()
        return response
    }

    private fun calculateBMI(weight: Int, height: Int): Double {
        val heightInMeters = height / 100.0
        return String.format("%.1f", weight / (heightInMeters * heightInMeters)).toDouble()
    }

    private fun calculateRecommendedWeight(height: Int): String {
        val heightInMeters = height / 100.0
        val minWeight = 18.5 * (heightInMeters * heightInMeters)
        val maxWeight = 24.9 * (heightInMeters * heightInMeters)
        return String.format("%.1f - %.1f", minWeight, maxWeight)
    }

    private fun getRecommendations(weight: Int, height: Int): String {
        val bmi = calculateBMI(weight, height)
        return when {
            bmi < 18.5 -> """
                - Увеличьте калорийность рациона
                - Добавьте силовые тренировки
                - Следите за потреблением белка
            """.trimIndent()
            bmi > 24.9 -> """
                - Создайте умеренный дефицит калорий
                - Увеличьте физическую активность
                - Добавьте кардио тренировки
            """.trimIndent()
            else -> """
                - Поддерживайте текущий баланс
                - Продолжайте регулярные тренировки
                - Следите за качеством питания
            """.trimIndent()
        }
    }

    private fun createProgressMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("⬅️ Назад в меню"))
        row1.add(KeyboardButton("⚙️ Обновить данные"))

        keyboard.keyboard = listOf(row1)
        return keyboard
    }
}
