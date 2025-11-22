package pro.sky.telegrambot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;

@Configuration
@EnableScheduling
public class SchedulerConfiguration {

    private final Logger logger = LoggerFactory.getLogger(SchedulerConfiguration.class);

    @PostConstruct
    public void init() {
        logger.info("=== SCHEDULER CONFIGURATION LOADED ===");
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("scheduled-task-");
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setErrorHandler(throwable -> {
            logger.error("Error in scheduled task: ", throwable);
        });
        scheduler.initialize();
        logger.info("Task scheduler initialized");
        return scheduler;
    }
}