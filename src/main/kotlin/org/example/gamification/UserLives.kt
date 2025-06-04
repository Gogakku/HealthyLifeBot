package org.example.gamification

import java.time.LocalDateTime
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*

data class UserLives(
    val userId: Long,
    var lives: Int = 3,
    var lastReset: LocalDateTime = LocalDateTime.now(),
    var achievements: MutableList<Achievement> = mutableListOf(),
    var lastWorkoutDate: LocalDate? = null,
    var lastWorkoutType: WorkoutType? = null
)

data class Achievement(
    val type: AchievementType,
    val dateEarned: LocalDateTime = LocalDateTime.now()
)

enum class AchievementType {
    PERFECT_WEEK,      // Неделя без пропусков
    COMEBACK_KING,     // Возвращение после потери всех жизней
    PERSISTENT_USER,   // Месяц активности
    FIRST_WORKOUT      // Первая успешная тренировка
}

enum class WorkoutType {
    COMPLETED, MISSED
}
