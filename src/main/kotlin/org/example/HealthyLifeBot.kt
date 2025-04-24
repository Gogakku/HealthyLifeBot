package org.example.org.example

import org.example.bot.handlers.*
import org.telegram.telegrambots.bots.TelegramWebhookBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.example.OpenAIService

class HealthyLifeBot : TelegramWebhookBot() {

    private val userProfiles = mutableMapOf<Long, UserProfile>()
    private val userStates = mutableMapOf<Long, UserState>()
    private val botToken: String = System.getenv("BOT_TOKEN") ?: throw IllegalStateException("BOT_TOKEN не установлен")
    private val openAIService = OpenAIService(System.getenv("OPENAI_API_KEY") ?: throw IllegalStateException("OPENAI_API_KEY не установлен"))

    fun getUserProfile(chatId: Long): UserProfile? = userProfiles[chatId]

    fun setUserState(chatId: Long, state: UserState) {
        userStates[chatId] = state
    }

    override fun getBotUsername(): String = "HealthyLifeBot"
    override fun getBotToken(): String = botToken
    override fun getBotPath(): String = "webhook"

    override fun onWebhookUpdateReceived(update: Update): BotApiMethod<*>? {
        val message = update.message ?: return null
        val chatId = message.chatId
        val text = message.text ?: return null

        if (text == "/start") {
            val name = message.from?.firstName ?: "друг"
            userProfiles[chatId] = UserProfile(name = name)
            userStates[chatId] = UserState.AWAITING_AGE

            return SendMessage(chatId.toString(), "👋 Привет, $name! Давай начнём с твоего возраста. Сколько тебе лет?")
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
                        return SendMessage(chatId.toString(), "Отлично! Теперь укажи свой рост в сантиметрах:")
                    } else {
                        return SendMessage(chatId.toString(), "Пожалуйста, введи корректный возраст (от 10 до 100 лет).")
                    }
                }

                UserState.AWAITING_HEIGHT -> {
                    val height = text.toIntOrNull()
                    if (height != null && height in 100..250) {
                        profile.height = height
                        userStates[chatId] = UserState.AWAITING_WEIGHT
                        return SendMessage(chatId.toString(), "Спасибо! А теперь укажи свой вес в кг:")
                    } else {
                        return SendMessage(chatId.toString(), "Введи корректный рост (например, 175):")
                    }
                }

                UserState.AWAITING_WEIGHT -> {
                    val weight = text.toIntOrNull()
                    if (weight != null && weight in 30..300) {
                        profile.weight = weight
                        userStates[chatId] = UserState.AWAITING_GOAL
                        return SendMessage(chatId.toString(), """
                            Почти готово! Теперь выбери свою цель:
                            1. Похудеть
                            2. Набрать мышечную массу
                            3. Поддерживать форму
                        """.trimIndent())
                    } else {
                        return SendMessage(chatId.toString(), "Введи корректный вес (например, 70):")
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
                        return response
                    } else {
                        return SendMessage(chatId.toString(), "Выбери одну из целей (1, 2 или 3):")
                    }
                }

                UserState.COMPLETED -> {
                    return handleUserInput(chatId, text)
                }

                UserState.AI_ASSISTANT -> {
                    if (text == "⬅️ Выход") {
                        userStates[chatId] = UserState.COMPLETED
                        val response = SendMessage(chatId.toString(), "Вы вернулись в главное меню")
                        response.replyMarkup = createMainMenu()
                        return response
                    } else {
                        val aiResponse = openAIService.generateResponse(text)  
                        val response = SendMessage(chatId.toString(), aiResponse)
                        val keyboard = ReplyKeyboardMarkup()
                        keyboard.resizeKeyboard = true
                        keyboard.keyboard = listOf(
                            KeyboardRow(listOf(KeyboardButton("⬅️ Выход")))
                        )
                        response.replyMarkup = keyboard
                        return response
                    }
                }

