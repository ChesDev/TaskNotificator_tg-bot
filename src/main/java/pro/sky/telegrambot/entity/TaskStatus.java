package pro.sky.telegrambot.entity;

public enum TaskStatus {
    PENDING,    // Ожидает выполнения
    COMPLETED,  // Выполнено (отправлено)
    CANCELLED   // Отменено пользователем
}