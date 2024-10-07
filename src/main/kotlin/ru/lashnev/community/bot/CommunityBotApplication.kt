package ru.lashnev.community.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication(scanBasePackages = ["ru.lashnev.community.bot"])
class CommunityBotApplication

fun main(args: Array<String>) {
    runApplication<CommunityBotApplication>(*args)
}
