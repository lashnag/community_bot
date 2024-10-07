package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.ParticipantDao
import ru.lashnev.community.bot.dao.hibernate.ParticipantRepository
import ru.lashnev.community.bot.dao.models.ParticipantEntity
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class ParticipantDaoImpl(private val participantRepository: ParticipantRepository) : ParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        participantRepository.save(
            ParticipantEntity(eventId = event.eventId!!, userId = user.userId!!)
        )
    }
}
