package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "confirmed_participant")
data class ConfirmedParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val confirmedParticipantId: Long? = null,
    val eventId: Long,
    val userId: Long,
)
