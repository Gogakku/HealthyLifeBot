# HealthyLifeBot

Telegram бот для здорового образа жизни, разработанный на Kotlin с использованием библиотеки TelegramBots.

## Функциональность

- Интеграция с OpenAI для умных ответов
- Советы по здоровому образу жизни
- Отслеживание активности пользователя

## Технологии

- Kotlin
- Gradle
- Docker
- OpenAI API
- Telegram Bots API

## Установка и запуск

### Локальный запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/yourusername/HealthyLifeBot.git
```

2. Создайте файл .env на основе .env.example:
```bash
cp .env.example .env
```

3. Заполните необходимые переменные окружения в .env

4. Запустите приложение:
```bash
./gradlew run
```

### Docker

1. Соберите Docker образ:
```bash
docker build -t healthylifebot .
```

2. Запустите контейнер:
```bash
docker run -p 8080:8080 --env-file .env healthylifebot
```

## Деплой на Railway

1. Создайте аккаунт на [Railway](https://railway.app/)
2. Установите Railway CLI:
```bash
npm i -g @railway/cli
```
3. Залогиньтесь в Railway:
```bash
railway login
```
4. Инициализируйте проект:
```bash
railway init
```
5. Деплой проекта:
```bash
railway up
```

## Переменные окружения

- `BOT_TOKEN` - Токен вашего Telegram бота
- `OPENAI_API_KEY` - Ключ API OpenAI
- `PORT` - Порт для запуска приложения (по умолчанию 8080)

## Лицензия

MIT
