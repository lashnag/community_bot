package ru.lashnev.community.bot.services.senders

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.request.SendPoll
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.utils.logger
import ru.lashnev.community.bot.utils.toNumberedMentionedList

@Service
class PollConfirmationService(val bot: TelegramBot, val eventDao: EventDao) : SenderService {
    override fun sendMessages() {
        val events = eventDao.getEventsToConfirmationPoll()
        for (event in events) {
            logger.info("Send poll confirmation on $event")
            val response = bot.execute(
                SendPoll(
                    event.community.chatId,
                    "Подтвердите свое участие в ${event.eventDescription} ${event.eventDate}",
                    "Да", "Нет"
                ).isAnonymous(false)
            )
            eventDao.save(event.copy(pollConfirmationId = response.message().poll().id().toString()))

            bot.execute(
                SendMessage(
                    event.community.chatId,
                    event.interestedParticipants.toNumberedMentionedList()
                ).parseMode(ParseMode.HTML)
            )
        }
    }

    companion object {
        private val logger = logger()
    }
}
