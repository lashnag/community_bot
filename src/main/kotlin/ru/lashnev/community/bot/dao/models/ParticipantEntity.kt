package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "participant")
data class ParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val participantId: Long? = null,
    val eventId: Long,
    val userId: Long,
)
