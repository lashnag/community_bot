package ru.lashnev.community.bot.admin

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.Keyboard
import org.springframework.stereotype.Service
import ru.lashnev.community.bot.dao.CommunityDao
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.models.Community
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User
import ru.lashnev.community.bot.utils.logger
import java.lang.StringBuilder
import java.time.LocalDateTime

@Service
class AdminBot(
    private val eventDao: EventDao,
    private val communityDao: CommunityDao,
    private val userDao: UserDAO,
    private val bot: TelegramBot,
) {
    val userContext: MutableMap<Long, State> = mutableMapOf()

    enum class ProcessStage {
        ENTER_DESCRIPTION,
        ENTER_APPOINTMENT_DATE,
        ENTER_POLL_DATE,
        ENTER_CONFIRMATION_POLL_DATE,
        ENTER_NOTIFICATION_DATE,
        DELETE_EVENT
    }

    var confirmButton: InlineKeyboardButton = InlineKeyboardButton("Подтвердить").callbackData("confirm")
    var cancelButton: InlineKeyboardButton = InlineKeyboardButton("Отмена").callbackData("cancel")

    data class State(
        var stage: ProcessStage,
        var description: String? = null,
        var appointmentDate: LocalDateTime? = null,
        var pollDate: LocalDateTime? = null,
        var confirmationDate: LocalDateTime? = null,
        var participantListPublicationDate: LocalDateTime? = null,
        var community: Community? = null,
    ) {
        override fun toString(): String {
            val sb = StringBuilder()
            sb.appendLine("Название сообщества: ${community?.name}")
            sb.appendLine("Описание мероприятия: $description")
            sb.appendLine("Дата проведения мероприятия: $appointmentDate")
            sb.appendLine("Дата проведения опроса желающих участвовать: $pollDate")
            if (confirmationDate != null) {
                sb.appendLine("Дата подтверждения участвующих: $confirmationDate")
            }
            sb.appendLine("Дата публикации итогового списка участвующих: $participantListPublicationDate")

            return sb.toString().trimIndent()
        }
    }

    fun onUpdateReceived(update: Update) {
        if (update.callbackQuery() != null) {
            val buttonCallBack: CallbackQuery = update.callbackQuery()
            if (buttonCallBack.data() == "confirm") {
                confirmButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data() == "cancel") {
                cancelButtonClicked(buttonCallBack)
            } else if (buttonCallBack.data().startsWith("choose-")) {
                communityChosen(buttonCallBack)
            }
            return
        }

        val msg = update.message()
        val telegramUser = msg.from()
        val isPrivateChat = msg.chat().type() == Chat.Type.Private

        if (isPrivateChat) {
            userDao.save(
                User(
                    telegramUsername = msg.from().username(),
                    telegramId = msg.from().id(),
                    firstName = msg.from().firstName(),
                    lastName = msg.from().lastName(),
                )
            )

            if (msg.text().startsWith("/")) {

                when (msg.text().toCommand()) {
                    AdminCommand.CREATE_APPOINTMENT -> {
                        startCreateAppointment(msg)
                        return
                    }

                    AdminCommand.FETCH_EVENTS -> {
                        val user: User = userDao.getByTelegramId(msg.from().id()).orElseThrow()
                        val allCommunitiesByAdmin = communityDao.getAllCommunitiesByAdmin(user)
                        val events: List<Event> = allCommunitiesByAdmin.flatMap {
                            eventDao.getEventsByCommunityId(it.communityId ?: throw IllegalStateException())
                        }

                        events.forEach { sendText(telegramUser.id(), it.toTelegramBotMessage()) }
                        if (events.isEmpty()) {
                            sendText(telegramUser.id(), "Нет активных мероприятий")
                        }

                        return
                    }

                    AdminCommand.FETCH_EVENTS_SHORT -> {
                        val user: User = userDao.getByTelegramId(msg.from().id()).orElseThrow()
                        val allCommunitiesByAdmin = communityDao.getAllCommunitiesByAdmin(user)
                        val events: List<Event> = allCommunitiesByAdmin.flatMap {
                            eventDao.getEventsByCommunityId(it.communityId ?: throw IllegalStateException())
                        }.filter {
                            it.eventDate.minusDays(SHORT_EVENT_COMMAND_MAXIMUM_DAYS).isBefore(LocalDateTime.now())
                        }

                        events.forEach { sendText(telegramUser.id(), it.toTelegramBotMessage()) }
                        if (events.isEmpty()) {
                            sendText(telegramUser.id(), "Нет активных мероприятий")
                        }

                        return
                    }

                    AdminCommand.UNKNOWN_COMMAND -> {
                        return
                    }

                    AdminCommand.DELETE_EVENT -> {
                        handleError(msg.from().id()) {
                            val userId: Long = msg.from().id()
                            val state = State(ProcessStage.DELETE_EVENT)
                            userContext[userId] = state
                            sendText(userId, "Введите идентификатор события")
                        }
                        return
                    }
                }
            }

            val userState = userContext[telegramUser.id()]
            if (userState != null) {
                when (userState.stage) {
                    ProcessStage.ENTER_DESCRIPTION -> descriptionEntered(msg, userState)
                    ProcessStage.ENTER_APPOINTMENT_DATE -> appointmentDateEntered(msg, userState)
                    ProcessStage.ENTER_POLL_DATE -> pollDateEntered(msg, userState)
                    ProcessStage.ENTER_CONFIRMATION_POLL_DATE -> pollConfirmationDateEntered(msg, userState)
                    ProcessStage.ENTER_NOTIFICATION_DATE -> notificationDateEntered(msg, userState)
                    ProcessStage.DELETE_EVENT -> deleteEvent(msg)
                }
            } else {
                sendText(telegramUser.id(), "Введите команду или данные для завершения предыдущего запроса")
            }
        }
    }

    private fun communityChosen(buttonCallBack: CallbackQuery) {
        val userId = buttonCallBack.from().id()
        val communityId = buttonCallBack.data().removePrefix("choose-")
        userContext[userId] = State(
            stage = ProcessStage.ENTER_DESCRIPTION,
            community = communityDao.findById(communityId.toLong())
        )
        sendText(userId, "Введите описание мероприятия")
    }

    private fun deleteEvent(msg: Message) {
        handleError(msg.from().id()) {
            sendText(msg.from().id(), "Событие удалено ${msg.text()}")
            eventDao.deleteById(msg.text().toLong())
            userContext.remove(msg.from().id())
        }
    }

    private fun cancelButtonClicked(buttonCallBack: CallbackQuery) {
        handleError(buttonCallBack.from().id()) {
            sendText(buttonCallBack.from().id(), "нажата кнопка отмены")
            userContext.remove(buttonCallBack.from().id())
        }
    }

    private fun confirmButtonClicked(buttonCallBack: CallbackQuery) {
        handleError(buttonCallBack.from().id()) {
            sendText(buttonCallBack.from().id(), "нажата кнопка подтверждения")
            val userState = userContext[buttonCallBack.from().id()]!!

            eventDao.save(
                Event(
                    eventDescription = userState.description!!,
                    eventDate = userState.appointmentDate!!,
                    pollDate = userState.pollDate!!,
                    pollConfirmationDate = userState.confirmationDate,
                    notificationDate = userState.participantListPublicationDate!!,
                    community = userState.community!!,
                )
            )

            userContext.remove(buttonCallBack.from().id())
        }
    }

    private fun notificationDateEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            userState.participantListPublicationDate = LocalDateTime.parse(msg.text())

            sendText(
                msg.from().id(), userState.toString(),
                replyMarkup = InlineKeyboardMarkup()
                    .addRow(confirmButton)
                    .addRow(cancelButton)
            )
        }
    }

    private fun pollConfirmationDateEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            userState.stage = ProcessStage.ENTER_NOTIFICATION_DATE

            if (msg.text().equals(REJECT_ANSWER, ignoreCase = true)) {
                userState.confirmationDate = null
            } else {
                userState.confirmationDate = LocalDateTime.parse(msg.text())
            }

            sendText(msg.from().id(), "Введите дату публикации списка участников \"${getSampleDate()}\"")
        }
    }

    private fun pollDateEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            userState.stage = ProcessStage.ENTER_CONFIRMATION_POLL_DATE

            if (msg.text().equals(REJECT_ANSWER, ignoreCase = true)) {
                userState.pollDate = null
            } else {
                userState.pollDate = LocalDateTime.parse(msg.text())
            }

            sendText(
                msg.from().id(),
                "Введите дату опроса подтверждения участия в мероприятии в формате \"${getSampleDate()}\". Если не нужно, отправьте \"$REJECT_ANSWER\""
            )
        }
    }

    private fun appointmentDateEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            userState.appointmentDate = LocalDateTime.parse(msg.text())
            userState.stage = ProcessStage.ENTER_POLL_DATE

            sendText(msg.from().id(), "Введите дату опроса желающих пойти на мероприятия в формате \"${getSampleDate()}\"")
        }
    }

    private fun descriptionEntered(msg: Message, userState: State) {
        handleError(msg.from().id()) {
            userState.description = msg.text()
            userState.stage = ProcessStage.ENTER_APPOINTMENT_DATE

            sendText(msg.from().id(), "Введите дату проведения мероприятия в формате \"${getSampleDate()}\"")
        }
    }

    private fun handleError(chatId: Long, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            logger.error("Exception in AdminBot ${e.message}", e)
            sendText(chatId, "Произошла ошибка попробуйте еще раз")
        }
    }

    private fun startCreateAppointment(msg: Message) {
        handleError(msg.from().id()) {
            val userId = msg.from().id()

            userDao.save(
                User(
                    telegramUsername = msg.from().username(),
                    telegramId = msg.from().id(),
                    firstName = msg.from().firstName(),
                    lastName = msg.from().lastName(),
                )
            )
            val user = userDao.getByTelegramId(userId).orElseThrow { NoSuchElementException("User should exist") }
            val communitySet = communityDao.getAllCommunitiesByAdmin(user)

            if (communitySet.isEmpty()) {
                sendText(userId, "Вы не являетесь администратором ни одного из сообществ")
                return@handleError
            }

            if (communitySet.size == 1) {
                val state = State(ProcessStage.ENTER_DESCRIPTION, community = communitySet.first())
                userContext[userId] = state
                sendText(userId, "Введите описание мероприятия")
                return@handleError
            }

            val communityNameButtons = communitySet.map {
                InlineKeyboardButton(it.name).callbackData("choose-${it.communityId}")
            }

            val keyBoardBuilder = InlineKeyboardMarkup()

            for (communityNameButton in communityNameButtons) {
                keyBoardBuilder.addRow(communityNameButton)
            }

            sendText(userId, "Выберете сообщество для которого хотите создать событие", replyMarkup = keyBoardBuilder)
        }
    }

    private fun getSampleDate(): String {
        val currentDate = LocalDateTime.now()
        return "${currentDate.year}-${String.format("%02d", currentDate.monthValue)}-${String.format("%02d", currentDate.dayOfMonth)}T10:15"
    }

    fun sendText(who: Long, what: String?, replyMarkup: Keyboard? = null) {
        logger.info("Send: what $what, who $who")
        val smBuilder = com.pengrad.telegrambot.request.SendMessage(who, what)
        if (replyMarkup != null) {
            smBuilder.replyMarkup(replyMarkup)
        }

        try {
            bot.execute(smBuilder)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private val logger = logger()
        private const val SHORT_EVENT_COMMAND_MAXIMUM_DAYS = 14L
        private const val REJECT_ANSWER = "НЕТ"
    }
}
