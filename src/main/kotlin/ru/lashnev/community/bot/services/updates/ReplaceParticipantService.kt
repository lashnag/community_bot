package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.request.SendMessage
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.ReplaceParticipantDao
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User
import ru.lashnev.community.bot.utils.formatter
import ru.lashnev.community.bot.utils.getChatId
import ru.lashnev.community.bot.utils.getChosenEvent
import ru.lashnev.community.bot.utils.getUserId
import ru.lashnev.community.bot.utils.isGroupMessage
import ru.lashnev.community.bot.utils.logger
import ru.lashnev.community.bot.utils.toMention

@Service
class ReplaceParticipantService(
    private val eventDao: EventDao,
    private val userDao: UserDAO,
    private val replaceParticipantDao: ReplaceParticipantDao,
    private val bot: TelegramBot,
) : UpdatesService {
    override fun processUpdates(update: Update) {
        if (!update.isReplaceCommand()) {
            return
        }

        if (update.isGroupMessage()) {
            sendOnlyPrivate(update.getChatId())
            return
        }

        val user: User = userDao.getByTelegramId(update.getUserId()).orElseThrow()
        val events = eventDao.getActiveEventsByUser(userId = user.userId!!)
        if (update.getChosenEvent(COMMAND_CHOOSE_EVENT_PART).isPresent && events.find { it.eventId == update.getChosenEvent(COMMAND_CHOOSE_EVENT_PART).get() } != null) {
            val event = eventDao.getEventById(update.getChosenEvent(COMMAND_CHOOSE_EVENT_PART).get())!!
            replaceInEvent(event, user)
            return
        }

        if (events.isEmpty()) {
            cantFindAnyEvents(update.getChatId())
        } else {
            sendChooseEvent(events, update.getChatId())
        }
    }

    private fun sendChooseEvent(events: Set<Event>, chatId: Long) {
        val communityNameButtons = events.map {
            InlineKeyboardButton("Вы участвуете в: ${it.eventDescription} в ${it.eventDate.format(formatter)}").callbackData("replace-${it.eventId}")
        }

        val keyBoardBuilder = InlineKeyboardMarkup()

        for (communityNameButton in communityNameButtons) {
            keyBoardBuilder.addRow(communityNameButton)
        }
        val smBuilder = SendMessage(chatId, CHOOSE_EVENT_TEXT)
        smBuilder.replyMarkup(keyBoardBuilder)
        bot.execute(smBuilder)
    }

    private fun replaceInEvent(event: Event, user: User) {
        val replacedUsers = event.replacedParticipants.mapNotNull { it.replaceUser }.toSet()
        val participantFromReserve = event.reserveParticipants.subtract(replacedUsers).firstOrNull()
        bot.execute(
            SendMessage(
                event.community.chatId,
                "Пользователь ${user.toMention()} отказался от участия в мероприятии ${event.eventDescription}"
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Send reply replace to user ${user.toMention()}")
        if (participantFromReserve != null) {
            bot.execute(
                SendMessage(
                    event.community.chatId,
                    "Замена на ${participantFromReserve.toMention()}"
                ).parseMode(ParseMode.HTML)
            )
            logger.info("Send replace on user ${participantFromReserve.toMention()}")
        }
        replaceParticipantDao.replaceParticipant(
            event = event,
            canceledUser = user,
            replaceUser = participantFromReserve
        )
    }

    private fun cantFindAnyEvents(chatId: Long) {
        bot.execute(
            SendMessage(
                chatId,
                NO_EVENTS_MESSAGE
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Send reply replace no event")
    }

    private fun sendOnlyPrivate(chatId: Long) {
        bot.execute(
            SendMessage(
                chatId,
                ONLY_PRIVATE
            ).parseMode(ParseMode.HTML)
        )
        logger.info("Send reply replace only private")
    }

    private fun Update.isReplaceCommand(): Boolean {
        return isReplaceCommandFromMessage() || isReplaceCommandFromChooseEvents()
    }

    private fun Update.isGroupMessage() =
        (this.callbackQuery() != null && this.callbackQuery().message().isGroupMessage()) || (this.message() != null && this.message().isGroupMessage())

    private fun Update.isReplaceCommandFromChooseEvents() =
        this.callbackQuery() != null && this.callbackQuery().data() != null && this.callbackQuery().data().contains(COMMAND_CHOOSE_EVENT_PART)

    private fun Update.isReplaceCommandFromMessage() =
        this.message() != null && this.message().text() != null && this.message().text().contains(COMMAND_PART_NAME)

    companion object {
        const val COMMAND_PART_NAME = "/replace"
        const val COMMAND_CHOOSE_EVENT_PART = "replace-"
        const val NO_EVENTS_MESSAGE = "Не нашел ни одного мероприятия в котором вы участвуете"
        const val CHOOSE_EVENT_TEXT = "Выберите мероприятие из которого хотите выписаться"
        const val ONLY_PRIVATE = "Выполните это сообщение в личке бота"
        private val logger = logger()
    }
}
