package org.example.gamification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class GamificationService {
    private val userLives = mutableMapOf<Long, UserLives>()

    fun getUserLives(userId: Long): UserLives {
        return userLives.getOrPut(userId) { UserLives(userId) }
    }

    fun decrementLife(userId: Long): String {
        val user = getUserLives(userId)
        if (user.lives > 0) {
            user.lives--
            return when (user.lives) {
                0 -> "❌ К сожалению, вы потеряли все жизни! Но не расстраивайтесь, новая неделя - новый старт! " +
                     "Продолжайте стремиться к своим целям! 💪"
                1 -> "⚠️ Осталась последняя жизнь! Будьте внимательны!"
                else -> "💔 Вы потеряли жизнь. Осталось жизней: ${user.lives}"
            }
        }
        return "У вас не осталось жизней. Дождитесь следующей недели для новой попытки!"
    }

    fun resetLives(userId: Long) {
        val user = getUserLives(userId)
        user.lives = 3
        user.lastReset = LocalDateTime.now()
    }

    fun checkAndAwardAchievements(userId: Long) {
        val user = getUserLives(userId)
        
        // Проверяем достижение за идеальную неделю
        if (user.lives == 3 && 
            ChronoUnit.DAYS.between(user.lastReset, LocalDateTime.now()) >= 7) {
            awardAchievement(userId, AchievementType.PERFECT_WEEK)
        }

        // Проверяем достижение за возвращение
        if (user.lives == 3 && user.achievements.any { it.type == AchievementType.COMEBACK_KING }) {
            awardAchievement(userId, AchievementType.COMEBACK_KING)
        }
    }

    private fun awardAchievement(userId: Long, type: AchievementType) {
        val user = getUserLives(userId)
        if (user.achievements.none { it.type == type }) {
            user.achievements.add(Achievement(type))
        }
    }

    fun getAchievementsMessage(userId: Long): String {
        val user = getUserLives(userId)
        if (user.achievements.isEmpty()) {
            return "У вас пока нет достижений. Продолжайте в том же духе, и они обязательно появятся! 🌟"
        }

        return buildString {
            appendLine("🏆 Ваши достижения:")
            user.achievements.forEach { achievement ->
                val emoji = when (achievement.type) {
                    AchievementType.PERFECT_WEEK -> "⭐"
                    AchievementType.COMEBACK_KING -> "👑"
                    AchievementType.PERSISTENT_USER -> "🎯"
                    AchievementType.FIRST_WORKOUT -> "🥇"
                }
                appendLine("$emoji ${achievement.type.name.replace('_', ' ')}")
            }
        }
    }

    fun markWorkoutCompleted(userId: Long): String {
        val user = getUserLives(userId)
        val today = LocalDate.now()
        if (user.lastWorkoutDate == today) {
            return "Сегодня вы уже отметили тренировку! Можно только одну отметку в день."
        }
        user.lastWorkoutDate = today
        user.lastWorkoutType = WorkoutType.COMPLETED
        // Достижение за первую тренировку
        if (user.achievements.none { it.type.name == "FIRST_WORKOUT" }) {
            user.achievements.add(Achievement(AchievementType.FIRST_WORKOUT))
            return "Поздравляем с первой тренировкой! 🥇 Вы получили достижение!"
        }
        return "Отлично! Тренировка отмечена. Продолжай в том же духе! 💪"
    }

    fun markWorkoutMissed(userId: Long): String {
        val user = getUserLives(userId)
        val today = LocalDate.now()
        if (user.lastWorkoutDate == today) {
            return "Сегодня вы уже отметили тренировку! Можно только одну отметку в день."
        }
        user.lastWorkoutDate = today
        user.lastWorkoutType = WorkoutType.MISSED
        if (user.lives > 0) {
            user.lives--
        }
        return when (user.lives) {
            0 -> "❌ К сожалению, вы потеряли все жизни! Но не расстраивайтесь, новая неделя - новый старт! Продолжайте стремиться к своим целям! 💪"
            1 -> "⚠️ Осталась последняя жизнь! Будьте внимательны!"
            else -> "💔 Вы потеряли жизнь. Осталось жизней: ${user.lives}"
        }
    }
}
