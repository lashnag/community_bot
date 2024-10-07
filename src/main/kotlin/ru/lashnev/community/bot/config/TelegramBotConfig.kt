package ru.lashnev.community.bot.config

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.TelegramBot.Builder
import okhttp3.OkHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetSocketAddress
import java.net.Proxy

@Configuration
class TelegramBotConfig(private val botProperties: BotProperties) {
    @Bean
    fun getBot(): TelegramBot {
        val httpClientBuilder = OkHttpClient().newBuilder()
        return Builder(botProperties.secretKey).okHttpClient(httpClientBuilder.build()).build()
    }
}
