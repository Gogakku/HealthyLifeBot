package org.example.config

import java.io.File
import java.util.Properties
import kotlin.system.exitProcess

class EnvironmentConfig {
    companion object {
        private val properties = Properties()
        
        init {
            val envFile = File(".env")
            if (envFile.exists()) {
                envFile.inputStream().use { input ->
                    properties.load(input)
                }
                
                // Устанавливаем системные свойства из .env
                properties.forEach { (key, value) ->
                    System.setProperty(key.toString(), value.toString())
                }
            }
        }
        
        fun getRequiredProperty(key: String): String {
            val value = System.getenv(key) ?: System.getProperty(key)
            return value ?: run {
                println("Ошибка: переменная окружения '$key' не найдена")
                println("Пожалуйста, убедитесь, что в файле .env есть следующие переменные:")
                println("TELEGRAM_BOT_TOKEN=ваш_токен_бота")
                println("OPENAI_API_KEY=ваш_api_key")
                exitProcess(1)
            }
        }
    }
}
