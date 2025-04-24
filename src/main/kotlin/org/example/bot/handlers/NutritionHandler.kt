package org.example.bot.handlers

import org.example.org.example.HealthyLifeBot
import org.telegram.telegrambots.meta.api.methods.BotApiMethod
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class NutritionHandler(private val bot: HealthyLifeBot) {
    fun handle(chatId: Long): BotApiMethod<*> {
        val profile = bot.getUserProfile(chatId)
        if (profile == null) {
            return SendMessage(chatId.toString(), "Сначала необходимо создать профиль!")
        }

        val nutritionPlan = when (profile.goal) {
            "Похудеть" -> """
                🥗 План питания для снижения веса:
                
                Общие рекомендации:
                - Создайте дефицит калорий 500-700 ккал
                - Ешьте больше белка и клетчатки
                - Пейте достаточно воды
                
                Примерное меню:
                
                🌅 Завтрак:
                - Овсянка с ягодами
                - Белковый омлет
                - Зеленый чай
                
                🌞 Обед:
                - Куриная грудка на гриле
                - Салат из свежих овощей
                - Бурый рис (небольшая порция)
                
                🌙 Ужин:
                - Рыба на пару
                - Овощи на гриле
                
                🍎 Перекусы:
                - Яблоко
                - Горсть орехов
                - Протеиновый коктейль
                
                💡 Совет: Старайтесь есть медленно и не пропускать приемы пищи
            """.trimIndent()
            
            "Набрать мышечную массу" -> """
                🍖 План питания для набора мышечной массы:
                
                Общие рекомендации:
                - Профицит калорий 300-500 ккал
                - Высокое потребление белка (2г на кг веса)
                - Достаточно сложных углеводов
                
                Примерное меню:
                
                🌅 Завтрак:
                - Овсянка с бананом и медом
                - Яичный белок (4-6 шт)
                - Цельнозерновой тост
                - Протеиновый коктейль
                
                🌞 Обед:
                - Куриная грудка (200г)
                - Бурый рис (150г)
                - Брокколи
                - Оливковое масло
                
                🌙 Ужин:
                - Лосось или говядина (200г)
                - Сладкий картофель
                - Овощной салат
                
                🍌 Перекусы:
                - Творог с орехами
                - Банан с арахисовой пастой
                - Протеиновый коктейль
                
                💡 Совет: Ешьте каждые 3-4 часа
            """.trimIndent()
            
            else -> """
                🥑 План сбалансированного питания:
                
                Общие рекомендации:
                - Сбалансированное потребление калорий
                - Разнообразное питание
                - Регулярные приемы пищи
                
                Примерное меню:
                
                🌅 Завтрак:
                - Цельнозерновая каша
                - Омлет с овощами
                - Фрукты
                
                🌞 Обед:
                - Нежирное мясо или рыба
                - Овощной салат
                - Крупа или картофель
                
                🌙 Ужин:
                - Индейка или рыба
                - Овощи на пару
                - Небольшая порция углеводов
                
                🍎 Перекусы:
                - Фрукты
                - Орехи
                - Йогурт
                
                💡 Совет: Придерживайтесь режима питания
            """.trimIndent()
        }

        val response = SendMessage(chatId.toString(), nutritionPlan)
        response.replyMarkup = createNutritionMenu()
        return response
    }

    private fun createNutritionMenu(): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.resizeKeyboard = true

        val row1 = KeyboardRow()
        row1.add(KeyboardButton("⬅️ Назад в меню"))

        keyboard.keyboard = listOf(row1)
        return keyboard
    }
}
