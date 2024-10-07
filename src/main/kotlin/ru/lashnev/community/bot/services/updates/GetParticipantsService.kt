package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Component
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.utils.formatter
import ru.lashnev.community.bot.utils.getChatId
import ru.lashnev.community.bot.utils.getChosenEvent
import ru.lashnev.community.bot.utils.getUserId
import ru.lashnev.community.bot.utils.logger
import ru.lashnev.community.bot.utils.toNumberedMentionedList

@Component
class GetParticipantsService(
    private val eventDao: EventDao,
    private val bot: TelegramBot,
    private val userDAO: UserDAO,
) : UpdatesService {
    override fun processUpdates(update: Update) {
        if (!update.isGetParticipantsCommand()) {
            return
        }

        val user = userDAO.getByTelegramId(update.getUserId())
        val admins = userDAO.getAdminsByChatId(update.getChatId())
        if (!user.get().isSuperuser && !admins.contains(user.orElseThrow())) {
            sendOnlyForAdmins(update.getChatId())
            return
        }

        if (update.getChosenEvent(COMMAND_CHOOSE_EVENT_PART).isPresent) {
            val event = eventDao.getEventById(update.getChosenEvent(COMMAND_CHOOSE_EVENT_PART).get())!!
            sendParticipantsList(event, update.getChatId())
            return
        }

        val events = eventDao.getActiveEventsByChatId(update.getChatId())
        when (events.count()) {
            0 -> {
                cantFindAnyEvents(update.getChatId())
            }
            1 -> {
                sendParticipantsList(events.first(), update.getChatId())
            }
            else -> {
                sendChooseEvent(events, update.getChatId())
            }
        }
    }

    private fun sendParticipantsList(event: Event, chatId: Long) {
        val participants = event.participants
        val reserveParticipants = event.reserveParticipants

        val replacedParticipants = event.replacedParticipants.map { it.user }
        val addedByReplaceParticipants = event.replacedParticipants.mapNotNull { it.replaceUser }

        val currentParticipants = participants - replacedParticipants.toSet() + addedByReplaceParticipants
        val currentReserveParticipants = reserveParticipants - addedByReplaceParticipants.toSet()

        if (currentParticipants.isNotEmpty()) {
            bot.execute(
                SendMessage(
                    chatId,
                    "Участвующие в ${event.eventDescription} в основном списке ${currentParticipants.toNumberedMentionedList()}"
                ).parseMode(ParseMode.HTML)
            )
            logger.info("Participants current main list: ${participants.toNumberedMentionedList()}")
            if (currentReserveParticipants.isNotEmpty()) {
                bot.execute(
                    SendMessage(
                        chatId,
                        "Участвующие в ${event.eventDescription} в запасном списке ${currentReserveParticipants.toNumberedMentionedList()}"
                    ).parseMode(ParseMode.HTML)
                )
                logger.info("Participants current reserve list: ${reserveParticipants.toNumberedMentionedList()}")
            }
        } else {
            bot.execute(SendMessage(chatId, "Нет участников").parseMode(ParseMode.HTML))
        }
    }

    private fun Update.isGetParticipantsCommand(): Boolean {
        return isParticipantCommandFromMessage() || isParticipantCommandAfterChooseEvent()
    }

    private fun Update.isParticipantCommandAfterChooseEvent() =
        this.callbackQuery() != null && this.callbackQuery().data() != null && this.callbackQuery().data().contains(COMMAND_CHOOSE_EVENT_PART)

    private fun Update.isParticipantCommandFromMessage() =
        this.message() != null && this.message().text() != null && this.message().text().contains(COMMAND_PART_NAME)

    private fun sendChooseEvent(events: Set<Event>, chatId: Long) {
        val communityNameButtons = events.map {
            InlineKeyboardButton("Выберите предстоящее мероприятие: ${it.eventDescription} в ${it.eventDate.format(formatter)}").callbackData(COMMAND_CHOOSE_EVENT_PART + it.eventId)
        }

        val keyBoardBuilder = InlineKeyboardMarkup()

        for (communityNameButton in communityNameButtons) {
            keyBoardBuilder.addRow(communityNameButton)
        }
        val smBuilder = SendMessage(chatId, CHOOSE_EVENT_TEXT)
        smBuilder.replyMarkup(keyBoardBuilder)
        bot.execute(smBuilder)
    }

    private fun cantFindAnyEvents(chatId: Long) {
        bot.execute(
            SendMessage(
                chatId,
                CANT_FIND_ANY_EVENTS_MESSAGE
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Send reply list no event")
    }

    private fun sendOnlyForAdmins(chatId: Long) {
        bot.execute(
            SendMessage(
                chatId,
                ONLY_FOR_ADMINS
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Send only for admins")
    }

    companion object {
        const val CANT_FIND_ANY_EVENTS_MESSAGE = "Не нашел активных мероприятий"
        const val COMMAND_PART_NAME = "/participants_list"
        const val COMMAND_CHOOSE_EVENT_PART = "list-"
        const val CHOOSE_EVENT_TEXT = "Выберите мероприятие по которому хотите получить список участников"
        const val NO_PARTICIPANTS_MESSAGE = "Нет участников"
        const val ONLY_FOR_ADMINS = "Эта команда только для админов"
        private val logger = logger()
    }
}
