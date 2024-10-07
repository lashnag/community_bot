package ru.lashnev.community.bot.services.updates

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import io.github.glytching.junit.extension.random.Random
import io.github.glytching.junit.extension.random.RandomBeansExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.ReplaceParticipantDao
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User
import ru.lashnev.community.bot.services.updates.ReplaceParticipantService.Companion.CHOOSE_EVENT_TEXT
import ru.lashnev.community.bot.services.updates.ReplaceParticipantService.Companion.COMMAND_CHOOSE_EVENT_PART
import ru.lashnev.community.bot.services.updates.ReplaceParticipantService.Companion.COMMAND_PART_NAME
import ru.lashnev.community.bot.services.updates.ReplaceParticipantService.Companion.NO_EVENTS_MESSAGE
import ru.lashnev.community.bot.services.updates.ReplaceParticipantService.Companion.ONLY_PRIVATE
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(RandomBeansExtension::class)
class ReplaceParticipantServiceTest {

    private lateinit var replaceParticipantService: ru.lashnev.community.bot.services.updates.ReplaceParticipantService
    private lateinit var eventDao: EventDao
    private lateinit var userDAO: UserDAO
    private lateinit var replaceParticipantDao: ru.lashnev.community.bot.dao.ReplaceParticipantDao
    private lateinit var telegramBot: TelegramBot
    private lateinit var update: Update
    private lateinit var message: Message
    private lateinit var chat: Chat
    private lateinit var callbackQuery: CallbackQuery
    private lateinit var daoUser: User
    private lateinit var captor: ArgumentCaptor<SendMessage>

    @BeforeEach
    fun setUp(@Random user: User) {
        eventDao = mock()
        userDAO = mock()
        daoUser = user
        userDAO.stub {
            on { getByTelegramId(any()) } doReturn Optional.of(daoUser)
        }
        replaceParticipantDao = mock()
        telegramBot = mock()
        replaceParticipantService = ru.lashnev.community.bot.services.updates.ReplaceParticipantService(
            eventDao,
            userDAO,
            replaceParticipantDao,
            telegramBot
        )

        message = mock()
        chat = mock()
        chat.stub {
            on { type() } doReturn Chat.Type.Private
            on { id() } doReturn ru.lashnev.community.bot.services.updates.ReplaceParticipantServiceTest.Companion.PRIVATE_CHAT_ID
        }
        val user = mock<com.pengrad.telegrambot.model.User>()
        user.stub {
            on { id() } doReturn 1
        }
        message.stub {
            on { chat() } doReturn chat
            on { text() } doReturn ""
            on { from() } doReturn user
        }
        callbackQuery = mock()
        callbackQuery.stub {
            on { message() } doReturn message
        }
        update = mock()
        update.stub {
            on { message() } doReturn message
        }

        captor = ArgumentCaptor.forClass(SendMessage::class.java)
    }

    @Test
    fun ifGroupChatMessageDontProcess() {
        message.stub {
            on { text() } doReturn COMMAND_PART_NAME
        }
        chat.stub {
            on { type() } doReturn Chat.Type.group
        }

        replaceParticipantService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertEquals(ONLY_PRIVATE, senderText)
    }

    @Test
    fun ifGroupChatWhenChooseEventDontProcess() {
        update.stub {
            on { callbackQuery() } doReturn callbackQuery
        }
        callbackQuery.stub {
            on { data() } doReturn COMMAND_CHOOSE_EVENT_PART + "33"
        }
        chat.stub {
            on { type() } doReturn Chat.Type.group
        }

        replaceParticipantService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertEquals(ONLY_PRIVATE, senderText)
    }

    @Test
    fun ifAddingToTheGroupUpdateDontProcess() {
        message.stub {
            on { text() } doReturn null
        }
        chat.stub {
            on { type() } doReturn Chat.Type.Private
        }

        replaceParticipantService.processUpdates(update)

        verify(telegramBot, times(0)).execute(captor.capture())
    }

