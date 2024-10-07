package ru.lashnev.community.bot.dao.hibernate

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.lashnev.community.bot.dao.models.EventEntity
import java.time.LocalDateTime

@Repository
interface EventRepository : JpaRepository<EventEntity, Long> {
    fun findByPollDateBeforeAndPollIdIsNull(pollDate: LocalDateTime): Set<EventEntity>
    fun findByPollConfirmationDateBeforeAndPollConfirmationIdIsNullAndPollIdIsNotNull(pollDate: LocalDateTime): Set<EventEntity>
    fun findByNotificationDateBeforeAndNotificationWasSentIsFalse(pollDate: LocalDateTime): Set<EventEntity>
    fun findByCommunityCommunityId(communityId: Long): Set<EventEntity>
    fun findByPollId(pollId: String): EventEntity?
    fun findByPollConfirmationId(pollConfirmationId: String): EventEntity?
    fun findByCommunityCommunityIdAndEventDateAfterAndNotificationDateIsNotNull(communityId: Long, notificationDate: LocalDateTime): Set<EventEntity>
    fun findByEventDateBeforeAndCommunityCommunityIdOrderByEventDate(eventDateTime: LocalDateTime, communityId: Long): Set<EventEntity>
    fun findByEventDateAfterAndParticipantsUserIdOrReplacedParticipantsUserUserId(
        eventDateTime: LocalDateTime,
        participantId: Long,
        sameParticipantId: Long,
    ): Set<EventEntity>
}