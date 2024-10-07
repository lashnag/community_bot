package ru.lashnev.community.bot.services.senders

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendPoll
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.utils.formatter
import ru.lashnev.community.bot.utils.logger

@Service
class PollService(val bot: TelegramBot, val eventDao: EventDao) : SenderService {
    override fun sendMessages() {
        val events = eventDao.getEventsToPoll()
        for (event in events) {
            logger.info("Send poll on $event")
            val response = bot.execute(
                SendPoll(
                    event.community.chatId,
                    "Будете ли вы участвовать в ${event.eventDescription} ${event.eventDate.format(formatter)}?",
                    "Да", "Нет"
                ).isAnonymous(false)
            )
            eventDao.save(event.copy(pollId = response.message().poll().id().toString()))
        }
    }

    companion object {
        private val logger = logger()
    }
}