                UserState.SETTINGS_AGE -> {
                    val age = text.toIntOrNull()
                    if (age != null && age in 10..100) {
                        profile.age = age
                        userStates[chatId] = UserState.COMPLETED
                        val handler = SettingsHandler(this)
                        return handler.handle(chatId)
                    } else {
                        return SendMessage(chatId.toString(), "Пожалуйста, введите корректный возраст (от 10 до 100 лет).")
                    }
                }

                UserState.SETTINGS_HEIGHT -> {
                    val height = text.toIntOrNull()
                    if (height != null && height in 100..250) {
                        profile.height = height
                        userStates[chatId] = UserState.COMPLETED
                        val handler = SettingsHandler(this)
                        return handler.handle(chatId)
                    } else {
                        return SendMessage(chatId.toString(), "Введите корректный рост (от 100 до 250 см).")
                    }
                }

                UserState.SETTINGS_WEIGHT -> {
                    val weight = text.toIntOrNull()
                    if (weight != null && weight in 30..300) {
                        profile.weight = weight
                        userStates[chatId] = UserState.COMPLETED
                        val handler = SettingsHandler(this)
                        return handler.handle(chatId)
                    } else {
                        return SendMessage(chatId.toString(), "Введите корректный вес (от 30 до 300 кг).")
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
                        return handler.handle(chatId)
                    } else {
                        return SendMessage(chatId.toString(), "Выберите одну из предложенных целей.")
                    }
                }
            }

            return null
        }

        return handleUserInput(chatId, text)
    }

    private fun handleUserInput(chatId: Long, text: String): BotApiMethod<*>? {
        val profile = userProfiles[chatId]

        if (profile?.goal != null) {
            when (text) {
                "🏋️ Тренировки" -> {
                    val response = SendMessage(chatId.toString(), "Выбери тип тренировки:")
                    response.replyMarkup = createTrainingMenu()
                    return response
                }
                "Кардио", "Силовые тренировки", "Растяжка" -> {
                    val handler = TrainingHandler(this)
                    return handler.handleTrainingSelection(chatId, text)
                }
                "🍕 Питание" -> {
                    val handler = NutritionHandler(this)
                    return handler.handle(chatId)
                }
                "🛌 Сон" -> {
                    val handler = SleepHandler(this)
                    return handler.handle(chatId)
                }
                "📅 Распорядок дня" -> {
                    val handler = ScheduleHandler(this)
                    return handler.handle(chatId)
                }
                "🎯 Цели" -> {
                    val handler = GoalsHandler(this)
                    return handler.handle(chatId)
                }
                "❤️ Мотивация" -> {
                    val handler = MotivationHandler(this)
                    return handler.handle(chatId)
                }
                "⬅️ Назад в меню" -> {
                    val response = SendMessage(chatId.toString(), "Главное меню:")
                    response.replyMarkup = createMainMenu()
                    return response
                }
                "🤖 ИИ Ассистент" -> return handleAIAssistant(chatId, profile)
                "📊 Мой прогресс" -> {
                    val handler = ProgressHandler(this)
                    return handler.handle(chatId)
                }
                "⚙️ Настройки" -> {
                    val handler = SettingsHandler(this)
                    return handler.handle(chatId)
                }
                "Изменить возраст", "Изменить рост", "Изменить вес", "Изменить цель" -> {
                    val handler = SettingsHandler(this)
                    return handler.handleSettingChange(chatId, text)
                }
                "⬅️ Назад в настройки" -> {
                    val handler = SettingsHandler(this)
                    return handler.handle(chatId)
                }
                else -> {
                    val aiResponse = openAIService.generateResponse(text)  
                    val response = SendMessage(chatId.toString(), aiResponse)
                    return response
                }
            }
        } else {
            return SendMessage(chatId.toString(), "Сначала завершите создание профиля!")
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

    private fun handleAIAssistant(chatId: Long, profile: UserProfile): BotApiMethod<*> {
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
        
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow(listOf(KeyboardButton("⬅️ Выход")))
        )
        response.replyMarkup = keyboard
        return response
    }
}
