package ru.lashnev.community.bot.services.senders

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.ParticipantDao
import ru.lashnev.community.bot.dao.ReserveParticipantDao
import ru.lashnev.community.bot.models.Community
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User
import java.time.LocalDateTime
import kotlin.test.assertTrue

class ConfirmationServiceTest {

    private lateinit var confirmationService: ConfirmationService
    private lateinit var telegramBot: TelegramBot
    private lateinit var eventDao: EventDao
    private lateinit var participantDao: ParticipantDao
    private lateinit var reserveParticipantDao: ReserveParticipantDao
    private lateinit var captor: ArgumentCaptor<SendMessage>

    @BeforeEach
    fun setUp() {
        telegramBot = mock(TelegramBot::class.java)
        captor = ArgumentCaptor.forClass(SendMessage::class.java)
        eventDao = mock(EventDao::class.java)
        participantDao = mock(ParticipantDao::class.java)
        reserveParticipantDao = mock(ReserveParticipantDao::class.java)
        confirmationService = ConfirmationService(telegramBot, eventDao, participantDao, reserveParticipantDao)
    }

    @Test
    fun dontSendMessageIfNoEvents() {
        eventDao.stub {
            on { eventDao.getEventsToNotification() } doReturn emptySet()
        }

        confirmationService.sendMessages()

        verify(telegramBot, times(0)).execute(captor.capture())
    }

    @Test
    fun dontSendMessageIfNoParticipants() {
        eventDao.stub {
            on { eventDao.getEventsToNotification() } doReturn setOf(createEvent())
        }

        confirmationService.sendMessages()

        verify(telegramBot, times(0)).execute(captor.capture())
    }

    @Test
    fun sendMessageWithoutReserveIfCapacityIsEnough() {
        eventDao.stub {
            on { eventDao.getEventsToNotification() } doReturn setOf(
                createEvent().copy(
                    interestedParticipants = setOf(createParticipant(123)),
                    confirmedParticipants = setOf(createParticipant(123))
                )
            )
        }

        confirmationService.sendMessages()

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertTrue { senderText.contains("123") }
    }

    @Test
    fun ifNoConfirmationThenSendToInterested() {
        eventDao.stub {
            on { eventDao.getEventsToNotification() } doReturn setOf(
                createEvent().copy(
                    interestedParticipants = setOf(createParticipant(123))
                )
            )
        }

        confirmationService.sendMessages()

        verify(telegramBot, times(1)).execute(captor.capture())
        val senderText = captor.value.parameters["text"] as String
        assertTrue { senderText.contains("123") }
    }

    @Test
    fun ifCapacityIsNotEnoughSendTheReserveRandomList() {
        eventDao.stub {
            on { eventDao.getEventsToNotification() } doReturn setOf(
                createEvent().copy(
                    interestedParticipants = setOf(createParticipant(123), createParticipant(321)),
                    confirmedParticipants = setOf(createParticipant(123), createParticipant(321)),
                    community = createCommunity().copy(capacity = 1)
                )
            )
        }

        confirmationService.sendMessages()

        verify(telegramBot, times(2)).execute(captor.capture())
        val allCapturedRequests = captor.allValues
        val participantsText = allCapturedRequests[0].parameters["text"] as String
        val reserveParticipants = allCapturedRequests[1].parameters["text"] as String
        assertTrue { participantsText.contains("@") }
        assertTrue { reserveParticipants.contains("@") }
    }

    @Test
    fun ifCapacityIsNotEnoughAndLastEventHasParticipantThenPrioritizeNewParticipant() {
        eventDao.stub {
            on { eventDao.getEventsToNotification() } doReturn setOf(
                createEvent().copy(
                    interestedParticipants = setOf(
                        createParticipant(321),
                        createParticipant(222),
                        createParticipant(123),
                        createParticipant(444),
                        createParticipant(555),
                        createParticipant(666),
                    ),
                    confirmedParticipants = setOf(
                        createParticipant(321),
                        createParticipant(222),
                        createParticipant(123),
                        createParticipant(444),
                        createParticipant(555),
                        createParticipant(666),
                    ),
                    community = createCommunity().copy(capacity = 1)
                )
            )
            on { eventDao.getPreviousEvent(any()) } doReturn createEvent().copy(
                participants = setOf(
                    createParticipant(321),
                    createParticipant(222),
                    createParticipant(444),
                    createParticipant(555),
                    createParticipant(666),
                )
            )
        }

        confirmationService.sendMessages()

        verify(telegramBot, times(2)).execute(captor.capture())
        val allCapturedRequests = captor.allValues
        val participantsText = allCapturedRequests[0].parameters["text"] as String
        val reserveParticipants = allCapturedRequests[1].parameters["text"] as String
        assertTrue { participantsText.contains("123") }
        assertTrue { reserveParticipants.contains("321") }
        assertTrue { reserveParticipants.contains("222") }
        assertTrue { reserveParticipants.contains("444") }
        assertTrue { reserveParticipants.contains("555") }
        assertTrue { reserveParticipants.contains("666") }
    }

    private fun createParticipant(id: Long): User {
        return User(
            telegramId = id,
            firstName = "$id",
            lastName = "$id",
            telegramUsername = "$id"
        )
    }

    private fun createEvent(): Event {
        return Event(
            interestedParticipants = emptySet(),
            confirmedParticipants = emptySet(),

            eventId = 1,
            community = createCommunity(),
            eventDate = LocalDateTime.now(),
            eventDescription = "Test",
            notificationDate = LocalDateTime.now(),
            pollConfirmationDate = LocalDateTime.now(),
            pollDate = LocalDateTime.now()
        )
    }

    private fun createCommunity(): Community {
        return Community(
            communityId = 2,
            chatId = "3",
            admins = emptySet(),
            capacity = 1000,
            name = "TestCommunity"
        )
    }
}
