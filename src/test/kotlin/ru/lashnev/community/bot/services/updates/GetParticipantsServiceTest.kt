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
import ru.lashnev.community.bot.dao.UserDAO
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.ReplacedParticipant
import ru.lashnev.community.bot.models.User
import ru.lashnev.community.bot.services.updates.GetParticipantsService.Companion.CHOOSE_EVENT_TEXT
import ru.lashnev.community.bot.services.updates.GetParticipantsService.Companion.NO_PARTICIPANTS_MESSAGE
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(RandomBeansExtension::class)
class GetParticipantsServiceTest {

    private lateinit var getParticipantsService: GetParticipantsService
    private lateinit var eventDao: EventDao
    private lateinit var userDAO: UserDAO
    private lateinit var telegramBot: TelegramBot
    private lateinit var update: Update
    private lateinit var message: Message
    private lateinit var chat: Chat
    private lateinit var callbackQuery: CallbackQuery
    private lateinit var captor: ArgumentCaptor<SendMessage>

    @BeforeEach
    fun setUp(@Random user: com.pengrad.telegrambot.model.User, @Random adminUser: User) {
        eventDao = mock()
        telegramBot = mock()
        userDAO = mock()
        userDAO.stub {
            on { getByTelegramId(any()) } doReturn Optional.of(adminUser.copy(isSuperuser = true))
            on { getAdminsByChatId(any()) } doReturn setOf(adminUser)
        }
        getParticipantsService = GetParticipantsService(eventDao, telegramBot, userDAO)

        message = mock()
        chat = mock()
        chat.stub {
            on { type() } doReturn Chat.Type.supergroup
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
    fun ifAddingToTheGroupDontProcess() {
        message.stub {
            on { text() } doReturn null
        }
        chat.stub {
            on { type() } doReturn Chat.Type.group
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(0)).execute(captor.capture())
    }

    @Test
    fun ifNotSuperuserAndNotAdminSendNotAdmin(@Random notAdminUser: User) {
        message.stub {
            on { text() } doReturn GetParticipantsService.COMMAND_PART_NAME
        }
        userDAO.stub {
            on { getByTelegramId(any()) } doReturn Optional.of(notAdminUser.copy(isSuperuser = false))
            on { getAdminsByChatId(any()) } doReturn emptySet()
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertEquals(GetParticipantsService.ONLY_FOR_ADMINS, senderText)
    }

    @Test
    fun ifNoEventsThenSendNoEventsReply() {
        message.stub {
            on { text() } doReturn GetParticipantsService.COMMAND_PART_NAME
        }
        eventDao.stub {
            on { getActiveEventsByChatId(any()) } doReturn emptySet()
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertEquals(GetParticipantsService.CANT_FIND_ANY_EVENTS_MESSAGE, senderText)
    }

    @Test
    fun ifAFewActiveEventSendEventSelector(@Random event1: Event, @Random event2: Event) {
        message.stub {
            on { text() } doReturn GetParticipantsService.COMMAND_PART_NAME
        }
        eventDao.stub {
            on { getActiveEventsByChatId(any()) } doReturn setOf(event1, event2)
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        val replyButtons = (captor.value.parameters["reply_markup"] as InlineKeyboardMarkup).inlineKeyboard()
        val button1 = replyButtons[0][0]
        val button2 = replyButtons[1][0]
        assertEquals(CHOOSE_EVENT_TEXT, senderText)
        assertTrue { button1.text?.contains(event1.eventDescription) ?: false }
        assertTrue { button2.text?.contains(event2.eventDescription) ?: false }
    }

    @Test
    fun ifSingleActiveEventSendParticipantsWithReserveMatchWithReplace(
        @Random event: Event,
        @Random user1: User,
        @Random user2: User,
        @Random user3: User,
        @Random user4: User,
    ) {
        message.stub {
            on { text() } doReturn GetParticipantsService.COMMAND_PART_NAME
        }
        val eventWithParticipants = event.copy(
            participants = setOf(user1, user2),
            reserveParticipants = setOf(user3, user4),
            replacedParticipants = setOf(ReplacedParticipant(user = user2, replaceUser = user3))
        )
        eventDao.stub {
            on { getActiveEventsByChatId(any()) } doReturn setOf(eventWithParticipants)
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(2)).execute(captor.capture())
        val allArgumentCaptors = captor.allValues
        val firstMessage = allArgumentCaptors[0].parameters["text"] as String
        val secondMessage = allArgumentCaptors[1].parameters["text"] as String
        assertTrue { firstMessage.contains(user1.telegramUsername.toString()) }
        assertTrue { firstMessage.contains(user3.telegramUsername.toString()) }
        assertTrue { secondMessage.contains(user4.telegramUsername.toString()) }
    }

    @Test
    fun isChooseEventThenSendEventParticipants(@Random event: Event, @Random user1: User, @Random user2: User) {
        update.stub {
            on { callbackQuery() } doReturn callbackQuery
        }
        val eventWithParticipants = event.copy(
            participants = setOf(user1),
            reserveParticipants = setOf(user2)
        )
        eventDao.stub {
            on { getEventById(any()) } doReturn eventWithParticipants
        }
        callbackQuery.stub {
            on { data() } doReturn GetParticipantsService.COMMAND_CHOOSE_EVENT_PART + "33"
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(2)).execute(captor.capture())
        val allArgumentCaptors = captor.allValues
        val firstMessage = allArgumentCaptors[0].parameters["text"] as String
        val secondMessage = allArgumentCaptors[1].parameters["text"] as String
        assertTrue { firstMessage.contains(user1.telegramUsername.toString()) }
        assertTrue { secondMessage.contains(user2.telegramUsername.toString()) }
    }

    @Test
    fun ifNoParticipantsThenSendEmptyMessage(@Random event: Event) {
        message.stub {
            on { text() } doReturn GetParticipantsService.COMMAND_PART_NAME
        }
        val eventWithoutParticipants = event.copy(
            participants = emptySet(),
            reserveParticipants = emptySet(),
            replacedParticipants = emptySet()
        )
        eventDao.stub {
            on { getActiveEventsByChatId(any()) } doReturn setOf(eventWithoutParticipants)
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertEquals(NO_PARTICIPANTS_MESSAGE, senderText)
    }

    @Test
    fun ifNoReserveParticipantsThenSendReserve(@Random event: Event, @Random user: User) {
        message.stub {
            on { text() } doReturn GetParticipantsService.COMMAND_PART_NAME
        }
        val eventSingleParticipants = event.copy(
            participants = setOf(user),
            reserveParticipants = emptySet(),
            replacedParticipants = emptySet()
        )
        eventDao.stub {
            on { getActiveEventsByChatId(any()) } doReturn setOf(eventSingleParticipants)
        }

        getParticipantsService.processUpdates(update)

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertTrue { senderText.contains(user.telegramUsername.toString()) }
    }
}
