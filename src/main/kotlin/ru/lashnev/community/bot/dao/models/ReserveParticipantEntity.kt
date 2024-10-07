package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "reserved_participants")
data class ReserveParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val reserveParticipantId: Long? = null,
    val eventId: Long,
    val userId: Long,
)
