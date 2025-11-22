package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.entity.TaskStatus;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static pro.sky.telegrambot.service.Constants.NOTIFICATION;

@Service
public class NotificationScheduler {

    private final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;

    public NotificationScheduler(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 * * * * *") // Каждую минуту в 00 секунд
    public void checkNotifications() {
        LocalDateTime currentDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // Ищем только PENDING напоминания
        List<NotificationTask> tasks = notificationTaskRepository.findPendingByNotificationDateTime(currentDateTime);

        if (!tasks.isEmpty()) {
            tasks.forEach(task -> {
                sendNotification(task);
                task.setStatus(TaskStatus.COMPLETED);
                notificationTaskRepository.save(task);
            });

            System.out.println("Отправлено " + tasks.size() + " напоминаний");
        }
    }

    void sendNotification(NotificationTask task) {
        logger.info("Was invoked method sendNotification");
        String notificationText = NOTIFICATION + task.getMessageText();

        SendMessage message = new SendMessage(task.getChatId(), notificationText);
        message.parseMode(ParseMode.valueOf("HTML"));
        SendResponse response = telegramBot.execute(message);

        logger.info("Notification success: {}", task.getChatId());

        if (!response.isOk()) {
            System.err.println("Ошибка отправки уведомления: " + response.errorCode());
            logger.error("Error sending notification: {} - {}", task.getChatId(), response.errorCode());
        }
    }
}
