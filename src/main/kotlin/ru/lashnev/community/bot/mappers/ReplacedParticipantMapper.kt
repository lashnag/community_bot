package ru.lashnev.community.bot.mappers

import org.springframework.stereotype.Component
import ru.lashnev.community.bot.dao.models.ReplacedParticipantEntity
import ru.lashnev.community.bot.models.ReplacedParticipant

@Component
class ReplacedParticipantMapper(private val userMapper: UserMapper) {
    fun toBO(replacedParticipant: ReplacedParticipantEntity): ReplacedParticipant {
        return ReplacedParticipant(
            user = userMapper.toBO(replacedParticipant.user),
            replaceUser = replacedParticipant.replaceUser?.let { userMapper.toBO(it) },
        )
    }
}