package ru.lashnev.community.bot.dao.hibernate

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.models.ReplacedParticipantEntity

@Repository
interface ReplacedParticipantRepository : JpaRepository<ReplacedParticipantEntity, Long> {
    fun findByEventEventId(eventId: Long): Set<ReplacedParticipantEntity>
    fun findByReplaceUserUserId(userId: Long): Set<ReplacedParticipantEntity>
}