package ru.lashnev.community.bot.services

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.services.senders.SenderService
import ru.lashnev.community.bot.utils.logger

@Service
class SenderScheduler(private val senders: List<SenderService>) {
    @Scheduled(fixedRate = 10000)
    fun send() {
        for (sender in senders) {
            try {
                sender.sendMessages()
            } catch (e: Exception) {
                logger.error(e.message, e)
            }
        }
    }

    companion object {
        private val logger = logger()
    }
}
