package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.ConfirmedParticipantDao
import ru.lashnev.community.bot.dao.hibernate.ConfirmedParticipantRepository
import ru.lashnev.community.bot.dao.models.ConfirmedParticipantEntity
import ru.lashnev.community.bot.mappers.EventMapper
import ru.lashnev.community.bot.mappers.UserMapper
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class ConfirmedParticipantDaoImpl(
    private val confirmedParticipantRepository: ConfirmedParticipantRepository,
    private val eventMapper: EventMapper,
    private val userMapper: UserMapper,
) : ConfirmedParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        confirmedParticipantRepository.save(
            ConfirmedParticipantEntity(event = eventMapper.toEntity(event), user = userMapper.toEntity(user))
        )
    }

    override fun removeParticipant(user: User, event: Event) {
        confirmedParticipantRepository.deleteByEventEventIdAndUserUserId(event.eventId!!, user.userId!!)
    }
}
