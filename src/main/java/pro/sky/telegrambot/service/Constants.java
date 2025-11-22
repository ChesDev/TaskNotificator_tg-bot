package pro.sky.telegrambot.service;

public class Constants {
    public static final String NOTIFICATION = "🔔 <b>Напоминание!</b> 🔔\n\n";
    public static final String CREATE_ERROR_PAST_TIME = "❌ Нельзя установить напоминание на прошедшее время!";
    public static final String CREATE_ERROR_INVALID_DATE_FORMAT = "❌ Неверный формат даты и времени! Используйте: dd.MM.yyyy HH:mm";
    public static final String CANCEL_TASK_SUCCESS = "✅ Напоминание ID: %s отменено!";
    public static final String CANCEL_ERROR_INVALID_COMMAND = "❌ Используйте: /delete ID\nНапример: /delete 5";
    public static final String CANCEL_ERROR_INVALID_ID = "❌ Неверный формат ID. Используйте число, например: <i>/delete 5</i>";
    public static final String CANCEL_ERROR_NOT_FOUND = "❌ Напоминание с ID: %s не найдено или вам не принадлежит.";
    public static final String NO_ACTIVE_NOTIFICATIONS = "📭 У вас нет активных напоминаний.";
    public static final String ACTIVE_NOTIFICATIONS_HEADER = "📋 <b>Ваши активные напоминания:</b>\n\n";
    public static final String ACTIVE_NOTIFICATIONS_TASKS = "<blockquote>⏳ <b>ID</b>: %s <b>|</b> %s\n📝 <i>%s</i></blockquote>";
    public static final String ACTIVE_NOTIFICATIONS_FOOTER = "💡 Для отмены используйте: <i>/delete ID</i>";
    public static final String NOTIFICATION_CREATE_SUCCESS = """
            ✅ <b>Напоминание создано!</b>
            
            <b>Дата и время:</b> %s
            <b>Текст:</b><blockquote>%s</blockquote>
            
            ⏰ Я пришлю вам это напоминание в указанное время!
            """;
    public static final String WELCOME_MESSAGE = """
            👋 Привет, %s!
            
            🤖 Я бот-напоминалка. Я помогу тебе не забывать о важных делах!
            
            📋 Чтобы создать напоминание, отправь сообщение в формате:
            <b><i>dd.MM.yyyy HH:mm Текст напоминания</i></b>
            
            Например:
            <blockquote>25.04.2026 09:00 Поздравить Егора с днем рождения
            13.10.2026 12:00 Встреча!
            </blockquote>
            Я буду присылать тебе уведомления в указанное время! ⏰
            """;
    public static final String HELP_MESSAGE = """
             📆 <b>Создание напоминания:</b>
            <i>Отправь сообщение с будующей датой, временем в 24-часовом формате и текстом напоминания.</i>
            
            <code>dd.MM.yyyy HH:mm Текст напоминания</code>
            
            <b>Например:</b>
            <blockquote>01.01.2026 00:00 С Новым Годом!
            31.12.2025 22:00 Поздравить друзей
            15.11.2025 09:00 Оплатить аренду
            </blockquote>
             📖 <b>Доступные команды:</b>
            /start - начать работу
            /help - помощь по боту
            /list - список всех напоминаний
            /stats - статистика напоминаний
            /delete ID - удалить напоминание
            """;
    public static final String STATISTIC_MESSAGE = """
            📊 Статистика ваших напоминаний:
            
            📋 Всего создано: %d
            ✅ Выполнено: %d
            ⏳ Ожидают выполнения: %d
            ❌ Отменено: %d
            
            🎯 Продуктивность: %.1f%%
            """;
}