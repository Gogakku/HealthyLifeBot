package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot // Путь импорта должен быть правильным
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class TrainingHandler(private val bot: HealthyLifeBot) {

    fun handleTrainingSelection(chatId: Long, text: String) {
        when (text) {
            "1", "Кардио" -> {
                val cardioPlan = """
                    Кардио тренировка на 30 минут:
                    - 5 минут разминка (легкая ходьба)
                    - 20 минут интенсивное кардио (бег, велотренажер, эллипс)
                    - 5 минут заминка (легкая ходьба)
                """.trimIndent()
                bot.execute(SendMessage(chatId.toString(), cardioPlan))
            }
            "2", "Силовые тренировки" -> {
                val strengthPlan = """
                    Силовая тренировка для всего тела:
                    - Приседания (3 подхода по 12 повторений)
                    - Жим лежа (3 подхода по 12 повторений)
                    - Подтягивания (3 подхода по 10 повторений)
                    - Становая тяга (3 подхода по 10 повторений)
                """.trimIndent()
                bot.execute(SendMessage(chatId.toString(), strengthPlan))
            }
            "3", "Растяжка" -> {
                val stretchingPlan = """
                    Растяжка для всего тела:
                    - Наклоны вперед (3 подхода по 30 секунд)
                    - Растяжка ног (3 подхода по 30 секунд)
                    - Растяжка спины (3 подхода по 30 секунд)
                """.trimIndent()
                bot.execute(SendMessage(chatId.toString(), stretchingPlan))
            }
            else -> {
                bot.execute(SendMessage(chatId.toString(), "Пожалуйста, выбери один из вариантов: Кардио, Силовые тренировки или Растяжка"))
            }
        }
    }
}
