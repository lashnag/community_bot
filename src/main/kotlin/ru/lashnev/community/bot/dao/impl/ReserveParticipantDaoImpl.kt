package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.ReserveParticipantDao
import ru.lashnev.community.bot.dao.hibernate.ReserveParticipantRepository
import ru.lashnev.community.bot.dao.models.ReserveParticipantEntity
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class ReserveParticipantDaoImpl(private val reserveParticipantRepository: ReserveParticipantRepository) : ReserveParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        reserveParticipantRepository.save(
            ReserveParticipantEntity(eventId = event.eventId!!, userId = user.userId!!)
        )
    }
}
