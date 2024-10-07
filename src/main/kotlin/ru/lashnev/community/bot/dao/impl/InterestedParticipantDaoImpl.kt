package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.InterestedParticipantDao
import ru.lashnev.community.bot.dao.hibernate.InterestedParticipantRepository
import ru.lashnev.community.bot.dao.models.InterestedParticipantEntity
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class InterestedParticipantDaoImpl(private val interestedParticipantRepository: InterestedParticipantRepository) : InterestedParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        interestedParticipantRepository.save(
            InterestedParticipantEntity(eventId = event.eventId!!, userId = user.userId!!)
        )
    }

    override fun removeParticipant(user: User, event: Event) {
        interestedParticipantRepository.deleteByEventIdAndUserId(event.eventId!!, user.userId!!)
    }
}