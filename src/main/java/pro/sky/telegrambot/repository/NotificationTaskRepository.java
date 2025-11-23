package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.entity.NotificationTask;
import pro.sky.telegrambot.entity.TaskStatus;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    // Находим напоминания для отправки (только PENDING)
    @Query("SELECT nt FROM NotificationTask nt WHERE nt.notificationDateTime = :dateTime AND nt.status = 'PENDING'")
    List<NotificationTask> findPendingByNotificationDateTime(@Param("dateTime") ZonedDateTime dateTime);

    List<NotificationTask> findByChatIdOrderByNotificationDateTime(Long chatId);

    List<NotificationTask> findByChatIdAndStatusOrderByNotificationDateTime(Long chatId, TaskStatus status);

    @Query("SELECT COUNT(nt) FROM NotificationTask nt WHERE nt.chatId = :chatId")
    long countByChatId(@Param("chatId") Long chatId);

    @Query("SELECT COUNT(nt) FROM NotificationTask nt WHERE nt.chatId = :chatId AND nt.status = 'COMPLETED'")
    long countCompletedByChatId(@Param("chatId") Long chatId);

    @Query("SELECT COUNT(nt) FROM NotificationTask nt WHERE nt.chatId = :chatId AND nt.status = 'PENDING'")
    long countPendingByChatId(@Param("chatId") Long chatId);

    @Query("SELECT COUNT(nt) FROM NotificationTask nt WHERE nt.chatId = :chatId AND nt.status = 'CANCELLED'")
    long countCancelledByChatId(@Param("chatId") Long chatId);

    Optional<NotificationTask> findByIdAndChatId(Long id, Long chatId);
}