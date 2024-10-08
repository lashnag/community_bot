package ru.lashnev.community.bot.dao.impl

import jakarta.annotation.PostConstruct
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.hibernate.*
import ru.lashnev.community.bot.mappers.EventMapper
import ru.lashnev.community.bot.mappers.ReplacedParticipantMapper
import ru.lashnev.community.bot.mappers.UserMapper
import ru.lashnev.community.bot.models.Event
import java.time.LocalDateTime
import java.util.Optional

@Repository
class EventDaoImpl(
    private val eventMapper: EventMapper,
    private val eventRepository: EventRepository,
    private val communityRepository: CommunityRepository,
    private val interestedParticipantRepository: InterestedParticipantRepository,
    private val confirmedParticipantRepository: ConfirmedParticipantRepository,
    private val participantRepository: ParticipantRepository,
    private val reserveParticipantRepository: ReserveParticipantRepository,
    private val replacedParticipantRepository: ReplacedParticipantRepository,
    private val userMapper: UserMapper,
    private val replacedParticipantMapper: ReplacedParticipantMapper
) : EventDao {

    override fun getEventById(eventId: Long): Event? {
        return eventRepository.findByIdOrNull(eventId)?.let { fillParticipants(eventMapper.toBO(it)) }
    }

    override fun getEventsToPoll(): Set<Event> {
        return eventRepository.findByPollDateBeforeAndPollIdIsNull(LocalDateTime.now()).map {
            fillParticipants(eventMapper.toBO(it))
        }.toSet()
    }

    override fun getEventsToConfirmationPoll(): Set<Event> {
        return eventRepository.findByPollConfirmationDateBeforeAndPollConfirmationIdIsNullAndPollIdIsNotNull(LocalDateTime.now()).map {
            fillParticipants(eventMapper.toBO(it))
        }.toSet()
    }

    override fun getEventsToNotification(): Set<Event> {
        return eventRepository.findByNotificationDateBeforeAndNotificationWasSentIsFalse(LocalDateTime.now()).map {
            fillParticipants(eventMapper.toBO(it))
        }.toSet()
    }

    override fun getEventsByCommunityId(communityId: Long): Set<Event> {
        return eventRepository.findByCommunityCommunityId(communityId).map {
            fillParticipants(eventMapper.toBO(it))
        }.toSet()
    }

    override fun getEventByPollId(pollId: String): Optional<Event> {
        return mapToOptional(eventRepository.findByPollId(pollId)?.let { fillParticipants(eventMapper.toBO(it)) })
    }

    private fun mapToOptional(user: Event?): Optional<Event> {
        return if (user == null) {
            Optional.empty<Event>()
        } else {
            return Optional.of(user)
        }
    }

    override fun getEventByPollConfirmationId(confirmationPollId: String): Optional<Event> {
        return mapToOptional(eventRepository.findByPollConfirmationId(confirmationPollId)?.let {
            fillParticipants(eventMapper.toBO(it))
        })
    }

    override fun isPollIdExist(id: String): Boolean {
        return eventRepository.findByPollId(id) != null
    }

    override fun isPollConfirmationIdExist(id: String): Boolean {
        return eventRepository.findByPollConfirmationId(id) != null
    }

    override fun save(event: Event) {
        eventRepository.save(eventMapper.toEntity(event))
    }

    override fun deleteById(eventId: Long) {
        eventRepository.deleteById(eventId)
    }

    override fun getPreviousEvent(event: Event): Event? {
        return eventRepository.findByEventDateBeforeAndCommunityCommunityIdOrderByEventDate(
            event.eventDate,
            event.community.communityId!!,
        ).firstOrNull()?.let { fillParticipants(eventMapper.toBO(it)) }
    }

    override fun getActiveEventsByUser(userId: Long): Set<Event> {
        val activeEventsAsParticipant = participantRepository.findByUserUserId(userId)
        val activeEventsAsReplacedParticipant = replacedParticipantRepository.findByReplaceUserUserId(userId)
        return (activeEventsAsParticipant.map { it.event } + activeEventsAsReplacedParticipant.map { it.event }).filter {
            it.eventDate > LocalDateTime.now()
        }.map { fillParticipants(eventMapper.toBO(it)) }.toSet()
    }

    override fun getActiveEventsByChatId(chatId: Long): Set<Event> {
        val community = communityRepository.findByChatId(chatId.toString()) ?: return emptySet()
        return eventRepository.findByCommunityCommunityIdAndEventDateAfterAndNotificationDateIsNotNull(
            community.communityId!!,
            LocalDateTime.now()
        ).map { fillParticipants(eventMapper.toBO(it)) }.toSet()
    }

    private fun fillParticipants(event: Event): Event {
        val interestedParticipants = interestedParticipantRepository.findByEventEventId(event.eventId!!)
        val confirmedParticipants = confirmedParticipantRepository.findByEventEventId(event.eventId)
        val participants = participantRepository.findByEventEventId(event.eventId)
        val reservedParticipants = reserveParticipantRepository.findByEventEventId(event.eventId)
        val replacedParticipants = replacedParticipantRepository.findByEventEventId(event.eventId)
        return event.copy(
            interestedParticipants = interestedParticipants.map { userMapper.toBO(it.user) }.toSet(),
            confirmedParticipants = confirmedParticipants.map { userMapper.toBO(it.user) }.toSet(),
            participants = participants.map { userMapper.toBO(it.user) }.toSet(),
            reserveParticipants = reservedParticipants.map { userMapper.toBO(it.user) }.toSet(),
            replacedParticipants = replacedParticipants.map { replacedParticipantMapper.toBO(it) }.toSet(),
        )
    }
}
