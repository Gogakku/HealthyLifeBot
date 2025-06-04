package org.example.org.example

import org.example.bot.handlers.*
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.example.OpenAIService
import org.example.gamification.GamificationService
import org.example.config.EnvironmentConfig
import org.example.service.DatabaseService
import org.example.service.Users

class HealthyLifeBot : TelegramLongPollingBot() {


    private val userProfiles = mutableMapOf<Long, UserProfile>()
    private val userStates = mutableMapOf<Long, UserState>()
    private val openAIService = OpenAIService(EnvironmentConfig.getRequiredProperty("OPENAI_API_KEY"))
    val gamificationService = GamificationService()
    private val savedWorkouts = mutableMapOf<Long, MutableList<String>>()
    private val lastAIWorkout = mutableMapOf<Long, String>()
    private val savedMeals = mutableMapOf<Long, MutableList<String>>()
    private val lastAIMeal = mutableMapOf<Long, String>()

    init {
        // Загружаем всех пользователей из БД при запуске
        val allUsers = DatabaseService.getAllUsers()
        allUsers.forEach { row ->
            val chatId = row[Users.chatId]
            val name = row[Users.name]
            val age = row[Users.age]
            val weight = row[Users.weight]
            val height = row[Users.height]
            val goal = row[Users.goal]
            val state = row[Users.state]
            userProfiles[chatId] = UserProfile(
                name = name,
                age = age,
                weight = weight,
                height = height,
                goal = goal
            )
            userStates[chatId] = try { UserState.valueOf(state) } catch (e: Exception) { UserState.COMPLETED }
        }
    }

    fun getUserProfile(chatId: Long): UserProfile? = userProfiles[chatId]

    fun setUserState(chatId: Long, state: UserState) {
        userStates[chatId] = state
        DatabaseService.updateUserState(chatId, state.name)
    }

    override fun getBotUsername(): String = "HealthyLifeBot"
    override fun getBotToken(): String = EnvironmentConfig.getRequiredProperty("TELEGRAM_BOT_TOKEN")

    override fun onUpdateReceived(update: Update) {
        val message = update.message ?: return
        val chatId = message.chatId
        val text = message.text ?: return

        // Если пользователя нет в памяти, пробуем загрузить из БД
        if (!userProfiles.containsKey(chatId)) {
            val row = DatabaseService.getUser(chatId)
            if (row != null) {
                userProfiles[chatId] = UserProfile(
                    name = row[Users.name],
                    age = row[Users.age],
                    weight = row[Users.weight],
                    height = row[Users.height],
                    goal = row[Users.goal]
                )
                userStates[chatId] = try { UserState.valueOf(row[Users.state]) } catch (e: Exception) { UserState.COMPLETED }
            }
        }

        if (text == "/start") {
            val name = message.from?.firstName ?: "друг"
            userProfiles[chatId] = UserProfile(name = name)
            setUserState(chatId, UserState.AWAITING_AGE)

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
                        setUserState(chatId, UserState.AWAITING_HEIGHT)
                        execute(SendMessage(chatId.toString(), "Отлично! Теперь укажи свой рост в сантиметрах:"))
                    } else {
                        execute(SendMessage(chatId.toString(), "Пожалуйста, введи корректный возраст (от 10 до 100 лет)."))
                    }
                }

                UserState.AWAITING_HEIGHT -> {
                    val height = text.toIntOrNull()
                    if (height != null && height in 100..250) {
                        profile.height = height
                        setUserState(chatId, UserState.AWAITING_WEIGHT)
                        execute(SendMessage(chatId.toString(), "Спасибо! А теперь укажи свой вес в кг:"))
                    } else {
                        execute(SendMessage(chatId.toString(), "Введи корректный рост (например, 175):"))
                    }
                }

