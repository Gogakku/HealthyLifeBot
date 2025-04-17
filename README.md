# HealthyLifeBot

Telegram бот для помощи в здоровом образе жизни и фитнесе.

## Развертывание на Railway.app

1. Создайте аккаунт на [Railway.app](https://railway.app/)
2. Создайте новый проект и выберите "Deploy from GitHub repo"
3. Подключите ваш GitHub репозиторий
4. Добавьте переменные окружения в настройках проекта:
   - `TELEGRAM_BOT_TOKEN` - токен вашего Telegram бота
   - `HUGGINGFACE_API_KEY` - ключ API Hugging Face

## Локальный запуск

1. Склонируйте репозиторий
2. Создайте файл `.env` на основе `.env.example`
3. Запустите:
```bash
./gradlew build
java -jar build/libs/HealthyLifeBot.jar
```
