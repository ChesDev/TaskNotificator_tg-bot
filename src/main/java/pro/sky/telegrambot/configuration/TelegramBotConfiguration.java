package pro.sky.telegrambot.configuration;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.DeleteMyCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TelegramBotConfiguration {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotConfiguration.class);

    @Value("${telegram.bot.token}")
    private String token;

    @Bean
    public TelegramBot telegramBot() {
        if (token == null || token.isEmpty()) {
            logger.error("Telegram bot token is not configured properly!");
            throw new IllegalStateException("Telegram bot token is not configured");
        }

        TelegramBot bot = new TelegramBot(token);
        bot.execute(new DeleteMyCommands());
        logger.info("Telegram bot initialized successfully");
        return bot;
    }

}
