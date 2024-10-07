package ru.lashnev.community.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ru.lashnev.community.database")
data class DatabaseProperties(val schema: String)
