package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.ReserveParticipantDao
import ru.lashnev.community.bot.dao.hibernate.ReserveParticipantRepository
import ru.lashnev.community.bot.dao.models.ReservedParticipantEntity
import ru.lashnev.community.bot.mappers.EventMapper
import ru.lashnev.community.bot.mappers.UserMapper
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class ReserveParticipantDaoImpl(
    private val reserveParticipantRepository: ReserveParticipantRepository,
    private val eventMapper: EventMapper,
    private val userMapper: UserMapper,
) : ReserveParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        reserveParticipantRepository.save(
            ReservedParticipantEntity(event = eventMapper.toEntity(event), user = userMapper.toEntity(user))
        )
    }
}
