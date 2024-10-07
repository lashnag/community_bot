package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.ReplaceParticipantDao
import ru.lashnev.community.bot.dao.hibernate.ReplacedParticipantRepository
import ru.lashnev.community.bot.dao.models.ReplacedParticipantEntity
import ru.lashnev.community.bot.mappers.EventMapper
import ru.lashnev.community.bot.mappers.UserMapper
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class ReplaceParticipantDaoImpl(
    private val replacedParticipantRepository: ReplacedParticipantRepository,
    private val eventMapper: EventMapper,
    private val userMapper: UserMapper,
) : ReplaceParticipantDao {
    override fun replaceParticipant(event: Event, canceledUser: User, replaceUser: User?) {
        replacedParticipantRepository.save(
            ReplacedParticipantEntity(
                event = eventMapper.toEntity(event),
                user = userMapper.toEntity(canceledUser),
                replaceUser = replaceUser?.let { userMapper.toEntity(it) }
            )
        )
    }
}
