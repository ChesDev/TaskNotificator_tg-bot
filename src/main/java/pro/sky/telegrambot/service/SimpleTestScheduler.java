package pro.sky.telegrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Date;

@Service
public class SimpleTestScheduler {

    private final Logger logger = LoggerFactory.getLogger(SimpleTestScheduler.class);
    private int counter = 0;

    @Scheduled(cron = "*/30 * * * * *")
    public void simpleTest() {
        counter++;
        logger.info("✅ SIMPLE TEST #{} - SCHEDULER IS WORKING! Time: {}", counter, LocalDateTime.now());
        System.out.println("✅ SIMPLE TEST #" + counter + " - " + new Date());
    }

    @Scheduled(fixedRate = 60000)
    public void fixedRateTest() {
        logger.info("🔄 FIXED RATE TEST - Working at: {}", LocalDateTime.now());
    }
}