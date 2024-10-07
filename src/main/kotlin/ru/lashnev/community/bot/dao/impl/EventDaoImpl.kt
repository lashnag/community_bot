package ru.lashnev.community.bot.dao.impl

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.EventDao
import ru.lashnev.community.bot.dao.hibernate.CommunityRepository
import ru.lashnev.community.bot.dao.hibernate.EventRepository
import ru.lashnev.community.bot.dao.hibernate.UserRepository
import ru.lashnev.community.bot.dao.models.ReplacedParticipantEntity
import ru.lashnev.community.bot.dao.models.UserEntity
import ru.lashnev.community.bot.mappers.EventMapper
import ru.lashnev.community.bot.models.Event
import java.time.LocalDateTime
import java.util.Optional

@Repository
class EventDaoImpl(
    private val eventMapper: EventMapper,
    private val eventRepository: EventRepository,
    private val communityRepository: CommunityRepository,
    private val userRepository: UserRepository,
) : EventDao {

    override fun getEventById(eventId: Long): Event? {
        return eventRepository.findByIdOrNull(eventId)?.let { eventMapper.toBO(it) }
    }

    override fun getEventsToPoll(): Set<Event> {
        return eventRepository.findByPollDateBeforeAndPollIdIsNull(LocalDateTime.now()).map { eventMapper.toBO(it) }.toSet()
    }

    override fun getEventsToConfirmationPoll(): Set<Event> {
        return eventRepository.findByPollConfirmationDateBeforeAndPollConfirmationIdIsNullAndPollIdIsNotNull(LocalDateTime.now()).map {
            eventMapper.toBO(it)
        }.toSet()
    }

    override fun getEventsToNotification(): Set<Event> {
        return eventRepository.findByNotificationDateBeforeAndNotificationWasSentIsFalse(LocalDateTime.now()).map {
            eventMapper.toBO(it)
        }.toSet()
    }

    override fun getEventsByCommunityId(communityId: Long): Set<Event> {
        return eventRepository.findByCommunityCommunityId(communityId).map { eventMapper.toBO(it) }.toSet()
    }

    override fun getEventByPollId(pollId: String): Optional<Event> {
        return mapToOptional(eventRepository.findByPollId(pollId)?.let { eventMapper.toBO(it) })
    }

    private fun mapToOptional(user: Event?): Optional<Event> {
        return if (user == null) {
            Optional.empty<Event>()
        } else {
            return Optional.of(user)
        }
    }

    override fun getEventByPollConfirmationId(confirmationPollId: String): Optional<Event> {
        return mapToOptional(eventRepository.findByPollConfirmationId(confirmationPollId)?.let { eventMapper.toBO(it) })
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
        ).firstOrNull()?.let { eventMapper.toBO(it) }
    }

    override fun getActiveEventsByUser(userId: Long): Set<Event> {
        return eventRepository.findByEventDateAfterAndParticipantsUserIdOrReplacedParticipantsUserUserId(
            LocalDateTime.now(),
            userId,
            userId
        ).map { eventMapper.toBO(it) }.toSet()
    }

    override fun getActiveEventsByChatId(chatId: Long): Set<Event> {
        val community = communityRepository.findByChatId(chatId.toString()) ?: return emptySet()
        return eventRepository.findByCommunityCommunityIdAndEventDateAfterAndNotificationDateIsNotNull(
            community.communityId!!,
            LocalDateTime.now()
        ).map { eventMapper.toBO(it) }.toSet()
    }
}
