package ru.lashnev.community.bot.dao.hibernate

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.models.InterestedParticipantEntity

@Repository
interface InterestedParticipantRepository : JpaRepository<InterestedParticipantEntity, Long> {
    fun deleteByEventEventIdAndUserUserId(eventId: Long, userId: Long)
    fun findByEventEventId(eventId: Long): Set<InterestedParticipantEntity>
}