    @Test
    fun ifNoEventsForParticipantThenSendNoEventsMessage() {
        message.stub {
            on { text() } doReturn COMMAND_PART_NAME
        }
        eventDao.stub {
            on { getActiveEventsByUser(any()) } doReturn emptySet()
        }

        replaceParticipantService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        val chatId = captor.value.parameters["chat_id"] as Long
        assertEquals(NO_EVENTS_MESSAGE, senderText)
        assertEquals(ru.lashnev.community.bot.services.updates.ReplaceParticipantServiceTest.Companion.PRIVATE_CHAT_ID, chatId)
    }

    @Test
    fun ifAHasActiveEventSendEventSelector(@Random event1: Event, @Random event2: Event) {
        message.stub {
            on { text() } doReturn COMMAND_PART_NAME
        }
        eventDao.stub {
            on { getActiveEventsByUser(any()) } doReturn setOf(event1, event2)
        }

        replaceParticipantService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        val replyButtons = (captor.value.parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        val button1 = replyButtons[0][0]
        val button2 = replyButtons[1][0]
        assertEquals(CHOOSE_EVENT_TEXT, senderText)
        assertTrue { button1.text?.contains(event1.eventDescription) ?: false }
        assertTrue { button2.text?.contains(event2.eventDescription) ?: false }
        val chatId = captor.value.parameters["chat_id"] as Long
        assertEquals(ru.lashnev.community.bot.services.updates.ReplaceParticipantServiceTest.Companion.PRIVATE_CHAT_ID, chatId)
    }

    @Test
    fun ifEventChooseAndHasReserveThanSendReplace(@Random event: Event, @Random userFromReserve: User) {
        update.stub {
            on { callbackQuery() } doReturn callbackQuery
        }
        val eventWithReserveParticipants = event.copy(
            community = event.community.copy(chatId = ru.lashnev.community.bot.services.updates.ReplaceParticipantServiceTest.Companion.PUBLIC_CHAT_ID),
            reserveParticipants = setOf(userFromReserve),
        )
        callbackQuery.stub {
            on { data() } doReturn COMMAND_CHOOSE_EVENT_PART + eventWithReserveParticipants.eventId
        }
        eventDao.stub {
            on { getActiveEventsByUser(any()) } doReturn setOf(eventWithReserveParticipants)
            on { getEventById(any()) } doReturn eventWithReserveParticipants
        }

        replaceParticipantService.processUpdates(update)

        verify(telegramBot, times(2)).execute(captor.capture())
        val allArgumentCaptors = captor.allValues
        val firstMessage = allArgumentCaptors[0].parameters["text"] as String
        val firstMessageChat = allArgumentCaptors[0].parameters["chat_id"] as String
        val secondMessage = allArgumentCaptors[1].parameters["text"] as String
        val secondMessageChat = allArgumentCaptors[1].parameters["chat_id"] as String
        assertTrue { firstMessage.contains(daoUser.telegramUsername.toString()) }
        assertTrue { secondMessage.contains(userFromReserve.telegramUsername.toString()) }
        assertEquals(ru.lashnev.community.bot.services.updates.ReplaceParticipantServiceTest.Companion.PUBLIC_CHAT_ID, firstMessageChat)
        assertEquals(ru.lashnev.community.bot.services.updates.ReplaceParticipantServiceTest.Companion.PUBLIC_CHAT_ID, secondMessageChat)
    }

    @Test
    fun ifDoubleClickStartReplaceLooksLikeFromCommand(@Random event: Event) {
        update.stub {
            on { callbackQuery() } doReturn callbackQuery
        }
        callbackQuery.stub {
            on { data() } doReturn COMMAND_CHOOSE_EVENT_PART + event.eventId
        }
        eventDao.stub {
            on { getActiveEventsByUser(any()) } doReturn emptySet()
            on { getEventById(any()) } doReturn event
        }

        replaceParticipantService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        val chatId = captor.value.parameters["chat_id"] as Long
        assertEquals(NO_EVENTS_MESSAGE, senderText)
        assertEquals(ru.lashnev.community.bot.services.updates.ReplaceParticipantServiceTest.Companion.PRIVATE_CHAT_ID, chatId)
    }

    companion object {
        const val PRIVATE_CHAT_ID = -123L
        const val PUBLIC_CHAT_ID = "-567"
    }
}
