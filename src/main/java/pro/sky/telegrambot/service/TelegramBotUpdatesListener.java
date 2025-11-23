package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.entity.TaskStatus;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pro.sky.telegrambot.service.Constants.*;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Pattern PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})\\s+(.+)");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm");
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final TelegramBot telegramBot;
    private final NotificationTaskRepository notificationTaskRepository;
    private static final ZoneId TIME_ZONE = ZoneId.of("Asia/Yekaterinburg");

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

            if (update.message() == null || update.message().text() == null) {
                return;
            }

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
                        processCancelCommand(chatId, messageText, userName);
                    } else {
                        processReminderMessage(chatId, messageText, userName);
                    }
                }
            }
        });

        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void processReminderMessage(Long chatId, String messageText, String userName) {
        logger.info("Was invoked method processReminderMessage");
        Matcher matcher = PATTERN.matcher(messageText);

        if (!matcher.matches()) {
            sendHelpMessage(chatId, userName);
            return;
        }

        String dateTimeString = matcher.group(1);
        String reminderText = matcher.group(2);
        LocalDateTime notificationDateTime;

        try {
            // Парсим время и устанавливаем часовой пояс
            LocalDateTime parsedDateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
            // Преобразуем в ZonedDateTime с указанием часового пояса
            ZonedDateTime zonedDateTime = parsedDateTime.atZone(TIME_ZONE);
            notificationDateTime = zonedDateTime.toLocalDateTime();

        } catch (DateTimeParseException e) {
            sendMessage(chatId, CREATE_ERROR_INVALID_DATE_FORMAT);
            logger.info("Sent wrong date format message to user {} in chat: {}", userName, chatId);
            return;
        }

        // Получаем текущее время в том же часовом поясе для сравнения
        LocalDateTime currentDateTime = ZonedDateTime.now(TIME_ZONE).toLocalDateTime();

        // Проверяем, что дата не в прошлом
        if (notificationDateTime.isBefore(currentDateTime)) {
            sendMessage(chatId, CREATE_ERROR_PAST_TIME);
            logger.info("Sent pastTimeError message to user {} in chat: {}", userName, chatId);
            return;
        }

        // Сохраняем напоминание в БД
        NotificationTask task = new NotificationTask(chatId, reminderText, notificationDateTime);
        notificationTaskRepository.save(task);

        String formattedDateTime = notificationDateTime.format(DISPLAY_FORMATTER);
        String successText = NOTIFICATION_CREATE_SUCCESS.formatted(formattedDateTime, reminderText);
        sendMessage(chatId, successText);
        logger.info("Sent message about adding a reminder to user {} in chat: {}", userName, chatId);
    }

    // Остальные методы остаются без изменений...
    private void sendWelcomeMessage(Long chatId, String firstName, String userName) {
        logger.info("Was invoked method sendWelcomeMessage");
        String welcomeText = WELCOME_MESSAGE.formatted(firstName);

        sendMessage(chatId, welcomeText);
        logger.info("Sent welcome message to user {} in chat: {}", userName, chatId);
    }

    private void sendHelpMessage(Long chatId, String userName) {
        logger.info("Was invoked method sendHelpMessage");
        sendMessage(chatId, HELP_MESSAGE);
        logger.info("Sent help message to user {} in chat: {}", userName, chatId);
    }

    private void showAllReminders(Long chatId, String userName) {
        logger.info("Was invoked method showAllReminders");
        List<NotificationTask> tasks = notificationTaskRepository.findByChatIdAndStatusOrderByNotificationDateTime(chatId, TaskStatus.PENDING);

        if (tasks.isEmpty()) {
            sendMessage(chatId, NO_ACTIVE_NOTIFICATIONS);
            return;
        }

        StringBuilder message = new StringBuilder(ACTIVE_NOTIFICATIONS_HEADER);
        for (NotificationTask task : tasks) {
            String formattedDateTime = task.getNotificationDateTime().format(DISPLAY_FORMATTER);
            String taskMessage = ACTIVE_NOTIFICATIONS_TASKS.formatted(
                    task.getId(),
                    formattedDateTime,
                    task.getMessageText()
            );

            message.append(taskMessage);
        }

        message.append(ACTIVE_NOTIFICATIONS_FOOTER);

        sendMessage(chatId, message.toString());
        logger.info("Sent list all reminders to user {} in chat: {}", userName, chatId);
    }

    private void processCancelCommand(Long chatId, String text, String userName) {
        logger.info("Was invoked method processCancelCommand");
        try {
            String[] parts = text.split(" ");
            if (parts.length != 2) {
                sendMessage(chatId, CANCEL_ERROR_INVALID_COMMAND);
                logger.info("Sent wrong cancel command message to user {} in chat: {}", userName, chatId);
                return;
            }

            Long taskId = Long.parseLong(parts[1]);
            Optional<NotificationTask> task = notificationTaskRepository.findByIdAndChatId(taskId, chatId);

            if (task.isPresent()) {
                NotificationTask notificationTask = task.get();
                notificationTask.setStatus(TaskStatus.CANCELLED);
                notificationTaskRepository.save(notificationTask);

                sendMessage(chatId, CANCEL_TASK_SUCCESS.formatted(taskId));
                logger.info("Sent success cancel message to user {} in chat: {}", userName, chatId);
            } else {
                sendMessage(chatId, CANCEL_ERROR_NOT_FOUND.formatted(taskId));
                logger.info("Sent not found remind message to user {} in chat: {}", userName, chatId);
            }

        } catch (NumberFormatException e) {
            sendMessage(chatId, CANCEL_ERROR_INVALID_ID);
            logger.info("Sent wrong format ID message to user {} in chat: {}", userName, chatId);
        }
    }

    private void showStatistics(Long chatId, String userName) {
        logger.info("Was invoked method showStatistics");
        long totalTasks = notificationTaskRepository.countByChatId(chatId);
        long completedTasks = notificationTaskRepository.countCompletedByChatId(chatId);
        long pendingTasks = notificationTaskRepository.countPendingByChatId(chatId);
        long cancelledTasks = notificationTaskRepository.countCancelledByChatId(chatId);

        String statsText = STATISTIC_MESSAGE.formatted(totalTasks, completedTasks, pendingTasks, cancelledTasks,
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