                UserState.AWAITING_WEIGHT -> {
                    val weight = text.toIntOrNull()
                    if (weight != null && weight in 30..300) {
                        profile.weight = weight
                        setUserState(chatId, UserState.AWAITING_GOAL)
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
                        // Сохраняем профиль в БД
                        DatabaseService.addOrUpdateUser(
                            chatId = chatId,
                            name = profile.name,
                            age = profile.age ?: 0,
                            weight = profile.weight ?: 0,
                            height = profile.height ?: 0,
                            goal = profile.goal
                        )
                        setUserState(chatId, UserState.COMPLETED)
                        gamificationService.resetLives(chatId)
                        val lives = gamificationService.getUserLives(chatId)
                        val heartsDisplay = "❤️".repeat(lives.lives)
                        
                        val summary = """
                            🎉 Отлично, вот твой профиль:
                            👤 Имя: ${profile.name}
                            📅 Возраст: ${profile.age}
                            📏 Рост: ${profile.height} см
                            ⚖️ Вес: ${profile.weight} кг
                            🎯 Цель: ${profile.goal}
                            ❤️ Жизни: $heartsDisplay
                            
                            🎮 Система геймификации активирована!
                            • У тебя есть 3 жизни на неделю
                            • Пропуск тренировки = -1 жизнь
                            • Твой прогресс и достижения доступны в меню "📊 Мой прогресс"
                            
                            Удачи в достижении твоих целей! 💪
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
                    val profile = userProfiles[chatId]
                    if (text == "⬅️ Назад в меню") {
                        setUserState(chatId, UserState.COMPLETED)
                        val response = SendMessage(chatId.toString(), "Вы вернулись в главное меню")
                        response.replyMarkup = createMainMenu()
                        execute(response)
                    } else {
                        // Формируем prompt с учётом профиля пользователя
                        val userInfo = if (profile != null) {
                            "Пользователь: ${profile.name}, возраст: ${profile.age ?: "не указан"}, рост: ${profile.height ?: "не указан"} см, вес: ${profile.weight ?: "не указан"} кг, цель: ${profile.goal.ifBlank { "не указана" }}."
                        } else {
                            "Пользователь не заполнил профиль."
                        }
                        val prompt = "$userInfo Вопрос: $text"
                        val aiResponse = openAIService.generateResponse(prompt)
                        // Обрезаем ответ до 3900 символов для гарантии, что Telegram не обрежет
                        val safeResponse = if (aiResponse.length > 3900) aiResponse.substring(0, 3900) else aiResponse
                        val response = SendMessage(chatId.toString(), safeResponse)
                        val keyboard = ReplyKeyboardMarkup()
                        keyboard.resizeKeyboard = true
                        keyboard.keyboard = listOf(
                            KeyboardRow(listOf(KeyboardButton("⬅️ Назад в меню")))
                        )
                        response.replyMarkup = keyboard
                        execute(response)
                    }
                }

                UserState.SETTINGS_AGE -> {
                    val age = text.toIntOrNull()
                    if (age != null && age in 10..100) {
                        profile.age = age
                        setUserState(chatId, UserState.COMPLETED)
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
                        setUserState(chatId, UserState.COMPLETED)
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
                        setUserState(chatId, UserState.COMPLETED)
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
                        setUserState(chatId, UserState.COMPLETED)
                        val handler = SettingsHandler(this)
                        handler.handle(chatId)
                    } else {
                        execute(SendMessage(chatId.toString(), "Выберите одну из предложенных целей."))
                    }
                }
                UserState.PROGRESS_MENU -> {
                    when (text.trim()) {
                        "✅ Выполнил сегодня тренировку" -> {
                            val result = gamificationService.markWorkoutCompleted(chatId)
                            execute(SendMessage(chatId.toString(), result))
                            val handler = org.example.bot.handlers.ProgressHandler(this)
                            handler.handle(chatId)
                            return
                        }
                        "❌ Пропустил тренировку" -> {
                            val result = gamificationService.markWorkoutMissed(chatId)
                            execute(SendMessage(chatId.toString(), result))
                            val handler = org.example.bot.handlers.ProgressHandler(this)
                            handler.handle(chatId)
                            return
                        }
                        "⬅️ Назад в меню" -> {
                            setUserState(chatId, UserState.COMPLETED)
                            val response = SendMessage(chatId.toString(), "Главное меню:")
                            response.replyMarkup = createMainMenu()
                            execute(response)
                            return
                        }
                        else -> {
                            execute(SendMessage(chatId.toString(), "Пожалуйста, используйте кнопки меню ниже."))
                            return
                        }
                    }
                }

                else -> {
                    handleUserInput(chatId, text)
                }
            }
            return
        }

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
                "Сохранённые тренировки" -> {
                    val workouts = savedWorkouts[chatId]
                    val msg = if (workouts.isNullOrEmpty()) "У вас нет сохранённых тренировок." else workouts.joinToString("\n\n")
                    execute(SendMessage(chatId.toString(), msg))
                }
                "🍕 Питание" -> {
                    val response = SendMessage(chatId.toString(), "Выберите действие:")
                    response.replyMarkup = createNutritionMenu()
                    execute(response)
                }
                "Пример питания" -> {
                    val handler = NutritionHandler(this)
                    handler.handle(chatId)
                }
                "Сохранённое питание" -> {
                    val meals = savedMeals[chatId]
                    val msg = if (meals.isNullOrEmpty()) "У вас нет сохранённых планов питания." else meals.joinToString("\n\n")
                    execute(SendMessage(chatId.toString(), msg))
                }
                "🤖 ИИ Ассистент" -> {
                    val aiWelcome = """
                        🤖 Я ваш ИИ ассистент по здоровому образу жизни!
                        Вы можете задать мне любой вопрос о:
                        - тренировках
                        - питании
                        - здоровом образе жизни
                        
                        Если вам понравилась сгенерированная тренировка, нажмите "Сохранить тренировку", чтобы добавить её в раздел "Сохранённые тренировки".
                        Если вам понравился план питания, нажмите "Сохранить план питания", чтобы добавить его в раздел "Сохранённое питание".
                        
                        Чтобы вернуться в главное меню, нажмите "⬅️ Назад в меню"
                        
                        Просто напишите ваш вопрос!
                    """.trimIndent()
                    val response = SendMessage(chatId.toString(), aiWelcome)
                    response.replyMarkup = createAIAssistantMenu()
                    execute(response)
                }
                "Сохранить тренировку" -> {
                    val last = lastAIWorkout[chatId]
                    if (last != null) {
                        savedWorkouts.getOrPut(chatId) { mutableListOf() }.add(last)
                        execute(SendMessage(chatId.toString(), "Тренировка сохранена!"))
                    } else {
                        execute(SendMessage(chatId.toString(), "Нет тренировки для сохранения."))
                    }
                }
                "Сохранить план питания" -> {
                    val last = lastAIMeal[chatId]
                    if (last != null) {
                        savedMeals.getOrPut(chatId) { mutableListOf() }.add(last)
                        execute(SendMessage(chatId.toString(), "План питания сохранён!"))
                    } else {
                        execute(SendMessage(chatId.toString(), "Нет плана питания для сохранения."))
                    }
                }
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
                "⬅️ Назад в меню" -> {
                    val response = SendMessage(chatId.toString(), "Главное меню:")
                    response.replyMarkup = createMainMenu()
                    execute(response)
                }
                else -> {
                    val aiResponse = openAIService.generateResponse(text)
                    lastAIWorkout[chatId] = aiResponse
                    lastAIMeal[chatId] = aiResponse
                    val parts = aiResponse.chunked(4096)
                    for (part in parts) {
                        val response = SendMessage(chatId.toString(), part)
                        response.replyMarkup = createAIAssistantMenu()
                        execute(response)
                    }
                }
            }
        } else {
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
            KeyboardRow().apply {
                add(KeyboardButton("Кардио"))
                add(KeyboardButton("Силовые тренировки"))
                add(KeyboardButton("Растяжка"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("Сохранённые тренировки"))
                add(KeyboardButton("⬅️ Назад в меню"))
            }
        )
        return keyboard
    }

    private fun createNutritionMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("Пример питания"))
                add(KeyboardButton("Сохранённое питание"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("⬅️ Назад в меню"))
            }
        )
        return keyboard
    }

    private fun createAIAssistantMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("Сохранить тренировку"))
                add(KeyboardButton("Сохранить план питания"))
                add(KeyboardButton("⬅️ Назад в меню"))
            }
        )
        return keyboard
    }

    private fun handleAIAssistant(chatId: Long, profile: UserProfile) {
        setUserState(chatId, UserState.AI_ASSISTANT)
        val message = """
            🤖 Я ваш ИИ ассистент по здоровому образу жизни! 
            Вы можете задать мне любой вопрос о:
            - тренировках
            - питании
            - здоровом образе жизни
            
            Чтобы вернуться в главное меню, нажмите "⬅️ Назад в меню"
            
            Просто напишите ваш вопрос!
        """.trimIndent()
        val response = SendMessage(chatId.toString(), message)
        
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow(listOf(KeyboardButton("⬅️ Назад в меню")))
        )
        response.replyMarkup = keyboard
        execute(response)
    }

    private fun showProfile(chatId: Long) {
        val profile = userProfiles[chatId] ?: return
        val lives = gamificationService.getUserLives(chatId)
        val heartsDisplay = "❤️".repeat(lives.lives)
        
        val message = """
            📊 Твой профиль:
            👤 Имя: ${profile.name}
            📅 Возраст: ${profile.age}
            📏 Рост: ${profile.height} см
            ⚖️ Вес: ${profile.weight} кг
            🎯 Цель: ${profile.goal}
            ❤️ Жизни: $heartsDisplay
        """.trimIndent()
        
        execute(SendMessage(chatId.toString(), message))
    }
}
