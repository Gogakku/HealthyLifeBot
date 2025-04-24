package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class TrainingHandler(private val bot: HealthyLifeBot) {

    fun handleTrainingSelection(chatId: Long, trainingType: String): BotApiMethod<*> {
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            return SendMessage(chatId.toString(), "Сначала необходимо создать профиль!")
        }

        val message = when (trainingType) {
            "Кардио" -> getCardioTraining(profile.goal)
            "Силовые тренировки" -> getStrengthTraining(profile.goal)
            "Растяжка" -> getStretchingExercises()
            else -> "Неизвестный тип тренировки"
        }

        val response = SendMessage(chatId.toString(), message)
        response.replyMarkup = createTrainingMenu()
        return response
    }

    private fun getCardioTraining(goal: String?): String {
        return when (goal) {
            "Похудеть" -> """
                🏃‍♂️ Кардио тренировка для сжигания жира:
                
                1. Разминка (5-10 минут):
                   - Ходьба на месте
                   - Легкий джоггинг
                   - Вращения руками и ногами
                
                2. Основная часть (30-40 минут):
                   - Интервальный бег (1 мин быстро, 2 мин медленно) x 10
                   - Прыжки на скакалке (3 подхода по 2 минуты)
                   - Берпи (3 подхода по 30 секунд)
                
                3. Заминка (5-10 минут):
                   - Ходьба
                   - Растяжка
                
                💡 Совет: Старайтесь поддерживать пульс в пределах 65-75% от максимального
            """.trimIndent()
            
            "Набрать мышечную массу" -> """
                🏃‍♂️ Кардио для набора массы:
                
                1. Разминка (5-7 минут):
                   - Ходьба на месте
                   - Вращения суставами
                
                2. Основная часть (15-20 минут):
                   - Спринты (30 сек) с отдыхом (90 сек) x 6
                   - Плиометрические упражнения
                
                3. Заминка (5 минут):
                   - Легкая ходьба
                   - Растяжка
                
                💡 Совет: Не перегружайтесь кардио при наборе массы
            """.trimIndent()
            
            else -> """
                🏃‍♂️ Общая кардио тренировка:
                
                1. Разминка (7-10 минут):
                   - Ходьба на месте
                   - Легкий джоггинг
                   - Суставная гимнастика
                
                2. Основная часть (20-30 минут):
                   - Чередование быстрой и медленной ходьбы
                   - Прыжки на скакалке
                   - Бег трусцой
                
                3. Заминка (5-7 минут):
                   - Ходьба
                   - Растяжка
                
                💡 Совет: Следите за своим дыханием
            """.trimIndent()
        }
    }

    private fun getStrengthTraining(goal: String?): String {
        return when (goal) {
            "Похудеть" -> """
                🏋️‍♂️ Силовая тренировка для сжигания жира:
                
                1. Разминка (10 минут):
                   - Суставная гимнастика
                   - Легкий кардио
                
                2. Круговая тренировка (3 круга):
                   - Приседания (20 повторений)
                   - Отжимания (10-15 повторений)
                   - Выпады (15 на каждую ногу)
                   - Планка (45 секунд)
                   - Берпи (10 повторений)
                   
                   Отдых между кругами: 2 минуты
                
                3. Заминка:
                   - Растяжка (10 минут)
                
                💡 Совет: Минимальный отдых между упражнениями
            """.trimIndent()
            
            "Набрать мышечную массу" -> """
                🏋️‍♂️ Силовая тренировка для набора массы:
                
                1. Разминка (10 минут):
                   - Суставная гимнастика
                   - Разогрев с легкими весами
                
                2. Основная часть:
                   - Приседания (4x8-12)
                   - Жим лежа (4x8-12)
                   - Тяга в наклоне (4x8-12)
                   - Жим плеч (3x8-12)
                   - Подъем на бицепс (3x12)
                   - Трицепс (3x12)
                   
                   Отдых между подходами: 2-3 минуты
                
                3. Заминка:
                   - Растяжка (5-10 минут)
                
                💡 Совет: Фокусируйтесь на технике выполнения
            """.trimIndent()
            
            else -> """
                🏋️‍♂️ Общая силовая тренировка:
                
                1. Разминка (10 минут):
                   - Суставная гимнастика
                   - Легкий кардио
                
                2. Основная часть:
                   - Приседания (3x15)
                   - Отжимания (3x10)
                   - Тяга в наклоне с гантелями (3x12)
                   - Планка (3x45 сек)
                   
                   Отдых между подходами: 1-2 минуты
                
                3. Заминка:
                   - Растяжка (10 минут)
                
                💡 Совет: Следите за правильной техникой
            """.trimIndent()
        }
    }

    private fun getStretchingExercises(): String {
        return """
            🧘‍♂️ Программа растяжки:
            
            1. Разминка (5 минут):
               - Ходьба на месте
               - Вращения суставами
            
            2. Основной комплекс (удерживать каждое положение 30-60 секунд):
               - Наклоны к прямым ногам
               - Бабочка
               - Растяжка квадрицепсов
               - Растяжка подколенных сухожилий
               - Растяжка икр
               - Растяжка спины
               - Скручивания позвоночника
            
            3. Заключительная часть:
               - Глубокое дыхание
               - Расслабление
            
            💡 Совет: Не делайте резких движений, растягивайтесь плавно
        """.trimIndent()
    }

    private fun createTrainingMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("Кардио"))
        row1.add(KeyboardButton("Силовые тренировки"))

        val row2 = KeyboardRow()
        row2.add(KeyboardButton("Растяжка"))
        row2.add(KeyboardButton("⬅️ Назад в меню"))

        keyboard.keyboard = listOf(row1, row2)
        return keyboard
    }
}
