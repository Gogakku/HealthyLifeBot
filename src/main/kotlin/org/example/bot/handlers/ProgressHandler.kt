package org.example.bot.handlers

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.example.gamification.AchievementType
import org.example.gamification.Achievement
import org.example.gamification.UserLives
import org.example.org.example.HealthyLifeBot
import org.example.org.example.UserProfile
import org.example.org.example.UserState

class ProgressHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long) {
        bot.setUserState(chatId, org.example.org.example.UserState.PROGRESS_MENU)
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            bot.execute(SendMessage(chatId.toString(), "Сначала необходимо создать профиль!"))
            return
        }

        val lives = bot.gamificationService.getUserLives(chatId)
        val heartsDisplay = "❤️".repeat(lives.lives)
        
        val message = buildString {
            appendLine("📊 Ваш прогресс:")
            appendLine()
            appendLine("👤 Профиль:")
            appendLine("Имя: ${profile.name}")
            appendLine("Возраст: ${profile.age}")
            appendLine("Рост: ${profile.height} см")
            appendLine("Вес: ${profile.weight} кг")
            appendLine("Цель: ${profile.goal}")
            appendLine()
            appendLine("❤️ Жизни: $heartsDisplay")
            appendLine()
            appendLine("\uD83D\uDC9B Как работает система геймификации:")
            appendLine("- У вас есть 3 жизни (сердца).\n- Каждый раз, когда вы пропускаете тренировку, теряете 1 жизнь.\n- Если выполните тренировку — жизни сохраняются.\n- Когда жизни заканчиваются, новая неделя — новый шанс!\n- За регулярные тренировки можно получить достижения.")
            if (lives.achievements.isNotEmpty()) {
                appendLine()
                appendLine("\uD83C\uDFC6 Достижения:")
                for (achievement in lives.achievements) {
                    val emoji = when (achievement.type) {
                        AchievementType.PERFECT_WEEK -> "⭐"
                        AchievementType.COMEBACK_KING -> "👑"
                        AchievementType.PERSISTENT_USER -> "🎯"
                        else -> "🏆"
                    }
                    val achievementName = achievement.type.toString()
                        .replace('_', ' ')
                        .lowercase()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    appendLine("$emoji $achievementName")
                }
            }
        }

        val response = SendMessage(chatId.toString(), message)
        response.replyMarkup = createProgressMenu()
        bot.execute(response)
    }

    private fun createProgressMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true
        keyboard.keyboard = listOf(
            KeyboardRow().apply {
                add(KeyboardButton("✅ Выполнил сегодня тренировку"))
                add(KeyboardButton("❌ Пропустил тренировку"))
            },
            KeyboardRow().apply {
                add(KeyboardButton("⬅️ Назад в меню"))
            }
        )
        return keyboard
    }
}
