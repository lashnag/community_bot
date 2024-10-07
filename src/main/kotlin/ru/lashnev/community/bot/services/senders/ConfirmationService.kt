package ru.lashnev.community.bot.services.senders

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.ParticipantDao
import ru.lashnev.community.bot.dao.ReserveParticipantDao
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User
import ru.lashnev.community.bot.utils.logger
import ru.lashnev.community.bot.utils.toNumberedMentionedList

@Service
class ConfirmationService(
    val bot: TelegramBot,
    val eventDao: EventDao,
    val participantDao: ParticipantDao,
    val reserveParticipantDao: ReserveParticipantDao
) : SenderService {

    override fun sendMessages() {
        val events = eventDao.getEventsToNotification()
        for (event in events) {
            logger.info("Try to send notification on $event")

            var possibleParticipants = event.interestedParticipants.intersect(event.confirmedParticipants)
            possibleParticipants = possibleParticipants.ifEmpty { event.interestedParticipants }

            if (possibleParticipants.count() > event.community.capacity) {
                chooseParticipantsWithReserve(possibleParticipants, event)
            } else if (possibleParticipants.isNotEmpty()) {
                chooseAllParticipants(event, possibleParticipants)
            } else {
                logger.info("No participants")
            }

            eventDao.save(event.copy(notificationWasSent = true))
        }
    }

    private fun chooseAllParticipants(event: Event, possibleParticipants: Set<User>) {
        logger.info("Capacity of community is enough")
        bot.execute(
            SendMessage(
                event.community.chatId,
                "Участвующие в ${event.eventDescription} ${possibleParticipants.toNumberedMentionedList()}"
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Participants list: ${possibleParticipants.toNumberedMentionedList()}")
        possibleParticipants.forEach {
            participantDao.addParticipant(it, event)
        }
    }

    private fun chooseParticipantsWithReserve(possibleParticipants: Set<User>, event: Event) {
        logger.info("Capacity of community is not enough, so randomize and prioritize new users")
        val participants = prioritizeByPreviousEventParticipation(possibleParticipants, event)
        bot.execute(
            SendMessage(
                event.community.chatId,
                "Участвующие в ${event.eventDescription} в основном списке ${participants.toNumberedMentionedList()}"
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Participants main list: ${participants.toNumberedMentionedList()}")
        val reserveParticipants = possibleParticipants.subtract(participants)
        bot.execute(
            SendMessage(
                event.community.chatId,
                "Участвующие в ${event.eventDescription} в запасном списке ${reserveParticipants.toNumberedMentionedList()}"
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Participants reserve list: ${reserveParticipants.toNumberedMentionedList()}")

        participants.forEach {
            participantDao.addParticipant(it, event)
        }
        reserveParticipants.forEach {
            reserveParticipantDao.addParticipant(it, event)
        }
    }

    private fun prioritizeByPreviousEventParticipation(possibleParticipants: Set<User>, event: Event): Set<User> {
        val possiblePriorityParticipants = mutableSetOf<User>()
        val previousEvent = eventDao.getPreviousEvent(event)
        val randomizedPossibleParticipants = possibleParticipants.shuffled()
        randomizedPossibleParticipants.forEach {
            if (previousEvent != null && !previousEvent.participants.contains(it)) {
                possiblePriorityParticipants.add(it)
            }
        }
        return (possiblePriorityParticipants + randomizedPossibleParticipants.subtract(possiblePriorityParticipants))
            .take(event.community.capacity)
            .toSet()
    }

    companion object {
        private val logger = logger()
    }
}
