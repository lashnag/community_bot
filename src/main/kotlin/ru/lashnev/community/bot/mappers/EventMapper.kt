package ru.lashnev.community.bot.mappers

import org.springframework.stereotype.Component
import ru.lashnev.community.bot.dao.models.EventEntity
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.ReplacedParticipant

@Component
class EventMapper(private val userMapper: UserMapper, private val communityMapper: CommunityMapper) {
    fun toBO(eventEntity: EventEntity): Event {
        return Event(
            eventId = eventEntity.eventId,
            eventDescription = eventEntity.eventDescription,
            pollId = eventEntity.pollId,
            pollConfirmationId = eventEntity.pollConfirmationId,
            community = communityMapper.toBO(eventEntity.community),
            interestedParticipants = eventEntity.interestedParticipants.map { userMapper.toBO(it) }.toSet(),
            confirmedParticipants = eventEntity.confirmedParticipants.map { userMapper.toBO(it) }.toSet(),
            participants = eventEntity.participants.map { userMapper.toBO(it) }.toSet(),
            reserveParticipants = eventEntity.reserveParticipants.map { userMapper.toBO(it) }.toSet(),
            replacedParticipants = eventEntity.replacedParticipants.map {
                ReplacedParticipant(
                    userMapper.toBO(it.user),
                    it.replaceUser?.let { userMapper.toBO(it) }
                )
            }.toSet(),
            eventDate = eventEntity.eventDate,
            pollDate = eventEntity.pollDate,
            pollConfirmationDate = eventEntity.pollConfirmationDate,
            notificationDate = eventEntity.notificationDate,
            notificationWasSent = eventEntity.notificationWasSent,
        )
    }

    fun toEntity(event: Event): EventEntity {
        return EventEntity(
            eventId = event.eventId,
            community = communityMapper.toEntity(event.community),
            eventDescription = event.eventDescription,
            pollId = event.pollId,
            pollConfirmationId = event.pollConfirmationId,
            eventDate = event.eventDate,
            pollDate = event.pollDate,
            pollConfirmationDate = event.pollConfirmationDate,
            notificationDate = event.notificationDate,
            notificationWasSent = event.notificationWasSent,
        )
    }
}