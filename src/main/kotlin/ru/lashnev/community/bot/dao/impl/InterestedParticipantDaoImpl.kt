package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.InterestedParticipantDao
import ru.lashnev.community.bot.dao.hibernate.InterestedParticipantRepository
import ru.lashnev.community.bot.dao.models.InterestedParticipantEntity
import ru.lashnev.community.bot.mappers.EventMapper
import ru.lashnev.community.bot.mappers.UserMapper
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class InterestedParticipantDaoImpl(
    private val interestedParticipantRepository: InterestedParticipantRepository,
    private val eventMapper: EventMapper,
    private val userMapper: UserMapper,
) : InterestedParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        interestedParticipantRepository.save(
            InterestedParticipantEntity(event = eventMapper.toEntity(event), user = userMapper.toEntity(user))
        )
    }

    override fun removeParticipant(user: User, event: Event) {
        interestedParticipantRepository.deleteByEventEventIdAndUserUserId(event.eventId!!, user.userId!!)
    }
}
