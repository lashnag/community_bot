package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "interested_participant")
data class InterestedParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val interestedParticipantId: Long? = null,
    val eventId: Long,
    val userId: Long,
)
