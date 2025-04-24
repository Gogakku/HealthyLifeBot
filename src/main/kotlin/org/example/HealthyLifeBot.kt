package org.example.org.example

import org.example.bot.handlers.*
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.example.OpenAIService  // Импортируем наш сервис для OpenAI

class HealthyLifeBot : TelegramLongPollingBot() {

    private val userProfiles = mutableMapOf<Long, UserProfile>()
    private val userStates = mutableMapOf<Long, UserState>()

    private val openAIService = OpenAIService("OPENAI_API_KEY")  // Передаём API-ключ в org.example.OpenAIService

    fun getUserProfile(chatId: Long): UserProfile? = userProfiles[chatId]

    fun setUserState(chatId: Long, state: UserState) {
        userStates[chatId] = state
    }

    override fun getBotUsername(): String = "HealthyLifeBot"
    override fun getBotToken(): String = "BOT_TOKEN"  // Заменить на актуальный токен

    override fun onUpdateReceived(update: Update) {
        val message = update.message ?: return
        val chatId = message.chatId
        val text = message.text ?: return

        if (text == "/start") {
            val name = message.from?.firstName ?: "друг"
            userProfiles[chatId] = UserProfile(name = name)
            userStates[chatId] = UserState.AWAITING_AGE

            val reply = SendMessage(chatId.toString(), "👋 Привет, $name! Давай начнём с твоего возраста. Сколько тебе лет?")
            execute(reply)
            return
        }

        val state = userStates[chatId]
        val profile = userProfiles[chatId]

        if (state != null && profile != null) {
            when (state) {
                UserState.AWAITING_AGE -> {
                    val age = text.toIntOrNull()
                    if (age != null && age in 10..100) {
                        profile.age = age
                        userStates[chatId] = UserState.AWAITING_HEIGHT
                        execute(SendMessage(chatId.toString(), "Отлично! Теперь укажи свой рост в сантиметрах:"))
                    } else {
                        execute(SendMessage(chatId.toString(), "Пожалуйста, введи корректный возраст (от 10 до 100 лет)."))
                    }
                }

                UserState.AWAITING_HEIGHT -> {
                    val height = text.toIntOrNull()
                    if (height != null && height in 100..250) {
                        profile.height = height
                        userStates[chatId] = UserState.AWAITING_WEIGHT
                        execute(SendMessage(chatId.toString(), "Спасибо! А теперь укажи свой вес в кг:"))
                    } else {
                        execute(SendMessage(chatId.toString(), "Введи корректный рост (например, 175):"))
                    }
                }

                UserState.AWAITING_WEIGHT -> {
                    val weight = text.toIntOrNull()
                    if (weight != null && weight in 30..300) {
                        profile.weight = weight
                        userStates[chatId] = UserState.AWAITING_GOAL
                        execute(SendMessage(chatId.toString(), """
                            Почти готово! Теперь выбери свою цель:
                            1. Похудеть
                            2. Набрать мышечную массу
                            3. Поддерживать форму
                        """.trimIndent()))
                    } else {
                        execute(SendMessage(chatId.toString(), "Введи корректный вес (например, 70):"))
                    }
                }

                UserState.AWAITING_GOAL -> {
                    val goal = when (text.trim().lowercase()) {
                        "1", "похудеть" -> "Похудеть"
                        "2", "набрать мышечную массу" -> "Набрать мышечную массу"
                        "3", "поддерживать форму" -> "Поддерживать форму"
                        else -> null
                    }

                    if (goal != null) {
                        profile.goal = goal
                        userStates[chatId] = UserState.COMPLETED
                        val summary = """
                            🎉 Отлично, вот твой профиль:
                            Имя: ${profile.name}
                            Возраст: ${profile.age}
                            Рост: ${profile.height} см
                            Вес: ${profile.weight} кг
                            Цель: ${profile.goal}
                            

                            Давай начнём планировать! Выбери пункт меню:
                        """.trimIndent()

                        val response = SendMessage(chatId.toString(), summary)
                        response.replyMarkup = createMainMenu()
                        execute(response)
                    } else {
                        execute(SendMessage(chatId.toString(), "Выбери одну из целей (1, 2 или 3):"))
                    }
                }

                UserState.COMPLETED -> {
                    handleUserInput(chatId, text)
                }

                UserState.AI_ASSISTANT -> {
                    if (text == "⬅️ Выход") {
                        userStates[chatId] = UserState.COMPLETED
                        val response = SendMessage(chatId.toString(), "Вы вернулись в главное меню")
                        response.replyMarkup = createMainMenu()
                        execute(response)
                    } else {
                        val aiResponse = openAIService.generateResponse(text)  // Здесь вызываем OpenAI для ответа на сообщение
                        val response = SendMessage(chatId.toString(), aiResponse)
                        // Сохраняем кнопку выхода
                        val keyboard = ReplyKeyboardMarkup()
                        keyboard.resizeKeyboard = true
                        keyboard.keyboard = listOf(
                            KeyboardRow(listOf(KeyboardButton("⬅️ Выход")))
                        )
                        response.replyMarkup = keyboard
                        execute(response)
                    }
                }

                UserState.SETTINGS_AGE -> {
                    val age = text.toIntOrNull()
                    if (age != null && age in 10..100) {
                        profile.age = age
                        userStates[chatId] = UserState.COMPLETED
                        val handler = SettingsHandler(this)
                        handler.handle(chatId)
                    } else {
                        execute(SendMessage(chatId.toString(), "Пожалуйста, введите корректный возраст (от 10 до 100 лет)."))
                    }
                }

                UserState.SETTINGS_HEIGHT -> {
                    val height = text.toIntOrNull()
                    if (height != null && height in 100..250) {
                        profile.height = height
                        userStates[chatId] = UserState.COMPLETED
                        val handler = SettingsHandler(this)
                        handler.handle(chatId)
                    } else {
                        execute(SendMessage(chatId.toString(), "Введите корректный рост (от 100 до 250 см)."))
                    }
                }

                UserState.SETTINGS_WEIGHT -> {
                    val weight = text.toIntOrNull()
                    if (weight != null && weight in 30..300) {
                        profile.weight = weight
                        userStates[chatId] = UserState.COMPLETED
                        val handler = SettingsHandler(this)
                        handler.handle(chatId)
                    } else {
                        execute(SendMessage(chatId.toString(), "Введите корректный вес (от 30 до 300 кг)."))
                    }
                }

                UserState.SETTINGS_GOAL -> {
                    val goal = when (text.trim()) {
                        "1. Похудеть", "1", "Похудеть" -> "Похудеть"
                        "2. Набрать мышечную массу", "2", "Набрать мышечную массу" -> "Набрать мышечную массу"
                        "3. Поддерживать форму", "3", "Поддерживать форму" -> "Поддерживать форму"
                        else -> null
                    }

                    if (goal != null) {
                        profile.goal = goal
                        userStates[chatId] = UserState.COMPLETED
                        val handler = SettingsHandler(this)
                        handler.handle(chatId)
                    } else {
                        execute(SendMessage(chatId.toString(), "Выберите одну из предложенных целей."))
                    }
                }
            }

            return
        }

        // Если нет состояния — обработка кнопок
        handleUserInput(chatId, text)
    }

