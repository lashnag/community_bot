package ru.lashnev.community.bot.dao.impl

import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.ConfirmedParticipantDao
import ru.lashnev.community.bot.dao.hibernate.ConfirmedParticipantRepository
import ru.lashnev.community.bot.dao.models.ConfirmedParticipantEntity
import ru.lashnev.community.bot.models.Event
import ru.lashnev.community.bot.models.User

@Repository
class ConfirmedParticipantDaoImpl(private val confirmedParticipantRepository: ConfirmedParticipantRepository) : ConfirmedParticipantDao {
    override fun addParticipant(user: User, event: Event) {
        confirmedParticipantRepository.save(
            ConfirmedParticipantEntity(eventId = event.eventId!!, userId = user.userId!!)
        )
    }

    override fun removeParticipant(user: User, event: Event) {
        confirmedParticipantRepository.deleteByEventIdAndUserId(event.eventId!!, user.userId!!)
    }
}
