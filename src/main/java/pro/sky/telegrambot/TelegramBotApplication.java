package pro.sky.telegrambot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableScheduling
public class TelegramBotApplication {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TelegramBotApplication.class, args);
        logger.info("=== APPLICATION STARTED SUCCESSFULLY ===");
        logger.info("=== SCHEDULING IS ENABLED ===");
        logger.info("=== CHECKING SCHEDULER CONFIGURATION ===");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        logger.info("🎯 APPLICATION READY - SCHEDULER SHOULD START WORKING");
        logger.info("🕒 Server time: {}", LocalDateTime.now());
    }
}