    private fun handleUserInput(chatId: Long, text: String) {
        val profile = userProfiles[chatId]

        if (profile?.goal != null) {
            when (text) {
                "🏋️ Тренировки" -> {
                    val response = SendMessage(chatId.toString(), "Выбери тип тренировки:")
                    response.replyMarkup = createTrainingMenu()
                    execute(response)
                }
                "Кардио", "Силовые тренировки", "Растяжка" -> {
                    val handler = TrainingHandler(this)
                    handler.handleTrainingSelection(chatId, text)
                }
                "🍕 Питание" -> {
                    val handler = NutritionHandler(this)
                    handler.handle(chatId)
                }
                "🛌 Сон" -> {
                    val handler = SleepHandler(this)
                    handler.handle(chatId)
                }
                "📅 Распорядок дня" -> {
                    val handler = ScheduleHandler(this)
                    handler.handle(chatId)
                }
                "🎯 Цели" -> {
                    val handler = GoalsHandler(this)
                    handler.handle(chatId)
                }
                "❤️ Мотивация" -> {
                    val handler = MotivationHandler(this)
                    handler.handle(chatId)
                }
                "⬅️ Назад в меню" -> {
                    val response = SendMessage(chatId.toString(), "Главное меню:")
                    response.replyMarkup = createMainMenu()
                    execute(response)
                }
                "🤖 ИИ Ассистент" -> handleAIAssistant(chatId, profile)
                "📊 Мой прогресс" -> {
                    val handler = ProgressHandler(this)
                    handler.handle(chatId)
                }
                "⚙️ Настройки" -> {
                    val handler = SettingsHandler(this)
                    handler.handle(chatId)
                }
                "Изменить возраст", "Изменить рост", "Изменить вес", "Изменить цель" -> {
                    val handler = SettingsHandler(this)
                    handler.handleSettingChange(chatId, text)
                }
                "⬅️ Назад в настройки" -> {
                    val handler = SettingsHandler(this)
                    handler.handle(chatId)
                }
                else -> {
                    // Интеграция с ИИ для получения ответа на вопросы
                    val aiResponse = openAIService.generateResponse(text)  // Здесь вызываем OpenAI для ответа на сообщение
                    val response = SendMessage(chatId.toString(), aiResponse)
                    execute(response)
                }
            }
        } else {
            // Если профиль не завершён, отправляем сообщение
            execute(SendMessage(chatId.toString(), "Сначала завершите создание профиля!"))
        }
    }

    private fun createMainMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("🏋️ Тренировки"))
                add(KeyboardButton("🍕 Питание"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("📊 Мой прогресс"))
                add(KeyboardButton("🤖 ИИ Ассистент"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("⚙️ Настройки"))
            }
        )
        return keyboard
    }

    private fun createTrainingMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow(listOf(
                KeyboardButton("Кардио"),
                KeyboardButton("Силовые тренировки"),
                KeyboardButton("Растяжка")
            )),
            KeyboardRow(listOf(
                KeyboardButton("⬅️ Назад в меню")
            ))
        )
        return keyboard
    }

    private fun handleAIAssistant(chatId: Long, profile: UserProfile) {
        userStates[chatId] = UserState.AI_ASSISTANT
        val message = """
            🤖 Я ваш ИИ ассистент по здоровому образу жизни! 
            Вы можете задать мне любой вопрос о:
            - тренировках
            - питании
            - здоровом образе жизни
            
            Чтобы вернуться в главное меню, нажмите "⬅️ Выход"
            
            Просто напишите ваш вопрос!
        """.trimIndent()
        val response = SendMessage(chatId.toString(), message)
        
        // Добавляем кнопку выхода
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow(listOf(KeyboardButton("⬅️ Выход")))
        )
        response.replyMarkup = keyboard
        execute(response)
    }
}
