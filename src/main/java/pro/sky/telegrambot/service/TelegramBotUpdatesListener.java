package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.entity.TaskStatus;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm");
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    @Autowired
    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskRepository notificationTaskRepository) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);

            if (update.message() != null && update.message().text() != null) {
                Long chatId = update.message().chat().id();
                String firstName = update.message().from().firstName();
                String userName = update.message().chat().username();
                String messageText = update.message().text();

                switch (messageText) {
                    case "/start" -> sendWelcomeMessage(chatId, firstName, userName);
                    case "/help" -> sendHelpMessage(chatId, userName);
                    case "/list" -> showAllReminders(chatId, userName);
                    case "/stats" -> showStatistics(chatId, userName);
                    default -> {
                        if (messageText.startsWith("/delete")) {
                            processDeleteCommand(chatId, messageText, userName);
                        } else {
                            processReminderMessage(chatId, messageText, userName);
                        }
                    }
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processReminderMessage(Long chatId, String messageText, String userName) {
        logger.info("Was invoked method processReminderMessage");
        Matcher matcher = PATTERN.matcher(messageText);

        if (matcher.matches()) {
            try {
                String dateTimeString = matcher.group(1);
                String reminderText = matcher.group(2);

                LocalDateTime notificationDateTime = LocalDateTime.parse(
                        dateTimeString,
                        DATE_TIME_FORMATTER
                );

                // Проверяем, что дата не в прошлом
                if (notificationDateTime.isBefore(LocalDateTime.now())) {
                    sendMessage(chatId, "❌ Нельзя установить напоминание на прошедшее время!");
                    logger.info("Sent pastTimeError message to user {} in chat: {}", userName, chatId);
                    return;
                }

                // Сохраняем напоминание в БД
                NotificationTask task = new NotificationTask(chatId, reminderText, notificationDateTime);
                notificationTaskRepository.save(task);

                String formattedDateTime = notificationDateTime.format(DISPLAY_FORMATTER);
                String successText = """
                        ✅ <b>Напоминание создано!</b>
                        
                         <b>Дата и время:</b> %s
                         <b>Текст:</b><blockquote>%s</blockquote>
                        
                        ⏰ Я пришлю вам это напоминание в указанное время!
                        """.formatted(formattedDateTime, reminderText);
                sendMessage(chatId, successText);
                logger.info("Sent message about adding a reminder to user {} in chat: {}", userName, chatId);

            } catch (DateTimeParseException e) {
                sendMessage(chatId, "❌ Неверный формат даты и времени! Используйте: dd.MM.yyyy HH:mm");
                logger.info("Sent wrong date format message to user {} in chat: {}", userName, chatId);
            }
        } else {
            sendHelpMessage(chatId, userName);
        }
    }

    private void sendWelcomeMessage(Long chatId, String firstName, String userName) {
        logger.info("Was invoked method sendWelcomeMessage");
        String welcomeText = """
                👋 Привет, %s!
                
                🤖 Я бот-напоминалка. Я помогу тебе не забывать о важных делах!
                
                📋 Чтобы создать напоминание, отправь сообщение в формате:
                <b><i>dd.MM.yyyy HH:mm Текст напоминания</i></b>
                
                Например:
                <blockquote>25.04.2026 09:00 Поздравить Егора с днем рождения
                13.10.2026 12:00 Встреча!
                </blockquote>
                Я буду присылать тебе уведомления в указанное время! ⏰
                """.formatted(firstName);

        sendMessage(chatId, welcomeText);
        logger.info("Sent welcome message to user {} in chat: {}", userName, chatId);
    }

    private void sendHelpMessage(Long chatId, String userName) {
        logger.info("Was invoked method sendHelpMessage");
        String helpText = """
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
        sendMessage(chatId, helpText);
        logger.info("Sent help message to user {} in chat: {}", userName, chatId);
    }

    private void showAllReminders(Long chatId, String userName) {
        logger.info("Was invoked method showAllReminders");
        List<NotificationTask> tasks = notificationTaskRepository.findByChatIdAndStatusOrderByNotificationDateTime(chatId, TaskStatus.PENDING);

        if (tasks.isEmpty()) {
            sendMessage(chatId, "📭 У вас нет активных напоминаний.");
            return;
        }

        StringBuilder message = new StringBuilder("📋 <b>Ваши активные напоминания:</b>\n\n");
        for (NotificationTask task : tasks) {
            String formattedDateTime = task.getNotificationDateTime().format(DISPLAY_FORMATTER);
            message.append("<blockquote>⏳ <b>ID</b>: ").append(task.getId())
                    .append(" <b>|</b> ").append(formattedDateTime)
                    .append("\n📝 <i>").append(task.getMessageText())
                    .append("</i></blockquote>\n\n");
        }

        message.append("💡 Для отмены используйте: <i>/delete ID</i>");

        sendMessage(chatId, message.toString());
        logger.info("Sent list all reminders to user {} in chat: {}", userName, chatId);
    }

    private void processDeleteCommand(Long chatId, String text, String userName) {
        logger.info("Was invoked method processDeleteCommand");
        try {
            String[] parts = text.split(" ");
            if (parts.length != 2) {
                sendMessage(chatId, "❌ Используйте: /delete ID\nНапример: /delete 5");
                logger.info("Sent wrong delete command message to user {} in chat: {}", userName, chatId);
                return;
            }

            Long taskId = Long.parseLong(parts[1]);
            Optional<NotificationTask> task = notificationTaskRepository.findByIdAndChatId(taskId, chatId);

            if (task.isPresent()) {
                NotificationTask notificationTask = task.get();
                notificationTask.setStatus(TaskStatus.CANCELLED);
                notificationTaskRepository.save(notificationTask);

                sendMessage(chatId, "✅ Напоминание ID: " + taskId + " отменено!");
                logger.info("Sent success delete message to user {} in chat: {}", userName, chatId);
            } else {
                sendMessage(chatId, "❌ Напоминание с ID: " + taskId + " не найдено или вам не принадлежит.");
                logger.info("Sent not found remind message to user {} in chat: {}", userName, chatId);
            }

        } catch (NumberFormatException e) {
            sendMessage(chatId, "❌ Неверный формат ID. Используйте число, например: <i>/delete 5</i>");
            logger.info("Sent wrong format ID message to user {} in chat: {}", userName, chatId);
        }
    }

    private void showStatistics(Long chatId, String userName) {
        logger.info("Was invoked method showStatistics");
        long totalTasks = notificationTaskRepository.countByChatId(chatId);
        long completedTasks = notificationTaskRepository.countCompletedByChatId(chatId);
        long pendingTasks = notificationTaskRepository.countPendingByChatId(chatId);
        long cancelledTasks = notificationTaskRepository.countCancelledByChatId(chatId);

        String statsText = """
                📊 Статистика ваших напоминаний:
                
                📋 Всего создано: %d
                ✅ Выполнено: %d
                ⏳ Ожидают выполнения: %d
                ❌ Отменено: %d
                
                🎯 Продуктивность: %.1f%%
                """.formatted(totalTasks, completedTasks, pendingTasks, cancelledTasks,
                totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0.0);

        sendMessage(chatId, statsText);
        logger.info("Sent statistics message to user {} in chat: {}", userName, chatId);
    }

    private void sendMessage(Long chatId, String messageText) {
        logger.info("Was invoked method sendMessage");
        SendMessage message = new SendMessage(chatId, messageText);
        message.parseMode(ParseMode.valueOf("HTML"));
        telegramBot.execute(message);
    }
}