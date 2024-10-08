package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.ParticipantDao
import ru.lashnev.community.bot.dao.hibernate.ParticipantRepository
import ru.lashnev.community.bot.dao.models.ParticipantEntity
import ru.lashnev.community.bot.mappers.EventMapper
import ru.lashnev.community.bot.mappers.UserMapper
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class ParticipantDaoImpl(
    private val participantRepository: ParticipantRepository,
    private val eventMapper: EventMapper,
    private val userMapper: UserMapper,
) : ParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        participantRepository.save(
            ParticipantEntity(event = eventMapper.toEntity(event), user = userMapper.toEntity(user))
        )
    }
}
