package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.example.org.example.UserState
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class SettingsHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            bot.execute(SendMessage(chatId.toString(), "Сначала необходимо создать профиль!"))
            return
        }

        val message = """
            ⚙️ Настройки:
            
            👤 Ваш профиль:
            Имя: ${profile.name}
            Возраст: ${profile.age}
            Рост: ${profile.height} см
            Вес: ${profile.weight} кг
            Цель: ${profile.goal}
            
            Выберите параметр для изменения:
        """.trimIndent()

        val response = SendMessage(chatId.toString(), message)
        response.replyMarkup = createSettingsMenu()
        bot.execute(response)
    }

    fun handleSettingChange(chatId: Long, setting: String) {
        val message = when (setting) {
            "Изменить возраст" -> {
                bot.setUserState(chatId, UserState.SETTINGS_AGE)
                "Введите новый возраст (от 10 до 100 лет):"
            }
            "Изменить рост" -> {
                bot.setUserState(chatId, UserState.SETTINGS_HEIGHT)
                "Введите новый рост в сантиметрах (от 100 до 250 см):"
            }
            "Изменить вес" -> {
                bot.setUserState(chatId, UserState.SETTINGS_WEIGHT)
                "Введите новый вес в килограммах (от 30 до 300 кг):"
            }
            "Изменить цель" -> {
                bot.setUserState(chatId, UserState.SETTINGS_GOAL)
                """
                    Выберите новую цель:
                    1. Похудеть
                    2. Набрать мышечную массу
                    3. Поддерживать форму
                """.trimIndent()
            }
            else -> {
                bot.setUserState(chatId, UserState.COMPLETED)
                "Неизвестная настройка"
            }
        }

        val response = SendMessage(chatId.toString(), message)
        if (setting == "Изменить цель") {
            response.replyMarkup = createGoalMenu()
        }
        bot.execute(response)
    }

    private fun createSettingsMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("Изменить возраст"))
                add(KeyboardButton("Изменить рост"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("Изменить вес"))
                add(KeyboardButton("Изменить цель"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("⬅️ Назад в меню"))
            }
        )
        return keyboard
    }

    private fun createGoalMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("1. Похудеть"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("2. Набрать мышечную массу"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("3. Поддерживать форму"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("⬅️ Назад в настройки"))
            }
        )
        return keyboard
    }
}
