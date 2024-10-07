package ru.lashnev.community.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ru.lashnev.community.bot")
data class BotProperties(val secretKey: String, val userName: String)
