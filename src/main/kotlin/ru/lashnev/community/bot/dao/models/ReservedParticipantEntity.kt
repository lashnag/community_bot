package ru.lashnev.community.bot.dao.models

import jakarta.persistence.*

@Entity
@Table(name = "reserved_participant")
data class ReservedParticipantEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val reservedParticipantId: Long? = null,

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: EventEntity,

    @ManyToOne
    @JoinColumn(name = "user_id")
    val user: UserEntity,
)
